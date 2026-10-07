package com.kail.location.inject.utils;

import android.os.Build;

public class LAntiDetect {
    private static boolean libraryInitialized;

    private static native int doHookLib(int sdkInt, byte[] dexBytes, String processName, String packageName);

    private static native int init(int sdkInt, byte[] dexBytes, String processName, String packageName);

    public static native boolean isMocking();

    public static native void setAntidetectFileNames(String[] fileNames);

    public static native void setMocking(boolean enabled);

    public static int hookLoadedLibrary(byte[] dexBytes, String processName, String packageName) {
        try {
            return doHookLib(Build.VERSION.SDK_INT, dexBytes, processName, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean loadAndInitialize(String packageName, byte[] dexBytes, String processName) {
        String fallbackPath;
        if (libraryInitialized) {
            return true;
        }
        try {
            if (is64BitRuntime()) {
                try {
                    System.load("/data/kail-loc/libantidetect64.so");
                } catch (Exception unused) {
                    fallbackPath = "_/data/kail-loc/libantidetect64.so";
                    System.load(fallbackPath);
                }
            } else {
                try {
                    System.load("/data/kail-loc/libantidetect.so");
                } catch (Exception unused2) {
                    fallbackPath = "_/data/kail-loc/libantidetect.so";
                    System.load(fallbackPath);
                }
            }
            init(Build.VERSION.SDK_INT, dexBytes, processName, packageName);
            libraryInitialized = true;
            return true;
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }

    public static boolean is64BitRuntime() {
        return ("" + Build.CPU_ABI).contains("64");
    }
}
