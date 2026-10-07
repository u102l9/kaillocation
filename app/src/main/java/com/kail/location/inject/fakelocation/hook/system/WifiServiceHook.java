package com.kail.location.inject.fakelocation.hook.system;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Parcel;
import android.os.WorkSource;
import android.util.ArrayMap;
import com.kail.location.inject.fakelocation.InjectDex;
import com.kail.location.inject.utils.CallingProcessUtils;
import com.kail.location.inject.utils.MockLocationHookManager;
import com.kail.location.inject.utils.MockWifiConfigManager;
import com.kail.location.inject.utils.PackageSignatureVerifier;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.lib.lhooker.LHooker;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.kail.location.inject.fakelocation.model.MockWifiNetwork;

public class WifiServiceHook {

    /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
    private static Class iWifiManagerStubClass = null;

    /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
    public static boolean scanResultsHooked = false;

    /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
    public static boolean connectionInfoHooked = false;

    /* JADX INFO: renamed from: ށ, reason: contains not printable characters */
    static ClassLoader wifiServiceClassLoader = null;

    /* JADX INFO: renamed from: ނ, reason: contains not printable characters */
    static int transactionGetConnectionInfo = -1;

    /* JADX INFO: renamed from: ރ, reason: contains not printable characters */
    static int transactionGetScanResults = -1;

    /* JADX INFO: renamed from: ބ, reason: contains not printable characters */
    static int transactionStartScan = -1;

    /* JADX INFO: renamed from: ޅ, reason: contains not printable characters */
    static WifiInfo cachedConnectionInfo;

    /* JADX INFO: renamed from: ކ, reason: contains not printable characters */
    static long cachedConnectionInfoTimeMillis;

    /* JADX INFO: renamed from: އ, reason: contains not printable characters */
    static String hookTraceTag;

    /* JADX INFO: renamed from: ވ, reason: contains not printable characters */
    static List<ScanResult> cachedScanResults = new ArrayList();

    /* JADX INFO: renamed from: މ, reason: contains not printable characters */
    static long cachedScanResultsTimeMillis = 0;

    static int checkNull(Object obj) {
        if (obj == null) {
            return -1;
        }
        return ((Integer) obj).intValue();
    }

    public static Class forName(ClassLoader classLoader, String str) {
        Class clsM106 = ReflectionUtils.loadClass(str, true, classLoader);
        if (clsM106 != null) {
            return clsM106;
        }
        try {
            if (wifiServiceClassLoader == null) {
                ClassLoader systemServiceManagerClassLoader = getSystemServiceManagerClassLoader(classLoader, "/apex/com.android.wifi/javalib/service-wifi.jar");
                wifiServiceClassLoader = systemServiceManagerClassLoader;
                log("pathClassLoader: ", systemServiceManagerClassLoader);
            }
            ClassLoader classLoader2 = wifiServiceClassLoader;
            return classLoader2 != null ? ReflectionUtils.loadClass(str, true, classLoader2) : clsM106;
        } catch (Exception e) {
            e.printStackTrace();
            return clsM106;
        }
    }

