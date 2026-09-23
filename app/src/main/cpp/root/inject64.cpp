// inject64.cpp
//
// Reconstructed, compilable source for the 64-bit (arm64-v8a) ptrace injector.
// Recovered from the IDA Hex-Rays decompilation in do/complete/inject64.c.
//
// The injector attaches to a target process with ptrace, locates libc / libdl /
// linker base addresses by parsing /proc/<pid>/maps, computes the remote address
// of calloc/free/dlopen/dlerror by ASLR-slide arithmetic, then drives a remote
// dlopen() of the payload .so and finally calls its exported doRun() symbol.
//
// Build: this is compiled as an executable named "kail_inject" and packaged into
// the APK as libkail_inject.so (see app/src/main/cpp/CMakeLists.txt).

#include <cerrno>
#include <cstdarg>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <dirent.h>
#include <dlfcn.h>
#include <elf.h>
#include <getopt.h>
#include <signal.h>
#include <time.h>
#include <unistd.h>

#include <sys/ptrace.h>
#include <sys/uio.h>
#include <sys/wait.h>

#include <android/log.h>

#include "kail_log.h"

// ---------------------------------------------------------------------------
// Globals
// ---------------------------------------------------------------------------
static const char *kInjectorLogTag = "LINJECT/Injector";
static int   gVerboseLoggingEnabled = 1;          // dword_5278
static int   gTargetPid             = 0;          // dword_5288
// Zygote mode (-Z): use the NeoZygisk-style hardened attach/call/detach path
// (PTRACE_SEIZE + BTYPE clearing + libc non-exec return address + GKI 2.0
// detach workaround). Only enabled explicitly so the proven system_server /
// app injection path stays byte-for-byte identical.
static bool  gZygoteMode           = false;
static bool  gAttachedViaSeize     = false;

static uint64_t gRemoteDlopen  = 0;               // qword_5290
static uint64_t gRemoteDlerror = 0;               // qword_5298
static uint64_t gRemoteDlsym   = 0;
static uint64_t gRemoteCalloc  = 0;               // qword_52A0
static uint64_t gRemoteFree    = 0;               // qword_52A8

static constexpr uint64_t kDoRunSuccess = 0x4b4c1000;
static constexpr uint64_t kDoRunAlreadyLoaded = 0x4b4c1001;

// Callback invoked when a recoverable ptrace error (ESRCH) is hit; used to
// re-stop the remote process before retrying.  (off_52B0)
static void (*gRemoteStopCallback)() = nullptr;

#define LOGV(...) \
  do { if (gVerboseLoggingEnabled) \
         KLOGD(kInjectorLogTag, __VA_ARGS__); } while (0)

// Forward declarations
static void     waitForRemoteStop();
static long     ptraceWithRetry(const char *label, int request, uintptr_t addr, uintptr_t data);
static uint64_t findLibraryBaseAddress(const char *libraryPath, int pid);

