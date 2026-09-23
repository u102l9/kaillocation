#include <jni.h>

#include <android/log.h>
#include <dlfcn.h>
#include <dobby.h>
#include <cstdio>
#include <cstring>
#include <cstdint>
#include <cerrno>
#include <sys/types.h>
#include <cinttypes>
#include <cmath>
#include <atomic>
#include <mutex>
#include <chrono>
#include <unistd.h>

#include "sensor_simulator.h"
#include "kail_log.h"
#include "elf_sym_resolver.h"

#define LOG_TAG "KailNativeSensor"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 统一日志标签（带 [file:line#func] 定位信息，见 kail_log.h）。
static const char *kHookTag = "NativeSensorHook.native";

#define SENSOR_TYPE_ACCELEROMETER 1
#define SENSOR_TYPE_GYROSCOPE 4
#define SENSOR_TYPE_LIGHT 5
#define SENSOR_TYPE_LINEAR_ACCELERATION 10
#define SENSOR_TYPE_STEP_COUNTER 19
#define SENSOR_TYPE_STEP_DETECTOR 18

#define EVENT_SIZE 0x68

typedef void (*SendObjectsFunc)(long*, void*, long, long);
static SendObjectsFunc original_send_objects = nullptr;
static bool send_objects_hook_installed = false;
static std::atomic<bool> route_simulation_active{false};
static uint64_t send_objects_offset = 0;

typedef void (*ConvertToSensorEventFunc)(void* param_1, void* param_2);
static ConvertToSensorEventFunc original_convert_to_sensor_event = nullptr;
static bool convert_to_sensor_event_hook_installed = false;
static uint64_t convert_to_sensor_event_offset = 0;

// Mangled symbol names used for RUNTIME resolution (plan B). Resolving the
// address from the in-memory ELF dynsym makes step simulation work on any ROM
// without an on-device readelf pass or a hardcoded offset. The offsets above
// are only a last-resort fallback when none of these names resolve.
static const char* kSendObjectsSymbols[] = {
    "_ZN7android7BitTube11sendObjectsERKNS_2spIS0_EEPKvmm",
};
static const char* kConvertSymbols[] = {
    // AIDL sensors HAL (Android 12+): convertToSensorEvent(aidl Event, ...)
    "_ZN7android8hardware7sensors14implementation20convertToSensorEventERKN4aidl7android8hardware7sensors5EventEP15sensors_event_t",
    // HIDL V1_0 fallback (older ROMs): convertToSensorEvent(V1_0 Event, ...)
    "_ZN7android8hardware7sensors4V1_014implementation20convertToSensorEventERKNS2_5EventEP15sensors_event_t",
};

static int stepdetectorTrigger = 0;
static int stepcounterTrigger = 0;
static int mSensorHandleStepDetector = -1;
static int mSensorHandleStepCounter = -1;
static std::atomic<int> isMocking{0};
static int isAuthorized = 0;
static std::atomic<int> step_sim_enabled{1};
static std::atomic<float> current_spm{120.0f};
static int step_event_counter = 0;
static std::atomic<uint64_t> step_synth_events{0};
static std::mutex hook_install_mutex;
static std::mutex sensor_simulator_mutex;
static std::mutex step_state_mutex;

// --- Cadence-accurate, time-based step synthesis (convertToSensorEvent path) ---
// The old logic turned EVERY light-sensor (type 5) event into a step event, so
// the emitted step rate tracked the light-sensor event rate instead of the
// configured cadence — that was the source of the large spm-vs-actual error.
// Instead we gate emission on REAL elapsed time: at cadence `current_spm`, one
// step is due every (60000/spm) ms. We use the sensor event's own monotonic
// timestamp (ns) as the clock, accumulate the exact fractional step debt, and
// emit a step-detector pulse + advance a cumulative step-counter only when a
// whole step is actually due.
static int64_t step_last_ts_ns = 0;     // timestamp of last processed trigger event
static double  step_debt = 0.0;         // fractional steps owed (0..1+)
static uint64_t step_count_total = 0;   // cumulative spoofed step counter
static bool    step_have_base = false;  // captured the device's real counter base
static int     pending_step_detector_events = 0;
static int     pending_step_counter_events = 0;
static int     step_emit_phase = 0;     // 0 = counter next, 1 = detector next
static const int64_t kStepMaxGapNs = 5LL * 1000000000LL; // clamp idle gaps to 5s

static std::atomic<uint64_t> g_so_entries{0};
static std::atomic<uint64_t> g_convert_entries{0};
static std::atomic<uint64_t> g_so_calls{0};
static std::atomic<uint64_t> g_so_alloc{0};
static std::atomic<uint64_t> g_so_free{0};
static std::atomic<uint64_t> g_so_live_bytes{0};
static std::atomic<uint64_t> g_so_total_bytes{0};
static std::atomic<uint64_t> g_dobby_hook_calls{0};
static std::atomic<uint64_t> g_native_init_calls{0};
static std::atomic<int64_t>  g_last_diag_ms{0};