    public static WifiInfo getConnectionInfo(Object obj) {
        WifiInfo wifiInfoM283;
        WifiInfo wifiInfoM207;
        log("getConnectionInfo: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        if (isHookWifi() && (wifiInfoM207 = MockWifiConfigManager.createWifiInfo(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
            return wifiInfoM207;
        }
        try {
            if (isHookLocation()) {
                try {
                    WifiInfo wifiInfo = (WifiInfo) WifiInfo.class.newInstance();
                    Method declaredMethod = WifiInfo.class.getDeclaredMethod("setMacAddress", String.class);
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(wifiInfo, "02:00:00:00:00:00");
                    Method declaredMethod2 = WifiInfo.class.getDeclaredMethod("setBSSID", String.class);
                    declaredMethod2.setAccessible(true);
                    declaredMethod2.invoke(wifiInfo, "02:00:00:00:00:00");
                    return wifiInfo;
                } catch (Throwable th) {
                    th.printStackTrace();
                    return null;
                }
            }
            if (System.currentTimeMillis() - cachedConnectionInfoTimeMillis < 2000) {
                return cachedConnectionInfo;
            }
            try {
                int i = Build.VERSION.SDK_INT;
                wifiInfoM283 = i >= 31 ? WifiServiceCompat.getConnectionInfoS(obj, null, null) : i >= 29 ? WifiServiceCompat.getConnectionInfoQ(obj, null, null) : getConnectionInfo_bak(obj);
            } catch (Throwable th2) {
                th2.printStackTrace();
                wifiInfoM283 = null;
            }
            log("srcConnectionInfo: " + wifiInfoM283);
            if (wifiInfoM283 == null) {
                try {
                    wifiInfoM283 = (WifiInfo) WifiInfo.class.newInstance();
                } catch (Throwable th3) {
                    th3.printStackTrace();
                    return null;
                }
            }
            cachedConnectionInfo = wifiInfoM283;
            cachedConnectionInfoTimeMillis = System.currentTimeMillis();
            return wifiInfoM283;
        } catch (Throwable th4) {
            th4.printStackTrace();
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
                stringBuffer.toString();
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public static WifiInfo getConnectionInfo2(Object obj, String str) {
        WifiInfo wifiInfoM283;
        WifiInfo wifiInfoM207;
        log("getConnectionInfo2: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid() + ", " + str);
        if (isHookWifi() && (wifiInfoM207 = MockWifiConfigManager.createWifiInfo(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
            return wifiInfoM207;
        }
        if (!isHookLocation()) {
            if (System.currentTimeMillis() - cachedConnectionInfoTimeMillis < 2000) {
                return cachedConnectionInfo;
            }
            try {
                int i = Build.VERSION.SDK_INT;
                wifiInfoM283 = i >= 31 ? WifiServiceCompat.getConnectionInfoS(obj, str, null) : i >= 29 ? WifiServiceCompat.getConnectionInfoQ(obj, str, null) : getConnectionInfo2_bak(obj, str);
            } catch (Throwable th) {
                th.printStackTrace();
                wifiInfoM283 = null;
            }
            log("srcConnectionInfo: " + wifiInfoM283);
            if (wifiInfoM283 == null) {
                try {
                    wifiInfoM283 = (WifiInfo) WifiInfo.class.newInstance();
                } catch (Throwable th2) {
                    th2.printStackTrace();
                    return null;
                }
            }
            cachedConnectionInfo = wifiInfoM283;
            cachedConnectionInfoTimeMillis = System.currentTimeMillis();
            return wifiInfoM283;
        }
        try {
            try {
                WifiInfo wifiInfo = (WifiInfo) WifiInfo.class.newInstance();
                Method declaredMethod = WifiInfo.class.getDeclaredMethod("setMacAddress", String.class);
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(wifiInfo, "02:00:00:00:00:00");
                Method declaredMethod2 = WifiInfo.class.getDeclaredMethod("setBSSID", String.class);
                declaredMethod2.setAccessible(true);
                declaredMethod2.invoke(wifiInfo, "02:00:00:00:00:00");
                return wifiInfo;
            } catch (Throwable th3) {
                th3.printStackTrace();
                return null;
            }
        } catch (Throwable th4) {
            th4.printStackTrace();
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
                stringBuffer.toString();
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public static WifiInfo getConnectionInfo2_bak(Object obj, String str) {
        try {
            log("getConnectionInfo2_bak", obj, str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
            return getConnectionInfo2_copy(obj, str);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return getConnectionInfo2_copy(obj, str);
        }
    }

    public static WifiInfo getConnectionInfo2_copy(Object obj, String str) {
        int i = 0;
        try {
            log("getConnectionInfo2_copy", obj, str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            while (i < 100) {
                i = i + 1 + 1;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                while (i < 100) {
                    i = i + 1 + 1;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
    }

    public static WifiInfo getConnectionInfo_R(Object obj, String str, String str2) {
        WifiInfo wifiInfoM207;
        hookTraceTag = "jit";
        log("getConnectionInfo_R: ", obj, Binder.getCallingPid() + ":" + Binder.getCallingUid() + ", " + str);
        hookTraceTag = "jit1";
        try {
            if (isHookWifi() && (wifiInfoM207 = MockWifiConfigManager.createWifiInfo(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
                return wifiInfoM207;
            }
            if (isHookLocation()) {
                try {
                    return MockWifiConfigManager.createWifiInfo(new MockWifiNetwork("WIFI", "WIFI", "02:00:00:00:00:00", 200, 866, 5745, "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]"));
                } catch (Throwable th) {
                    th.printStackTrace();
                    return getEmptyWifiInfo();
                }
            }
            WifiInfo wifiInfoM283 = null;
            try {
                wifiInfoM283 = Build.VERSION.SDK_INT >= 31 ? WifiServiceCompat.getConnectionInfoS(obj, str, str2) : WifiServiceCompat.getConnectionInfoQ(obj, str, str2);
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
            log("srcConnectionInfo: " + wifiInfoM283);
            if (wifiInfoM283 != null) {
                return wifiInfoM283;
            }
            try {
                return (WifiInfo) WifiInfo.class.newInstance();
            } catch (Throwable th3) {
                th3.printStackTrace();
                return getEmptyWifiInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getEmptyWifiInfo();
        }
    }

    public static WifiInfo getConnectionInfo_R_bak(Object obj, String str, String str2) {
        try {
            log("getConnectionInfo_R_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.toString();
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                StringBuffer stringBuffer6 = new StringBuffer();
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.append("#");
                stringBuffer6.toString();
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
                for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            try {
                StringBuffer stringBuffer7 = new StringBuffer();
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.append("#");
                stringBuffer7.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            hookTraceTag = "jit_bak";
            return getConnectionInfo_R_copy(obj, str, str2);
        } catch (Exception e7) {
            e7.printStackTrace();
            return getConnectionInfo_R_copy(obj, str, str2);
        }
    }

    public static WifiInfo getConnectionInfo_R_copy(Object obj, String str, String str2) {
        int i = 0;
        try {
            log("getConnectionInfo_R_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            while (i < 100) {
                i = i + 1 + 1;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                while (i < 100) {
                    i = i + 1 + 1;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
    }

    public static WifiInfo getConnectionInfo_bak(Object obj) {
        try {
            log("getConnectionInfo_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            return getConnectionInfo_copy(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return getConnectionInfo_copy(obj);
        }
    }

    public static WifiInfo getConnectionInfo_copy(Object obj) {
        int i = 0;
        try {
            log("getConnectionInfo_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            while (i < 100) {
                i = i + 1 + 1;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                while (i < 100) {
                    i = i + 1 + 1;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
    }

    private static WifiInfo getEmptyWifiInfo() {
        try {
            return (WifiInfo) WifiInfo.class.newInstance();
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static List<ScanResult> getScanResults(Object obj, String str) {
        ScanResult scanResultM206;
        log("getScanResults: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        List<ScanResult> arrayList = new ArrayList<>();
        try {
            if (isHookWifi() && (scanResultM206 = MockWifiConfigManager.createScanResult(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
                ArrayList arrayList2 = isHookLocation() ? new ArrayList() : new ArrayList(arrayList);
                arrayList2.add(scanResultM206);
                List<MockWifiNetwork> listM195 = MockWifiConfigManager.getMockWifiNetworks();
                for (int i5 = 1; i5 < listM195.size(); i5++) {
                    ScanResult scanResultM2062 = MockWifiConfigManager.createScanResult(listM195.get(i5));
                    if (scanResultM2062 != null) {
                        arrayList2.add(scanResultM2062);
                    }
                }
                return arrayList2;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (!isHookLocation()) {
            if (System.currentTimeMillis() - cachedScanResultsTimeMillis < 2000) {
                return cachedScanResults;
            }
            try {
                arrayList = Build.VERSION.SDK_INT >= 29 ? WifiServiceCompat.getScanResults(obj, str, null) : getScanResults_bak(obj, str);
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
            if (arrayList == null) {
                arrayList = new ArrayList<>();
            }
            cachedScanResults = arrayList;
            cachedScanResultsTimeMillis = System.currentTimeMillis();
            return arrayList;
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return new ArrayList();
    }

    public static List<ScanResult> getScanResults_R(Object obj, String str, String str2) {
        ScanResult scanResultM206;
        log("getScanResults_R: ", obj, str, Binder.getCallingPid() + ":" + Binder.getCallingUid());
        List<ScanResult> arrayList = new ArrayList<>();
        try {
            if (isHookWifi() && (scanResultM206 = MockWifiConfigManager.createScanResult(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
                ArrayList arrayList2 = isHookLocation() ? new ArrayList() : new ArrayList(arrayList);
                arrayList2.add(scanResultM206);
                List<MockWifiNetwork> listM195 = MockWifiConfigManager.getMockWifiNetworks();
                for (int i = 1; i < listM195.size(); i++) {
                    ScanResult scanResultM2062 = MockWifiConfigManager.createScanResult(listM195.get(i));
                    if (scanResultM2062 != null) {
                        arrayList2.add(scanResultM2062);
                    }
                }
                return arrayList2;
            }
            try {
                if (!isHookLocation()) {
                    try {
                        arrayList = WifiServiceCompat.getScanResults(obj, str, str2);
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    return arrayList == null ? new ArrayList() : arrayList;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList arrayList3 = new ArrayList();
            ScanResult scanResultM2063 = MockWifiConfigManager.createScanResult(new MockWifiNetwork("WIFI", "WIFI", "02:00:00:00:00:00", 200, 866, 5745, "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]"));
            if (scanResultM2063 != null) {
                arrayList3.add(scanResultM2063);
            }
            return arrayList3;
        } catch (Throwable th2) {
            th2.printStackTrace();
            return new ArrayList();
        }
    }

    public static List<ScanResult> getScanResults_R_bak(Object obj, String str, String str2) {
        try {
            log("getScanResults_R_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
            return getScanResults_R_copy(obj, str, str2);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return getScanResults_R_copy(obj, str, str2);
        }
    }

    public static List<ScanResult> getScanResults_R_copy(Object obj, String str, String str2) {
        int i = 0;
        try {
            log("getScanResults_R_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            while (i < 100) {
                i = i + 1 + 1;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                while (i < 100) {
                    i = i + 1 + 1;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
    }

    public static List<ScanResult> getScanResults_bak(Object obj, String str) {
        try {
            log("getScanResults_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
            return getScanResults_copy(obj, str);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return getScanResults_copy(obj, str);
        }
    }

    public static List<ScanResult> getScanResults_copy(Object obj, String str) {
        int i = 0;
        try {
            log("getScanResults_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            while (i < 100) {
                i = i + 1 + 1;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + obj);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + obj);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                while (i < 100) {
                    i = i + 1 + 1;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
    }

    public static final ClassLoader getSystemServiceManagerClassLoader(ClassLoader classLoader, String str) {
        Class clsM104;
        Object service;
        String str2;
        if (Build.VERSION.SDK_INT >= 33) {
            clsM104 = ReflectionUtils.loadClass(classLoader, "com.android.internal.os.SystemServerClassLoaderFactory");
            service = null;
            str2 = "sLoadedPaths";
        } else {
            clsM104 = ReflectionUtils.loadClass(classLoader, "com.android.server.SystemServiceManager");
            // LocalServices is a system_server-internal class not present in
            // the app classpath. Resolve it reflectively so this code compiles
            // in the controller app and still works when loaded into
            // system_server via the inject dex.
            try {
                Class<?> localServicesCls = ReflectionUtils.loadClass(classLoader, "com.android.server.LocalServices");
                java.lang.reflect.Method getService = localServicesCls.getMethod("getService", Class.class);
                service = getService.invoke(null, clsM104);
            } catch (Throwable t) {
                service = null;
            }
            str2 = "mLoadedPaths";
        }
        ArrayMap arrayMap = (ArrayMap) ReflectionUtils.getFieldValue(service, clsM104, str2);
        PathClassLoader pathClassLoader = (PathClassLoader) arrayMap.get(str);
        if (pathClassLoader != null) {
            return pathClassLoader;
        }
        for (Object keyObj : arrayMap.keySet()) {
            String str3 = (String) keyObj;
            log("getSystemServiceManagerClassLoader.key: " + str3);
            if (str3.contains(str)) {
                return (PathClassLoader) arrayMap.get(str3);
            }
        }
        return pathClassLoader;
    }

    private static void getTransactionCode(Class cls) {
        try {
            transactionGetConnectionInfo = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getConnectionInfo"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            transactionGetScanResults = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_getScanResults"));
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            transactionStartScan = checkNull(ReflectionUtils.getFieldValue(null, cls, "TRANSACTION_startScan"));
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public static synchronized void hook(ClassLoader classLoader) {
        if (scanResultsHooked) {
            return;
        }
        try {
            try {
                Class clsForName = forName(classLoader, "android.net.wifi.IWifiManager$Stub");
                iWifiManagerStubClass = clsForName;
                getTransactionCode(clsForName);
                PackageSignatureVerifier.verifyPackageSignature(InjectDex.getApplicationContext(), "com.kail.location", "com.android.server.wifi.WifiServiceImpl");
                int i = Build.VERSION.SDK_INT;
                if (i >= 29) {
                    WifiServiceCompat.init(classLoader);
                }
                if (i >= 30) {
                    LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.wifi.WifiServiceImpl"), "getScanResults", List.class, new Class[]{String.class, String.class}, WifiServiceHook.class, "getScanResults_R", "getScanResults_R_bak");
                } else {
                    LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.wifi.WifiServiceImpl"), "getScanResults", List.class, new Class[]{String.class}, WifiServiceHook.class, "getScanResults", "getScanResults_bak");
                }
                scanResultsHooked = true;
            } catch (RuntimeException unused) {
                MockWifiConfigManager.setMockWifiEnabled(false);
                scanResultsHooked = true;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            scanResultsHooked = false;
        }
        String str = hookTraceTag;
        if (str != null) {
            log("jit", str);
        }
    }

    public static synchronized void hookGetConnectionInfo(ClassLoader classLoader) {
        if (connectionInfoHooked) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.wifi.WifiServiceImpl"), "getConnectionInfo", WifiInfo.class, new Class[]{String.class, String.class}, WifiServiceHook.class, "getConnectionInfo_R", "getConnectionInfo_R_bak");
            } else {
                LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.wifi.WifiServiceImpl"), "getConnectionInfo", WifiInfo.class, null, WifiServiceHook.class, "getConnectionInfo", "getConnectionInfo_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, "com.android.server.wifi.WifiServiceImpl"), "getConnectionInfo", WifiInfo.class, new Class[]{String.class}, WifiServiceHook.class, "getConnectionInfo2", "getConnectionInfo2_bak");
            }
            connectionInfoHooked = true;
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private static boolean isHookLocation() {
        boolean z = MockLocationHookManager.isMocking() && !CallingProcessUtils.isCallingSystemUid() && MockLocationHookManager.isAllowMockPackage(CallingProcessUtils.getPackageNameForPidOrUid(Binder.getCallingPid(), Binder.getCallingUid()));
        log("isHookLocation: " + z, "uid: " + Binder.getCallingUid(), "pid: " + Binder.getCallingPid());
        return z;
    }

    private static boolean isHookWifi() {
        boolean z = MockWifiConfigManager.isMockWifiEnabled() && !CallingProcessUtils.isCallingSystemUid() && MockWifiConfigManager.isPackageAllowedForPidUid(Binder.getCallingPid(), Binder.getCallingUid());
        log("isHookWifi: " + z, "uid: " + Binder.getCallingUid(), "pid: " + Binder.getCallingPid());
        return z;
    }

    public static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("WifiServiceHook", objArr);
    }

    private static WifiInfo mock_getConnectionInfo() {
        WifiInfo wifiInfoM207;
        if (isHookWifi() && (wifiInfoM207 = MockWifiConfigManager.createWifiInfo(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
            return wifiInfoM207;
        }
        if (!isHookLocation()) {
            return null;
        }
        try {
            return MockWifiConfigManager.createWifiInfo(new MockWifiNetwork("WIFI", "WIFI", "02:00:00:00:00:00", 200, 866, 5745, "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]"));
        } catch (Throwable th) {
            th.printStackTrace();
            return getEmptyWifiInfo();
        }
    }

    private static List<ScanResult> mock_getScanResults(String str) {
        ScanResult scanResultM206;
        ArrayList arrayList = new ArrayList();
        try {
            if (isHookWifi() && (scanResultM206 = MockWifiConfigManager.createScanResult(MockWifiConfigManager.getPrimaryMockWifiNetwork())) != null) {
                ArrayList arrayList2 = isHookLocation() ? new ArrayList() : new ArrayList(arrayList);
                arrayList2.add(scanResultM206);
                List<MockWifiNetwork> listM195 = MockWifiConfigManager.getMockWifiNetworks();
                for (int i = 1; i < listM195.size(); i++) {
                    ScanResult scanResultM2062 = MockWifiConfigManager.createScanResult(listM195.get(i));
                    if (scanResultM2062 != null) {
                        arrayList2.add(scanResultM2062);
                    }
                }
                return arrayList2;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (!isHookLocation()) {
            return null;
        }
        ArrayList arrayList3 = new ArrayList();
        ScanResult scanResultM2063 = MockWifiConfigManager.createScanResult(new MockWifiNetwork("WIFI", "WIFI", "02:00:00:00:00:00", 200, 866, 5745, "[WPA-PSK-TKIP+CCMP][WPA2-PSK-TKIP+CCMP][ESS][WPS]"));
        if (scanResultM2063 != null) {
            arrayList3.add(scanResultM2063);
        }
        return arrayList3;
    }

    /* JADX WARN: Removed duplicated region for block: B:50:0x0129 A[LOOP:4: B:49:0x0127->B:50:0x0129, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0130 A[LOOP:5: B:52:0x012e->B:53:0x0130, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:59:0x0158 A[LOOP:6: B:58:0x0156->B:59:0x0158, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:62:0x015f A[LOOP:7: B:61:0x015d->B:62:0x015f, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0187 A[LOOP:8: B:67:0x0185->B:68:0x0187, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:71:0x018e A[LOOP:9: B:70:0x018c->B:71:0x018e, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:77:0x01b6 A[LOOP:10: B:76:0x01b4->B:77:0x01b6, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:80:0x01bd A[LOOP:11: B:79:0x01bb->B:80:0x01bd, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:86:0x01e5 A[LOOP:12: B:85:0x01e3->B:86:0x01e5, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:89:0x01ec A[LOOP:13: B:88:0x01ea->B:89:0x01ec, LOOP_END] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static boolean onTransact(java.lang.Object r10, int r11, android.os.Parcel r12, android.os.Parcel r13, int r14) {
        /*
            Method dump skipped, instruction units count: 501
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kail.location.inject.fakelocation.hook.system.WifiServiceHook.onTransact(java.lang.Object, int, android.os.Parcel, android.os.Parcel, int):boolean");
    }

    public static boolean onTransact_bak(Object obj, int i, Parcel parcel, Parcel parcel2, int i2) {
        if (obj == null || parcel == null || parcel2 == null) {
            return false;
        }
        log("onTransact_bak", obj);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        try {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.append("#");
            stringBuffer6.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        try {
            StringBuffer stringBuffer7 = new StringBuffer();
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.append("#");
            stringBuffer7.toString();
            for (int i15 = 0; i15 < 100; i15 = i15 + 1 + 1) {
            }
            for (int i16 = 0; i16 < 100; i16 = i16 + 1 + 1) {
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        try {
            StringBuffer stringBuffer8 = new StringBuffer();
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.append("#");
            stringBuffer8.toString();
            for (int i17 = 0; i17 < 100; i17 = i17 + 1 + 1) {
            }
            for (int i18 = 0; i18 < 100; i18 = i18 + 1 + 1) {
            }
        } catch (Exception e8) {
            e8.printStackTrace();
        }
        try {
            StringBuffer stringBuffer9 = new StringBuffer();
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.append("#");
            stringBuffer9.toString();
            for (int i19 = 0; i19 < 100; i19 = i19 + 1 + 1) {
            }
            for (int i20 = 0; i20 < 100; i20 = i20 + 1 + 1) {
            }
        } catch (Exception e9) {
            e9.printStackTrace();
        }
        new Random().nextBoolean();
        return onTransact_copy(obj, i, parcel, parcel2, i2);
    }

    public static boolean onTransact_copy(Object obj, int i, Parcel parcel, Parcel parcel2, int i2) {
        try {
            log("onTransact_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return false;
    }

    public static boolean startScan(Object obj, String str) {
        log("startScan: ", str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (isHookLocation()) {
            return true;
        }
        try {
            return startScan_bak(obj, str);
        } catch (Throwable th) {
            th.printStackTrace();
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            return true;
        }
    }

    public static void startScan2(Object obj, Object obj2, WorkSource workSource) {
        log("startScan: ", obj2, workSource);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (isHookLocation()) {
            return;
        }
        try {
            startScan2_bak(obj, obj2, workSource);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
    }

    public static void startScan2_bak(Object obj, Object obj2, WorkSource workSource) {
        try {
            log("startScan2_bak", obj);
            startScan2_copy(obj, obj2, workSource);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public static void startScan2_copy(Object obj, Object obj2, WorkSource workSource) {
        try {
            log("startScan2_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public static void startScan3(Object obj, Object obj2, WorkSource workSource, String str) {
        log("startScan: ", obj2, workSource, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (isHookLocation()) {
            return;
        }
        try {
            startScan3_bak(obj, obj2, workSource, str);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#");
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
    }

    public static void startScan3_bak(Object obj, Object obj2, WorkSource workSource, String str) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            log("startScan3_bak", obj);
            startScan3_copy(obj, obj2, workSource, str);
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public static void startScan3_copy(Object obj, Object obj2, WorkSource workSource, String str) {
        try {
            log("startScan3_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public static boolean startScan_R(Object obj, int i, String str) {
        log("startScan_R: ", str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (isHookLocation()) {
            return true;
        }
        try {
            return startScan_R_bak(obj, i, str);
        } catch (Throwable th) {
            th.printStackTrace();
            try {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.toString();
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
                stringBuffer4.toString();
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
                for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#");
                stringBuffer5.append("#" + obj);
                stringBuffer5.toString();
                for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                }
                for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            return true;
        }
    }

    public static boolean startScan_R_bak(Object obj, int i, String str) {
        try {
            log("startScan_R_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return startScan_R_copy(obj, i, str);
    }

    public static boolean startScan_R_copy(Object obj, int i, String str) {
        try {
            log("startScan_R_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return false;
    }

    public static boolean startScan_bak(Object obj, String str) {
        try {
            log("startScan_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return startScan_copy(obj, str);
    }

    public static boolean startScan_copy(Object obj, String str) {
        try {
            log("startScan_copy", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        try {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#");
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#");
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return false;
    }
}
