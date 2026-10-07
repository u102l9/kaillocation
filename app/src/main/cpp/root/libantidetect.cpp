// libantidetect.cpp
//
// Reconstructed, compilable source for the anti-detection libc-interception
// library (arm64).  Recovered from do/complete/libantidetect64.c.
//
// It installs PLT/GOT hooks (via elf_hooker.h) on libc filesystem entry points
// so that, while "mocking" is enabled, paths that would reveal the framework's
// own files (or common root/emulator artifacts) are rewritten before the real
// libc call runs.  The rewrites:
//   - any path containing a registered "antidetect file name"  -> garbled name
//   - paths ending in "/maps"   (when via fopen/open/system)    -> "/status"
//   - paths ending in "/su"                                     -> "/su_f"
//   - opendir("/sbin")          -> "/etc"
//   - opendir(".../Android/data")-> ".../Documents/data"
//   - opendir(".../data/kail-loc")-> "/data/_null"
//
// Only the project-specific logic is reconstructed here; the large body of
// statically-linked C++ runtime / libc++abi demangler / unwinder code present
// in the decompilation is provided automatically by the toolchain and is not
// part of the original source.
//
// JNI surface (com.kail.location.inject.utils.LAntiDetect):
//   init(), setMocking(z), isMocking(), setAntidetectFileNames(String[]),
//   doHookLib(..., String libraryPath)

#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <ctime>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include <jni.h>
#include <android/log.h>

#include "elf_hooker.h"

using namespace elfhook;

static const char *kAdTag = "LAntiDetect.native";
static const char *kGarbledToken = "qazxswefs";

// ---------------------------------------------------------------------------
// State (decompiled globals at 0x4C0xx)
// ---------------------------------------------------------------------------
static int    gMocking    = 0;   // isMocking
static int    gAuthorized = 0;   // isAuthorized
static int    gHooked     = 0;   // isHooked

static char **gFileNames     = nullptr;  // mAntidetectFileNames
static int    gFileNameCount = 0;        // mAntidetectFileNamesSize

// ---------------------------------------------------------------------------
// Saved originals of every hooked libc function.
// ---------------------------------------------------------------------------
static FILE *(*src_fopen)(const char *, const char *) = nullptr;
static int   (*src_open)(const char *, int, ...) = nullptr;
static int   (*src_open64)(const char *, int, ...) = nullptr;
static DIR  *(*src_opendir)(const char *) = nullptr;
static int   (*src_system)(const char *) = nullptr;
static int   (*src_faccessat)(int, const char *, int, int) = nullptr;
static int   (*src_fchmodat)(int, const char *, mode_t, int) = nullptr;
static int   (*src_fstatat)(int, const char *, void *, int) = nullptr;
static int   (*src_fstatat64)(int, const char *, void *, int) = nullptr;
static int   (*src_mknodat)(int, const char *, mode_t, dev_t) = nullptr;
static int   (*src_mknod)(const char *, mode_t, dev_t) = nullptr;
static int   (*src_utimensat)(int, const char *, const void *, int) = nullptr;
static int   (*src_fchownat)(int, const char *, uid_t, gid_t, int) = nullptr;
static int   (*src_renameat)(int, const char *, int, const char *) = nullptr;
static int   (*src_rename)(const char *, const char *) = nullptr;
static int   (*src_unlinkat)(int, const char *, int) = nullptr;
static int   (*src_unlink)(const char *) = nullptr;
static int   (*src_symlinkat)(const char *, int, const char *) = nullptr;
static int   (*src_symlink)(const char *, const char *) = nullptr;
static int   (*src_linkat)(int, const char *, int, const char *, int) = nullptr;
static int   (*src_link)(const char *, const char *) = nullptr;
static int   (*src_access)(const char *, int) = nullptr;
static int   (*src_chmod)(const char *, mode_t) = nullptr;
static int   (*src_chown)(const char *, uid_t, gid_t) = nullptr;
static int   (*src_lstat)(const char *, void *) = nullptr;
static int   (*src_stat)(const char *, void *) = nullptr;
static int   (*src_stat64)(const char *, void *) = nullptr;
static int   (*src_mkdirat)(int, const char *, mode_t) = nullptr;
static int   (*src_mkdir)(const char *, mode_t) = nullptr;
static int   (*src_rmdir)(const char *) = nullptr;
static int   (*src_readlinkat)(int, const char *, char *, size_t) = nullptr;
static int   (*src_readlink)(const char *, char *, size_t) = nullptr;
static int   (*src___statfs64)(const char *, size_t, void *) = nullptr;
static int   (*src_statfs64)(const char *, void *) = nullptr;
static int   (*src_truncate)(const char *, off_t) = nullptr;
static int   (*src_chdir)(const char *) = nullptr;
static int   (*src___openat)(int, const char *, int, int) = nullptr;
static int   (*src_openat)(int, const char *, int, int) = nullptr;
static int   (*src___open)(const char *, int, int) = nullptr;
static int   (*src___statfs)(const char *, size_t, void *) = nullptr;
static int   (*src_statfs)(const char *, void *) = nullptr;

