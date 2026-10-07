package com.kail.location.inject.utils;

public class SleepUtils {
    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
