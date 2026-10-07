package com.kail.location.inject.utils;

public class LStepSensor {
    private static native int doHook(byte[] dexBytes, String targetProcessName);

    public static native boolean isMocking();

    public static native void setMocking(boolean enabled);

    public static native void setSensorValues(int sensorType, int accuracy, float[] values);

    public static boolean loadAndHook(byte[] dexBytes, String targetProcessName) {
        // The kail rebrand authorises via host-package signature verification
        // inside the native doHook (see libStepSensor.cpp), not the original
        // DES license blob — so dexBytes / targetProcessName are unused and may
        // be null. Always load the SO and run doHook; do NOT early-return on
        // null params (that left libStepSensor.so unloaded, so setSensorValues
        // had "No implementation found").
        try {
            System.load("/data/kail-loc/libStepSensor.so");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        try {
            return doHook(dexBytes, targetProcessName) == 0;
        } catch (Throwable th2) {
            th2.printStackTrace();
            return false;
        }
    }
}
