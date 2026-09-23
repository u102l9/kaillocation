// inject.cpp
//
// Reconstructed, compilable source for the 32-bit (armeabi-v7a) ptrace injector.
// Recovered from the IDA Hex-Rays decompilation in do/complete/inject.c.
//
// Same design as the 64-bit injector (inject64.cpp) but for 32-bit ARM:
//   - PTRACE_GETREGS / PTRACE_SETREGS with struct pt_regs (18 words)
//   - arguments r0..r3, pc = ARM_pc, return value in r0
//   - remote words are POKE'd 4 bytes at a time
//   - thumb-bit handling toggles the CPSR T flag
//
// Build target: kail_inject_32 (executable packaged as a .so).

#include <cerrno>
#include <cstdarg>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <dirent.h>
#include <dlfcn.h>
#include <getopt.h>
#include <time.h>
#include <unistd.h>

#include <sys/ptrace.h>
#include <sys/user.h>
#include <sys/wait.h>

#include <android/log.h>

#include "kail_log.h"

// ---------------------------------------------------------------------------
// Globals
// ---------------------------------------------------------------------------
static const char *kInjectorLogTag = "LINJECT/Injector";
static int gVerboseLoggingEnabled = 1;            // dword_4EDC
static int gTargetPid             = 0;            // dword_4EE4

static uint32_t gRemoteDlopen  = 0;               // dword_4EE8
static uint32_t gRemoteDlerror = 0;               // dword_4EEC
static uint32_t gRemoteDlsym   = 0;
static uint32_t gRemoteCalloc  = 0;               // dword_4EF0
static uint32_t gRemoteFree    = 0;               // dword_4EF4

static constexpr uint32_t kDoRunSuccess = 0x4b4c1000;
static constexpr uint32_t kDoRunAlreadyLoaded = 0x4b4c1001;

static void (*gRemoteStopCallback)() = nullptr;   // off_4EF8

#define LOGV(...) \
  do { if (gVerboseLoggingEnabled) \
         KLOGD(kInjectorLogTag, __VA_ARGS__); } while (0)

static void     waitForRemoteStop();
static long     ptraceWithRetry(const char *label, int request, uintptr_t addr, uintptr_t data);
static uint32_t findLibraryBaseAddress(const char *libraryPath, int pid);

// ---------------------------------------------------------------------------
// findPidByProcessName  (sub_13F0)
// ---------------------------------------------------------------------------
static int findPidByProcessName(const char *processName) {
  if (!processName)
    return -1;

  DIR *proc = opendir("/proc");
  if (!proc)
    return -1;

  int foundPid = -1;
  for (struct dirent *entry = readdir(proc); entry; entry = readdir(proc)) {
    // NB: the original indexes d_name+8; kept here as plain d_name which is the
    // correct behaviour for matching numeric /proc entries.
    int pid = atoi(entry->d_name);
    if (pid == 0)
      continue;

    char cmdlinePath[32];
    snprintf(cmdlinePath, sizeof(cmdlinePath), "/proc/%d/cmdline", pid);

    FILE *fp = fopen(cmdlinePath, "r");
    if (!fp)
      continue;

    char cmdline[256] = {0};
    fgets(cmdline, sizeof(cmdline), fp);
    fclose(fp);

    if (strcmp(processName, cmdline) == 0) {
      foundPid = pid;
      break;
    }
  }

  closedir(proc);
  return foundPid;
}

// ---------------------------------------------------------------------------
// waitForRemoteStop  (sub_1928)
// ---------------------------------------------------------------------------
static uint64_t nowMillis() {
  struct timespec tp;
  clock_gettime(CLOCK_MONOTONIC, &tp);
  return (uint64_t)((double)tp.tv_nsec / 1000000.0 + (double)(1000 * tp.tv_sec));
}

