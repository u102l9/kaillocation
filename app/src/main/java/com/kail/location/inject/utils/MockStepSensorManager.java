package com.kail.location.inject.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import java.text.SimpleDateFormat;

public class MockStepSensorManager {
    private static float baseStepCount = 0.0f;
    private static float stepCountOffset = 0.0f;
    private static float stepSpeed = 1.0f;
    private static boolean mocking = false;
    private static boolean monitorStarted = false;
    private static int lastStepCounterHandle = -1;
    private static int lastStepDetectorHandle = -1;
    private static long mockStartTimeMillis = 0;
    private static long lastStepUpdateTimeMillis = 0;
    private static boolean dataInjectionHookEnabled = false;
    private static boolean sensorFeatureEnabled = false;
    static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    static int updateIntervalMillis = 50;

    static class StepSensorMonitor implements Runnable {
        class SensorListener implements SensorEventListener {
            SensorListener() {
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
            }
        }

        StepSensorMonitor() {
        }

        /**
         * Step-sensor feeder loop.
         *
         * Reconstructed from the FakeLocation behaviour (the original was a
         * non-decompiled stub that threw UnsupportedOperationException — which,
         * running on this background thread inside system_server, is exactly
         * what crashed system_server when step mocking was enabled).
         *
         * The native libStepSensor hook ({@link LStepSensor}) only rewrites /
         * synthesises step events once we hand it concrete sensor values +
         * handles via {@link LStepSensor#setSensorValues}. This loop:
         *   1. resolves the step-counter / step-detector Sensor handles from a
         *      SensorManager (so the native convertToSensorEvent retypes a
         *      generic event to the correct sensor),
         *   2. advances a synthetic step count at the configured cadence
         *      (steps/second == stepSpeed), and
         *   3. feeds STEP_COUNTER (cumulative) + STEP_DETECTOR (per-step pulse)
         *      values to the native hook.
         *
         * Everything is wrapped so the loop can never propagate an exception
         * out of the thread.
         */
        @Override
        public void run() {
            int stepCounterHandle = -1;
            int stepDetectorHandle = -1;
            try {
                int[] handles = MockStepSensorManager.resolveStepSensorHandles();
                stepCounterHandle = handles[0];
                stepDetectorHandle = handles[1];
            } catch (Throwable t) {
                InjectLog.w("MockStepSensor", "resolveStepSensorHandles failed: " + t.getMessage());
            }

            double accumulatedSteps = 0.0;
            long lastTick = System.currentTimeMillis();

            while (true) {
                try {
                    if (!mocking) {
                        Thread.sleep(500);
                        lastTick = System.currentTimeMillis();
                        continue;
                    }

                    long now = System.currentTimeMillis();
                    double dtSec = (now - lastTick) / 1000.0;
                    if (dtSec < 0) dtSec = 0;
                    lastTick = now;

                    // stepSpeed is interpreted as steps-per-second (the UI
                    // cadence in steps/minute is divided by 60 before being
                    // sent to setStepSpeed). Advance the synthetic counter.
                    double sps = stepSpeed;
                    if (sps < 0) sps = 0;
                    accumulatedSteps += sps * dtSec;

                    int wholeSteps = (int) accumulatedSteps;
                    if (wholeSteps > 0) {
                        accumulatedSteps -= wholeSteps;
                        baseStepCount += wholeSteps;

                        long total = (long) (baseStepCount + stepCountOffset);
                        // STEP_COUNTER: cumulative count in values[0].
                        try {
                            LStepSensor.setSensorValues(19, stepCounterHandle,
                                    new float[]{(float) total, 0f, 0f});
                        } catch (Throwable ignored) {
                        }
                        // STEP_DETECTOR: a 1.0 pulse per detected step.
                        try {
                            LStepSensor.setSensorValues(18, stepDetectorHandle,
                                    new float[]{1f, 0f, 0f});
                        } catch (Throwable ignored) {
                        }
                        lastStepUpdateTimeMillis = now;
                    }

                    // Pace the loop near the per-step interval (cadence), but
                    // keep a sane floor/ceiling so a 0 cadence doesn't spin.
                    long sleepMs;
                    if (sps > 0) {
                        sleepMs = (long) (1000.0 / sps);
                        if (sleepMs < updateIntervalMillis) sleepMs = updateIntervalMillis;
                        if (sleepMs > 1000) sleepMs = 1000;
                    } else {
                        sleepMs = 500;
                    }
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable t) {
                    // Never let anything escape this thread — that is what
                    // brought system_server down originally.
                    try { Thread.sleep(500); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        /**
         * Resolve the platform step-counter / step-detector sensor handles via
         * SensorManager. Returns {stepCounterHandle, stepDetectorHandle};
         * either may be -1 if unavailable.
         */
        private int[] resolveStepSensorHandles() {
            return MockStepSensorManager.resolveStepSensorHandles();
        }

        private int sensorHandle(Sensor sensor) {
            return MockStepSensorManager.sensorHandle(sensor);
        }
    }

    public static boolean hook_nativeIsDataInjectionEnabled(long nativePtr) {
        if (!mocking || !dataInjectionHookEnabled) {
            return hook_nativeIsDataInjectionEnabled_bak(nativePtr);
        }
        hook_nativeIsDataInjectionEnabled_bak(nativePtr);
        return true;
    }

    public static boolean hook_nativeIsDataInjectionEnabled_bak(long nativePtr) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hook_nativeIsDataInjectionEnabled_copy(long nativePtr) {
        return false;
    }

    public static long getMockStepCount() {
        return (long) (baseStepCount + stepCountOffset);
    }

    public static float getStepSpeed() {
        return stepSpeed;
    }

    private static void startMonitorThread() {
        InjectLog.i("MockStepSensor", "startMonitorThread: starting step sensor monitor");
        mockStartTimeMillis = System.currentTimeMillis();
        new Thread(new StepSensorMonitor()).start();
        monitorStarted = true;
    }

    private static android.hardware.SensorManager sensorManager() {
        try {
            android.content.Context ctx =
                    com.kail.location.inject.fakelocation.InjectDex.getApplicationContext();
            if (ctx == null) return null;
            return (android.hardware.SensorManager)
                    ctx.getSystemService(android.content.Context.SENSOR_SERVICE);
        } catch (Throwable t) {
            InjectLog.w("MockStepSensor", "sensorManager: " + t.getMessage());
            return null;
        }
    }

    private static int[] resolveStepSensorHandles() {
        int counter = -1;
        int detector = -1;
        try {
            android.hardware.SensorManager sm = sensorManager();
            if (sm == null) return new int[]{counter, detector};
            Sensor sc = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            Sensor sd = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (sc != null) counter = sensorHandle(sc);
            if (sd != null) detector = sensorHandle(sd);
        } catch (Throwable t) {
            InjectLog.w("MockStepSensor", "resolveStepSensorHandles: " + t.getMessage());
        }
        lastStepCounterHandle = counter;
        lastStepDetectorHandle = detector;
        InjectLog.i("MockStepSensor", "resolved step handles counter=" + counter
                + " detector=" + detector);
        return new int[]{counter, detector};
    }

    private static int sensorHandle(Sensor sensor) {
        try {
            java.lang.reflect.Method m = Sensor.class.getDeclaredMethod("getHandle");
            m.setAccessible(true);
            Object h = m.invoke(sensor);
            if (h instanceof Integer) return (Integer) h;
        } catch (Throwable t) {
            InjectLog.w("MockStepSensor", "sensorHandle: " + t.getMessage());
        }
        return -1;
    }

    public static int getStepCounterHandle() {
        return lastStepCounterHandle;
    }

    public static int getStepDetectorHandle() {
        return lastStepDetectorHandle;
    }

    public static boolean isSensorFeatureEnabled() {
        return sensorFeatureEnabled;
    }

    public static boolean isStepSensorMocking() {
        return mocking;
    }

    public static long getMockElapsedMillis() {
        long start = mockStartTimeMillis;
        if (start <= 0) return 0L;
        long elapsed = System.currentTimeMillis() - start;
        return elapsed < 0L ? 0L : elapsed;
    }

    static boolean isSameDay(long timestampMillis) {
        return dayFormat.format(Long.valueOf(System.currentTimeMillis())).equals(dayFormat.format(Long.valueOf(timestampMillis)));
    }

    public static void setSensorFeatureEnabled(boolean enabled) {
        sensorFeatureEnabled = enabled;
    }

    public static void setBaseStepCount(long stepCount) {
        baseStepCount = stepCount;
    }

    public static void setStepCountOffset(long stepCount) {
        stepCountOffset = stepCount;
        baseStepCount = 0.0f;
    }

    public static void setStepSpeed(float speed) {
        mockStartTimeMillis = System.currentTimeMillis();
        stepSpeed = speed;
    }

    public static boolean loadStepSensorHook(byte[] dexBytes, String targetProcessName) {
        return LStepSensor.loadAndHook(dexBytes, targetProcessName);
    }

    public static void startStepSensorMock() {
        startStepSensorMock(0, 0);
    }

    public static void startStepSensorMock(int mode, int scheme) {
        mockStartTimeMillis = System.currentTimeMillis();
        mocking = true;
        lastStepUpdateTimeMillis = mockStartTimeMillis;
        maybeInstallNativeStepHook(mode, scheme);
    }

    private static void maybeInstallNativeStepHook(int mode, int scheme) {
        try {
            long[] off = readSensorOffsets();
            int[] handles = resolveStepSensorHandles();
            float spm = stepSpeed > 0 ? stepSpeed * 60f : 120f;
            NativeStepHook.install(off[0], off[1], spm);
            NativeStepHook.configureStepSensorHandles(handles[0], handles[1]);
            NativeStepHook.start(spm, mode, scheme);
            Log.i("MSU", "global native step hook install requested (spm=" + spm
                    + ", mode=" + mode + ", scheme=" + scheme
                    + ", counterHandle=" + handles[0] + ", detectorHandle=" + handles[1]
                    + ", fallback write=" + off[0] + " convert=" + off[1] + ")");
        } catch (Throwable th) {
            InjectLog.e("MockStepSensor", "global native step hook install failed", th);
        }
    }

    public static void stopStepSensorMock() {
        mocking = false;
        try {
            NativeStepHook.stop();
        } catch (Throwable th) {
            InjectLog.e("MockStepSensor", "global native step hook stop failed", th);
        }
    }

    private static long[] readSensorOffsets() {
        long write = 0L;
        long convert = 0L;
        try {
            java.io.File f = new java.io.File("/data/local/kail-lib/kail_sensor_offsets.txt");
            if (f.exists()) {
                for (String line : new String(readAll(f)).split("\n")) {
                    String t = line.trim();
                    if (t.startsWith("send_objects=")) {
                        write = parseOffset(t.substring("send_objects=".length()).trim());
                    } else if (t.startsWith("convert_to_sensor_event=")) {
                        convert = parseOffset(t.substring("convert_to_sensor_event=".length()).trim());
                    }
                }
            }
        } catch (Throwable t) {
            Log.w("MSU", "readSensorOffsets: " + t.getMessage());
        }
        return new long[]{write, convert};
    }

    private static long parseOffset(String s) {
        try {
            String v = s;
            if (v.startsWith("0x") || v.startsWith("0X")) v = v.substring(2);
            return Long.parseLong(v, 16);
        } catch (Throwable t) {
            try {
                return Long.parseLong(s);
            } catch (Throwable t2) {
                return 0L;
            }
        }
    }

    private static byte[] readAll(java.io.File f) throws Exception {
        try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }

}
