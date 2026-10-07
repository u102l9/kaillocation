// libStepSensor.cpp
//
// Reconstructed, compilable source for the step-sensor spoofing library (arm64).
// Recovered from do/complete/libStepSensor.c.
//
// It hooks libsensorservice.so via the ELF PLT/GOT engine in elf_hooker.h so
// that accelerometer / step-detector / step-counter sensor events delivered to
// apps can be overridden with mock values supplied from Java.
//
// Hooked symbols:
//   android::SensorEventQueue::write(...)                 -> new_SensorEventQueue_write
//   android::hardware::sensors::V1_0::implementation::
//       convertToSensorEvent(Event const&, sensors_event_t*) -> new_convertToSensorEvent
//
// JNI surface (registered by name):
//   LStepSensor.doHook(env, license, key)
//   LStepSensor.setMocking(env, enabled)
//   LStepSensor.isMocking()
//   LStepSensor.setSensorValues(env, type, handle, float[])

#include <cstdint>
#include <cstdlib>
#include <ctime>

#include <jni.h>
#include <android/log.h>

#include "elf_hooker.h"
#include "fakeloc_common.h"
#include "kail_log.h"

using namespace elfhook;

// 本库统一日志标签。
static const char *kStepTag = "StepSensor.native";

// Sensor type constants used by the framework.
static const int SENSOR_TYPE_ACCELEROMETER = 1;
static const int SENSOR_TYPE_STEP_DETECTOR = 18;
static const int SENSOR_TYPE_STEP_COUNTER  = 19;

// ---------------------------------------------------------------------------
// State (decompiled globals 0x1A0xx)
// ---------------------------------------------------------------------------
static int mSensorHandleAccelerometer = -1;
static int mSensorHandleStepDetector  = -1;
static int mSensorHandleStepCounter   = -1;

// Each mock value is a vec3 (x,y,z); -1 in every component means "unset".
static float mAccelerometer[3] = {-1.f, -1.f, -1.f};
static float mStepDetector[3]  = {-1.f, -1.f, -1.f};
static float mStepCounter[3]   = {-1.f, -1.f, -1.f};

static int  gMocking          = 0;   // isMocking
static int  gAuthorized       = 0;   // isAuthorized
static int  gHooked           = 0;   // isHooked
static int  gStepDetectorTrig = 0;   // stepdetectorTrigger
static int  gStepCounterTrig  = 0;   // stepcounterTrigger

// Saved originals of the hooked functions.
//
// IMPORTANT: SensorEventQueue::write is reconstructed as a 3-register function
// (this, events, count) to match the decompiled original exactly. The Hex-Rays
// output `new_SensorEventQueue_write(a1, a2, a3)` treats a2 as the events
// pointer (`a2 + 24` is the first event's data, type read at `a2 + 8`) and a3
// as the element count (`if (... && a3)` guard, `v4 = a3` loop counter). An
// earlier rebuild added a spurious 4th `bitTube` parameter, which shifted
// events into x2 and count into the undefined x3 — the loop then walked a
// garbage pointer/count and crashed SensorService (SIGSEGV @0xc inside
// libStepSensor.so). Keep this at 3 args.
static int64_t (*gOrigSensorEventQueueWrite)(void *, void *, size_t) = nullptr;
static int64_t (*gOrigConvertToSensorEvent)(void *, void *) = nullptr;

// sensors_event_t layout used by the framework (104 bytes per event on arm64):
//   off 0  : int64 version/size header word (treated opaque)
//   off 4  : int32 sensor handle
//   off 8  : int32 sensor type
//   off 24 : float data[...] (sensor payload)
struct SensorEvent {
  uint8_t raw[104];
};

static inline int32_t &eventHandle(uint8_t *e) { return *reinterpret_cast<int32_t *>(e + 4); }
static inline int32_t &eventType(uint8_t *e)   { return *reinterpret_cast<int32_t *>(e + 8); }
static inline float   *eventData(uint8_t *e)   { return reinterpret_cast<float *>(e + 24); }