static void waitForRemoteStop() {
  kill(gTargetPid, SIGSTOP);

  uint64_t start = nowMillis();
  int status = 0;
  while (true) {
    if (nowMillis() - start > 200ULL)
      return;
    pid_t r = waitpid(gTargetPid, &status, WUNTRACED | WNOHANG);
    if (r == gTargetPid) {
      int stopSig = status & 0x7f;
      if (((status + 1) & 0x7e) != 0) {
        ptraceWithRetry("remote_stop", PTRACE_CONT, 0, (uintptr_t)stopSig);
      } else if ((status & 0x7f) == 0 || stopSig == 0x7f) {
        return;
      }
    } else if (r == -1) {
      return;
    }
    usleep(1000);
  }
}

// ---------------------------------------------------------------------------
// ptraceWithRetry  (sub_19E0)
// ---------------------------------------------------------------------------
static const char *ptraceRequestName(int request) {
  switch (request) {
    case PTRACE_PEEKTEXT:   return "PTRACE_PEEKTEXT";
    case PTRACE_PEEKDATA:   return "PTRACE_PEEKDATA";
    case PTRACE_POKETEXT:   return "PTRACE_POKETEXT";
    case PTRACE_POKEDATA:   return "PTRACE_POKEDATA";
    case PTRACE_CONT:       return "PTRACE_CONT";
    case PTRACE_KILL:       return "PTRACE_KILL";
    case PTRACE_SINGLESTEP: return "PTRACE_SINGLESTEP";
    case PTRACE_GETREGS:    return "PTRACE_GETREGS";
    case PTRACE_SETREGS:    return "PTRACE_SETREGS";
    case PTRACE_GETFPREGS:  return "PTRACE_GETFPREGS";
    case PTRACE_SETFPREGS:  return "PTRACE_SETFPREGS";
    case PTRACE_ATTACH:     return "PTRACE_ATTACH";
    case PTRACE_DETACH:     return "PTRACE_DETACH";
    case PTRACE_SYSCALL:    return "PTRACE_SYSCALL";
    case PTRACE_SETOPTIONS: return "PTRACE_SETOPTIONS";
    case PTRACE_GETEVENTMSG:return "PTRACE_GETEVENTMSG";
    case PTRACE_GETSIGINFO: return "PTRACE_GETSIGINFO";
    case PTRACE_SETSIGINFO: return "PTRACE_SETSIGINFO";
    default:                return nullptr;
  }
}

static long ptraceWithRetry(const char *label, int request, uintptr_t addr, uintptr_t data) {
  int *err = &errno;
  *err = 0;

  long result = -1;
  for (int attempts = 10; attempts > 0; --attempts) {
    result = ptrace((int)request, gTargetPid, (void *)addr, (void *)data);
    if (result != -1)
      break;

    int e = *err;
    if ((unsigned)e > 0x10 || ((1 << e) & 0x14008) == 0)
      break;

    char errBuf[16];
    const char *errName = "ESRCH";
    if (e != ESRCH) {
      snprintf(errBuf, sizeof(errBuf), "%d", e);
      errName = errBuf;
    }

    char reqBuf[16];
    const char *reqName = ptraceRequestName(request);
    if (!reqName) {
      snprintf(reqBuf, sizeof(reqBuf), "%d", request);
      reqName = reqBuf;
    }

    LOGV("ptrace [%s] error [%s] on request [%s]", label, errName, reqName);
    if (*err != ESRCH)
      break;

    LOGV("ptrace remote_stop/retry");
    if (gRemoteStopCallback)
      gRemoteStopCallback();
  }
  return result;
}