// ---------------------------------------------------------------------------
// strrpc: return a freshly-allocated copy of `text` with every occurrence of
// `search` replaced by `replace`.  (decompiled strrpc, made heap-based.)
// ---------------------------------------------------------------------------
static char *strrpc(const char *text, const char *search, const char *replace) {
  size_t searchLen = strlen(search);
  if (searchLen == 0)
    return strdup(text);

  size_t replaceLen = strlen(replace);
  size_t count = 0;
  for (const char *p = text; (p = strstr(p, search)); p += searchLen)
    ++count;

  size_t outLen = strlen(text) + count * (replaceLen > searchLen ? replaceLen - searchLen : 0) + 1;
  char *out = (char *)malloc(outLen);
  out[0] = 0;

  const char *cur = text;
  const char *hit;
  char *w = out;
  while ((hit = strstr(cur, search))) {
    size_t pre = (size_t)(hit - cur);
    memcpy(w, cur, pre);
    w += pre;
    memcpy(w, replace, replaceLen);
    w += replaceLen;
    cur = hit + searchLen;
  }
  strcpy(w, cur);
  return out;
}

// generateRandomStr: 10-char random alphanumeric token.  (decompiled helper;
// retained for completeness even though the hooks use the fixed token above.)
static const char *generateRandomStr() {
  static char buf[11];
  srand((unsigned)time(nullptr));
  for (int i = 0; i < 10; ++i) {
    int kind = rand() % 3;
    if (kind == 0)      buf[i] = 'a' + rand() % 26;
    else if (kind == 1) buf[i] = 'A' + rand() % 26;
    else                buf[i] = '0' + rand() % 10;
  }
  buf[10] = 0;
  return buf;
}

static bool endsWith(const char *s, const char *suffix) {
  size_t ls = strlen(s), lf = strlen(suffix);
  return ls >= lf && lf > 0 && memcmp(s + ls - lf, suffix, lf) == 0;
}

// relocatePath: if `path` contains a registered antidetect name, return a
// rewritten heap copy (caller must not free in the original; the hooks pass the
// result straight to libc, mirroring the decompilation's lifetime behaviour).
static const char *relocateByFileNames(const char *path) {
  if (!path || gFileNameCount < 1)
    return path;
  for (int i = 0; i < gFileNameCount; ++i) {
    if (strstr(path, gFileNames[i]))
      return strrpc(path, gFileNames[i], kGarbledToken);
  }
  return path;
}

