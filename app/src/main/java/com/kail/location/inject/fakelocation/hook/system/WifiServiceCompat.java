package com.kail.location.inject.fakelocation.hook.system;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import com.kail.location.inject.utils.ReflectionUtils;
import java.util.List;

public class WifiServiceCompat {
    private static Object clientModeImpl;
    private static Class wifiServiceImplClass;
    private static Class clientModeImplClass;
    private static Class clientModeClass;
    private static Class scanRequestProxyClass;
    private static Class wifiThreadRunnerClass;

    private static void enforceAccessPermission(Object service) throws Exception {
        ReflectionUtils.invokeMethod(service, wifiServiceImplClass, "enforceAccessPermission", null, null);
    }

    private static Class loadWifiClass(ClassLoader classLoader, String className) {
        return WifiServiceHook.forName(classLoader, className);
    }

    public static WifiInfo getConnectionInfoQ(Object service, String callingPackage, String attributionTag) throws Exception {
        enforceAccessPermission(service);
        long identity = Binder.clearCallingIdentity();
        try {
            return syncRequestConnectionInfoFromClientModeImpl(service);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static WifiInfo getConnectionInfoS(Object service, String callingPackage, String attributionTag) throws Exception {
        enforceAccessPermission(service);
        int callingUid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            return syncRequestConnectionInfoFromClientMode(service, callingUid, callingPackage);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static List<ScanResult> getScanResults(Object service, String callingPackage, String attributionTag) throws Exception {
        enforceAccessPermission(service);
        long identity = Binder.clearCallingIdentity();
        try {
            Object scanRequestProxy = ReflectionUtils.getFieldValue(service, wifiServiceImplClass, "mScanRequestProxy");
            return (List) ReflectionUtils.invokeMethod(scanRequestProxy, scanRequestProxyClass, "getScanResults", null, null);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static void init(ClassLoader classLoader) {
        wifiServiceImplClass = loadWifiClass(classLoader, "com.android.server.wifi.WifiServiceImpl");
        clientModeImplClass = loadWifiClass(classLoader, "com.android.server.wifi.ClientModeImpl");
        clientModeClass = loadWifiClass(classLoader, "com.android.server.wifi.ClientMode");
        scanRequestProxyClass = loadWifiClass(classLoader, "com.android.server.wifi.ScanRequestProxy");
        wifiThreadRunnerClass = loadWifiClass(classLoader, "com.android.server.wifi.WifiThreadRunner");
    }

    private static Object getClientModeImpl(Object service) {
        if (clientModeImpl == null) {
            clientModeImpl = ReflectionUtils.getFieldValue(service, wifiServiceImplClass, "mClientModeImpl");
        }
        return clientModeImpl;
    }

    private static WifiInfo syncRequestConnectionInfoFromClientModeImpl(Object service) throws Exception {
        return (WifiInfo) ReflectionUtils.invokeMethod(getClientModeImpl(service), clientModeImplClass, "syncRequestConnectionInfo", null, null);
    }

    private static WifiInfo syncRequestConnectionInfoFromClientMode(Object service, int callingUid, String callingPackage) throws Exception {
        Object clientMode = ReflectionUtils.invokeMethod(service, wifiServiceImplClass, "getClientModeManagerIfSecondaryCmmRequestedByCallerPresent", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(callingUid), callingPackage});
        return (WifiInfo) ReflectionUtils.invokeMethod(clientMode, clientModeClass, "syncRequestConnectionInfo", null, null);
    }
}