// ---------------------------------------------------------------------------
// findLibraryBaseAddress  (sub_1C00)
// ---------------------------------------------------------------------------
static uint32_t findLibraryBaseAddress(const char *libraryPath, int pid) {
  char mapsPath[256] = {0};
  int target = (pid == -1) ? gTargetPid : pid;
  snprintf(mapsPath, sizeof(mapsPath), "/proc/%d/maps", target);

  FILE *fp = fopen(mapsPath, "rt");
  if (!fp) {
    LOGV("fopen error");
    return 0;
  }

  const char *baseName = strrchr(libraryPath, '/');
  baseName = baseName ? baseName + 1 : libraryPath;

  uint32_t base = 0;
  char line[1024] = {0};
  while (fgets(line, sizeof(line), fp)) {
    if (strstr(line, libraryPath)) {
      base = strtoul(line, nullptr, 16);
      break;
    }
  }

  // Fallback: match by basename only (catches cases where maps shows a
  // resolved/symlinked path different from the dlopen argument).
  // System library mappings (/apex, /system, /vendor, ...) must NOT satisfy a
  // fallback match: stock processes map /system/.../libc.so, and the
  // runtime-tree probe in injectLibraryIntoProcess would otherwise
  // misclassify a normal zygote/system_server as a VMOS guest, then resolve
  // remote dlopen/doRun symbols through VMOS paths, break the ASLR slide and
  // fault immediately.
  if (!base && baseName) {
    rewind(fp);
    while (fgets(line, sizeof(line), fp)) {
      if (strstr(line, baseName) && strchr(line, '/')) {
        if (strstr(line, "/apex/") || strstr(line, "/system/") ||
            strstr(line, "/vendor/") || strstr(line, "/system_ext/") ||
            strstr(line, "/product/"))
          continue;
        base = strtoul(line, nullptr, 16);
        break;
      }
    }
  }

  fclose(fp);
  return base;
}

// ---------------------------------------------------------------------------
// resolveRemoteSymbolAddress  (sub_1CE0)
// ---------------------------------------------------------------------------
static uint32_t resolveRemoteSymbolAddress(const char *libraryPath, uint32_t localAddr) {
  uint32_t localBase  = findLibraryBaseAddress(libraryPath, getpid());
  uint32_t remoteBase = findLibraryBaseAddress(libraryPath, gTargetPid);
  if (!localAddr || !localBase || !remoteBase) {
    KLOGE(kInjectorLogTag, "resolveRemoteSymbolAddress: localAddr=0x%x localBase=0x%x remoteBase=0x%x path=%s",
          (unsigned)localAddr, (unsigned)localBase, (unsigned)remoteBase, libraryPath);
    return 0;
  }
  return remoteBase + localAddr - localBase;
}

// ---------------------------------------------------------------------------
// callRemoteFunction  (sub_1D90)
//   ARM: struct pt_regs holds r0..r10, fp, ip, sp, lr, pc, cpsr, orig_r0.
//   uregs[0..3]  = r0..r3 args
//   uregs[15]    = pc
//   uregs[16]    = cpsr  (T-bit at 0x20)
// ---------------------------------------------------------------------------
//
// The watchdog timeout is configurable: see inject64.cpp for rationale. The
// short default protects calloc/dlopen calls; the long timeout is set
// around the doRun() call which does the expensive InjectDex JNI bring-up.
static uint64_t gCallTimeoutMs = 5000;