// Shared rewrite for fopen/open/open64/openat/system: handle /maps and /su
// specially, then fall back to the registered-name replacement.
static const char *rewriteForFileOrExec(const char *path) {
  if (gMocking != 1 || !path)
    return path;
  // /proc/<pid>/maps, /proc/self/task/<tid>/maps, /proc/self/smaps: redirect to
  // status so a detector scanning for our injected .so names sees no mappings.
  if (endsWith(path, "/maps"))
    return strrpc(path, "/maps", "/status");
  if (endsWith(path, "/smaps"))
    return strrpc(path, "/smaps", "/status");
  if (endsWith(path, "/su"))
    return strrpc(path, "/su", "/su_f");
  return relocateByFileNames(path);
}

// ===========================================================================
// Hooks
// ===========================================================================
static FILE *hook_fopen(const char *path, const char *mode) {
  return src_fopen(rewriteForFileOrExec(path), mode);
}
static int hook_open(const char *path, int flags, ...) {
  return src_open(rewriteForFileOrExec(path), flags);
}
static int hook_open64(const char *path, int flags, ...) {
  return src_open64(rewriteForFileOrExec(path), flags);
}
static int hook_system(const char *cmd) {
  return src_system(rewriteForFileOrExec(cmd));
}

static DIR *hook_opendir(const char *path) {
  const char *p = path;
  if (gMocking == 1) {
    if (strstr(path, "/sbin"))
      p = strrpc(path, "/sbin", "/etc");
    else if (strstr(path, "/sdcard/Android/data"))
      p = strrpc(path, "/sdcard/Android/data", "/sdcard/Documents/data");
    else if (strstr(path, "/mnt/sdcard/Android/data"))
      p = strrpc(path, "/mnt/sdcard/Android/data", "/sdcard/Documents/data");
    else if (strstr(path, "/mnt/user/0/primary/Android/data"))
      p = strrpc(path, "/mnt/user/0/primary/Android/data", "/sdcard/Documents/data");
    else if (strstr(path, "/storage/emulated/0/Android/data"))
      p = strrpc(path, "/storage/emulated/0/Android/data", "/sdcard/Documents/data");
    else if (strstr(path, "/storage/sdcard/Android/data"))
      p = strrpc(path, "/storage/sdcard/Android/data", "/sdcard/Documents/data");
    else if (strstr(path, "/data/kail-loc"))
      p = "/data/_null";
    else
      p = relocateByFileNames(path);
  }
  return src_opendir(p);
}

// The remaining hooks share the simple "replace registered names" behaviour.
#define ANTIDETECT_PATH(arg) (gMocking == 1 ? relocateByFileNames(arg) : (arg))