#define ALOGI_TO_FILE(...) ALOGI(__VA_ARGS__)
#define ALOGE_TO_FILE(...) ALOGE(__VA_ARGS__)

static void logLeakDiag() {
    long rss_pages = 0;
    FILE* fp = fopen("/proc/self/statm", "re");
    if (fp) {
        long total_pages = 0;
        if (fscanf(fp, "%ld %ld", &total_pages, &rss_pages) != 2) rss_pages = 0;
        fclose(fp);
    }
    long page_kb = (long)(sysconf(_SC_PAGESIZE) / 1024);
    uint64_t alloc = g_so_alloc.load(std::memory_order_relaxed);
    uint64_t freed = g_so_free.load(std::memory_order_relaxed);
    KLOGI(kHookTag,
          "[LEAK] so_entry=%llu conv_entry=%llu calls=%llu alloc=%llu free=%llu live=%lld live_bytes=%llu total_bytes=%llu dobby=%llu init=%llu rss_kb=%ld",
          (unsigned long long)g_so_entries.load(std::memory_order_relaxed),
          (unsigned long long)g_convert_entries.load(std::memory_order_relaxed),
          (unsigned long long)g_so_calls.load(std::memory_order_relaxed),
          (unsigned long long)alloc,
          (unsigned long long)freed,
          (long long)((int64_t)alloc - (int64_t)freed),
          (unsigned long long)g_so_live_bytes.load(std::memory_order_relaxed),
          (unsigned long long)g_so_total_bytes.load(std::memory_order_relaxed),
          (unsigned long long)g_dobby_hook_calls.load(std::memory_order_relaxed),
          (unsigned long long)g_native_init_calls.load(std::memory_order_relaxed),
          (long)(rss_pages * page_kb));
}

static void maybeLogLeakDiag() {
    // 该检查位于传感器事件热路径（每次 send_objects / convert 都进来），
    // 打印本身节流到 60 秒一次，避免持续模拟时 logcat 被 [LEAK] 刷屏。
    int64_t now_ms = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
    int64_t last = g_last_diag_ms.load(std::memory_order_relaxed);
    if (now_ms - last < 60000) return;
    if (!g_last_diag_ms.compare_exchange_strong(last, now_ms, std::memory_order_relaxed)) return;
    logLeakDiag();
}

static bool is_plausible_userspace_ptr(const void* ptr) {
    uintptr_t value = (uintptr_t)ptr;
#if UINTPTR_MAX > 0xffffffffULL
    // Android arm64 may pass TBI/MTE tagged userspace pointers such as
    // 0xb4000073....  Strip the top byte before sanity checks; otherwise valid
    // sensor event buffers get misclassified and the hook swallows real events.
    value &= 0x00ffffffffffffffULL;
#endif
    if (value < 0x100000ULL) return false;
    return true;
}

static void updateSensorSimulatorParams(float spm, int mode, int scheme, bool enable) {
    std::lock_guard<std::mutex> lock(sensor_simulator_mutex);
    gait::SensorSimulator::Get().UpdateParams(spm, mode, scheme, enable);
}

static void processSensorSimulatorEvents(sensors_event_t* events, size_t count) {
    std::lock_guard<std::mutex> lock(sensor_simulator_mutex);
    gait::SensorSimulator::Get().ProcessSensorEvents(events, count);
}

static void initSensorSimulator() {
    std::lock_guard<std::mutex> lock(sensor_simulator_mutex);
    gait::SensorSimulator::Get().Init();
}

static bool reloadSensorSimulatorConfig() {
    std::lock_guard<std::mutex> lock(sensor_simulator_mutex);
    return gait::SensorSimulator::Get().ReloadConfig();
}

void setRouteSimulationActive(bool active) {
    route_simulation_active.store(active, std::memory_order_release);
    if (!active) {
        updateSensorSimulatorParams(120.0f, 0, 0, false);
    }
}

// Reset the cadence-accurate step synthesis state (debt clock + counter base).
// step_count_total is preserved across re-arming within a session so the
// cumulative counter keeps climbing; it is only zeroed on a full reset.
static void resetStepDebtClock(bool zeroCounter) {
    std::lock_guard<std::mutex> lock(step_state_mutex);
    step_last_ts_ns = 0;
    step_debt = 0.0;
    pending_step_detector_events = 0;
    pending_step_counter_events = 0;
    step_emit_phase = 0;
    if (zeroCounter) {
        step_count_total = 0;
        step_have_base = false;
    }
}

static void resetStepHandles() {
    std::lock_guard<std::mutex> lock(step_state_mutex);
    stepdetectorTrigger = 0;
    stepcounterTrigger = 0;
    mSensorHandleStepDetector = -1;
    mSensorHandleStepCounter = -1;
}