static bool vecIsSet(const float v[3]) {
  return v[0] != -1.f || v[1] != -1.f || v[2] != -1.f;
}

// ---------------------------------------------------------------------------
// new_SensorEventQueue_write  (sub_3040)
//   Rewrite the payload of each outgoing sensor event with the mock values.
//   3-arg layout: self (x0), events (x1), count (x2) — matches the original.
// ---------------------------------------------------------------------------
static int64_t new_SensorEventQueue_write(void *self, void *events, size_t count) {
  if (gMocking && gAuthorized && events && count) {
    uint8_t *e = reinterpret_cast<uint8_t *>(events);
    for (size_t i = 0; i < count; ++i, e += sizeof(SensorEvent)) {
      int32_t type = eventType(e);
      float *data = eventData(e);
      if (type == SENSOR_TYPE_STEP_COUNTER) {
        if (vecIsSet(mStepCounter))
          data[0] = mStepCounter[0];
      } else if (type == SENSOR_TYPE_STEP_DETECTOR) {
        if (vecIsSet(mStepDetector)) {
          data[0] = mStepDetector[0];
          data[1] = mStepDetector[1];
        }
      } else if (type == SENSOR_TYPE_ACCELEROMETER) {
        if (vecIsSet(mAccelerometer)) {
          data[0] = mAccelerometer[0];
          data[1] = mAccelerometer[1];
          data[2] = mAccelerometer[2];
        }
      }
    }
  }
  if (gOrigSensorEventQueueWrite)
    return gOrigSensorEventQueueWrite(self, events, count);
  KLOGE(kStepTag, "failed to get original SensorEventQueue_write");
  return -1;
}

// ---------------------------------------------------------------------------
// new_convertToSensorEvent  (sub_31B4)
//   When a step-detector/counter trigger is pending, retype the next event so
//   a synthetic step is injected into the stream.
// ---------------------------------------------------------------------------
static int64_t new_convertToSensorEvent(void *eventIn, void *eventOut) {
  int64_t result = gOrigConvertToSensorEvent(eventIn, eventOut);
  if (gMocking && gAuthorized) {
    uint8_t *out = reinterpret_cast<uint8_t *>(eventOut);
    if (eventType(out) == 3 /* generic motion */) {
      if (gStepDetectorTrig == 1 && mSensorHandleStepDetector != -1) {
        gStepDetectorTrig = 0;
        eventHandle(out) = mSensorHandleStepDetector;
        eventType(out)   = SENSOR_TYPE_STEP_DETECTOR;
      } else if (gStepCounterTrig == 1 && mSensorHandleStepCounter != -1) {
        gStepCounterTrig = 0;
        eventHandle(out) = mSensorHandleStepCounter;
        eventType(out)   = SENSOR_TYPE_STEP_COUNTER;
        *reinterpret_cast<int64_t *>(out + 24) = 0;
      }
    }
  }
  return result;
}