static int hook_faccessat(int fd, const char *path, int mode, int flags) {
  return src_faccessat(fd, ANTIDETECT_PATH(path), mode, flags);
}
static int hook_fchmodat(int fd, const char *path, mode_t mode, int flags) {
  return src_fchmodat(fd, ANTIDETECT_PATH(path), mode, flags);
}
static int hook_fstatat(int fd, const char *path, void *st, int flags) {
  return src_fstatat(fd, ANTIDETECT_PATH(path), st, flags);
}
static int hook_fstatat64(int fd, const char *path, void *st, int flags) {
  return src_fstatat64(fd, ANTIDETECT_PATH(path), st, flags);
}
static int hook_mknodat(int fd, const char *path, mode_t mode, dev_t dev) {
  return src_mknodat(fd, ANTIDETECT_PATH(path), mode, dev);
}
static int hook_mknod(const char *path, mode_t mode, dev_t dev) {
  return src_mknod(ANTIDETECT_PATH(path), mode, dev);
}
static int hook_utimensat(int fd, const char *path, const void *times, int flags) {
  return src_utimensat(fd, ANTIDETECT_PATH(path), times, flags);
}
static int hook_fchownat(int fd, const char *path, uid_t u, gid_t g, int flags) {
  return src_fchownat(fd, ANTIDETECT_PATH(path), u, g, flags);
}
static int hook_renameat(int ofd, const char *oldp, int nfd, const char *newp) {
  return src_renameat(ofd, ANTIDETECT_PATH(oldp), nfd, newp);
}
static int hook_rename(const char *oldp, const char *newp) {
  return src_rename(ANTIDETECT_PATH(oldp), newp);
}
static int hook_unlinkat(int fd, const char *path, int flags) {
  return src_unlinkat(fd, ANTIDETECT_PATH(path), flags);
}
static int hook_unlink(const char *path) {
  return src_unlink(ANTIDETECT_PATH(path));
}
static int hook_symlinkat(const char *target, int fd, const char *path) {
  return src_symlinkat(target, fd, ANTIDETECT_PATH(path));
}
static int hook_symlink(const char *target, const char *path) {
  return src_symlink(target, ANTIDETECT_PATH(path));
}
static int hook_linkat(int ofd, const char *oldp, int nfd, const char *newp, int flags) {
  return src_linkat(ofd, ANTIDETECT_PATH(oldp), nfd, newp, flags);
}
static int hook_link(const char *oldp, const char *newp) {
  return src_link(ANTIDETECT_PATH(oldp), newp);
}
static int hook_access(const char *path, int mode) {
  return src_access(ANTIDETECT_PATH(path), mode);
}
static int hook_chmod(const char *path, mode_t mode) {
  return src_chmod(ANTIDETECT_PATH(path), mode);
}
static int hook_chown(const char *path, uid_t u, gid_t g) {
  return src_chown(ANTIDETECT_PATH(path), u, g);
}
static int hook_lstat(const char *path, void *st) {
  return src_lstat(ANTIDETECT_PATH(path), st);
}
static int hook_stat(const char *path, void *st) {
  return src_stat(ANTIDETECT_PATH(path), st);
}
static int hook_stat64(const char *path, void *st) {
  return src_stat64(ANTIDETECT_PATH(path), st);
}
static int hook_mkdirat(int fd, const char *path, mode_t mode) {
  return src_mkdirat(fd, ANTIDETECT_PATH(path), mode);
}
static int hook_mkdir(const char *path, mode_t mode) {
  return src_mkdir(ANTIDETECT_PATH(path), mode);
}
static int hook_rmdir(const char *path) {
  return src_rmdir(ANTIDETECT_PATH(path));
}
static int hook_readlinkat(int fd, const char *path, char *buf, size_t len) {
  return src_readlinkat(fd, ANTIDETECT_PATH(path), buf, len);
}
static int hook_readlink(const char *path, char *buf, size_t len) {
  return src_readlink(ANTIDETECT_PATH(path), buf, len);
}
static int hook___statfs64(const char *path, size_t sz, void *st) {
  return src___statfs64(ANTIDETECT_PATH(path), sz, st);
}
static int hook_statfs64(const char *path, void *st) {
  return src_statfs64(ANTIDETECT_PATH(path), st);
}
static int hook_truncate(const char *path, off_t len) {
  return src_truncate(ANTIDETECT_PATH(path), len);
}
static int hook_chdir(const char *path) {
  return src_chdir(ANTIDETECT_PATH(path));
}
static int hook___openat(int fd, const char *path, int flags, int mode) {
  return src___openat(fd, rewriteForFileOrExec(path), flags, mode);
}
static int hook_openat(int fd, const char *path, int flags, int mode) {
  return src_openat(fd, rewriteForFileOrExec(path), flags, mode);
}
static int hook___open(const char *path, int flags, int mode) {
  return src___open(rewriteForFileOrExec(path), flags, mode);
}
static int hook___statfs(const char *path, size_t sz, void *st) {
  return src___statfs(ANTIDETECT_PATH(path), sz, st);
}
static int hook_statfs(const char *path, void *st) {
  return src_statfs(ANTIDETECT_PATH(path), st);
}

#undef ANTIDETECT_PATH