static void setStepSensorHandles(int counterHandle, int detectorHandle) {
    std::lock_guard<std::mutex> lock(step_state_mutex);
    if (counterHandle >= 0) {
        mSensorHandleStepCounter = counterHandle;
    }
    if (detectorHandle >= 0) {
        mSensorHandleStepDetector = detectorHandle;
    }
    KLOGI(kHookTag, "step sensor handles: counter=%d detector=%d",
          mSensorHandleStepCounter, mSensorHandleStepDetector);
}

static bool isStepCarrierType(int sensorType) {
    return sensorType == SENSOR_TYPE_ACCELEROMETER ||
           sensorType == SENSOR_TYPE_LINEAR_ACCELERATION ||
           sensorType == SENSOR_TYPE_GYROSCOPE ||
           sensorType == SENSOR_TYPE_LIGHT;
}

static bool synthesizeStepEventFromCarrierLocked(void* eventOut, int carrierType) {
    if (!eventOut) return false;

    if (mSensorHandleStepDetector < 0 && mSensorHandleStepCounter < 0) {
        static int warned = 0;
        if (!warned) {
            warned = 1;
            KLOGW(kHookTag, "step synth skipped: no step sensor handles");
        }
        return false;
    }

    int64_t ts = *(int64_t*)((char*)eventOut + 0x10);
    float spm = current_spm.load(std::memory_order_acquire);
    if (spm < 1.0f) spm = 1.0f;
    if (spm > 400.0f) spm = 400.0f;
    const double sps = (double)spm / 60.0;

    if (step_last_ts_ns == 0) {
        step_last_ts_ns = ts;
    }
    int64_t delta = ts - step_last_ts_ns;
    if (delta < 0) delta = 0;
    if (delta > kStepMaxGapNs) delta = kStepMaxGapNs;
    step_last_ts_ns = ts;

    step_debt += (double)delta * 1e-9 * sps;
    uint64_t due = (uint64_t)std::floor(step_debt);
    if (due > 0) {
        step_debt -= (double)due;
        step_count_total += due;
        if (mSensorHandleStepDetector >= 0) {
            uint64_t detectorPending = (uint64_t)pending_step_detector_events + due;
            pending_step_detector_events = detectorPending > 1000 ? 1000 : (int)detectorPending;
        }
        if (mSensorHandleStepCounter >= 0) {
            // A single cumulative counter event carries the full caught-up total.
            pending_step_counter_events = 1;
        }
    }

    bool canEmitCounter = pending_step_counter_events > 0 && mSensorHandleStepCounter >= 0;
    bool canEmitDetector = pending_step_detector_events > 0 && mSensorHandleStepDetector >= 0;
    if (canEmitCounter && (!canEmitDetector || step_emit_phase == 0)) {
        pending_step_counter_events--;
        *(int*)((char*)eventOut + 0x04) = mSensorHandleStepCounter;
        *(int*)((char*)eventOut + 0x08) = SENSOR_TYPE_STEP_COUNTER;
        *(uint64_t*)((char*)eventOut + 0x18) = step_count_total;
        step_emit_phase = 1;
        uint64_t synth = step_synth_events.fetch_add(1, std::memory_order_relaxed) + 1;
        if (synth <= 5 || (synth % 100ULL) == 0ULL) {
            KLOGI(kHookTag, "step COUNTER emit #%llu carrier=%d handle=%d total=%llu pendingDetector=%d",
                  (unsigned long long)synth, carrierType, mSensorHandleStepCounter,
                  (unsigned long long)step_count_total, pending_step_detector_events);
        }
        return true;
    } else if (canEmitDetector) {
        pending_step_detector_events--;
        *(int*)((char*)eventOut + 0x04) = mSensorHandleStepDetector;
        *(int*)((char*)eventOut + 0x08) = SENSOR_TYPE_STEP_DETECTOR;
        *(float*)((char*)eventOut + 0x18) = 1.0f;
        step_emit_phase = 0;
        uint64_t synth = step_synth_events.fetch_add(1, std::memory_order_relaxed) + 1;
        if (synth <= 5 || (synth % 100ULL) == 0ULL) {
            KLOGI(kHookTag, "step DETECTOR emit #%llu carrier=%d handle=%d total=%llu pendingCounter=%d",
                  (unsigned long long)synth, carrierType, mSensorHandleStepDetector,
                  (unsigned long long)step_count_total, pending_step_counter_events);
        }
        return true;
    }
    return false;
}

