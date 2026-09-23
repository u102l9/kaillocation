// libfakeloc_initzygote.cpp
//
// Reconstructed, compilable source for the zygote init entry library.
// Recovered from do/complete/libfakeloc_initzygote.c (arm) and
// libfakeloc_initzygote64.c (arm64); one portable JNI source covers both ABIs.
//
// Flow:
//   doRun(JavaVM**, arg) -> AttachCurrentThread -> init(env)
//   init(env):
//     - verify release signature (allowing -2 "no context yet" in zygote)
//     - build a DexClassLoader over /data/kail-loc/libfakeloc.so with an opt
//       directory of /data/kail-loc/zygote_dex and the system class loader as
//       parent
//     - load com.kail.location.inject.fakelocation.InjectDex
//     - call InjectDex.initZygote(systemClassLoader) reflectively

#include "fakeloc_common.h"

using namespace fakeloc;

static const char *kOptDir = "/data/kail-loc/zygote_dex";
static bool gInitLoaded = false;     // byte_5960 / byte_6E28

// doRun 返回码，与 libfakeloc_apphook.cpp 对齐：0x4b4c1000 成功 / 0x4b4c1001 已加载。
// 注入器 kail_inject 把 x0 当返回值比对（inject64.cpp kDoRunSuccess），
// void 返回时 x0 是随机值会被误判为 "Inject fail"。
static constexpr uint64_t kRunSuccess = 0x4b4c1000;
static constexpr uint64_t kRunAlreadyLoaded = 0x4b4c1001;
static constexpr uint64_t kRunNullVmPtr = 0x4b4c3001;
static constexpr uint64_t kRunNullVm = 0x4b4c3002;
static constexpr uint64_t kRunAttachFailed = 0x4b4c3003;
static constexpr uint64_t kRunInitFailed = 0x4b4c3004;

// ---------------------------------------------------------------------------
// init  (sub_1D34 / sub_23E0)
// ---------------------------------------------------------------------------
static void init(JNIEnv *env) {
  KLOGI(kLogTag, "InitZygote is Executing");

  if (!env) {
    KLOGI(kLogTag, "jni_env is NULL!!");
    return;
  }

  int sig = verifyReleaseSignature(env);
  if (sig != 0 && sig != -2)
    return;

  jstring optDir  = env->NewStringUTF(kOptDir);
  jstring dexPath = env->NewStringUTF(kPayloadPath);

  jclass dclClass = env->FindClass("dalvik/system/DexClassLoader");
  jmethodID dclCtor = env->GetMethodID(
      dclClass, "<init>",
      "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
  jmethodID dclLoad = env->GetMethodID(
      dclClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");

  jobject systemLoader = getSystemClassLoader(env);

  jobject loader = env->NewObject(dclClass, dclCtor, dexPath, optDir, nullptr, systemLoader);

  jstring injectClassName = env->NewStringUTF("com.kail.location.inject.fakelocation.InjectDex");
  jclass injectClass = (jclass)env->CallObjectMethod(loader, dclLoad, injectClassName);

  jmethodID initZygote = env->GetStaticMethodID(
      injectClass, "initZygote", "(Ljava/lang/Object;)[Ljava/lang/Object;");
  env->CallStaticObjectMethod(injectClass, initZygote, systemLoader);

  KLOGI(kLogTag, "InitZygote is finished");

  env->DeleteLocalRef(optDir);
  env->DeleteLocalRef(dexPath);
  env->DeleteLocalRef(dclClass);
  env->DeleteLocalRef(systemLoader);
  env->DeleteLocalRef(injectClassName);
}

// ---------------------------------------------------------------------------
// doRun  (sub_205C / sub_2940) -- exported entry point used by the injector.
//
// 返回 0x4b4c1000 表示成功（与 libfakeloc_apphook / libfakeloc_init 对齐），
// 否则注入器会把 x0 里的随机值误判为失败，打印 "Inject fail"，
// 即使 hook 实际已装上。
// ---------------------------------------------------------------------------
extern "C" __attribute__((visibility("default"))) uint64_t doRun(JavaVM **vmPtr, const char *arg) {
  (void)arg;
  (void)vmPtr;
  // On the ZTE/stock zygote the injector's AndroidRuntime::mJavaVM symbol
  // resolution points at the wrong mapping (the hardcoded /system/lib64
  // path does not match the zygote's /apex/com.android.runtime image), so
  // *vmPtr is garbage. Resolve the VM ourselves via JNI_GetCreatedJavaVMs.
  if (gInitLoaded) {
    KLOGE(kLogTag, "-- Already loaded");
    return kRunAlreadyLoaded;
  }

  JavaVM *vm = getJavaVM();
  if (!vm) {
    KLOGE(kLogTag, "getJavaVM() returned NULL");
    return kRunNullVm;
  }

  JNIEnv *env = nullptr;
  if (vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
    KLOGE(kLogTag, "AttachCurrentThread (main) != JNI_OK");
    return kRunAttachFailed;
  }
  init(env);
  gInitLoaded = true;
  return kRunSuccess;
}
