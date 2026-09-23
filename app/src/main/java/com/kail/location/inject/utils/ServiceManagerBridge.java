package com.kail.location.inject.utils;

import android.os.IBinder;

public class ServiceManagerBridge {
    public static boolean addService(ClassLoader classLoader, String serviceName, IBinder serviceBinder) {
        try {
            ReflectionUtils.invokeMethod(null, ReflectionUtils.loadClass("android.os.ServiceManager", true, classLoader), "addService", new Class[]{String.class, IBinder.class}, new Object[]{serviceName, serviceBinder});
            return true;
        } catch (Throwable th) {
            Throwable cause = th.getCause() != null ? th.getCause() : th;
            InjectLog.e("ServiceManagerBridge", "addService failed: " + serviceName + " cause=" + cause);
            return false;
        }
    }

    public static IBinder getService(ClassLoader classLoader, String serviceName) {
        try {
            return (IBinder) ReflectionUtils.invokeMethod(null, ReflectionUtils.loadClass("android.os.ServiceManager", true, classLoader), "getService", new Class[]{String.class}, new Object[]{serviceName});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