extern "C" void hooked_send_objects(long* param_1, void* param_2, long param_3, long param_4) {
    g_so_entries.fetch_add(1, std::memory_order_relaxed);

    if (!route_simulation_active.load(std::memory_order_acquire)) {
        if (original_send_objects) {
            original_send_objects(param_1, param_2, param_3, param_4);
        }
        return;
    }

    if (!param_2) {
        if (original_send_objects) {
            original_send_objects(param_1, param_2, param_3, param_4);
        }
        return;
    }

    int count = (int)param_3;
    
    if (count <= 0 || count > 1000) {
        if (original_send_objects) {
            original_send_objects(param_1, param_2, param_3, param_4);
        }
        return;
    }

    size_t buffer_size = count * EVENT_SIZE;
    
    if (buffer_size > 65536) {
        if (original_send_objects) {
            original_send_objects(param_1, param_2, param_3, param_4);
        }
        return;
    }

    char* heap_buffer = new char[buffer_size];
    g_so_calls.fetch_add(1, std::memory_order_relaxed);
    g_so_alloc.fetch_add(1, std::memory_order_relaxed);
    g_so_live_bytes.fetch_add(buffer_size, std::memory_order_relaxed);
    g_so_total_bytes.fetch_add(buffer_size, std::memory_order_relaxed);
    memcpy(heap_buffer, param_2, buffer_size);

    for (int i = 0; i < count; i++) {
        void* event = heap_buffer + i * EVENT_SIZE;
        uintptr_t addr = (uintptr_t)event;
        if (addr < 0x10000) {
            continue;
        }

        int type = *(int*)((char*)event + 0x08);

        int64_t timestamp = *(int64_t*)((char*)event + 0x10);

        if (type == SENSOR_TYPE_STEP_COUNTER) {
            uint64_t data0 = *(uint64_t*)((char*)event + 0x18);

            sensors_event_t se;
            memset(&se, 0, sizeof(se));
            se.type = type;
            se.timestamp = timestamp;
            se.data[0] = (float)data0;

            processSensorSimulatorEvents(&se, 1);

            *(uint64_t*)((char*)event + 0x18) = (uint64_t)se.data[0];
        } else {
            float data0 = *(float*)((char*)event + 0x18);
            float data1 = *(float*)((char*)event + 0x1C);
            float data2 = *(float*)((char*)event + 0x20);

            sensors_event_t se;
            memset(&se, 0, sizeof(se));
            se.type = type;
            se.timestamp = timestamp;
            se.data[0] = data0;
            se.data[1] = data1;
            se.data[2] = data2;

            processSensorSimulatorEvents(&se, 1);

            *(float*)((char*)event + 0x18) = se.data[0];
            *(float*)((char*)event + 0x1C) = se.data[1];
            *(float*)((char*)event + 0x20) = se.data[2];
        }

        int outType = *(int*)((char*)event + 0x08);
        if ((isMocking.load(std::memory_order_acquire) != 0) &&
            (step_sim_enabled.load(std::memory_order_acquire) != 0) &&
            isStepCarrierType(outType)) {
            std::lock_guard<std::mutex> lock(step_state_mutex);
            synthesizeStepEventFromCarrierLocked(event, outType);
        }
    }

    memcpy(param_2, heap_buffer, buffer_size);
    g_so_free.fetch_add(1, std::memory_order_relaxed);
    g_so_live_bytes.fetch_sub(buffer_size, std::memory_order_relaxed);
    delete[] heap_buffer;
    maybeLogLeakDiag();

    if (!original_send_objects) {
        return;
    }

    original_send_objects(param_1, param_2, param_3, param_4);
}

