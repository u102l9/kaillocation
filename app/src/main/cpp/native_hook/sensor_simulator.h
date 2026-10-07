#pragma once

#include <cstdint>
#include <atomic>
#include <string>

#ifndef TYPE_ACCELEROMETER
#define TYPE_ACCELEROMETER 1
#endif

#ifndef TYPE_GYROSCOPE
#define TYPE_GYROSCOPE 4
#endif

#ifndef TYPE_LINEAR_ACCELERATION
#define TYPE_LINEAR_ACCELERATION 10
#endif

#ifndef TYPE_STEP_DETECTOR
#define TYPE_STEP_DETECTOR 18
#endif

#ifndef TYPE_STEP_COUNTER
#define TYPE_STEP_COUNTER 19
#endif

typedef struct sensors_event_t {
    int version;
    int sensor;
    int type;
    int64_t timestamp;
    float data[16];
} sensors_event_t;

namespace gait {

enum class GaitMode : int {
    Walk = 0,
    Run = 1,
    FastRun = 2
};

enum class SimScheme : int {
    Fourier = 0,
    SineNoise = 1
};

struct GaitConfig {
    float steps_per_minute = 120.0f;
    GaitMode mode = GaitMode::Walk;
    SimScheme scheme = SimScheme::Fourier;
    bool enable = true;
};

class SensorSimulator {
public:
    static SensorSimulator& Get();
    
    void Init();
    
    void UpdateParams(float spm, int mode, int scheme, bool enable);
    
    void ProcessSensorEvents(sensors_event_t* events, size_t count);
    
    void ProcessSensorEvent(sensors_event_t& event);
    
    bool ReloadConfig();
    
    GaitConfig GetConfig() const;
    
    bool isInitialized() const { return initialized_.load(); }

private:
    SensorSimulator() = default;
    ~SensorSimulator() = default;
    SensorSimulator(const SensorSimulator&) = delete;
    SensorSimulator& operator=(const SensorSimulator&) = delete;

    void EnsureInitialized(int64_t ts_ns);
    void SmoothStepRate(double dt);
    void AdvancePhase(double dt);

    // Exact step total at the given event timestamp (segment-integrated).
    double StepsAt(int64_t ts_ns);
    // Re-anchor the segment when cadence changes so counting stays continuous.
    void RebaseStepSegment(int64_t ts_ns, float new_spm);

    void ApplyAccelerometer(sensors_event_t& e, double dt);
    void ApplyAccelerometerSine(sensors_event_t& e, double dt);
    void ApplyLinearAcceleration(sensors_event_t& e, double dt);
    void ApplyLinearAccelerationSine(sensors_event_t& e, double dt);
    void ApplyGyroscope(sensors_event_t& e, double dt);
    void ApplyGyroscopeSine(sensors_event_t& e, double dt);
    void ApplyStepCounter(sensors_event_t& e, double dt);
    void ApplyStepDetector(sensors_event_t& e, double dt);

    double NextSignedNoise(double amplitude);

    static float ModeDefaultSpm(GaitMode m);

    GaitConfig config_;
    int64_t last_ts_ns_ = 0;
    double phase_ = 0.0;
    double step_counter_ = 0.0;
    double step_phase_acc_ = 0.0;
    float current_spm_ = 120.0f;
    float target_spm_ = 120.0f;
    uint64_t rng_state_ = 0x243F6A8885A308D3ULL;
    int64_t last_config_poll_ns_ = 0;
    std::atomic<bool> initialized_{false};

    // --- Time-based step counting (exact, immune to sensor-event sparsity) ---
    // The step counter is computed as base + elapsed_time * cadence, using each
    // event's own (real, monotonic) timestamp as "now". This removes the
    // dependency on how frequently sensor events flow and the old dt clamp that
    // caused large undercounts. Cadence changes are integrated piecewise: on a
    // change we snapshot the steps-so-far into step_base_ and restart the
    // segment clock.
    int64_t step_start_ns_ = 0;     // segment start timestamp
    double  step_base_ = 0.0;       // cumulative steps before current segment (+ real hw base)
    float   seg_spm_ = 120.0f;      // cadence in effect for the current segment
    bool    step_base_captured_ = false; // captured the real hw counter base once
    long    detector_emitted_ = 0;  // whole steps already surfaced as detector events

    static constexpr double kTwoPi = 6.283185307179586476925286766559;
    static constexpr double kNsToSec = 1e-9;
    static constexpr double kMaxDeltaSec = 0.050;
    static constexpr int64_t kReloadIntervalNs = 1000000000LL;
    static constexpr const char* kConfigPath = "/data/local/tmp/step_config";
};

}  // namespace gait