static uint32_t callRemoteFunction(uint32_t func, int argc, ...) {
  struct pt_regs regs;
  struct pt_regs backup;
  memset(&regs, 0, sizeof(regs));
  memset(&backup, 0, sizeof(backup));

  waitForRemoteStop();

  ptraceWithRetry("backup", PTRACE_GETREGS, 0, (uintptr_t)&regs);
  memcpy(&backup, &regs, sizeof(backup));

  int count = argc;
  if ((unsigned)(argc - 1) >= 3)
    count = 4;

  va_list ap;
  va_start(ap, argc);
  for (int i = 0; i < count; ++i)
    regs.uregs[i] = va_arg(ap, uint32_t);    // r0..r3
  va_end(ap);

  regs.uregs[14] = 0;       // lr -> 0
  regs.uregs[15] = func;    // pc
  if (func & 1) {
    regs.uregs[15] = func & ~1u;
    regs.uregs[16] |= 0x20;     // set thumb bit in CPSR
  } else {
    regs.uregs[16] &= ~0x20u;   // clear thumb bit
  }

  ptraceWithRetry("call", PTRACE_SETREGS, 0, (uintptr_t)&regs);
  ptraceWithRetry("call", PTRACE_CONT, 0, 0);

  // Watchdog: avoid permafreeze if remote func hangs (e.g. linker mutex
  // contention with sibling threads in system_server). See inject64.cpp for
  // the same pattern.
  int status = 0;
  uint64_t startMs = nowMillis();
  bool timed_out = false;
  while (true) {
    if (nowMillis() - startMs > gCallTimeoutMs) {
      KLOGE(kInjectorLogTag, "callRemoteFunction watchdog tripped (%llums); aborting", (unsigned long long)gCallTimeoutMs);
      timed_out = true;
      break;
    }
    pid_t r = waitpid(gTargetPid, &status, WUNTRACED | WNOHANG);
    if (r == gTargetPid) {
      if ((status & 0xff7f) == 0xb7f)
        break;
      ptraceWithRetry("waitpid", PTRACE_CONT, 0, 0);
      continue;
    }
    if (r == -1) {
      timed_out = true;
      break;
    }
    usleep(2000);
  }

  if (timed_out) {
    ptraceWithRetry("restore", PTRACE_SETREGS, 0, (uintptr_t)&backup);
    ptraceWithRetry("detach", PTRACE_DETACH, 0, 0);
    kill(gTargetPid, SIGCONT);
    return 0;
  }

  ptraceWithRetry("return", PTRACE_GETREGS, 0, (uintptr_t)&regs);
  ptraceWithRetry("restore", PTRACE_SETREGS, 0, (uintptr_t)&backup);
  ptraceWithRetry("continue", PTRACE_CONT, 0, 0);

  return (uint32_t)regs.uregs[0];
}

// ---------------------------------------------------------------------------
// writeRemoteString  (sub_1D10)
// ---------------------------------------------------------------------------
static uint32_t writeRemoteString(const char *value) {
  size_t len = strlen(value) + 1;
  uint32_t remote = callRemoteFunction(gRemoteCalloc, 2, (uint32_t)len, (uint32_t)1);

  waitForRemoteStop();

  size_t padded = len + (len & 3);
  char *local = (char *)malloc(padded);
  memset(local, 0, padded);
  memcpy(local, value, len);

  for (size_t off = 0; off < len; off += 4) {
    if (ptraceWithRetry("string", PTRACE_POKETEXT, (uintptr_t)(remote + off),
                        *(uint32_t *)&local[off]) == -1)
      break;
  }

  free(local);
  return remote;
}

static void readRemoteCString(uint32_t remote, char *out, size_t outSize, const char *label) {
  if (!remote || !out || outSize == 0)
    return;

  waitForRemoteStop();
  memset(out, 0, outSize);
  for (size_t off = 0; off < outSize - 1; off += 4) {
    long word = ptraceWithRetry(label, PTRACE_PEEKTEXT, (uintptr_t)(remote + off), 0);
    if (word == -1)
      break;
    *(uint32_t *)&out[off] = (uint32_t)word;
    if (memchr(&out[off], '\0', 4))
      break;
  }
  out[outSize - 1] = '\0';
}