// ---------------------------------------------------------------------------
// findPidByProcessName  (sub_19D0)
//   Scan /proc, read each /proc/<pid>/cmdline and match the requested name.
// ---------------------------------------------------------------------------
static int findPidByProcessName(const char *processName) {
  if (!processName)
    return -1;

  DIR *proc = opendir("/proc");
  if (!proc)
    return -1;

  int foundPid = -1;
  for (struct dirent *entry = readdir(proc); entry; entry = readdir(proc)) {
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
// waitForRemoteStop  (sub_20E0)
//   Send SIGSTOP and spin on waitpid until the tracee is genuinely stopped,
//   forwarding any other delivered signal back via PTRACE_CONT.  Bails out
//   after ~128 ms if the expected stop never arrives.
// ---------------------------------------------------------------------------
static uint64_t nowMillis() {
  struct timespec tp;
  clock_gettime(CLOCK_MONOTONIC, &tp);
  return (uint64_t)((double)tp.tv_nsec / 1000000.0 + (double)(1000 * tp.tv_sec));
}

static void waitForRemoteStop() {
  if (gZygoteMode && gAttachedViaSeize)
    return;  // SEIZE mode: tracee is already stopped at the event stop.
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
        // Stopped by a signal other than the expected stop: forward it.
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
// ptraceWithRetry  (sub_2240)
//   ptrace() wrapper with logging and retry-on-ESRCH (up to 10 attempts).
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
    case PTRACE_ATTACH:     return "PTRACE_ATTACH";
    case PTRACE_DETACH:     return "PTRACE_DETACH";
    case PTRACE_SYSCALL:    return "PTRACE_SYSCALL";
    case PTRACE_SETOPTIONS: return "PTRACE_SETOPTIONS";
    case PTRACE_GETEVENTMSG:return "PTRACE_GETEVENTMSG";
    case PTRACE_GETSIGINFO: return "PTRACE_GETSIGINFO";
    case PTRACE_SETSIGINFO: return "PTRACE_SETSIGINFO";
    case PTRACE_GETREGSET:  return "PTRACE_GETREGSET";
    case PTRACE_SETREGSET:  return "PTRACE_SETREGSET";
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
    // Only retry for a small set of transient errnos (ESRCH/EFAULT/EIO).
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
// findLibraryBaseAddress  (sub_24C4)
//   Parse /proc/<pid>/maps and return the first base address of a mapping
//   whose path contains libraryPath.  pid == -1 means "this process".
// ---------------------------------------------------------------------------
static uint64_t findLibraryBaseAddress(const char *libraryPath, int pid) {
  char mapsPath[256] = {0};
  int target = (pid == -1) ? gTargetPid : pid;
  snprintf(mapsPath, sizeof(mapsPath), "/proc/%d/maps", target);

  FILE *fp = fopen(mapsPath, "rt");
  if (!fp) {
    LOGV("fopen error");
    return 0;
  }

  // Extract basename for fallback matching (handles path resolution differences
  // on Android 16+ where loaded library paths in maps may differ from dlopen arg)
  const char *baseName = strrchr(libraryPath, '/');
  baseName = baseName ? baseName + 1 : libraryPath;

  uint64_t base = 0;
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
  // fallback match: stock processes map /apex/.../libc.so, and the runtime-tree
  // probe in injectLibraryIntoProcess would otherwise misclassify a normal
  // zygote64/system_server as a VMOS guest, then resolve remote dlopen/doRun
  // symbols through VMOS paths, break the ASLR slide and fault immediately.
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
// findLibcNonExecReturnAddr (NeoZygisk-style)
//   Scan /proc/<pid>/maps for the first mapping of libc.so without the
//   executable permission and return its start address. Used as the remote
//   LR in zygote mode: when a remote call "returns", it traps with SIGSEGV at
//   this address instead of 0, which is more predictable across kernels.
// ---------------------------------------------------------------------------
static uint64_t findLibcNonExecReturnAddr(int pid) {
  char mapsPath[256];
  snprintf(mapsPath, sizeof(mapsPath), "/proc/%d/maps", pid);
  FILE *fp = fopen(mapsPath, "rt");
  if (!fp)
    return 0;

  uint64_t addr = 0;
  char line[1024] = {0};
  while (fgets(line, sizeof(line), fp)) {
    if (!strstr(line, "libc.so"))
      continue;
    // perms field is the 2nd column (r-w-x-p).
    const char *perms = strchr(line, ' ');
    if (!perms)
      continue;
    perms++;
    if (perms[0] == 'r' && perms[1] != 'x' && strchr(line, '/')) {
      addr = strtoull(line, nullptr, 16);
      break;
    }
  }
  fclose(fp);
  return addr;
}

// ---------------------------------------------------------------------------
// resolveRemoteSymbolAddress  (sub_25E8)
//   Translate a local symbol address into the remote process by applying the
//   difference between the local and remote module base addresses.
// ---------------------------------------------------------------------------
static uint64_t resolveRemoteSymbolAddress(const char *libraryPath, uint64_t localAddr) {
  uint64_t localBase  = findLibraryBaseAddress(libraryPath, getpid());
  uint64_t remoteBase = findLibraryBaseAddress(libraryPath, gTargetPid);
  if (!localAddr || !localBase || !remoteBase) {
    KLOGE(kInjectorLogTag, "resolveRemoteSymbolAddress: localAddr=0x%llx localBase=0x%llx remoteBase=0x%llx path=%s",
          (unsigned long long)localAddr, (unsigned long long)localBase, (unsigned long long)remoteBase, libraryPath);
    return 0;
  }
  return localAddr - localBase + remoteBase;
}

// ---------------------------------------------------------------------------
// callRemoteFunction  (sub_26FC)
//   Backup the tracee's registers, load up to 8 arguments into x0..x7, set pc
//   to the target function and lr to 0 (so the return faults with SIGSEGV),
//   continue, wait for the fault, read x0 as the return value, then restore.
// ---------------------------------------------------------------------------
//
// The watchdog timeout is configurable so the long-running doRun() call (which
// does the InjectDex JNI bring-up, including DexClassLoader compile of the
// 33MB APK from system_server) does not get killed by the same 5s timeout
// that protects the cheap calloc/dlopen calls.
static uint64_t gCallTimeoutMs = 5000;

static uint64_t callRemoteFunction(uint64_t func, int argc, ...) {
  // aarch64 user_pt_regs: x0..x30, sp, pc, pstate == 34 * 8 == 272 bytes.
  uint64_t regs[34]   = {0};
  uint64_t backup[34] = {0};

  waitForRemoteStop();

  struct iovec iov;
  iov.iov_base = regs;
  iov.iov_len  = sizeof(regs);
  ptraceWithRetry("backup", PTRACE_GETREGSET, NT_PRSTATUS, (uintptr_t)&iov);
  memcpy(backup, regs, sizeof(regs));

  int count = argc;
  if ((unsigned)(argc - 1) >= 7)
    count = 8;

  va_list ap;
  va_start(ap, argc);
  for (int i = 0; i < count; ++i)
    regs[i] = va_arg(ap, uint64_t);   // x0..x7
  va_end(ap);

  uint64_t zygoteReturnAddr = 0;
  if (gZygoteMode) {
    // NeoZygisk-style: LR points at a non-executable libc mapping so the
    // remote call traps deterministically at a real address, and the BTYPE
    // bits of PSTATE (10..11) are cleared — the zygote thread is typically
    // stopped right after an indirect branch (BTYPE=0b11), and jumping into
    // BTI-protected bionic functions with that value raises SIGILL.
    zygoteReturnAddr = findLibcNonExecReturnAddr(gTargetPid);
    regs[30] = zygoteReturnAddr;
    regs[33] &= ~(3ULL << 10);
    if (zygoteReturnAddr == 0) {
      KLOGE(kInjectorLogTag, "zygote mode: no non-exec libc mapping; falling back to LR=0");
      regs[30] = 0;
    }
  } else {
    regs[30] = 0;        // x30 (lr) -> 0  : return address faults
  }
  regs[32] = func;     // pc
  // The thumb-bit handling below is dead leftover from the 32-bit variant; it
  // is harmless on aarch64 and preserved for behavioural fidelity.
  if (func & 1) {
    regs[32] = func & ~1ull;
    regs[33] |= 0x20;
  } else {
    regs[33] &= ~0x20u;
  }

  iov.iov_base = regs;
  iov.iov_len  = sizeof(regs);
  ptraceWithRetry("call", PTRACE_SETREGSET, NT_PRSTATUS, (uintptr_t)&iov);
  ptraceWithRetry("call", PTRACE_CONT, 0, 0);
  KLOGD(kInjectorLogTag, "callRemoteFunction: continued tracee at func=0x%llx", (unsigned long long)func);

  // WATCHDOG: if the remote function hangs (typically because the linker
  // mutex is held by a sibling thread of the tracee), waitpid will block
  // forever and the tracee stays in ptrace_stop, which on system_server
  // freezes the entire device. Cap the total wait at gCallTimeoutMs ms,
  // then PTRACE_DETACH so the tracee resumes runnable.
  int status = 0;
  uint64_t startMs = nowMillis();
  bool timed_out = false;
  while (true) {
    if (nowMillis() - startMs > gCallTimeoutMs) {
      KLOGE(kInjectorLogTag, "callRemoteFunction watchdog tripped (%llums); "
                          "remote func 0x%llx never returned. Aborting.", (unsigned long long)gCallTimeoutMs, (unsigned long long)func);
      timed_out = true;
      break;
    }
    pid_t r = waitpid(gTargetPid, &status, WUNTRACED | WNOHANG);
    if (r == gTargetPid) {
      if ((status & 0xff7f) == 0xb7f) { // stopped by SIGSEGV
        // FORENSIC: capture the exact fault address of the SIGSEGV.
        siginfo_t si = {};
        if (ptrace(PTRACE_GETSIGINFO, gTargetPid, 0, &si) == 0) {
          KLOGI(kInjectorLogTag,
                "remote fault: signo=%d code=%d addr=0x%llx (func=0x%llx)",
                si.si_signo, si.si_code, (unsigned long long)si.si_addr,
                (unsigned long long)func);
          printf("inject diag: fault signo=%d code=%d addr=0x%llx func=0x%llx\n",
                 si.si_signo, si.si_code, (unsigned long long)si.si_addr,
                 (unsigned long long)func);
        }
        break;
      }
      ptraceWithRetry("waitpid", PTRACE_CONT, 0, 0);
      continue;
    }
    if (r == -1) {
      // tracee gone (probably crashed); bail to detach path.
      timed_out = true;
      break;
    }
    usleep(2000);
  }

  if (timed_out) {
    // Restore registers we have and PTRACE_DETACH so the tracee resumes,
    // even if it does so in a weird mid-call state. Better than
    // permafrozen. Caller will get garbage in regs[0]; treat as 0.
    iov.iov_base = backup;
    iov.iov_len  = sizeof(backup);
    ptraceWithRetry("restore", PTRACE_SETREGSET, NT_PRSTATUS, (uintptr_t)&iov);
    ptraceWithRetry("detach", PTRACE_DETACH, 0, 0);
    kill(gTargetPid, SIGCONT);
    return 0;
  }

  iov.iov_base = regs;
  iov.iov_len  = sizeof(regs);
  ptraceWithRetry("return", PTRACE_GETREGSET, NT_PRSTATUS, (uintptr_t)&iov);

  // FORENSIC: where did the SIGSEGV land? regs[32] is pc at stop time.
  printf("inject diag: post-call pc=0x%llx sp=0x%llx x0=0x%llx\n",
         (unsigned long long)regs[32], (unsigned long long)regs[31],
         (unsigned long long)regs[0]);
  if (gZygoteMode && zygoteReturnAddr != 0 && regs[32] != zygoteReturnAddr) {
    KLOGE(kInjectorLogTag,
          "zygote mode: stopped at pc=0x%llx, expected return addr 0x%llx",
          (unsigned long long)regs[32], (unsigned long long)zygoteReturnAddr);
    printf("inject diag: zygote stop mismatch pc=0x%llx ret=0x%llx\n",
           (unsigned long long)regs[32], (unsigned long long)zygoteReturnAddr);
  }

  iov.iov_base = backup;
  iov.iov_len  = sizeof(backup);
  ptraceWithRetry("restore", PTRACE_SETREGSET, NT_PRSTATUS, (uintptr_t)&iov);
  if (!gZygoteMode) {
    // Legacy path: resume the tracee after restoring the original registers.
    ptraceWithRetry("continue", PTRACE_CONT, 0, 0);
  }
  // Zygote mode (SEIZE): keep the tracee stopped at the SIGSEGV stop; the next
  // remote call overwrites the registers again, and only the final detach
  // (GKI workaround: PTRACE_SYSCALL + PTRACE_DETACH SIGCONT) lets it run.

  return regs[0];
}

// ---------------------------------------------------------------------------
// writeRemoteString  (sub_2638)
//   Allocate a buffer in the tracee with the remote calloc, then POKE the
//   string bytes 8-at-a-time into that buffer.  Returns the remote address.
// ---------------------------------------------------------------------------
static uint64_t writeRemoteString(const char *value) {
  size_t len = strlen(value) + 1;
  uint64_t remote = callRemoteFunction(gRemoteCalloc, 2, (uint64_t)len, (uint64_t)1);

  waitForRemoteStop();

  size_t padded = len + (len & 7);
  char *local = (char *)malloc(padded);
  memset(local, 0, padded);
  memcpy(local, value, len);

  for (size_t off = 0; off < len; off += 8) {
    if (ptraceWithRetry("string", PTRACE_POKETEXT, (uintptr_t)(remote + off),
                        *(uint64_t *)&local[off]) == -1)
      break;
  }

  free(local);
  return remote;
}

static void readRemoteCString(uint64_t remote, char *out, size_t outSize, const char *label) {
  if (!remote || !out || outSize == 0)
    return;

  waitForRemoteStop();
  memset(out, 0, outSize);
  for (size_t off = 0; off < outSize - 1; off += 8) {
    long word = ptraceWithRetry(label, PTRACE_PEEKTEXT, (uintptr_t)(remote + off), 0);
    if (word == -1)
      break;
    *(uint64_t *)&out[off] = (uint64_t)word;
    if (memchr(&out[off], '\0', 8))
      break;
  }
  out[outSize - 1] = '\0';
}

// ---------------------------------------------------------------------------
// injectLibraryIntoProcess  (sub_1B88)
// ---------------------------------------------------------------------------
static int injectLibraryIntoProcess(int pid, const char *libraryPath, const char *entryArg) {
  gTargetPid = pid;
  gRemoteStopCallback = waitForRemoteStop;

  if (gZygoteMode) {
    // NeoZygisk-style attach: PTRACE_SEIZE first (no SIGSTOP, clean
    // PTRACE_EVENT_STOP), falling back to classic ATTACH. In seize mode the
    // tracee is already stopped after the event stop; skip the extra SIGSTOP.
    if (ptrace(PTRACE_SEIZE, gTargetPid, 0, PTRACE_O_EXITKILL) == -1) {
      if (errno != EIO) {
        LOGV("PTRACE_SEIZE failed errno=%d", errno);
        return 1;
      }
      KLOGI(kInjectorLogTag, "zygote mode: SEIZE EIO, falling back to ATTACH");
      if (ptraceWithRetry("attach", PTRACE_ATTACH, 0, 0) == -1) {
        LOGV("Failed to attach to process %d", gTargetPid);
        return 1;
      }
      gAttachedViaSeize = false;
      kill(gTargetPid, SIGSTOP);
      waitForRemoteStop();
    } else {
      gAttachedViaSeize = true;
      // PTRACE_SEIZE does NOT stop the tracee by itself: a running process
      // only reports PTRACE_EVENT_STOP when it stops (signal / group stop) or
      // when the tracer requests it via PTRACE_INTERRUPT. Running zygote
      // never stops on its own, so interrupt it explicitly.
      if (ptrace(PTRACE_INTERRUPT, gTargetPid, 0, 0) == -1) {
        KLOGE(kInjectorLogTag, "zygote mode: PTRACE_INTERRUPT failed errno=%d", errno);
        return 1;
      }
      // Bounded wait (no watchdog on this path -> would deadlock forever).
      uint64_t deadline = nowMillis() + 5000;
      bool stopped = false;
      int status = 0;
      while (nowMillis() < deadline) {
        pid_t r = waitpid(gTargetPid, &status, __WALL | WNOHANG);
        if (r == gTargetPid) {
          stopped = true;
          break;
        }
        if (r == -1)
          break;
        usleep(2000);
      }
      if (!stopped) {
        KLOGE(kInjectorLogTag, "zygote mode: no event stop after INTERRUPT");
        return 1;
      }
    }
  } else {
    if (ptraceWithRetry("attach", PTRACE_ATTACH, 0, 0) == -1) {
      LOGV("Failed to attach to process %d", gTargetPid);
      return 1;
    }
    kill(gTargetPid, SIGSTOP);
    waitForRemoteStop();
  }

  // FORENSIC: snapshot where the target thread was parked when we stopped it.
  {
    uint64_t origRegs[34] = {0};
    struct iovec origIov;
    origIov.iov_base = origRegs;
    origIov.iov_len  = sizeof(origRegs);
    ptraceWithRetry("originals", PTRACE_GETREGSET, NT_PRSTATUS, (uintptr_t)&origIov);
    printf("inject diag: original pc=0x%llx sp=0x%llx x0=0x%llx\n",
           (unsigned long long)origRegs[32], (unsigned long long)origRegs[31],
           (unsigned long long)origRegs[0]);
  }

  // Resolve which runtime tree the target uses (VMOS / Twoyi / VPhoneGaGa /
  // stock Android) by probing libc base addresses.
  const char *libcPath   = "/data/data/com.vmos.app/osimg/r/ot01/system/lib64/libc.so";
  const char *libdlPath;
  const char *linkerPath;

  uint64_t vmosApp     = findLibraryBaseAddress("/data/data/com.vmos.app/osimg/r/ot01/system/lib64/libc.so", gTargetPid);
  uint64_t vmosProOt01 = findLibraryBaseAddress("/data/data/com.vmos.pro/osimg/r/ot01/system/lib64/libc.so", gTargetPid);
  uint64_t vmosProOt02 = findLibraryBaseAddress("/data/data/com.vmos.pro/osimg/r/ot02/system/lib64/libc.so", gTargetPid);
  uint64_t twoyi       = findLibraryBaseAddress("/data/data/io.twoyi/rootfs/system/lib64/libc.so", gTargetPid);
  uint64_t vphone      = findLibraryBaseAddress("/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/lib64/libc.so", gTargetPid);

  if (vmosApp) {
    linkerPath = "/data/data/com.vmos.app/osimg/r/ot01/system/bin/linker64";
    libdlPath  = "/data/data/com.vmos.app/osimg/r/ot01/system/lib64/libdl.so";
  } else if (vmosProOt01) {
    linkerPath = "/data/data/com.vmos.pro/osimg/r/ot01/system/bin/linker64";
    libcPath   = "/data/data/com.vmos.pro/osimg/r/ot01/system/lib64/libc.so";
    libdlPath  = "/data/data/com.vmos.pro/osimg/r/ot01/system/lib64/libdl.so";
  } else if (vmosProOt02) {
    linkerPath = "/data/data/com.vmos.pro/osimg/r/ot02/system/bin/linker64";
    libcPath   = "/data/data/com.vmos.pro/osimg/r/ot02/system/lib64/libc.so";
    libdlPath  = "/data/data/com.vmos.pro/osimg/r/ot02/system/lib64/libdl.so";
  } else if (twoyi) {
    linkerPath = "/data/data/io.twoyi/rootfs/system/bin/linker64";
    libcPath   = "/data/data/io.twoyi/rootfs/system/lib64/libc.so";
    libdlPath  = "/data/data/io.twoyi/rootfs/system/lib64/libdl.so";
  } else if (vphone) {
    linkerPath = "/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/bin/linker64";
    libcPath   = "/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/lib64/libc.so";
    libdlPath  = "/data/data/com.vphonegaga.titan/files/androidfs_7.1.2/system/lib64/libdl.so";
  } else {
    libcPath  = "/apex/com.android.runtime/lib64/bionic/libc.so";
    libdlPath = "/apex/com.android.runtime/lib64/bionic/libdl.so";
    if (access("/apex/com.android.runtime/lib64/bionic/libc.so", R_OK))
      libcPath = "/system/lib64/libc.so";
    linkerPath = "/apex/com.android.runtime/bin/linker64";
    if (access("/apex/com.android.runtime/lib64/bionic/libdl.so", R_OK))
      libdlPath = "/system/lib64/libdl.so";
    if (access("/apex/com.android.runtime/bin/linker64", R_OK))
      linkerPath = "/system/bin/linker64";
  }

  LOGV("libc:%s", libcPath);

  gRemoteCalloc = resolveRemoteSymbolAddress(libcPath, (uint64_t)&calloc);
  gRemoteFree   = resolveRemoteSymbolAddress(libcPath, (uint64_t)&free);

  // On newer Androids dlopen/dlerror live in libdl.so rather than the linker.
  if (findLibraryBaseAddress(libdlPath, -1) && findLibraryBaseAddress(libdlPath, gTargetPid)) {
    void *handle = dlopen(libdlPath, RTLD_NOW);
    gRemoteDlopen  = resolveRemoteSymbolAddress(libdlPath, (uint64_t)dlsym(handle, "dlopen"));
    gRemoteDlerror = resolveRemoteSymbolAddress(libdlPath, (uint64_t)dlsym(handle, "dlerror"));
    gRemoteDlsym   = resolveRemoteSymbolAddress(libdlPath, (uint64_t)dlsym(handle, "dlsym"));
    dlclose(handle);
  } else {
    gRemoteDlopen  = resolveRemoteSymbolAddress(linkerPath, (uint64_t)&dlopen);
    gRemoteDlerror = resolveRemoteSymbolAddress(linkerPath, (uint64_t)&dlerror);
    gRemoteDlsym   = resolveRemoteSymbolAddress(linkerPath, (uint64_t)&dlsym);
  }

  void *runtime = dlopen("/system/lib64/libandroid_runtime.so", RTLD_NOW);
  uint64_t javaVm = resolveRemoteSymbolAddress(
      "/system/lib64/libandroid_runtime.so",
      (uint64_t)dlsym(runtime, "_ZN7android14AndroidRuntime7mJavaVME"));
  dlclose(runtime);

  LOGV("calloc:%p free:%p dlopen:%p dlsym:%p dlerror:%p javavm:%p",
       (void *)gRemoteCalloc, (void *)gRemoteFree, (void *)gRemoteDlopen,
       (void *)gRemoteDlsym, (void *)gRemoteDlerror, (void *)javaVm);
  printf("inject diag: calloc=0x%llx free=0x%llx dlopen=0x%llx dlsym=0x%llx dlerror=0x%llx javavm=0x%llx\n",
         (unsigned long long)gRemoteCalloc, (unsigned long long)gRemoteFree,
         (unsigned long long)gRemoteDlopen, (unsigned long long)gRemoteDlsym,
         (unsigned long long)gRemoteDlerror,
         (unsigned long long)javaVm);

  uint64_t remotePath   = writeRemoteString(libraryPath);
  KLOGI(kInjectorLogTag, "writeRemoteString done, remotePath=0x%llx", (unsigned long long)remotePath);
  uint64_t remoteHandle = callRemoteFunction(gRemoteDlopen, 2, remotePath, (uint64_t)RTLD_NOW);
  KLOGI(kInjectorLogTag, "remote dlopen returned 0x%llx", (unsigned long long)remoteHandle);
  printf("inject diag: remotePath=0x%llx remoteHandle=0x%llx\n",
         (unsigned long long)remotePath, (unsigned long long)remoteHandle);

  callRemoteFunction(gRemoteFree, 1, remotePath);

  int result;
  if (remoteHandle) {
    void *handle = dlopen(libraryPath, RTLD_NOW);
    void *doRunLocal = handle ? dlsym(handle, "doRun") : nullptr;
    uint64_t doRunRemoteBySlide = resolveRemoteSymbolAddress(libraryPath, (uint64_t)doRunLocal);
    if (handle)
      dlclose(handle);

    uint64_t doRunRemoteByDlsym = 0;
    if (gRemoteDlsym) {
      uint64_t remoteSymbolName = writeRemoteString("doRun");
      if (remoteSymbolName) {
        doRunRemoteByDlsym = callRemoteFunction(gRemoteDlsym, 2, remoteHandle, remoteSymbolName);
        callRemoteFunction(gRemoteFree, 1, remoteSymbolName);
      }
    }

    uint64_t doRunRemote = doRunRemoteByDlsym ? doRunRemoteByDlsym : doRunRemoteBySlide;
    KLOGI(kInjectorLogTag, "doRun local=%p remote=0x%llx dlsym=0x%llx slide=0x%llx",
          doRunLocal, (unsigned long long)doRunRemote,
          (unsigned long long)doRunRemoteByDlsym, (unsigned long long)doRunRemoteBySlide);
    printf("inject diag: doRunLocal=%p doRunRemote=0x%llx remoteDlsym=0x%llx slide=0x%llx\n",
           doRunLocal, (unsigned long long)doRunRemote,
           (unsigned long long)doRunRemoteByDlsym, (unsigned long long)doRunRemoteBySlide);

    if (!doRunRemoteByDlsym && gRemoteDlerror) {
      char errbuf[1024] = {0};
      uint64_t errPtr = callRemoteFunction(gRemoteDlerror, 0);
      readRemoteCString(errPtr, errbuf, sizeof(errbuf), "dlerror");
      if (errbuf[0]) {
        KLOGW(kInjectorLogTag, "remote dlerror: %s", errbuf);
        printf("inject diag: remote dlerror: %s\n", errbuf);
      }
    }

    result = 1;
    if (doRunRemote) {
      uint64_t remoteArg = writeRemoteString(entryArg);
      // doRun does the InjectDex JNI bring-up, which includes
      // DexClassLoader compile of the 33MB APK from inside system_server.
      // The first cold-cache run on the ZTE NX769J takes >5s, so allow a
      // generous 120s window for this single call. Subsequent cleanup
      // calls go back to the default 5s timeout.
      gCallTimeoutMs = 120000;
      uint64_t doRunResult = callRemoteFunction(doRunRemote, 2, javaVm, remoteArg);
      gCallTimeoutMs = 5000;
      callRemoteFunction(gRemoteFree, 1, remoteArg);
      KLOGI(kInjectorLogTag, "doRun call returned 0x%llx", (unsigned long long)doRunResult);
      printf("inject diag: doRunResult=0x%llx\n", (unsigned long long)doRunResult);
      if (doRunResult == kDoRunSuccess || doRunResult == kDoRunAlreadyLoaded) {
        result = 0;
      } else {
        KLOGE(kInjectorLogTag, "doRun returned failure stage 0x%llx", (unsigned long long)doRunResult);
        printf("inject diag: doRun failed stage=0x%llx\n", (unsigned long long)doRunResult);
      }
    } else {
      KLOGE(kInjectorLogTag, "doRun resolve failed: local=%p remote=0x%llx", doRunLocal, (unsigned long long)doRunRemote);
      printf("inject diag: doRun resolve failed\n");
    }
  } else {
    // dlopen failed remotely: read the error string out of the tracee.
    char errbuf[1024] = {0};
    uint64_t errPtr = callRemoteFunction(gRemoteDlerror, 0);
    readRemoteCString(errPtr, errbuf, sizeof(errbuf), "dlerror");
    LOGV("dlopen failed: %s", errbuf);
    printf("inject diag: dlopen failed: %s\n", errbuf);
    result = 1;
  }

  waitForRemoteStop();
  if (gZygoteMode) {
    // GKI 2.0 workaround: advance the tracee by one syscall stop to clear the
    // kernel-side ptrace signal-stop state, then detach with SIGCONT so the
    // zygote resumes cleanly.
    if (ptrace(PTRACE_SYSCALL, gTargetPid, 0, 0) == -1) {
      KLOGE(kInjectorLogTag, "zygote mode: PTRACE_SYSCALL failed, force detach");
      ptrace(PTRACE_DETACH, gTargetPid, 0, SIGCONT);
    } else {
      int st = 0;
      waitpid(gTargetPid, &st, __WALL);
      ptrace(PTRACE_DETACH, gTargetPid, 0, SIGCONT);
    }
  } else {
    ptraceWithRetry("detach", PTRACE_DETACH, 0, 0);
    kill(gTargetPid, SIGCONT);
  }
  return result;
}

// ---------------------------------------------------------------------------
// injectMain  (sub_2A04)
// ---------------------------------------------------------------------------
static int injectMain(int argc, char **argv) {
  int   pid          = 0;
  char *processName  = nullptr;
  char *libraryPath  = nullptr;
  char *packageName  = nullptr;
  char *entryArg     = nullptr;

  int opt;
  bool ok = true;
  while ((opt = getopt(argc, argv, "p:P:l:n:a:hZ")) != -1) {
    switch (opt) {
      case 'Z':
        gZygoteMode = true;
        break;
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