extern "C" void hooked_convert_to_sensor_event(void* param_1, void* param_2) {
    g_convert_entries.fetch_add(1, std::memory_order_relaxed);
    maybeLogLeakDiag();

    if (!is_plausible_userspace_ptr(param_2)) {
        KLOGW(kHookTag, "[DIAG] convertToSensorEvent invalid out ptr=%p, drop event", param_2);
        return;
    }

    if (!is_plausible_userspace_ptr(param_1)) {
        KLOGW(kHookTag, "[DIAG] convertToSensorEvent invalid in ptr=%p, drop event", param_1);
        return;
    }

    if (!param_2) {
        if (original_convert_to_sensor_event) {
            original_convert_to_sensor_event(param_1, param_2);
        }
        return;
    }

    // Call the original function FIRST so param_2 contains valid
    // sensors_event_t data, THEN read sensor_type from it.
    if (original_convert_to_sensor_event) {
        uintptr_t ptr = (uintptr_t)original_convert_to_sensor_event;
        if (!is_plausible_userspace_ptr((const void*)ptr)) {
            KLOGW(kHookTag, "[DIAG] original_convert_to_sensor_event=%p SUSPICIOUS, skipping call",
                  (void*)ptr);
        } else {
            original_convert_to_sensor_event(param_1, param_2);
        }
    }

    int sensor_type = *(int*)((char*)param_2 + 0x08);

    bool isStepType = sensor_type == SENSOR_TYPE_STEP_DETECTOR ||
                      sensor_type == SENSOR_TYPE_STEP_COUNTER;
    bool isCarrierType = isStepCarrierType(sensor_type);

    if (isStepType || isCarrierType) {
        std::lock_guard<std::mutex> lock(step_state_mutex);
        if (sensor_type == SENSOR_TYPE_STEP_DETECTOR) {
            mSensorHandleStepDetector = *(int*)((char*)param_2 + 0x04);
        } else if (sensor_type == SENSOR_TYPE_STEP_COUNTER) {
            mSensorHandleStepCounter = *(int*)((char*)param_2 + 0x04);
            uint64_t realCounter = *(uint64_t*)((char*)param_2 + 0x18);
            if (!step_have_base || realCounter > step_count_total) {
                step_count_total = realCounter;
                step_have_base = true;
            }
        }

        // Cadence-accurate step synthesis: retype an existing low-risk carrier
        // event into step-detector / step-counter events when whole steps are
        // due. We intentionally avoid the old system_server-side active
        // SensorManager.registerListener carrier path because it was unstable
        // on this ROM.
        if ((isMocking.load(std::memory_order_acquire) != 0) &&
            (step_sim_enabled.load(std::memory_order_acquire) != 0) &&
            isCarrierType) {
            synthesizeStepEventFromCarrierLocked(param_2, sensor_type);
        }
    }
    if (original_convert_to_sensor_event) {
        uintptr_t ptr = (uintptr_t)original_convert_to_sensor_event;
        if (!is_plausible_userspace_ptr((const void*)ptr)) {
            KLOGW(kHookTag, "[DIAG] AFTER step writes: original_convert_to_sensor_event=%p CORRUPTED!",
                  (void*)ptr);
        }
    }
}

static void install_send_objects_hook() {
    std::lock_guard<std::mutex> lock(hook_install_mutex);
    if (send_objects_hook_installed) {
        KLOGI(kHookTag, "install_send_objects_hook: already installed, skip");
        return;
    }
    // Plan B: resolve the live address from the in-memory ELF dynsym first.
    // This works on any ROM without an on-device readelf pass or a hardcoded
    // offset. Falls back to the offset supplied via JNI only if resolution
    // fails (e.g. the symbol is unexpectedly absent from .dynsym).
    void* addr = kailsym::resolve("libsensor.so", kSendObjectsSymbols,
                                  (int)(sizeof(kSendObjectsSymbols) / sizeof(kSendObjectsSymbols[0])),
                                  &send_objects_offset);

    if (!addr) {
        void* base = nullptr;
        FILE* fp = fopen("/proc/self/maps", "r");
        if (!fp) {
            KLOGE(kHookTag, "install_send_objects_hook: cannot open /proc/self/maps");
            return;
        }
        char line[512];
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, "libsensor.so")) {
                unsigned long long start = 0;
                sscanf(line, "%llx-", &start);
                base = (void*)(uintptr_t)start;
                break;
            }
        }
        fclose(fp);

        if (!base) {
            KLOGW(kHookTag, "install_send_objects_hook: libsensor.so not mapped in this process");
            return;
        }
        if (send_objects_offset == 0) {
            KLOGW(kHookTag, "install_send_objects_hook: runtime resolve failed and offset is 0, skip");
            return;
        }
        addr = (void*)((char*)base + send_objects_offset);
        KLOGW(kHookTag, "install_send_objects_hook: using fallback offset 0x%llx",
              (unsigned long long)send_objects_offset);
    }

    g_dobby_hook_calls.fetch_add(1, std::memory_order_relaxed);
    int ret = DobbyHook(addr, (void*)hooked_send_objects, (void**)&original_send_objects);
    
    if (ret == 0) {
        send_objects_hook_installed = true;
        KLOGI(kHookTag, "install_send_objects_hook: hooked libsensor.so send_objects at %p (off=0x%llx)",
              addr, (unsigned long long)send_objects_offset);
    } else {
        KLOGE(kHookTag, "install_send_objects_hook: DobbyHook failed rc=%d at %p", ret, addr);
    }
}

