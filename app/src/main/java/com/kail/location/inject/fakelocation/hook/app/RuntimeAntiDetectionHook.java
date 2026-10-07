package com.kail.location.inject.fakelocation.hook.app;

import android.os.Build;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.inject.utils.AntiDetectionServiceManager;
import com.kail.location.inject.utils.HideRootServiceManager;
import com.kail.location.inject.utils.LAntiDetect;
import com.kail.location.lib.lhooker.LHooker;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ؠ, reason: contains not printable characters */
/* JADX INFO: loaded from: /home/kail/code/tool/jadx-1.5.5/bin/classes.dex */
public class RuntimeAntiDetectionHook {

    /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
    private static ClassLoader nativeLoadClassLoader;

    /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
    private static String nativeLibraryDir;

    /* JADX INFO: renamed from: ރ, reason: contains not printable characters */
    private static String nativeLibraryName;

    /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
    public static final String fileClassName = File.class.getName();

    /* JADX INFO: renamed from: ށ, reason: contains not printable characters */
    static int xposedHelpersLoadState = -1;

    /* JADX INFO: renamed from: ނ, reason: contains not printable characters */
    static int xposedBridgeLoadState = -1;

    /* JADX INFO: renamed from: ބ, reason: contains not printable characters */
    static HashMap<String, String> safeSystemProperties = new SafeSystemPropertiesMap();

    /* JADX INFO: renamed from: ޅ, reason: contains not printable characters */
    static Object nativeLoadLock = new Object();

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ށ.ؠ$֏, reason: contains not printable characters */
    static class SafeSystemPropertiesMap extends HashMap<String, String> {
        SafeSystemPropertiesMap() {
            put("ro.boot.vbmeta.device_state", "locked");
            put("ro.boot.verifiedbootstate", "green");
            put("ro.boot.flash.locked", "1");
            put("ro.boot.veritymode", "enforcing");
            put("ro.boot.warranty_bit", "0");
            put("ro.warranty_bit", "0");
            put("ro.debuggable", "0");
            put("ro.secure", "1");
            put("ro.build.type", "user");
            put("ro.build.tags", "release-keys");
            put("ro.build.selinux", "0");
            put("ro.build.system_root_image", "false");
        }
    }

    private static boolean isCurrentPackageIn(List<String> packages) {
        String packageName = AppProcessHook.currentPackageName;
        return packageName != null && packages != null && !packages.isEmpty() && packages.contains(packageName);
    }