// ---------------------------------------------------------------------------
// doHook  (decompiled doHook): parse the target libc image and install every
// filesystem hook.
// ---------------------------------------------------------------------------
static void doHook(const char *libcPath) {
  void *base = ElfHooker::getModuleBase(libcPath);
  const char *name = libcPath;
  if (!base) {
    const char *path = ElfHooker::getModulePath("libc.so");
    if (!path)
      return;
    name = path;
    base = ElfHooker::getModuleBase(path);
    if (!base)
      return;
  }

  ElfReader reader(name, base);
  if (reader.parse() != 0) {
    KLOGE(kAdTag, "failed to parse %s in %d maps at %p", "libc.so", getpid(), base);
    return;
  }

  reader.hook("fopen",      (void *)hook_fopen,      (void **)&src_fopen);
  reader.hook("open",       (void *)hook_open,       (void **)&src_open);
  reader.hook("open64",     (void *)hook_open64,     (void **)&src_open64);
  reader.hook("opendir",    (void *)hook_opendir,    (void **)&src_opendir);
  reader.hook("system",     (void *)hook_system,     (void **)&src_system);
  reader.hook("faccessat",  (void *)hook_faccessat,  (void **)&src_faccessat);
  reader.hook("__openat_2", (void *)hook___openat,   (void **)&src___openat);
  reader.hook("openat",     (void *)hook_openat,     (void **)&src_openat);
  reader.hook("fchmodat",   (void *)hook_fchmodat,   (void **)&src_fchmodat);
  reader.hook("fchownat",   (void *)hook_fchownat,   (void **)&src_fchownat);
  reader.hook("renameat",   (void *)hook_renameat,   (void **)&src_renameat);
  reader.hook("fstatat64",  (void *)hook_fstatat64,  (void **)&src_fstatat64);
  reader.hook("__statfs",   (void *)hook___statfs,   (void **)&src___statfs);
  reader.hook("__statfs64", (void *)hook___statfs64, (void **)&src___statfs64);
  reader.hook("statfs",     (void *)hook_statfs,     (void **)&src_statfs);
  reader.hook("statfs64",   (void *)hook_statfs64,   (void **)&src_statfs64);
  reader.hook("mkdirat",    (void *)hook_mkdirat,    (void **)&src_mkdirat);
  reader.hook("mknodat",    (void *)hook_mknodat,    (void **)&src_mknodat);
  reader.hook("truncate",   (void *)hook_truncate,   (void **)&src_truncate);
  reader.hook("linkat",     (void *)hook_linkat,     (void **)&src_linkat);
  reader.hook("readlinkat", (void *)hook_readlinkat, (void **)&src_readlinkat);
  reader.hook("unlinkat",   (void *)hook_unlinkat,   (void **)&src_unlinkat);
  reader.hook("symlinkat",  (void *)hook_symlinkat,  (void **)&src_symlinkat);
  reader.hook("utimensat",  (void *)hook_utimensat,  (void **)&src_utimensat);
  reader.hook("chdir",      (void *)hook_chdir,      (void **)&src_chdir);
  reader.hook("access",     (void *)hook_access,     (void **)&src_access);
  reader.hook("__open",     (void *)hook___open,     (void **)&src___open);
  reader.hook("stat",       (void *)hook_stat,       (void **)&src_stat);
  reader.hook("stat64",     (void *)hook_stat64,     (void **)&src_stat64);
  reader.hook("lstat",      (void *)hook_lstat,      (void **)&src_lstat);
  reader.hook("fstatat",    (void *)hook_fstatat,    (void **)&src_fstatat);
  reader.hook("chmod",      (void *)hook_chmod,      (void **)&src_chmod);
  reader.hook("chown",      (void *)hook_chown,      (void **)&src_chown);
  reader.hook("rename",     (void *)hook_rename,     (void **)&src_rename);
  reader.hook("rmdir",      (void *)hook_rmdir,      (void **)&src_rmdir);
  reader.hook("mkdir",      (void *)hook_mkdir,      (void **)&src_mkdir);
  reader.hook("mknod",      (void *)hook_mknod,      (void **)&src_mknod);
  reader.hook("link",       (void *)hook_link,       (void **)&src_link);
  reader.hook("unlink",     (void *)hook_unlink,     (void **)&src_unlink);
  reader.hook("readlink",   (void *)hook_readlink,   (void **)&src_readlink);
  reader.hook("symlink",    (void *)hook_symlink,    (void **)&src_symlink);
}