static void install_convert_to_sensor_event_hook() {
    std::lock_guard<std::mutex> lock(hook_install_mutex);
    if (convert_to_sensor_event_hook_installed) {
        KLOGI(kHookTag, "install_convert_to_sensor_event_hook: already installed, skip");
        return;
    }
    void* base = nullptr;

    // Plan B: resolve the live address from libsensorservice.so's dynsym first
    // (works on any ROM, no readelf, no hardcoded 0x5b420). Fall back to the
    // JNI-supplied offset only if every known symbol name fails to resolve.
    void* addr = kailsym::resolve("libsensorservice.so", kConvertSymbols,
                                  (int)(sizeof(kConvertSymbols) / sizeof(kConvertSymbols[0])),
                                  &convert_to_sensor_event_offset);

    if (!addr) {
        FILE* fp = fopen("/proc/self/maps", "r");
        if (!fp) {
            KLOGE(kHookTag, "install_convert_to_sensor_event_hook: cannot open /proc/self/maps");
            return;
        }
        char line[512];
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, "libsensorservice.so")) {
                unsigned long long start = 0;
                sscanf(line, "%llx-", &start);
                base = (void*)(uintptr_t)start;
                break;
            }
        }
        fclose(fp);

        if (!base) {
            KLOGW(kHookTag, "install_convert_to_sensor_event_hook: libsensorservice.so not mapped in this process");
            return;
        }
        if (convert_to_sensor_event_offset == 0) {
            KLOGW(kHookTag, "install_convert_to_sensor_event_hook: runtime resolve failed and offset is 0, skip");
            return;
        }
        addr = (void*)((char*)base + convert_to_sensor_event_offset);
        KLOGW(kHookTag, "install_convert_to_sensor_event_hook: using fallback offset 0x%llx",
              (unsigned long long)convert_to_sensor_event_offset);
    }

    g_dobby_hook_calls.fetch_add(1, std::memory_order_relaxed);
    int ret = DobbyHook(addr, (void*)hooked_convert_to_sensor_event, (void**)&original_convert_to_sensor_event);
    
    if (ret == 0) {
        convert_to_sensor_event_hook_installed = true;
        KLOGI(kHookTag, "install_convert_to_sensor_event_hook: hooked libsensorservice.so at %p (off=0x%llx)",
              addr, (unsigned long long)convert_to_sensor_event_offset);
    } else {
        KLOGE(kHookTag, "install_convert_to_sensor_event_hook: DobbyHook failed rc=%d at %p", ret, addr);
    }
}