// ---------------------------------------------------------------------------
// injectLibraryIntoProcess  (sub_14B0)
// ---------------------------------------------------------------------------
static int injectLibraryIntoProcess(int pid, const char *libraryPath, const char *entryArg) {
  gRemoteStopCallback = waitForRemoteStop;
  gTargetPid = pid;

  if (ptraceWithRetry("attach", PTRACE_ATTACH, 0, 0) == -1) {
    LOGV("Failed to attach to process %d", gTargetPid);
    return 1;
  }

  kill(gTargetPid, SIGSTOP);
  waitForRemoteStop();

  const char *libcPath   = "/data/data/com.vmos.app/osimg/r/ot01/system/lib/libc.so";
  const char *libdlPath;
  const char *linkerPath;

  uint32_t vmosApp     = findLibraryBaseAddress("/data/data/com.vmos.app/osimg/r/ot01/system/lib/libc.so", gTargetPid);
  uint32_t vmosProOt01 = findLibraryBaseAddress("/data/data/com.vmos.pro/osimg/r/ot01/system/lib/libc.so", gTargetPid);
  uint32_t vmosProOt02 = findLibraryBaseAddress("/data/data/com.vmos.pro/osimg/r/ot02/system/lib/libc.so", gTargetPid);
  uint32_t twoyi       = findLibraryBaseAddress("/data/data/io.twoyi/rootfs/system/lib64/libc.so", gTargetPid);
  uint32_t vphone      = findLibraryBaseAddress("/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/lib/libc.so", gTargetPid);

  if (vmosApp) {
    libdlPath  = "/data/data/com.vmos.app/osimg/r/ot01/system/lib/libdl.so";
    linkerPath = "/data/data/com.vmos.app/osimg/r/ot01/system/bin/linker";
  } else if (vmosProOt01) {
    libcPath   = "/data/data/com.vmos.pro/osimg/r/ot01/system/lib/libc.so";
    linkerPath = "/data/data/com.vmos.pro/osimg/r/ot01/system/bin/linker";
    libdlPath  = "/data/data/com.vmos.pro/osimg/r/ot01/system/lib/libdl.so";
  } else if (vmosProOt02) {
    libcPath   = "/data/data/com.vmos.pro/osimg/r/ot02/system/lib/libc.so";
    linkerPath = "/data/data/com.vmos.pro/osimg/r/ot02/system/bin/linker";
    libdlPath  = "/data/data/com.vmos.pro/osimg/r/ot02/system/lib/libdl.so";
  } else if (twoyi) {
    libcPath   = "/data/data/io.twoyi/rootfs/system/lib64/libc.so";
    linkerPath = "/data/data/io.twoyi/rootfs/system/bin/linker64";
    libdlPath  = "/data/data/io.twoyi/rootfs/system/lib64/libdl.so";
  } else if (vphone) {
    libcPath   = "/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/lib/libc.so";
    linkerPath = "/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/bin/linker";
    libdlPath  = "/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/lib/libdl.so";
  } else {
    libcPath   = "/system/lib/libc.so";
    libdlPath  = "/system/lib/libdl.so";
    linkerPath = "/system/bin/linker";
    if (!access("/apex/com.android.runtime/lib/bionic/libc.so", R_OK))
      libcPath = "/apex/com.android.runtime/lib/bionic/libc.so";
    if (!access("/apex/com.android.runtime/lib/bionic/libdl.so", R_OK))
      libdlPath = "/apex/com.android.runtime/lib/bionic/libdl.so";
    if (!access("/apex/com.android.runtime/bin/linker", R_OK))
      linkerPath = "/apex/com.android.runtime/bin/linker";
  }

  LOGV("libc:%s", libcPath);

  gRemoteCalloc = resolveRemoteSymbolAddress(libcPath, (uint32_t)(uintptr_t)&calloc);
  gRemoteFree   = resolveRemoteSymbolAddress(libcPath, (uint32_t)(uintptr_t)&free);

  if (findLibraryBaseAddress(libdlPath, -1) && findLibraryBaseAddress(libdlPath, gTargetPid)) {
    void *handle = dlopen(libdlPath, RTLD_NOW);
    gRemoteDlopen  = resolveRemoteSymbolAddress(libdlPath, (uint32_t)(uintptr_t)dlsym(handle, "dlopen"));
    gRemoteDlerror = resolveRemoteSymbolAddress(libdlPath, (uint32_t)(uintptr_t)dlsym(handle, "dlerror"));
    gRemoteDlsym   = resolveRemoteSymbolAddress(libdlPath, (uint32_t)(uintptr_t)dlsym(handle, "dlsym"));
    dlclose(handle);
  } else {
    gRemoteDlopen  = resolveRemoteSymbolAddress(linkerPath, (uint32_t)(uintptr_t)&dlopen);
    gRemoteDlerror = resolveRemoteSymbolAddress(linkerPath, (uint32_t)(uintptr_t)&dlerror);
    gRemoteDlsym   = resolveRemoteSymbolAddress(linkerPath, (uint32_t)(uintptr_t)&dlsym);
  }

  void *runtime = dlopen("/system/lib/libandroid_runtime.so", RTLD_NOW);
  uint32_t javaVm = resolveRemoteSymbolAddress(
      "/system/lib/libandroid_runtime.so",
      (uint32_t)(uintptr_t)dlsym(runtime, "_ZN7android14AndroidRuntime7mJavaVME"));
  dlclose(runtime);

  LOGV("calloc:%p free:%p dlopen:%p dlsym:%p dlerror:%p javavm:%p",
       (void *)(uintptr_t)gRemoteCalloc, (void *)(uintptr_t)gRemoteFree,
       (void *)(uintptr_t)gRemoteDlopen, (void *)(uintptr_t)gRemoteDlsym,
       (void *)(uintptr_t)gRemoteDlerror,
       (void *)(uintptr_t)javaVm);
  printf("inject diag: calloc=0x%x free=0x%x dlopen=0x%x dlsym=0x%x dlerror=0x%x javavm=0x%x\n",
         gRemoteCalloc, gRemoteFree, gRemoteDlopen, gRemoteDlsym, gRemoteDlerror, javaVm);

  uint32_t remotePath   = writeRemoteString(libraryPath);
  uint32_t remoteHandle = callRemoteFunction(gRemoteDlopen, 2, remotePath, (uint32_t)RTLD_NOW);
  printf("inject diag: remotePath=0x%x remoteHandle=0x%x\n", remotePath, remoteHandle);
  callRemoteFunction(gRemoteFree, 1, remotePath);

  int result;
  if (remoteHandle) {
    void *handle = dlopen(libraryPath, RTLD_NOW);
    void *doRunLocal = handle ? dlsym(handle, "doRun") : nullptr;
    uint32_t doRunRemoteBySlide = resolveRemoteSymbolAddress(libraryPath, (uint32_t)(uintptr_t)doRunLocal);
    if (handle)
      dlclose(handle);

    uint32_t doRunRemoteByDlsym = 0;
    if (gRemoteDlsym) {
      uint32_t remoteSymbolName = writeRemoteString("doRun");
      if (remoteSymbolName) {
        doRunRemoteByDlsym = callRemoteFunction(gRemoteDlsym, 2, remoteHandle, remoteSymbolName);
        callRemoteFunction(gRemoteFree, 1, remoteSymbolName);
      }
    }

    uint32_t doRunRemote = doRunRemoteByDlsym ? doRunRemoteByDlsym : doRunRemoteBySlide;
    KLOGI(kInjectorLogTag, "doRun local=%p remote=0x%x dlsym=0x%x slide=0x%x",
          doRunLocal, doRunRemote, doRunRemoteByDlsym, doRunRemoteBySlide);
    printf("inject diag: doRunLocal=%p doRunRemote=0x%x remoteDlsym=0x%x slide=0x%x\n",
           doRunLocal, doRunRemote, doRunRemoteByDlsym, doRunRemoteBySlide);

    if (!doRunRemoteByDlsym && gRemoteDlerror) {
      char errbuf[1024] = {0};
      uint32_t errPtr = callRemoteFunction(gRemoteDlerror, 0);
      readRemoteCString(errPtr, errbuf, sizeof(errbuf), "dlerror");
      if (errbuf[0]) {
        KLOGW(kInjectorLogTag, "remote dlerror: %s", errbuf);
        printf("inject diag: remote dlerror: %s\n", errbuf);
      }
    }

    result = 1;
    if (doRunRemote) {
      uint32_t remoteArg = writeRemoteString(entryArg);
      // doRun does the InjectDex JNI bring-up — see inject64.cpp comment.
      gCallTimeoutMs = 120000;
      uint32_t doRunResult = callRemoteFunction(doRunRemote, 2, javaVm, remoteArg);
      gCallTimeoutMs = 5000;
      callRemoteFunction(gRemoteFree, 1, remoteArg);
      KLOGI(kInjectorLogTag, "doRun call returned 0x%x", doRunResult);
      printf("inject diag: doRunResult=0x%x\n", doRunResult);
      if (doRunResult == kDoRunSuccess || doRunResult == kDoRunAlreadyLoaded) {
        result = 0;
      } else {
        KLOGE(kInjectorLogTag, "doRun returned failure stage 0x%x", doRunResult);
        printf("inject diag: doRun failed stage=0x%x\n", doRunResult);
      }
    } else {
      KLOGE(kInjectorLogTag, "doRun resolve failed: local=%p remote=0x%x", doRunLocal, doRunRemote);
      printf("inject diag: doRun resolve failed\n");
    }
  } else {
    char errbuf[1024] = {0};
    uint32_t errPtr = callRemoteFunction(gRemoteDlerror, 0);
    readRemoteCString(errPtr, errbuf, sizeof(errbuf), "dlerror");
    LOGV("dlopen failed: %s", errbuf);
    printf("inject diag: dlopen failed: %s\n", errbuf);
    result = 1;
  }

  waitForRemoteStop();
  ptraceWithRetry("detach", PTRACE_DETACH, 0, 0);
  kill(gTargetPid, SIGCONT);
  return result;
}