// doInit  (decompiled doInit): pick the libc that belongs to the current
// runtime tree (VMOS variants / APEX / system) and hook it.
static void doInit() {
  if (!gAuthorized || gHooked == 1)
    return;
  gHooked = 1;

  const char *libc = "/data/data/com.vmos.app/osimg/r/ot01/system/lib64/libc.so";
  void *vmosApp     = ElfHooker::getModuleBase("/data/data/com.vmos.app/osimg/r/ot01/system/lib64/libc.so");
  void *vmosProOt01 = ElfHooker::getModuleBase("/data/data/com.vmos.pro/osimg/r/ot01/system/lib64/libc.so");
  void *vmosProOt02 = ElfHooker::getModuleBase("/data/data/com.vmos.pro/osimg/r/ot02/system/lib64/libc.so");

  if (!vmosApp) {
    if (vmosProOt01) {
      libc = "/data/data/com.vmos.pro/osimg/r/ot01/system/lib64/libc.so";
    } else if (vmosProOt02) {
      libc = "/data/data/com.vmos.pro/osimg/r/ot02/system/lib64/libc.so";
    } else {
      libc = "/apex/com.android.runtime/lib64/bionic/libc.so";
      if (access("/apex/com.android.runtime/lib64/bionic/libc.so", R_OK))
        libc = "/system/lib64/libc.so";
    }
  }
  doHook(libc);
}

// ---------------------------------------------------------------------------
// JNI exports
// ---------------------------------------------------------------------------
extern "C" {

JNIEXPORT jint JNICALL
Java_com_kail_location_inject_utils_LAntiDetect_init(JNIEnv *, jobject) {
  gAuthorized = 1;
  if (!gHooked)
    doInit();
  return 0;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_LAntiDetect_setMocking(JNIEnv *, jobject, jboolean enabled) {
  gMocking = (enabled == JNI_TRUE) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_kail_location_inject_utils_LAntiDetect_isMocking(JNIEnv *, jobject) {
  return (gHooked != 0 && gMocking != 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_LAntiDetect_setAntidetectFileNames(
    JNIEnv *env, jobject, jobjectArray fileNames) {
  char **old = gFileNames;
  if (fileNames) {
    int count = env->GetArrayLength(fileNames);
    gFileNames = (char **)malloc(sizeof(char *) * (count > 0 ? count : 1));
    for (int i = 0; i < count; ++i) {
      jstring s = (jstring)env->GetObjectArrayElement(fileNames, i);
      const char *chars = env->GetStringUTFChars(s, nullptr);
      gFileNames[i] = strdup(chars);
      env->ReleaseStringUTFChars(s, chars);
    }
    gFileNameCount = count;
  } else {
    gFileNameCount = 0;
    gFileNames = (char **)malloc(sizeof(char *));
  }
  free(old);
}

JNIEXPORT jint JNICALL
Java_com_kail_location_inject_utils_LAntiDetect_doHookLib(
    JNIEnv *env, jobject, jobject, jobject, jobject, jstring libraryPath) {
  gAuthorized = 1;
  const char *path = env->GetStringUTFChars(libraryPath, nullptr);
  doHook(path);
  env->ReleaseStringUTFChars(libraryPath, path);
  return 0;
}

}  // extern "C"

// Keep the otherwise-unused helper referenced so -Wall stays clean; it mirrors
// the decompiled generateRandomStr export.
__attribute__((used)) static const void *kKeepRandom = (const void *)generateRandomStr;