    private static boolean shouldSpoofSystemProperties() {
        try {
            HideRootServiceManager hide = HideRootServiceManager.getInstance();
            if (hide.isHideRootEnabled() && isCurrentPackageIn(hide.getHiddenPackages())) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        try {
            AntiDetectionServiceManager anti = AntiDetectionServiceManager.getInstance();
            if (anti.isAntiDetectionEnabled()
                    && anti.isHookRulesEnabled()
                    && isCurrentPackageIn(anti.getHookTargetPackages())) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static String StackTraceElement_getClassName(Object obj) {
        try {
            String strStackTraceElement_getClassName_bak = StackTraceElement_getClassName_bak(obj);
            if (!AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() || !AntiDetectionServiceManager.getInstance().isFileNameHidingEnabled()) {
                return strStackTraceElement_getClassName_bak;
            }
            if ("de.robv.android.xposed.XposedBridge".equals(strStackTraceElement_getClassName_bak)) {
                strStackTraceElement_getClassName_bak = "com.miui.securitycenter.remote";
            }
            return "de.robv.android.xposed.XposedHelpers".equals(strStackTraceElement_getClassName_bak) ? "com.miui.securitycenter.remote" : strStackTraceElement_getClassName_bak;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String StackTraceElement_getClassName_bak(Object obj) {
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
        return StackTraceElement_getClassName_copy(obj);
    }

    public static String StackTraceElement_getClassName_copy(Object obj) {
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
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doLoad(Object obj, String str, ClassLoader classLoader) {
        log("doLoad", str, classLoader);
        try {
            String strDoLoad_bak = doLoad_bak(obj, str, classLoader);
            if (strDoLoad_bak == null) {
                try {
                    LAntiDetect.hookLoadedLibrary(null, null, str);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
            log("doLoad.err", strDoLoad_bak);
            return strDoLoad_bak;
        } catch (Throwable th2) {
            th2.printStackTrace();
            return "";
        }
    }

    public static String doLoad_bak(Object obj, String str, ClassLoader classLoader) {
        log("doLoad_bak", str, classLoader);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doLoad_copy(obj, str, classLoader);
    }

    public static String doLoad_copy(Object obj, String str, ClassLoader classLoader) {
        log("doLoad_copy", str, classLoader);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean exists(Object obj) {
        String absolutePath;
        List<String> listM138;
        log("exists", obj);
        try {
            boolean zExists_bak = exists_bak(obj);
            if (zExists_bak && (absolutePath = ((File) obj).getAbsolutePath()) != null && AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && (listM138 = AntiDetectionServiceManager.getInstance().getHiddenFileNames()) != null && !listM138.isEmpty()) {
                for (String str : listM138) {
                    log("exists.contains:", Boolean.valueOf(absolutePath.contains(str)), absolutePath);
                    if (absolutePath.contains(str)) {
                        log("exists.正在反检测: " + absolutePath);
                        return false;
                    }
                }
            }
            return zExists_bak;
        } catch (Throwable th) {
            th.printStackTrace();
            try {
                log("exists_bak", obj);
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
                log("exists_bak", obj);
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
            return false;
        }
    }

    public static boolean exists_bak(Object obj) {
        try {
            log("exists_bak", obj);
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
        return exists_copy(obj);
    }

    public static boolean exists_copy(Object obj) {
        try {
            log("exists_copy", obj);
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
        return false;
    }

    public static String get(String str) {
        log("get", str);
        if (shouldSpoofSystemProperties() && safeSystemProperties.containsKey(str)) {
            return safeSystemProperties.get(str);
        }
        try {
            return get_bak(str);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log("get", str);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + str);
                stringBuffer.toString();
                for (int i = 0; i < 100; i = i + 1 + 1) {
                }
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + str);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + str);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
                return "";
            } catch (Exception e4) {
                e4.printStackTrace();
                return "";
            }
        }
    }

    public static String get2(String str, String str2) {
        log("get2", str);
        if (shouldSpoofSystemProperties() && safeSystemProperties.containsKey(str)) {
            return safeSystemProperties.get(str);
        }
        try {
            return get2_bak(str, str2);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log("get", str);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + str);
                stringBuffer.toString();
                for (int i = 0; i < 100; i = i + 1 + 1) {
                }
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + str);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + str);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + str);
                stringBuffer4.toString();
                for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
                }
                for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            return str2;
        }
    }

    public static String get2_bak(String str, String str2) {
        try {
            log("get2_bak", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return get2_copy(str, str2);
    }

    public static String get2_copy(String str, String str2) {
        try {
            log("get2_copy", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str2;
    }

    public static boolean getBoolean(String str, boolean z) {
        log("getBoolean", str);
        try {
            return shouldSpoofSystemProperties() && safeSystemProperties.containsKey(str) ? Boolean.parseBoolean(safeSystemProperties.get(str)) : getBoolean_bak(str, z);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log("get", str);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + str);
                stringBuffer.toString();
                for (int i = 0; i < 100; i = i + 1 + 1) {
                }
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + str);
                stringBuffer2.toString();
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#");
                stringBuffer3.append("#" + str);
                stringBuffer3.toString();
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
                for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            return z;
        }
    }

    public static boolean getBoolean_bak(String str, boolean z) {
        try {
            log("getBoolean_bak", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBoolean_copy(str, z);
    }

    public static boolean getBoolean_copy(String str, boolean z) {
        try {
            log("getBoolean_copy", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return z;
    }

    public static int getInt(String str, int i) {
        log("getInt", str);
        try {
            return shouldSpoofSystemProperties() && safeSystemProperties.containsKey(str) ? Integer.parseInt(safeSystemProperties.get(str)) : getInt_bak(str, i);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                log("get", str);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + str);
                stringBuffer.toString();
                for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
                }
                for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                log("get", str);
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#");
                stringBuffer2.append("#" + str);
                stringBuffer2.toString();
                for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
                }
                for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return i;
        }
    }

    public static int getInt_bak(String str, int i) {
        try {
            log("getInt_bak", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getInt_copy(str, i);
    }

    public static int getInt_copy(String str, int i) {
        try {
            log("getInt_copy", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public static String get_bak(String str) {
        try {
            log("get_bak", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return get_copy(str);
    }

    public static String get_copy(String str) {
        try {
            log("get_copy", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void hookFileList(ClassLoader classLoader) {
        try {
            LHooker.hookMethodWithBackup(File.class, "list", String[].class, null, RuntimeAntiDetectionHook.class, "list", "list_bak");
            LHooker.hookMethodWithBackup(File.class, "exists", Boolean.TYPE, null, RuntimeAntiDetectionHook.class, "exists", "exists_bak");
        } catch (Throwable th) {
            th.printStackTrace();
            log(th.getMessage());
        }
    }

    public static void hookHideXposed(ClassLoader classLoader) {
        try {
            LHooker.hookMethodWithBackup(ClassLoader.class, "loadClass", Class.class, new Class[]{String.class}, RuntimeAntiDetectionHook.class, "loadClass", "loadClass_bak");
            LHooker.hookMethodWithBackup(StackTraceElement.class, "getClassName", String.class, null, RuntimeAntiDetectionHook.class, "StackTraceElement_getClassName", "StackTraceElement_getClassName_bak");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hookNativeLoad(String str, String str2, ClassLoader classLoader) {
        log("hookNativeLoad: " + str + "," + str2);
        nativeLibraryName = str;
        nativeLibraryDir = str2;
        nativeLoadClassLoader = classLoader;
        try {
            int i = Build.VERSION.SDK_INT;
            if (i < 28) {
                LHooker.hookMethodWithBackup(Runtime.class, "doLoad", String.class, new Class[]{String.class, ClassLoader.class}, RuntimeAntiDetectionHook.class, "doLoad", "doLoad_bak");
                return;
            }
            if (i >= 29) {
                LHooker.hookMethodWithBackup(Runtime.class, "loadLibrary0", Void.TYPE, new Class[]{ClassLoader.class, Class.class, String.class}, RuntimeAntiDetectionHook.class, "loadLibrary0_Q", "loadLibrary0_Q_bak");
            } else {
                LHooker.hookMethodWithBackup(Runtime.class, "loadLibrary0", Void.TYPE, new Class[]{ClassLoader.class, String.class}, RuntimeAntiDetectionHook.class, "loadLibrary0", "loadLibrary0_bak");
            }
            LHooker.hookMethodWithBackup(Runtime.class, "load0", Void.TYPE, new Class[]{Class.class, String.class}, RuntimeAntiDetectionHook.class, "load0", "load0_bak");
        } catch (Throwable th) {
            th.printStackTrace();
            log("hookNativeLoad Exception: " + th.getMessage());
        }
    }

    public static void hookSystemProperties(ClassLoader classLoader) {
        try {
            LHooker.hookMethodWithBackup(ReflectionUtils.forName("android.os.SystemProperties"), "get", String.class, new Class[]{String.class}, RuntimeAntiDetectionHook.class, "get", "get_bak");
            LHooker.hookMethodWithBackup(ReflectionUtils.forName("android.os.SystemProperties"), "get", String.class, new Class[]{String.class, String.class}, RuntimeAntiDetectionHook.class, "get2", "get2_bak");
            Class clsM105 = ReflectionUtils.forName("android.os.SystemProperties");
            Class cls = Boolean.TYPE;
            LHooker.hookMethodWithBackup(clsM105, "getBoolean", cls, new Class[]{String.class, cls}, RuntimeAntiDetectionHook.class, "getBoolean", "getBoolean_bak");
            Class clsM1052 = ReflectionUtils.forName("android.os.SystemProperties");
            Class cls2 = Integer.TYPE;
            LHooker.hookMethodWithBackup(clsM1052, "getInt", cls2, new Class[]{String.class, cls2}, RuntimeAntiDetectionHook.class, "getInt", "getInt_bak");
        } catch (Throwable th2) {
            th2.printStackTrace();
            log(th2.getMessage());
        }
    }

    public static String[] list(Object obj) {
        log("list", obj);
        try {
            String[] strArrList_bak = list_bak(obj);
            log("list.strings:", strArrList_bak);
            if (strArrList_bak != null && strArrList_bak.length > 0 && AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled()) {
                List<String> listM138 = AntiDetectionServiceManager.getInstance().getHiddenFileNames();
                log("antiDetectedPackages:", listM138);
                if (listM138 != null && !listM138.isEmpty()) {
                    for (int i = 0; i < strArrList_bak.length; i++) {
                        log("contains:", Boolean.valueOf(listM138.contains(strArrList_bak[i])), strArrList_bak[i]);
                        if (listM138.contains(strArrList_bak[i])) {
                            log("正在反检测: " + strArrList_bak[i]);
                            strArrList_bak[i] = i + "" + System.currentTimeMillis() + Math.random();
                        }
                    }
                }
            }
            return strArrList_bak;
        } catch (Throwable th) {
            th.printStackTrace();
            return new String[0];
        }
    }

    public static String[] list_bak(Object obj) {
        try {
            log("list_bak", obj);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
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
        return list_copy(obj);
    }

    public static String[] list_copy(Object obj) {
        try {
            log("list_copy", obj);
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
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void load0(Object obj, Class<?> cls, String str) {
        log("load0", str, cls);
        try {
            load0_bak(obj, cls, str);
            LAntiDetect.hookLoadedLibrary(null, null, str);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void load0_bak(Object obj, Class<?> cls, String str) {
        log("load0_bak", str, cls);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        load0_copy(obj, cls, str);
    }

    public static void load0_copy(Object obj, Class<?> cls, String str) {
        log("load0_copy", str, cls);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<?> loadClass(Object obj, String str) throws ClassNotFoundException {
        try {
            log("loadClass", obj, str);
            if (AntiDetectionServiceManager.getInstance().isAntiDetectionEnabled() && AntiDetectionServiceManager.getInstance().isFileNameHidingEnabled() && "de.robv.android.xposed.XposedHelpers".equals(str)) {
                if (xposedHelpersLoadState != -1) {
                    str = "de.robvf.android.xposed.XposedHelpers";
                }
                xposedHelpersLoadState = 0;
            }
            return loadClass_bak(obj, str);
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static Class<?> loadClass_bak(Object obj, String str) throws ClassNotFoundException {
        try {
            log("loadClass_bak", obj, str);
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
        return loadClass_copy(obj, str);
    }

    public static Class<?> loadClass_copy(Object obj, String str) {
        try {
            log("loadClass_copy", obj, str);
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
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadLibrary0(Object obj, ClassLoader classLoader, String str) {
        log("loadLibrary0", str, classLoader);
        try {
            loadLibrary0_bak(obj, classLoader, str);
            LAntiDetect.hookLoadedLibrary(null, null, nativeLibraryDir + "/lib" + str + ".so");
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void loadLibrary0_Q(Object obj, ClassLoader classLoader, Class<?> cls, String str) {
        log("loadLibrary0_Q", str, classLoader);
        try {
            loadLibrary0_Q_bak(obj, classLoader, cls, str);
            LAntiDetect.hookLoadedLibrary(null, null, nativeLibraryDir + "/lib" + str + ".so");
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void loadLibrary0_Q_bak(Object obj, ClassLoader classLoader, Class<?> cls, String str) {
        log("loadLibrary0_Q_bak", str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadLibrary0_Q_copy(obj, classLoader, cls, str);
    }

    public static void loadLibrary0_Q_copy(Object obj, ClassLoader classLoader, Class<?> cls, String str) {
        log("loadLibrary0_Q_copy", str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadLibrary0_bak(Object obj, ClassLoader classLoader, String str) {
        log("loadLibrary0_bak", str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadLibrary0_copy(obj, classLoader, str);
    }

    public static void loadLibrary0_copy(Object obj, ClassLoader classLoader, String str) {
        log("loadLibrary0_copy", str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("RuntimeAntiDetectionHook", objArr);
    }

    public static String nativeLoad(String str, ClassLoader classLoader) {
        String strNativeLoad_bak;
        synchronized (nativeLoadLock) {
            log("nativeLoad", str);
            strNativeLoad_bak = nativeLoad_bak(str, classLoader);
            log("nativeLoad.error", strNativeLoad_bak);
            if (strNativeLoad_bak == null) {
                try {
                    LAntiDetect.hookLoadedLibrary(null, null, str);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
        return strNativeLoad_bak;
    }

    public static String nativeLoad27(String str, ClassLoader classLoader, String str2) {
        String strNativeLoad27_bak;
        synchronized (nativeLoadLock) {
            log("nativeLoad27", str);
            strNativeLoad27_bak = nativeLoad27_bak(str, classLoader, str2);
            log("nativeLoad27.error", strNativeLoad27_bak);
            if (strNativeLoad27_bak == null) {
                try {
                    LAntiDetect.hookLoadedLibrary(null, null, str);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
        return strNativeLoad27_bak;
    }

    public static String nativeLoad27_bak(String str, ClassLoader classLoader, String str2) {
        try {
            log("nativeLoad27_bak", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nativeLoad27_copy(str, classLoader, str2);
    }

    public static String nativeLoad27_copy(String str, ClassLoader classLoader, String str2) {
        try {
            log("nativeLoad27_copy", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String nativeLoad_bak(String str, ClassLoader classLoader) {
        try {
            log("nativeLoad_bak", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nativeLoad_copy(str, classLoader);
    }

    public static String nativeLoad_copy(String str, ClassLoader classLoader) {
        try {
            log("nativeLoad_copy", str);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + str);
            stringBuffer.toString();
            for (int i = 0; i < 100; i = i + 1 + 1) {
            }
            for (int i2 = 0; i2 < 100; i2 = i2 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