// ---------------------------------------------------------------------------
// doHook  (sub_3354)
//   Resolve libsensorservice.so, parse it and install the two PLT hooks.
// ---------------------------------------------------------------------------
static void doHook() {
  if (!gAuthorized)
    return;

  const char *kSensorLib = "/system/lib64/libsensorservice.so";
  void *base = ElfHooker::getModuleBase(kSensorLib);
  const char *name = kSensorLib;
  if (!base) {
    const char *path = ElfHooker::getModulePath("libsensorservice.so");
    if (!path) {
      KLOGE(kStepTag, "libsensorservice.so not found in maps; abort hook");
      return;
    }
    KLOGI(kStepTag, "find module %s", path);
    base = ElfHooker::getModuleBase(path);
    if (!base) {
      KLOGE(kStepTag, "failed to resolve base of %s", path);
      return;
    }
    name = path;
  }

  ElfReader reader(name, base);
  if (reader.parse() != 0) {
    KLOGE(kStepTag, "failed to parse %s in pid %d maps at %p", kSensorLib, getpid(), base);
    return;
  }

  // SensorEventQueue::write has two mangled signatures across versions (m / j).
  int writeRc = reader.hook(
      "_ZN7android16SensorEventQueue5writeERKNS_2spINS_7BitTubeEEEPK12ASensorEventm",
      (void *)new_SensorEventQueue_write, (void **)&gOrigSensorEventQueueWrite);
  if (writeRc != 0) {
    writeRc = reader.hook(
        "_ZN7android16SensorEventQueue5writeERKNS_2spINS_7BitTubeEEEPK12ASensorEventj",
        (void *)new_SensorEventQueue_write, (void **)&gOrigSensorEventQueueWrite);
  }
  int convertRc = reader.hook(
      "_ZN7android8hardware7sensors4V1_014implementation20convertToSensorEventERKNS2_5EventEP15sensors_event_t",
      (void *)new_convertToSensorEvent, (void **)&gOrigConvertToSensorEvent);

  gHooked = 1;
  KLOGI(kStepTag, "doHook finished: SensorEventQueue::write rc=%d, convertToSensorEvent rc=%d",
        writeRc, convertRc);
}

// ---------------------------------------------------------------------------
// JNI exports
// ---------------------------------------------------------------------------
extern "C" {

JNIEXPORT jint JNICALL
Java_com_kail_location_inject_utils_LStepSensor_doHook(JNIEnv *env, jobject, jbyteArray license, jstring key) {
  // Original framework gated this on a DES-encoded license blob keyed by
  // ("Lerist." + key) plus a remote license server. The kail rebrand replaces
  // that with a host-package signature check; same authorization intent,
  // without the third-party server dependency.
  (void)license;
  (void)key;
  gAuthorized = 0;
  if (fakeloc::verifyReleaseSignature(env) != 0) {
    KLOGE(kStepTag, "doHook denied: release signature verification failed");
    return -2;
  }
  gAuthorized = 1;
  KLOGI(kStepTag, "doHook authorized (alreadyHooked=%d)", gHooked);
  if (!gHooked)
    doHook();
  return 0;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_LStepSensor_setMocking(JNIEnv *, jobject, jboolean enabled) {
  gMocking = (gAuthorized != 0 && enabled == JNI_TRUE) ? 1 : 0;
  KLOGI(kStepTag, "setMocking enabled=%d -> gMocking=%d (authorized=%d)",
        enabled == JNI_TRUE, gMocking, gAuthorized);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_location_inject_utils_LStepSensor_isMocking(JNIEnv *, jobject) {
  return (gHooked != 0 && gMocking != 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_LStepSensor_setSensorValues(
    JNIEnv *env, jobject, jint type, jint handle, jfloatArray values) {
  jfloat *vals = env->GetFloatArrayElements(values, nullptr);
  switch (type) {
    case SENSOR_TYPE_STEP_COUNTER:
      mSensorHandleStepCounter = handle;
      mStepCounter[0] = vals[0];
      mStepCounter[1] = vals[1];
      mStepCounter[2] = vals[2];
      gStepCounterTrig = 1;
      break;
    case SENSOR_TYPE_STEP_DETECTOR:
      mSensorHandleStepDetector = handle;
      mStepDetector[0] = vals[0];
      mStepDetector[1] = vals[1];
      mStepDetector[2] = vals[2];
      gStepDetectorTrig = 1;
      break;
    case SENSOR_TYPE_ACCELEROMETER:
      mSensorHandleAccelerometer = handle;
      mAccelerometer[0] = vals[0];
      mAccelerometer[1] = vals[1];
      mAccelerometer[2] = vals[2];
      break;
    default:
      break;
  }
  env->ReleaseFloatArrayElements(values, vals, 0);
  KLOGV(kStepTag, "setSensorValues type=%d handle=%d", type, handle);
}

}  // extern "C"