// ---------------------------------------------------------------------------
// injectMain  (sub_1F54)
// ---------------------------------------------------------------------------
static int injectMain(int argc, char **argv) {
  int   pid          = 0;
  char *processName  = nullptr;
  char *libraryPath  = nullptr;
  char *packageName  = nullptr;
  char *entryArg     = nullptr;

  int opt;
  bool ok = true;
  while ((opt = getopt(argc, argv, "p:P:l:n:a:h")) != -1) {
    switch (opt) {
      case 'l':
        libraryPath = optarg;
        break;
      case 'n':
        packageName = optarg;
        break;
      case 'a':
        entryArg = optarg;
        break;
      case 'p':
        pid = atoi(optarg);
        break;
      case 'P':
        processName = optarg;
        if (!optarg || !*optarg) {
          KLOGE("LINJECT", "process name is NULL");
          ok = false;
        } else {
          pid = findPidByProcessName(optarg);
          if (pid <= 0) {
            KLOGE("LINJECT", "failed to get Pid of process %s", processName);
            ok = false;
          }
        }
        break;
      default:
        ok = false;
        break;
    }
    if (!ok)
      break;
  }

  KLOGD("LINJECT", "Inject[%d|%s] library: %s\n", pid, processName, libraryPath);

  if (!ok || (pid < 1 && processName == nullptr) || !libraryPath || !packageName ||
      strcmp("com.kail.location", packageName))
    exit(-1);

  if (access(libraryPath, R_OK) == -1) {
    fprintf(stderr, "%s must chmod r\n", libraryPath);
    KLOGE("LINJECT", "%s must chmod r\n", libraryPath);
    exit(-1);
  }

  int rc = injectLibraryIntoProcess(pid, libraryPath,
                                    (entryArg && *entryArg) ? entryArg : packageName);
  if (rc) {
    printf("Inject fail %d.\n", rc);
    KLOGE("LINJECT", "Inject fail %d.\n", rc);
  } else {
    puts("Inject ok.");
    KLOGD("LINJECT", "Inject ok.\n");
  }
  return rc;
}

int main(int argc, char **argv) {
  return injectMain(argc, argv);
}