extern "C" {

// ============================================================
// Inject-package JNI functions (com.kail.location.inject.utils.NativeStepHook)
//
// These are driven from INSIDE system_server by the FakeLocation inject
// (InjectDex), which loads this .so by absolute path. system_server maps
// libsensorservice.so, so hooked_convert_to_sensor_event installs there and
// synthesises step-detector/counter events into the global sensor stream.
// The class com.kail.location.inject.utils.NativeStepHook lives in the slim
// inject dex, so its JNI names must be bound here.
// ============================================================

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetWriteOffset(
    JNIEnv* env, jclass clazz, jlong offset) {
    send_objects_offset = (uint64_t)offset;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetConvertOffset(
    JNIEnv* env, jclass clazz, jlong offset) {
    convert_to_sensor_event_offset = (uint64_t)offset;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetStepSensorHandles(
    JNIEnv* env, jclass clazz, jint counterHandle, jint detectorHandle) {
    setStepSensorHandles((int)counterHandle, (int)detectorHandle);
}

JNIEXPORT jlong JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeGetStepSynthEvents(
    JNIEnv* env, jclass clazz) {
    return (jlong)step_synth_events.load(std::memory_order_relaxed);
}

JNIEXPORT jint JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeGetHookState(
    JNIEnv* env, jclass clazz) {
    int state = 0;
    if (send_objects_hook_installed) state |= 1;
    if (convert_to_sensor_event_hook_installed) state |= 2;
    return (jint)state;
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetRouteSimulation(
    JNIEnv* env, jclass clazz, jboolean active, jfloat spm, jint mode) {
    bool isActive = (active != JNI_FALSE);
    if (isActive) {
        current_spm.store(spm, std::memory_order_release);
        setRouteSimulationActive(true);
        updateSensorSimulatorParams(spm, mode, 0, true);
        isMocking.store(1, std::memory_order_release);
        step_event_counter = 0;
        resetStepDebtClock(false);
    } else {
        setRouteSimulationActive(false);
        isMocking.store(0, std::memory_order_release);
    }
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetGaitParams(
    JNIEnv* env, jclass clazz, jfloat spm, jint mode, jint scheme, jboolean enable) {
    if (spm > 0.0f) current_spm.store(spm, std::memory_order_release);   // keep the time-based synthesizer in sync
    updateSensorSimulatorParams(spm, mode, scheme, enable);
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetMocking(
    JNIEnv* env, jclass clazz, jint mocking) {
    isMocking.store((int)mocking, std::memory_order_release);
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeSetStepSimEnabled(
    JNIEnv* env, jclass clazz, jboolean enabled) {
    step_sim_enabled.store((enabled != JNI_FALSE) ? 1 : 0, std::memory_order_release);
}

JNIEXPORT void JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeReset(
    JNIEnv* env, jclass clazz) {
    step_sim_enabled.store(0, std::memory_order_release);
    route_simulation_active.store(false, std::memory_order_release);
    isMocking.store(0, std::memory_order_release);
    step_event_counter = 0;
    step_synth_events.store(0, std::memory_order_relaxed);
    resetStepDebtClock(true);
    resetStepHandles();
    current_spm.store(120.0f, std::memory_order_release);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_location_inject_utils_NativeStepHook_nativeInitHook(
    JNIEnv* env, jclass clazz) {
    g_native_init_calls.fetch_add(1, std::memory_order_relaxed);
    initSensorSimulator();
    // Always attempt installation: the install functions resolve the target
    // address at runtime from the in-memory ELF dynsym, so they no longer need
    // a non-zero offset to be supplied up front. The offset globals are only a
    // fallback when runtime resolution fails.
    install_send_objects_hook();
    install_convert_to_sensor_event_hook();
    reloadSensorSimulatorConfig();
    // Report whether at least one hook is installed so Java can log status.
    bool ok = send_objects_hook_installed || convert_to_sensor_event_hook_installed;
    KLOGI(kHookTag, "NativeStepHook.nativeInitHook: sendObjects=%d convert=%d -> ok=%d",
          send_objects_hook_installed, convert_to_sensor_event_hook_installed, ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ============================================================
// Root module JNI functions (com.kail.location.root.NativeSensorHook)
// ============================================================

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetWriteOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    send_objects_offset = (uint64_t)offset;
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetConvertOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    convert_to_sensor_event_offset = (uint64_t)offset;
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetRouteSimulation(
    JNIEnv* env,
    jclass clazz,
    jboolean active,
    jfloat spm,
    jint mode
) {
    bool isActive = (active != JNI_FALSE);
    
    if (isActive) {
        current_spm.store(spm, std::memory_order_release);
        setRouteSimulationActive(true);
        updateSensorSimulatorParams(spm, mode, 0, true);
        isMocking.store(1, std::memory_order_release);
        step_event_counter = 0;
        resetStepDebtClock(false);
    } else {
        setRouteSimulationActive(false);
        isMocking.store(0, std::memory_order_release);
    }
    KLOGI(kHookTag, "nativeSetRouteSimulation: active=%d spm=%.2f mode=%d", isActive, spm, mode);
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    if (spm > 0.0f) current_spm.store(spm, std::memory_order_release);   // keep the time-based synthesizer in sync
    updateSensorSimulatorParams(spm, mode, scheme, enable);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeReloadConfig(
    JNIEnv* env,
    jclass clazz
) {
    return reloadSensorSimulatorConfig() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetMocking(
    JNIEnv* env,
    jclass clazz,
    jint mocking
) {
    isMocking.store((int)mocking, std::memory_order_release);
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetAuthorized(
    JNIEnv* env,
    jclass clazz,
    jint authorized
) {
    isAuthorized = (int)authorized;
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeSetStepSimEnabled(
    JNIEnv* env,
    jclass clazz,
    jboolean enabled
) {
    step_sim_enabled.store((enabled != JNI_FALSE) ? 1 : 0, std::memory_order_release);
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeReset(
    JNIEnv* env,
    jclass clazz
) {
    step_sim_enabled.store(0, std::memory_order_release);
    route_simulation_active.store(false, std::memory_order_release);
    isMocking.store(0, std::memory_order_release);
    step_event_counter = 0;
    step_synth_events.store(0, std::memory_order_relaxed);
    resetStepDebtClock(true);
    resetStepHandles();
    current_spm.store(120.0f, std::memory_order_release);
}

JNIEXPORT void JNICALL
Java_com_kail_location_root_NativeSensorHook_nativeInitHook(
    JNIEnv* env,
    jclass clazz
) {
    g_native_init_calls.fetch_add(1, std::memory_order_relaxed);
    initSensorSimulator();
    
    install_send_objects_hook();
    
    install_convert_to_sensor_event_hook();
    
    reloadSensorSimulatorConfig();
    KLOGI(kHookTag, "root.NativeSensorHook.nativeInitHook: sendObjects=%d convert=%d",
          send_objects_hook_installed, convert_to_sensor_event_hook_installed);
}

// ============================================================
// Xposed module JNI functions (com.kail.locationxposed.xposed.sensor.NativeSensorHook)
// ============================================================

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetWriteOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetWriteOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetConvertOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetConvertOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetRouteSimulation(
    JNIEnv* env,
    jclass clazz,
    jboolean active,
    jfloat spm,
    jint mode
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetRouteSimulation(env, clazz, active, spm, mode);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetGaitParams(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeReloadConfig(
    JNIEnv* env,
    jclass clazz
) {
    return Java_com_kail_location_root_NativeSensorHook_nativeReloadConfig(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetMocking(
    JNIEnv* env,
    jclass clazz,
    jint mocking
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetMocking(env, clazz, mocking);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetAuthorized(
    JNIEnv* env,
    jclass clazz,
    jint authorized
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetAuthorized(env, clazz, authorized);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeSetStepSimEnabled(
    JNIEnv* env,
    jclass clazz,
    jboolean enabled
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetStepSimEnabled(env, clazz, enabled);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeReset(
    JNIEnv* env,
    jclass clazz
) {
    Java_com_kail_location_root_NativeSensorHook_nativeReset(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_sensor_NativeSensorHook_nativeInit(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
    Java_com_kail_location_root_NativeSensorHook_nativeInitHook(env, clazz);
}

// ============================================================
// Main app Xposed package JNI functions (com.kail.location.xposed.sensor.NativeSensorHook)
// ============================================================

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetWriteOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetWriteOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetConvertOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetConvertOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetRouteSimulation(
    JNIEnv* env,
    jclass clazz,
    jboolean active,
    jfloat spm,
    jint mode
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetRouteSimulation(env, clazz, active, spm, mode);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetGaitParams(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeReloadConfig(
    JNIEnv* env,
    jclass clazz
) {
    return Java_com_kail_location_root_NativeSensorHook_nativeReloadConfig(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetMocking(
    JNIEnv* env,
    jclass clazz,
    jint mocking
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetMocking(env, clazz, mocking);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetAuthorized(
    JNIEnv* env,
    jclass clazz,
    jint authorized
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetAuthorized(env, clazz, authorized);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeSetStepSimEnabled(
    JNIEnv* env,
    jclass clazz,
    jboolean enabled
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetStepSimEnabled(env, clazz, enabled);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeReset(
    JNIEnv* env,
    jclass clazz
) {
    Java_com_kail_location_root_NativeSensorHook_nativeReset(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_sensor_NativeSensorHook_nativeInit(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
    Java_com_kail_location_root_NativeSensorHook_nativeInitHook(env, clazz);
}

// ============================================================
// Main app package JNI binding (com.kail.location.xposed.core.FakeLocState)
// ============================================================

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetWriteOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetWriteOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetConvertOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetConvertOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetRouteSimulation(
    JNIEnv* env,
    jclass clazz,
    jboolean active,
    jfloat spm,
    jint mode
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetRouteSimulation(env, clazz, active, spm, mode);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetGaitParams(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeReloadConfig(
    JNIEnv* env,
    jclass clazz
) {
    return Java_com_kail_location_root_NativeSensorHook_nativeReloadConfig(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetMocking(
    JNIEnv* env,
    jclass clazz,
    jint mocking
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetMocking(env, clazz, mocking);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetAuthorized(
    JNIEnv* env,
    jclass clazz,
    jint authorized
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetAuthorized(env, clazz, authorized);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeSetStepSimEnabled(
    JNIEnv* env,
    jclass clazz,
    jboolean enabled
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetStepSimEnabled(env, clazz, enabled);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeInitHook(
    JNIEnv* env,
    jclass clazz
) {
    Java_com_kail_location_root_NativeSensorHook_nativeInitHook(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_location_xposed_core_FakeLocState_nativeInit(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
    Java_com_kail_location_root_NativeSensorHook_nativeInitHook(env, clazz);
}

// ============================================================
// Xposed module JNI binding (com.kail.locationxposed.xposed.core.FakeLocState)
// ============================================================

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetWriteOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetWriteOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetConvertOffset(
    JNIEnv* env,
    jclass clazz,
    jlong offset
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetConvertOffset(env, clazz, offset);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetRouteSimulation(
    JNIEnv* env,
    jclass clazz,
    jboolean active,
    jfloat spm,
    jint mode
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetRouteSimulation(env, clazz, active, spm, mode);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetGaitParams(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
}

JNIEXPORT jboolean JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeReloadConfig(
    JNIEnv* env,
    jclass clazz
) {
    return Java_com_kail_location_root_NativeSensorHook_nativeReloadConfig(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetMocking(
    JNIEnv* env,
    jclass clazz,
    jint mocking
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetMocking(env, clazz, mocking);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetAuthorized(
    JNIEnv* env,
    jclass clazz,
    jint authorized
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetAuthorized(env, clazz, authorized);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeSetStepSimEnabled(
    JNIEnv* env,
    jclass clazz,
    jboolean enabled
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetStepSimEnabled(env, clazz, enabled);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeInitHook(
    JNIEnv* env,
    jclass clazz
) {
    Java_com_kail_location_root_NativeSensorHook_nativeInitHook(env, clazz);
}

JNIEXPORT void JNICALL
Java_com_kail_locationxposed_xposed_core_FakeLocState_nativeInit(
    JNIEnv* env,
    jclass clazz,
    jfloat spm,
    jint mode,
    jint scheme,
    jboolean enable
) {
    Java_com_kail_location_root_NativeSensorHook_nativeSetGaitParams(env, clazz, spm, mode, scheme, enable);
    Java_com_kail_location_root_NativeSensorHook_nativeInitHook(env, clazz);
}

}
