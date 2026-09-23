// libfakeloc_apphook.cpp
//
// Reconstructed, compilable source for the app-process hook entry library.
// Recovered from do/complete/libfakeloc_apphook.c (arm) and
// libfakeloc_apphook64.c (arm64); both ABIs are reconstructed from this single
// portable JNI source.
//
// Flow:
//   doRun(JavaVM**, arg)  -- called by the ptrace injector inside the target
//     -> AttachCurrentThread, then init(env)
//   init(env):
//     - verify payload MD5 and release signature
//     - build a DexClassLoader over /data/kail-loc/libfakeloc.so
//     - load com.kail.location.inject.fakelocation.InjectDex
//     - call InjectDex.hookApplication(context) reflectively

#include "fakeloc_common.h"

using namespace fakeloc;

static bool gAppHookLoaded = false;     // byte_5D48 / byte_74A8

// doRun 返回码，与 libfakeloc_init.cpp 对齐：0x4b4c1000 成功 / 0x4b4c1001 已加载。
static constexpr uint64_t kRunSuccess = 0x4b4c1000;
static constexpr uint64_t kRunAlreadyLoaded = 0x4b4c1001;
static constexpr uint64_t kRunNullVmPtr = 0x4b4c3001;
static constexpr uint64_t kRunNullVm = 0x4b4c3002;
static constexpr uint64_t kRunAttachFailed = 0x4b4c3003;
static constexpr uint64_t kRunInitFailed = 0x4b4c3004;

// ---------------------------------------------------------------------------
// init  (sub_2030 / sub_289C)
// ---------------------------------------------------------------------------
static bool init(JNIEnv *env) {
  KLOGI(kLogTag, "AppHook is Executing");

  if (verifyApkMd5() != 0)
    return false;
  if (!env) {
    KLOGI(kLogTag, "jni_env is NULL!!");
    return false;
  }
  if (verifyReleaseSignature(env) != 0)
    return false;

  jobject context = getGlobalContext(env);

  jclass ctxClass = env->FindClass("android/content/Context");
  jmethodID getPkgName = env->GetMethodID(ctxClass, "getPackageName", "()Ljava/lang/String;");
  jstring pkgName = (jstring)env->CallObjectMethod(context, getPkgName);

  const char *pkgChars = env->GetStringUTFChars(pkgName, nullptr);
  jstring dataDir = concatString(env, "/data/data/", pkgChars);
  env->ReleaseStringUTFChars(pkgName, pkgChars);

  jstring dexPath  = env->NewStringUTF(kPayloadPath);
  jstring optDir   = dataDir;

  jclass dclClass = env->FindClass("dalvik/system/DexClassLoader");
  jmethodID dclCtor = env->GetMethodID(
      dclClass, "<init>",
      "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
  jmethodID dclLoad = env->GetMethodID(
      dclClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");

  jmethodID getCl = env->GetMethodID(ctxClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
  jobject parentLoader = env->CallObjectMethod(context, getCl);

  jobject loader = env->NewObject(dclClass, dclCtor, dexPath, optDir, nullptr, parentLoader);

  jstring injectClassName = env->NewStringUTF("com.kail.location.inject.fakelocation.InjectDex");
  jclass injectClass = (jclass)env->CallObjectMethod(loader, dclLoad, injectClassName);

  jmethodID hookApp = env->GetStaticMethodID(
      injectClass, "hookApplication", "(Ljava/lang/Object;)[Ljava/lang/Object;");
  // hookApplication is reflectively invoked on a freshly default-constructed
  // InjectDex instance; the original called it as a static-style helper.
  env->CallStaticObjectMethod(injectClass, hookApp, context);

  KLOGI(kLogTag, "AppHook is finished");

  env->DeleteLocalRef(context);
  env->DeleteLocalRef(ctxClass);
  env->DeleteLocalRef(pkgName);
  env->DeleteLocalRef(dexPath);
  env->DeleteLocalRef(optDir);
  env->DeleteLocalRef(dclClass);
  env->DeleteLocalRef(parentLoader);
  env->DeleteLocalRef(injectClassName);
  return true;
}

// ---------------------------------------------------------------------------
// doRun  (sub_23D4 / sub_2EA0) -- exported entry point used by the injector.
//
// 返回 0x4b4c1000 表示成功，与 libfakeloc_init（system_server 载荷）对齐：
// 否则注入器会把这个返回值（原来 doRun 是 void，x0 里是随机值）误判为失败，
// 打印 "Inject fail"，即使 hook 实际已装上。
// ---------------------------------------------------------------------------
extern "C" __attribute__((visibility("default"))) uint64_t doRun(JavaVM **vmPtr, const char *arg) {
  (void)arg;
  if (gAppHookLoaded) {
    KLOGE(kLogTag, "-- Already loaded");
    return kRunAlreadyLoaded;
  }
  gAppHookLoaded = true;

  if (!vmPtr) {
    KLOGE(kLogTag, "JavaVM** == NULL");
    gAppHookLoaded = false;
    return kRunNullVmPtr;
  }
  JavaVM *vm = *vmPtr;
  if (!vm) {
    KLOGE(kLogTag, "JavaVM* == NULL");
    gAppHookLoaded = false;
    return kRunNullVm;
  }

  JNIEnv *env = nullptr;
  if (vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
    KLOGE(kLogTag, "AttachCurrentThread (main) != JNI_OK");
    gAppHookLoaded = false;
    return kRunAttachFailed;
  }
  bool ok = init(env);
  if (!ok) {
    KLOGE(kLogTag, "AppHook init failed");
    gAppHookLoaded = false;
    return kRunInitFailed;
  }
  return kRunSuccess;
}
