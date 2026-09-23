package com.kail.location.inject.fakelocation.hook.system;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.VersionedPackage;
import android.os.Binder;
import android.os.Build;
import com.kail.location.inject.fakelocation.InjectDex;
import com.kail.location.inject.utils.CallingProcessUtils;
import com.kail.location.inject.utils.ReflectionUtils;
import com.kail.location.inject.utils.PackageAntiDetectionConfig;
import com.kail.location.inject.utils.PackageSignatureVerifier;
import com.kail.location.lib.lhooker.LHooker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: renamed from: ֏.֏.ހ.֏.ހ.֏, reason: contains not printable characters */
/* JADX INFO: loaded from: /home/kail/code/tool/jadx-1.5.5/bin/classes.dex */
public class PackageManagerServiceHook {

    /* JADX INFO: renamed from: ֏, reason: contains not printable characters */
    public static String packageManagerServiceClassName = "com.android.server.pm.PackageManagerService";

    /* JADX INFO: renamed from: ؠ, reason: contains not printable characters */
    private static Class parceledListSliceClass = null;

    /* JADX INFO: renamed from: ހ, reason: contains not printable characters */
    private static boolean packageManagerHooked = false;

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ހ.֏$֏, reason: contains not printable characters */
    public static class AppsFilterHook {
        static String getPackageNameFromPackageSettings(Object obj) {
            String string = obj.toString();
            return string.substring(string.lastIndexOf(32) + 1, string.lastIndexOf(47));
        }

        public static void hook(ClassLoader classLoader) {
            Class clsForName = PackageManagerServiceHook.forName(classLoader, "com.android.server.pm.AppsFilter");
            Class cls = Boolean.TYPE;
            Class cls2 = Integer.TYPE;
            LHooker.hookMethodWithBackup(clsForName, "shouldFilterApplication", cls, new Class[]{cls2, PackageManagerServiceHook.forName(classLoader, "com.android.server.pm.SettingBase"), PackageManagerServiceHook.forName(classLoader, "com.android.server.pm.PackageSetting"), cls2}, AppsFilterHook.class, "shouldFilterApplication", "shouldFilterApplication_bak");
        }

        public static boolean shouldFilterApplication(Object obj, int i, Object obj2, Object obj3, int i2) {
            PackageManagerServiceHook.log("R.shouldFilterApplication", obj, Integer.valueOf(i), obj2, obj3, Integer.valueOf(i2));
            if (PackageManagerServiceHook.isHookAntiDetection(i) && PackageManagerServiceHook.isPackageDetected(getPackageNameFromPackageSettings(obj3))) {
                return true;
            }
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
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
            return shouldFilterApplication_bak(obj, i, obj2, obj3, i2);
        }

        public static boolean shouldFilterApplication_bak(Object obj, int i, Object obj2, Object obj3, int i2) {
            PackageManagerServiceHook.log("R.shouldFilterApplication_bak", obj, Integer.valueOf(i), obj2, obj3, Integer.valueOf(i2));
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
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
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
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
                stringBuffer5.append("#" + obj);
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
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            return shouldFilterApplication_copy(obj, i, obj2, obj3, i2);
        }

        public static boolean shouldFilterApplication_copy(Object obj, int i, Object obj2, Object obj3, int i2) {
            PackageManagerServiceHook.log("R.shouldFilterApplication_copy", obj, Integer.valueOf(i), obj2, obj3, Integer.valueOf(i2));
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
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
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
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
                stringBuffer5.append("#" + obj);
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
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            return false;
        }
    }

    /* JADX INFO: renamed from: ֏.֏.ހ.֏.ހ.֏$ؠ, reason: contains not printable characters */
    public static class AppsFilterImplHook {
        static String getPackageNameFromPackageSettings(Object obj) {
            String string = obj.toString();
            return string.substring(string.lastIndexOf(32) + 1, string.lastIndexOf(47));
        }

        public static void hook(ClassLoader classLoader) {
            Class clsForName = PackageManagerServiceHook.forName(classLoader, "com.android.server.pm.AppsFilterImpl");
            Class cls = Boolean.TYPE;
            Class cls2 = Integer.TYPE;
            LHooker.hookMethodWithBackup(clsForName, "shouldFilterApplication", cls, new Class[]{PackageManagerServiceHook.forName(classLoader, "com.android.server.pm.snapshot.PackageDataSnapshot"), cls2, Object.class, PackageManagerServiceHook.forName(classLoader, "com.android.server.pm.pkg.PackageStateInternal"), cls2}, AppsFilterImplHook.class, "shouldFilterApplication", "shouldFilterApplication_bak");
        }

        public static boolean shouldFilterApplication(Object obj, Object obj2, int i, Object obj3, Object obj4, int i2) {
            PackageManagerServiceHook.log("T.shouldFilterApplication", obj, obj2, Integer.valueOf(i), obj3, obj4, Integer.valueOf(i2));
            if (PackageManagerServiceHook.isHookAntiDetection(i) && PackageManagerServiceHook.isPackageDetected(getPackageNameFromPackageSettings(obj4))) {
                return true;
            }
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
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
            return shouldFilterApplication_bak(obj, obj2, i, obj3, obj4, i2);
        }

        public static boolean shouldFilterApplication_bak(Object obj, Object obj2, int i, Object obj3, Object obj4, int i2) {
            PackageManagerServiceHook.log("T.shouldFilterApplication_bak", obj, obj2, Integer.valueOf(i), obj3, obj4, Integer.valueOf(i2));
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
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
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
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
                stringBuffer5.append("#" + obj);
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
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            return shouldFilterApplication_copy(obj, obj2, i, obj3, obj4, i2);
        }

        public static boolean shouldFilterApplication_copy(Object obj, Object obj2, int i, Object obj3, Object obj4, int i2) {
            PackageManagerServiceHook.log("T.shouldFilterApplication_copy", obj, obj2, Integer.valueOf(i), obj3, obj4, Integer.valueOf(i2));
            try {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#");
                stringBuffer.append("#" + obj);
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
            try {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#");
                stringBuffer4.append("#" + obj);
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
                stringBuffer5.append("#" + obj);
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
                stringBuffer6.append("#" + obj);
                stringBuffer6.toString();
                for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
                }
                for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            return false;
        }
    }

    public static int checkPermission(Object obj, String str, String str2, int i) {
        log("checkPermission", obj, str, str2);
        try {
            if (isHookAntiDetection() && isPackageDetected(str2)) {
                str2 = "" + System.currentTimeMillis();
            }
            return checkPermission_bak(obj, str, str2, i);
        } catch (Throwable th) {
            th.printStackTrace();
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
                return -1;
            } catch (Exception e2) {
                e2.printStackTrace();
                return -1;
            }
        }
    }

    public static int checkPermission_bak(Object obj, String str, String str2, int i) {
        log("checkPermission_bak", obj, str, str2);
        try {
            isHookAntiDetection();
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
        return checkPermission_copy(obj, str, str2, i);
    }

    public static int checkPermission_copy(Object obj, String str, String str2, int i) {
        log("checkPermission_copy", obj, str, str2);
        try {
            isHookAntiDetection();
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
            return -1;
        } catch (Exception e3) {
            e3.printStackTrace();
            return -1;
        }
    }

    public static int checkUidPermission(Object obj, String str, int i) {
        log("checkUidPermission", obj, str, Integer.valueOf(i));
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
        try {
            if (isHookAntiDetection() && isPackageDetected(CallingProcessUtils.getPackageNameForPidOrUid(-1, i))) {
                return -1;
            }
            return checkUidPermission_bak(obj, str, i);
        } catch (Throwable th) {
            th.printStackTrace();
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
            return -1;
        }
    }

    public static int checkUidPermission_bak(Object obj, String str, int i) {
        log("checkUidPermission_bak", obj, str, Integer.valueOf(i));
        try {
            isHookAntiDetection();
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
        return checkUidPermission_copy(obj, str, i);
    }

    public static int checkUidPermission_copy(Object obj, String str, int i) {
        log("checkUidPermission_copy", obj, str, Integer.valueOf(i));
        try {
            isHookAntiDetection();
            StringBuffer stringBuffer = new StringBuffer();
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
            return -1;
        } catch (Exception e3) {
            e3.printStackTrace();
            return -1;
        }
    }

    public static Class forName(ClassLoader classLoader, String str) {
        return ReflectionUtils.loadClass(str, true, classLoader);
    }

    public static ActivityInfo getActivityInfo(Object obj, ComponentName componentName, int i, int i2) {
        log("getActivityInfo", obj, componentName, Integer.valueOf(i), Integer.valueOf(i2));
        if (componentName != null) {
            try {
                if (isHookAntiDetection() && isPackageDetected(componentName.getPackageName())) {
                    componentName = new ComponentName("" + System.currentTimeMillis(), "" + System.currentTimeMillis());
                }
            } catch (Throwable th) {
                th.printStackTrace();
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("#");
                    stringBuffer.append("#");
                    stringBuffer.append("#");
                    stringBuffer.append("#");
                    stringBuffer.append("#");
                    stringBuffer.append("#" + obj);
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
                try {
                    StringBuffer stringBuffer4 = new StringBuffer();
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#" + obj);
                    stringBuffer4.toString();
                    for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                    }
                    for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                    }
                    return null;
                } catch (Exception e4) {
                    e4.printStackTrace();
                    return null;
                }
            }
        }
        return getActivityInfo_bak(obj, componentName, i, i2);
    }

    public static ActivityInfo getActivityInfo_bak(Object obj, ComponentName componentName, int i, int i2) {
        log("getActivityInfo_bak", obj, componentName);
        try {
            isHookAntiDetection();
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
        return getActivityInfo_copy(obj, componentName, i, i2);
    }

    public static ActivityInfo getActivityInfo_copy(Object obj, ComponentName componentName, int i, int i2) {
        log("getActivityInfo_copy", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            isHookAntiDetection();
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
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            return null;
        } catch (Exception e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public static List<String> getAllPackages(Object obj) {
        log("getAllPackages", obj);
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
        List<String> allPackages_bak = null;
        try {
            allPackages_bak = getAllPackages_bak(obj);
            if (allPackages_bak != null && !allPackages_bak.isEmpty() && isHookAntiDetection()) {
                int i5 = 0;
                while (i5 < allPackages_bak.size()) {
                    if (isPackageDetected(allPackages_bak.get(i5))) {
                        allPackages_bak.remove(i5);
                        i5--;
                    }
                    i5++;
                }
            }
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
        return allPackages_bak;
    }

    public static List<String> getAllPackages_bak(Object obj) {
        log("getAllPackages_bak", obj);
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
        return getAllPackages_copy(obj);
    }

    public static List<String> getAllPackages_copy(Object obj) {
        log("getAllPackages_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo(Object obj, String str, int i, int i2) {
        log("getApplicationInfo", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
        if (str == null) {
            return getApplicationInfo_bak(obj, str, i, i2);
        }
        if (str.startsWith("assenti-")) {
            return getApplicationInfo_bak(obj, str.replaceFirst("assenti-", ""), i, i2);
        }
        if (isHookAntiDetection() && isPackageDetected(str)) {
            str = "" + System.currentTimeMillis();
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
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
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return getApplicationInfo_bak(obj, str, i, i2);
    }

    public static ApplicationInfo getApplicationInfo_bak(Object obj, String str, int i, int i2) {
        try {
            log("getApplicationInfo_bak", obj);
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
        return getApplicationInfo_copy(obj, str, i, i2);
    }

    public static ApplicationInfo getApplicationInfo_copy(Object obj, String str, int i, int i2) {
        try {
            log("getApplicationInfo_copy", obj);
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static InstallSourceInfo getInstallSourceInfo(Object obj, String str) throws PackageManager.NameNotFoundException {
        log("getInstallSourceInfo", obj, str);
        if (isHookAntiDetection() && isPackageDetected(str)) {
            throw new PackageManager.NameNotFoundException();
        }
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
        return getInstallSourceInfo_bak(obj, str);
    }

    public static InstallSourceInfo getInstallSourceInfo_bak(Object obj, String str) {
        log("getInstallSourceInfo_bak", obj, str);
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
        return getInstallSourceInfo_copy(obj, str);
    }

    public static InstallSourceInfo getInstallSourceInfo_copy(Object obj, String str) {
        log("getInstallSourceInfo_copy", obj, str);
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
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static Object getInstalledApplications(Object obj, int i, int i2) {
        log("getInstalledApplications", obj, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object installedApplications_bak = null;
        try {
            installedApplications_bak = getInstalledApplications_bak(obj, i, i2);
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(installedApplications_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ApplicationInfo applicationInfo = (ApplicationInfo) arrayList.get(i7);
                    if (isPackageDetected(applicationInfo.packageName)) {
                        arrayList.remove(applicationInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    installedApplications_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (installedApplications_bak != null) {
            return installedApplications_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return installedApplications_bak;
        }
    }

    public static Object getInstalledApplications_Q(Object obj, int i, String str, int i2) {
        log("getInstalledApplications_Q", obj, Integer.valueOf(i), str, Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object installedApplications_Q_bak = null;
        try {
            installedApplications_Q_bak = getInstalledApplications_Q_bak(obj, i, str, i2);
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(installedApplications_Q_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ApplicationInfo applicationInfo = (ApplicationInfo) arrayList.get(i7);
                    if (isPackageDetected(applicationInfo.packageName)) {
                        arrayList.remove(applicationInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    installedApplications_Q_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (installedApplications_Q_bak != null) {
            return installedApplications_Q_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return installedApplications_Q_bak;
        }
    }

    public static Object getInstalledApplications_Q_bak(Object obj, int i, String str, int i2) {
        try {
            log("getInstalledApplications_Q_bak", obj);
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
        return getInstalledApplications_Q_copy(obj, i, str, i2);
    }

    public static Object getInstalledApplications_Q_copy(Object obj, int i, String str, int i2) {
        try {
            log("getInstalledApplications_Q_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object getInstalledApplications_bak(Object obj, int i, int i2) {
        try {
            log("getInstalledApplications_bak", obj);
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
        return getInstalledApplications_copy(obj, i, i2);
    }

    public static Object getInstalledApplications_copy(Object obj, int i, int i2) {
        try {
            log("getInstalledApplications_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object getInstalledPackages(Object obj, int i, int i2) {
        log("getInstalledPackages", obj, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object installedPackages_bak = null;
        try {
            installedPackages_bak = getInstalledPackages_bak(obj, i, i2);
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(installedPackages_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    PackageInfo packageInfo = (PackageInfo) arrayList.get(i7);
                    if (isPackageDetected(packageInfo.packageName)) {
                        arrayList.remove(packageInfo);
                        i7--;
                        log("getInstalledPackages.已移除", obj, packageInfo.packageName);
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    installedPackages_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (installedPackages_bak != null) {
            return installedPackages_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return installedPackages_bak;
        }
    }

    public static Object getInstalledPackages_Q(Object obj, int i, String str) {
        Object installedPackages_Q_bak;
        log("getInstalledPackages_Q", obj, Integer.valueOf(i), str);
        try {
            installedPackages_Q_bak = getInstalledPackages_Q_bak(obj, i, str);
        } catch (Throwable th) {
            th = th;
            installedPackages_Q_bak = null;
        }
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(installedPackages_Q_bak, new Object[0]));
                int i2 = 0;
                boolean z = false;
                while (i2 < arrayList.size()) {
                    PackageInfo packageInfo = (PackageInfo) arrayList.get(i2);
                    if (isPackageDetected(packageInfo.packageName)) {
                        arrayList.remove(packageInfo);
                        i2--;
                        z = true;
                    }
                    i2++;
                }
                if (z) {
                    installedPackages_Q_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        if (installedPackages_Q_bak != null) {
            return installedPackages_Q_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th3) {
            th3.printStackTrace();
            return installedPackages_Q_bak;
        }
    }

    public static Object getInstalledPackages_Q_bak(Object obj, int i, String str) {
        try {
            log("getInstalledPackages_Q_bak", obj);
            if (isHookAntiDetection()) {
                isPackageDetected("");
            }
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
        return getInstalledPackages_Q_copy(obj, i, str);
    }

    public static Object getInstalledPackages_Q_copy(Object obj, int i, String str) {
        try {
            log("getInstalledPackages_Q_copy", obj);
            if (isHookAntiDetection()) {
                isPackageDetected("");
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object getInstalledPackages_bak(Object obj, int i, int i2) {
        try {
            log("getInstalledPackages_bak", obj);
            if (isHookAntiDetection()) {
                isPackageDetected("");
            }
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
        return getInstalledPackages_copy(obj, i, i2);
    }

    public static Object getInstalledPackages_copy(Object obj, int i, int i2) {
        try {
            log("getInstalledPackages_copy", obj);
            if (isHookAntiDetection()) {
                isPackageDetected("");
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static String getInstallerPackageName(Object obj, String str) {
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
        try {
            log("getInstallerPackageName", obj, str);
            if (isHookAntiDetection() && isPackageDetected(str)) {
                str = "" + System.currentTimeMillis();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return getInstallerPackageName_bak(obj, str);
    }

    public static String getInstallerPackageName_bak(Object obj, String str) {
        try {
            log("getInstallerPackageName_bak", obj);
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
        return getInstallerPackageName_copy(obj, str);
    }

    public static String getInstallerPackageName_copy(Object obj, String str) {
        try {
            log("getInstallerPackageName_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static int[] getPackageGids(Object obj, String str, int i, int i2) {
        log("getPackageGids", obj, str);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        if (isHookAntiDetection() && isPackageDetected(str)) {
            str = "" + System.currentTimeMillis();
        }
        return getPackageGids_bak(obj, str, i, i2);
    }

    public static int[] getPackageGids_2(Object obj, String str, int i) {
        log("getPackageGids_2", obj, str);
        if (isHookAntiDetection() && isPackageDetected(str)) {
            str = "" + System.currentTimeMillis();
        }
        return getPackageGids_2_bak(obj, str, i);
    }

    public static int[] getPackageGids_2_bak(Object obj, String str, int i) {
        log("getPackageGids_2_bak", obj, str);
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
        return getPackageGids_2_copy(obj, str, i);
    }

    public static int[] getPackageGids_2_copy(Object obj, String str, int i) {
        log("getPackageGids_2_copy", obj, str);
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static int[] getPackageGids_bak(Object obj, String str, int i, int i2) {
        log("getPackageGids_bak", obj, str);
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
        return getPackageGids_copy(obj, str, i, i2);
    }

    public static int[] getPackageGids_copy(Object obj, String str, int i, int i2) {
        log("getPackageGids_copy", obj, str);
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static PackageInfo getPackageInfo(Object obj, String str, int i, int i2) {
        log("getPackageInfo", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
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
            stringBuffer6.append("#" + obj);
            stringBuffer6.toString();
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
        if (str == null) {
            return getPackageInfo_bak(obj, str, i, i2);
        }
        if (str.startsWith("assenti-")) {
            return getPackageInfo_bak(obj, str.replaceFirst("assenti-", ""), i, i2);
        }
        if (isHookAntiDetection() && isPackageDetected(str)) {
            str = "" + System.currentTimeMillis();
        }
        return getPackageInfo_bak(obj, str, i, i2);
    }

    public static PackageInfo getPackageInfoVersioned(Object obj, VersionedPackage versionedPackage, int i, int i2) {
        log("getPackageInfoVersioned", obj, versionedPackage, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        if (versionedPackage == null) {
            return getPackageInfoVersioned_bak(obj, versionedPackage, i, i2);
        }
        String packageName = versionedPackage.getPackageName();
        if (packageName.startsWith("assenti-")) {
            return getPackageInfoVersioned_bak(obj, new VersionedPackage(packageName.replaceFirst("assenti-", ""), versionedPackage.getLongVersionCode()), i, i2);
        }
        if (isHookAntiDetection() && isPackageDetected(packageName)) {
            versionedPackage = new VersionedPackage("" + System.currentTimeMillis(), versionedPackage.getLongVersionCode());
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
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getPackageInfoVersioned_bak(obj, versionedPackage, i, i2);
    }

    public static PackageInfo getPackageInfoVersioned_bak(Object obj, VersionedPackage versionedPackage, int i, int i2) {
        try {
            log("getPackageInfoVersioned_bak", obj);
            if (isHookAntiDetection()) {
                isPackageDetected(versionedPackage.getPackageName());
            }
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
        return getPackageInfoVersioned_copy(obj, versionedPackage, i, i2);
    }

    public static PackageInfo getPackageInfoVersioned_copy(Object obj, VersionedPackage versionedPackage, int i, int i2) {
        try {
            log("getPackageInfoVersioned_copy", obj);
            if (isHookAntiDetection()) {
                isPackageDetected(versionedPackage.getPackageName());
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static PackageInfo getPackageInfo_bak(Object obj, String str, int i, int i2) {
        try {
            log("getPackageInfo_bak", obj);
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
        return getPackageInfo_copy(obj, str, i, i2);
    }

    public static PackageInfo getPackageInfo_copy(Object obj, String str, int i, int i2) {
        try {
            log("getPackageInfo_copy", obj);
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static int getPackageUid(Object obj, String str, int i, int i2) throws PackageManager.NameNotFoundException {
        log("getPackageUid", obj, str);
        if (isHookAntiDetection() && isPackageDetected(str)) {
            throw new PackageManager.NameNotFoundException(str);
        }
        return getPackageUid_bak(obj, str, i, i2);
    }

    public static int getPackageUid_2(Object obj, String str, int i) throws PackageManager.NameNotFoundException {
        log("getPackageUid_2", obj, str);
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
        if (isHookAntiDetection() && isPackageDetected(str)) {
            throw new PackageManager.NameNotFoundException(str);
        }
        return getPackageUid_2_bak(obj, str, i);
    }

    public static int getPackageUid_2_bak(Object obj, String str, int i) {
        log("getPackageUid_2_bak", obj, str);
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
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return getPackageUid_2_copy(obj, str, i);
    }

    public static int getPackageUid_2_copy(Object obj, String str, int i) {
        log("getPackageUid_2_copy", obj, str);
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
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            return -1;
        } catch (Exception e3) {
            e3.printStackTrace();
            return -1;
        }
    }

    public static int getPackageUid_bak(Object obj, String str, int i, int i2) {
        log("getPackageUid_bak", obj, str);
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
        return getPackageUid_copy(obj, str, i, i2);
    }

    public static int getPackageUid_copy(Object obj, String str, int i, int i2) {
        log("getPackageUid_copy", obj, str);
        try {
            if (isHookAntiDetection()) {
                isPackageDetected(str);
            }
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
            return -1;
        } catch (Exception e2) {
            e2.printStackTrace();
            return -1;
        }
    }

    public static Object getPackagesHoldingPermissions(Object obj, String[] strArr, int i, int i2) {
        log("getPackagesHoldingPermissions", obj, strArr, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object packagesHoldingPermissions_bak = getPackagesHoldingPermissions_bak(obj, strArr, i, i2);
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(packagesHoldingPermissions_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    PackageInfo packageInfo = (PackageInfo) arrayList.get(i7);
                    if (isPackageDetected(packageInfo.packageName)) {
                        arrayList.remove(packageInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    packagesHoldingPermissions_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (packagesHoldingPermissions_bak != null) {
            return packagesHoldingPermissions_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return packagesHoldingPermissions_bak;
        }
    }

    public static Object getPackagesHoldingPermissions_bak(Object obj, String[] strArr, int i, int i2) {
        try {
            log("getPackagesHoldingPermissions_bak", obj);
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
        return getPackagesHoldingPermissions_copy(obj, strArr, i, i2);
    }

    public static Object getPackagesHoldingPermissions_copy(Object obj, String[] strArr, int i, int i2) {
        try {
            log("getPackagesHoldingPermissions_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static PermissionInfo getPermissionInfo(Object obj, String str, String str2, int i) {
        log("getPermissionInfo", obj, str, str2);
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
        try {
            if (isHookAntiDetection() && isPackageDetected(str2)) {
                str2 = "" + System.currentTimeMillis();
            }
            return getPermissionInfo_bak(obj, str, str2, i);
        } catch (Throwable th) {
            th.printStackTrace();
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
                return null;
            } catch (Exception e4) {
                e4.printStackTrace();
                return null;
            }
        }
    }

    public static PermissionInfo getPermissionInfo_bak(Object obj, String str, String str2, int i) {
        log("getPermissionInfo_bak", obj, str, str2);
        try {
            isHookAntiDetection();
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
        return getPermissionInfo_copy(obj, str, str2, i);
    }

    public static PermissionInfo getPermissionInfo_copy(Object obj, String str, String str2, int i) {
        log("getPermissionInfo_copy", obj, str, str2);
        try {
            isHookAntiDetection();
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static int getPreferredActivities(Object obj, List<IntentFilter> list, List<ComponentName> list2, String str) {
        log("getPreferredActivities", obj, str, list);
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
        if (isHookAntiDetection() && isPackageDetected(str)) {
            str = "" + System.currentTimeMillis();
        }
        int preferredActivities_bak = getPreferredActivities_bak(obj, list, list2, str);
        if (list2 != null && !list2.isEmpty() && isHookAntiDetection()) {
            int i5 = 0;
            while (i5 < list2.size()) {
                if (isPackageDetected(list2.get(i5).getPackageName())) {
                    list2.remove(i5);
                    i5--;
                }
                i5++;
            }
            preferredActivities_bak = list2.size();
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
        return preferredActivities_bak;
    }

    public static int getPreferredActivities_bak(Object obj, List<IntentFilter> list, List<ComponentName> list2, String str) {
        log("getPreferredActivities_bak", obj, str, list);
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
        return getPreferredActivities_copy(obj, list, list2, str);
    }

    public static int getPreferredActivities_copy(Object obj, List<IntentFilter> list, List<ComponentName> list2, String str) {
        log("getPreferredActivities_copy", obj, str, list);
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
            stringBuffer2.append("#");
            stringBuffer2.append("#");
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
            return -1;
        } catch (Exception e4) {
            e4.printStackTrace();
            return -1;
        }
    }

    public static ProviderInfo getProviderInfo(Object obj, ComponentName componentName, int i, int i2) {
        log("getProviderInfo", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        if (componentName != null) {
            try {
                if (isHookAntiDetection() && isPackageDetected(componentName.getPackageName())) {
                    componentName = new ComponentName("" + System.currentTimeMillis(), "" + System.currentTimeMillis());
                }
            } catch (Throwable th) {
                th.printStackTrace();
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
                try {
                    StringBuffer stringBuffer4 = new StringBuffer();
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#");
                    stringBuffer4.append("#" + obj);
                    stringBuffer4.toString();
                    for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
                    }
                    for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
                    }
                    return null;
                } catch (Exception e4) {
                    e4.printStackTrace();
                    return null;
                }
            }
        }
        return getProviderInfo_bak(obj, componentName, i, i2);
    }

    public static ProviderInfo getProviderInfo_bak(Object obj, ComponentName componentName, int i, int i2) {
        log("getProviderInfo_bak", obj, componentName);
        try {
            isHookAntiDetection();
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
        return getProviderInfo_copy(obj, componentName, i, i2);
    }

    public static ProviderInfo getProviderInfo_copy(Object obj, ComponentName componentName, int i, int i2) {
        log("getProviderInfo_copy", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            isHookAntiDetection();
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
            stringBuffer3.append("#" + obj);
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
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static ActivityInfo getReceiverInfo(Object obj, ComponentName componentName, int i, int i2) {
        log("getReceiverInfo", obj, componentName);
        if (componentName != null && isHookAntiDetection() && isPackageDetected(componentName.getPackageName())) {
            componentName = new ComponentName("" + System.currentTimeMillis(), "" + System.currentTimeMillis());
        }
        return getReceiverInfo_bak(obj, componentName, i, i2);
    }

    public static ActivityInfo getReceiverInfo_bak(Object obj, ComponentName componentName, int i, int i2) {
        log("getReceiverInfo_bak", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            isHookAntiDetection();
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
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return getReceiverInfo_copy(obj, componentName, i, i2);
    }

    public static ActivityInfo getReceiverInfo_copy(Object obj, ComponentName componentName, int i, int i2) {
        log("getReceiverInfo_copy", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            isHookAntiDetection();
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
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            return null;
        } catch (Exception e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public static ServiceInfo getServiceInfo(Object obj, ComponentName componentName, int i, int i2) {
        log("getServiceInfo", obj, componentName);
        if (componentName != null) {
            try {
                if (isHookAntiDetection()) {
                    if (isPackageDetected(componentName.getPackageName())) {
                        componentName = new ComponentName("" + System.currentTimeMillis(), "" + System.currentTimeMillis());
                    }
                    if (componentName.getPackageName().equals("com.szzc.ucar.ptdriver") && componentName.getClassName().contains("com.szzc.ucar.cheatdetect.")) {
                        log("getServiceInfo.服务已拦截", componentName);
                        componentName = new ComponentName("" + System.currentTimeMillis(), "" + System.currentTimeMillis());
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }
        return getServiceInfo_bak(obj, componentName, i, i2);
    }

    public static ServiceInfo getServiceInfo_bak(Object obj, ComponentName componentName, int i, int i2) {
        log("getServiceInfo_bak", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            isHookAntiDetection();
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
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return getServiceInfo_copy(obj, componentName, i, i2);
    }

    public static ServiceInfo getServiceInfo_copy(Object obj, ComponentName componentName, int i, int i2) {
        log("getServiceInfo_copy", obj, componentName);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            isHookAntiDetection();
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
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            return null;
        } catch (Exception e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public static void hook(ClassLoader classLoader) {
        if (packageManagerHooked) {
            return;
        }
        try {
            int i = Build.VERSION.SDK_INT;
            if (i >= 33) {
                AppsFilterImplHook.hook(classLoader);
            } else if (i >= 30) {
                AppsFilterHook.hook(classLoader);
            } else {
                Class clsForName = forName(classLoader, packageManagerServiceClassName);
                Class cls = Integer.TYPE;
                LHooker.hookMethodWithBackup(clsForName, "getActivityInfo", ActivityInfo.class, new Class[]{ComponentName.class, cls, cls}, PackageManagerServiceHook.class, "getActivityInfo", "getActivityInfo_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getReceiverInfo", ActivityInfo.class, new Class[]{ComponentName.class, cls, cls}, PackageManagerServiceHook.class, "getReceiverInfo", "getReceiverInfo_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getServiceInfo", ServiceInfo.class, new Class[]{ComponentName.class, cls, cls}, PackageManagerServiceHook.class, "getServiceInfo", "getServiceInfo_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getProviderInfo", ProviderInfo.class, new Class[]{ComponentName.class, cls, cls}, PackageManagerServiceHook.class, "getProviderInfo", "getProviderInfo_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getPermissionInfo", PermissionInfo.class, new Class[]{String.class, String.class, cls}, PackageManagerServiceHook.class, "getPermissionInfo", "getPermissionInfo_bak");
                if (i < 31) {
                    LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "checkPermission", cls, new Class[]{String.class, String.class, cls}, PackageManagerServiceHook.class, "checkPermission", "checkPermission_bak");
                }
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getPackageInfo", PackageInfo.class, new Class[]{String.class, cls, cls}, PackageManagerServiceHook.class, "getPackageInfo", "getPackageInfo_bak");
                if (i >= 26) {
                    LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getPackageInfoVersioned", PackageInfo.class, new Class[]{VersionedPackage.class, cls, cls}, PackageManagerServiceHook.class, "getPackageInfoVersioned", "getPackageInfoVersioned_bak");
                }
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getApplicationInfo", ApplicationInfo.class, new Class[]{String.class, cls, cls}, PackageManagerServiceHook.class, "getApplicationInfo", "getApplicationInfo_bak");
                Class clsForName2 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName3 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName3;
                LHooker.hookMethodWithBackup(clsForName2, "getInstalledPackages", clsForName3, new Class[]{cls, cls}, PackageManagerServiceHook.class, "getInstalledPackages", "getInstalledPackages_bak");
                Class clsForName4 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName5 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName5;
                LHooker.hookMethodWithBackup(clsForName4, "getInstalledPackages", clsForName5, new Class[]{cls, String.class}, PackageManagerServiceHook.class, "getInstalledPackages_Q", "getInstalledPackages_Q_bak");
                Class clsForName6 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName7 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName7;
                LHooker.hookMethodWithBackup(clsForName6, "getInstalledApplications", clsForName7, new Class[]{cls, cls}, PackageManagerServiceHook.class, "getInstalledApplications", "getInstalledApplications_bak");
                Class clsForName8 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName9 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName9;
                LHooker.hookMethodWithBackup(clsForName8, "getInstalledApplications", clsForName9, new Class[]{cls, String.class, cls}, PackageManagerServiceHook.class, "getInstalledApplications_Q", "getInstalledApplications_Q_bak");
                Class clsForName10 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName11 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName11;
                LHooker.hookMethodWithBackup(clsForName10, "getPackagesHoldingPermissions", clsForName11, new Class[]{String[].class, cls, cls}, PackageManagerServiceHook.class, "getPackagesHoldingPermissions", "getPackagesHoldingPermissions_bak");
                Class clsForName12 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName13 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName13;
                LHooker.hookMethodWithBackup(clsForName12, "queryIntentActivities", clsForName13, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentActivities", "queryIntentActivities_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryIntentActivities", List.class, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentActivities_2", "queryIntentActivities_2_bak");
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryIntentActivities", List.class, new Class[]{Intent.class, String.class, cls, cls, cls}, PackageManagerServiceHook.class, "queryIntentActivities_S", "queryIntentActivities_S_bak");
                }
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getInstallerPackageName", String.class, new Class[]{String.class}, PackageManagerServiceHook.class, "getInstallerPackageName", "getInstallerPackageName_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getInstallSourceInfo", InstallSourceInfo.class, new Class[]{String.class}, PackageManagerServiceHook.class, "getInstallSourceInfo", "getInstallSourceInfo_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getAllPackages", List.class, null, PackageManagerServiceHook.class, "getAllPackages", "getAllPackages_bak");
                if (i < 30 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "resolveIntent", ResolveInfo.class, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "resolveIntent", "resolveIntent_bak");
                }
                if (PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    LHooker.hookMethodAutoBackup(forName(classLoader, packageManagerServiceClassName), "resolveIntent", ResolveInfo.class, new Class[]{Intent.class, String.class, cls, cls, cls, Boolean.TYPE, cls}, PackageManagerServiceHook.class, "resolveIntent_S");
                }
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    Class clsForName14 = forName(classLoader, packageManagerServiceClassName);
                    Class clsForName15 = forName(classLoader, "android.content.pm.ParceledListSlice");
                    parceledListSliceClass = clsForName15;
                    LHooker.hookMethodWithBackup(clsForName14, "queryIntentActivityOptions", clsForName15, new Class[]{ComponentName.class, Intent[].class, String[].class, Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentActivityOptions", "queryIntentActivityOptions_bak");
                }
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    Class clsForName16 = forName(classLoader, packageManagerServiceClassName);
                    Class clsForName17 = forName(classLoader, "android.content.pm.ParceledListSlice");
                    parceledListSliceClass = clsForName17;
                    LHooker.hookMethodWithBackup(clsForName16, "queryIntentReceivers", clsForName17, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentReceivers", "queryIntentReceivers_bak");
                }
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "resolveService", ResolveInfo.class, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "resolveService", "resolveService_bak");
                }
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    Class clsForName18 = forName(classLoader, packageManagerServiceClassName);
                    Class clsForName19 = forName(classLoader, "android.content.pm.ParceledListSlice");
                    parceledListSliceClass = clsForName19;
                    LHooker.hookMethodWithBackup(clsForName18, "queryIntentServices", clsForName19, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentServices", "queryIntentServices_bak");
                }
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    Class clsForName20 = forName(classLoader, packageManagerServiceClassName);
                    Class clsForName21 = forName(classLoader, "android.content.pm.ParceledListSlice");
                    parceledListSliceClass = clsForName21;
                    LHooker.hookMethodWithBackup(clsForName20, "queryIntentContentProviders", clsForName21, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentContentProviders", "queryIntentContentProviders_bak");
                }
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    Class clsForName22 = forName(classLoader, packageManagerServiceClassName);
                    Class clsForName23 = forName(classLoader, "android.content.pm.ParceledListSlice");
                    parceledListSliceClass = clsForName23;
                    LHooker.hookMethodWithBackup(clsForName22, "queryContentProviders", clsForName23, new Class[]{String.class, cls, cls, String.class}, PackageManagerServiceHook.class, "queryContentProviders", "queryContentProviders_bak");
                }
                Class clsForName24 = forName(classLoader, packageManagerServiceClassName);
                Class clsForName25 = forName(classLoader, "android.content.pm.ParceledListSlice");
                parceledListSliceClass = clsForName25;
                LHooker.hookMethodWithBackup(clsForName24, "queryContentProviders", clsForName25, new Class[]{String.class, cls, cls}, PackageManagerServiceHook.class, "queryContentProviders_N", "queryContentProviders_N_bak");
                if (i < 31 || PackageAntiDetectionConfig.isPackageFilterEnabled()) {
                    Class clsForName26 = forName(classLoader, packageManagerServiceClassName);
                    Class clsForName27 = forName(classLoader, "android.content.pm.ParceledListSlice");
                    parceledListSliceClass = clsForName27;
                    LHooker.hookMethodWithBackup(clsForName26, "queryInstrumentation", clsForName27, new Class[]{String.class, cls}, PackageManagerServiceHook.class, "queryInstrumentation", "queryInstrumentation_bak");
                }
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "getPreferredActivities", cls, new Class[]{List.class, List.class, String.class}, PackageManagerServiceHook.class, "getPreferredActivities", "getPreferredActivities_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryContentProviders", List.class, new Class[]{String.class, cls, cls}, PackageManagerServiceHook.class, "queryContentProviders_L", "queryContentProviders_L_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryInstrumentation", List.class, new Class[]{String.class, cls}, PackageManagerServiceHook.class, "queryInstrumentation_L", "queryInstrumentation_L_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryIntentContentProviders", List.class, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentContentProviders_L", "queryIntentContentProviders_L_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryIntentActivityOptions", List.class, new Class[]{ComponentName.class, Intent[].class, String[].class, Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentActivityOptions_L", "queryIntentActivityOptions_L_bak");
                LHooker.hookMethodWithBackup(forName(classLoader, packageManagerServiceClassName), "queryIntentActivities", List.class, new Class[]{Intent.class, String.class, cls, cls}, PackageManagerServiceHook.class, "queryIntentActivities_L", "queryIntentActivities_L_bak");
            }
            PackageSignatureVerifier.verifyPackageSignature(InjectDex.getApplicationContext(), "com.kail.location", packageManagerServiceClassName);
            packageManagerHooked = true;
        } catch (RuntimeException unused) {
            PackageAntiDetectionConfig.setPackageManagerHookEnabled(false);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static boolean isHookAntiDetection() {
        return PackageAntiDetectionConfig.isPackageManagerHookEnabled() && (CallingProcessUtils.isCallingShellUid() || !CallingProcessUtils.isCallingSystemUid()) && PackageAntiDetectionConfig.isPackageAllowedForPidUid(Binder.getCallingPid(), Binder.getCallingUid());
    }

    public static boolean isHookAntiDetection(int i) {
        boolean z = PackageAntiDetectionConfig.isPackageManagerHookEnabled() && !CallingProcessUtils.isSystemUid(i) && PackageAntiDetectionConfig.isPackageAllowedForPidUid(-1, i);
        log("isHookAntiDetection: " + z, "uid: " + Binder.getCallingUid(), "pid: " + Binder.getCallingPid());
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isPackageDetected(String str) {
        return PackageAntiDetectionConfig.isDetectedPackage(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void log(Object... objArr) {
        if (!com.kail.location.inject.utils.InjectLog.hookLogEnabled) return;
        com.kail.location.inject.utils.InjectLog.log("PackageManagerServiceHook", objArr);
    }

    public static Object queryContentProviders(Object obj, String str, int i, int i2, String str2) {
        log("queryContentProviders", obj, str, Integer.valueOf(i), Integer.valueOf(i2), str2);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object objQueryContentProviders_bak = queryContentProviders_bak(obj, str, i, i2, str2);
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryContentProviders_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ProviderInfo providerInfo = (ProviderInfo) arrayList.get(i7);
                    if (isPackageDetected(providerInfo.packageName)) {
                        arrayList.remove(providerInfo);
                        i7--;
                        log("queryContentProviders.已移除", obj, str, providerInfo.packageName);
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryContentProviders_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (objQueryContentProviders_bak != null) {
            return objQueryContentProviders_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryContentProviders_bak;
        }
    }

    public static List<ProviderInfo> queryContentProviders_L(Object obj, String str, int i, int i2) {
        int i3 = 0;
        log("queryContentProviders", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        List<ProviderInfo> listQueryContentProviders_L_bak = queryContentProviders_L_bak(obj, str, i, i2);
        if (listQueryContentProviders_L_bak != null) {
            try {
                if (isHookAntiDetection()) {
                    List<ProviderInfo> arrayList = new ArrayList<>(listQueryContentProviders_L_bak);
                    boolean z = false;
                    while (i3 < arrayList.size()) {
                        ProviderInfo providerInfo = arrayList.get(i3);
                        if (isPackageDetected(providerInfo.packageName)) {
                            arrayList.remove(providerInfo);
                            i3--;
                            z = true;
                        }
                        i3++;
                    }
                    if (z) {
                        listQueryContentProviders_L_bak = arrayList;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (listQueryContentProviders_L_bak != null) {
            return listQueryContentProviders_L_bak;
        }
        try {
            return new ArrayList();
        } catch (Throwable th2) {
            th2.printStackTrace();
            return listQueryContentProviders_L_bak;
        }
    }

    public static List<ProviderInfo> queryContentProviders_L_bak(Object obj, String str, int i, int i2) {
        log("queryContentProviders_L_bak", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryContentProviders_L_copy(obj, str, i, i2);
    }

    public static List<ProviderInfo> queryContentProviders_L_copy(Object obj, String str, int i, int i2) {
        log("queryContentProviders_L_copy", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object queryContentProviders_N(Object obj, String str, int i, int i2) {
        log("queryContentProviders", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object objQueryContentProviders_N_bak = queryContentProviders_N_bak(obj, str, i, i2);
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryContentProviders_N_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ProviderInfo providerInfo = (ProviderInfo) arrayList.get(i7);
                    if (isPackageDetected(providerInfo.packageName)) {
                        arrayList.remove(providerInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryContentProviders_N_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (objQueryContentProviders_N_bak != null) {
            return objQueryContentProviders_N_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryContentProviders_N_bak;
        }
    }

    public static Object queryContentProviders_N_bak(Object obj, String str, int i, int i2) {
        log("queryContentProviders_N_bak", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
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
        return queryContentProviders_N_copy(obj, str, i, i2);
    }

    public static Object queryContentProviders_N_copy(Object obj, String str, int i, int i2) {
        log("queryContentProviders_N_copy", obj, str, Integer.valueOf(i), Integer.valueOf(i2));
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object queryContentProviders_bak(Object obj, String str, int i, int i2, String str2) {
        log("queryContentProviders_bak", obj, str, Integer.valueOf(i), Integer.valueOf(i2), str2);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            stringBuffer3.append("#" + obj);
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
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return queryContentProviders_copy(obj, str, i, i2, str2);
    }

    public static Object queryContentProviders_copy(Object obj, String str, int i, int i2, String str2) {
        log("queryContentProviders_copy", obj, str, Integer.valueOf(i), Integer.valueOf(i2), str2);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object queryInstrumentation(Object obj, String str, int i) {
        log("queryInstrumentation", obj, str, Integer.valueOf(i));
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
        Object objQueryInstrumentation_bak = queryInstrumentation_bak(obj, str, i);
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryInstrumentation_bak, new Object[0]));
                int i6 = 0;
                boolean z = false;
                while (i6 < arrayList.size()) {
                    InstrumentationInfo instrumentationInfo = (InstrumentationInfo) arrayList.get(i6);
                    if (isPackageDetected(instrumentationInfo.packageName)) {
                        arrayList.remove(instrumentationInfo);
                        i6--;
                        z = true;
                    }
                    i6++;
                }
                if (z) {
                    objQueryInstrumentation_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
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
            stringBuffer3.append("#" + obj);
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
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (objQueryInstrumentation_bak != null) {
            return objQueryInstrumentation_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryInstrumentation_bak;
        }
    }

    public static List<InstrumentationInfo> queryInstrumentation_L(Object obj, String str, int i) {
        int i2 = 0;
        log("queryInstrumentation_L", obj, str, Integer.valueOf(i));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        List<InstrumentationInfo> listQueryInstrumentation_L_bak = queryInstrumentation_L_bak(obj, str, i);
        if (listQueryInstrumentation_L_bak != null) {
            try {
                if (isHookAntiDetection()) {
                    List<InstrumentationInfo> arrayList = new ArrayList<>(listQueryInstrumentation_L_bak);
                    boolean z = false;
                    while (i2 < arrayList.size()) {
                        InstrumentationInfo instrumentationInfo = arrayList.get(i2);
                        if (isPackageDetected(instrumentationInfo.packageName)) {
                            arrayList.remove(instrumentationInfo);
                            i2--;
                            z = true;
                        }
                        i2++;
                    }
                    if (z) {
                        listQueryInstrumentation_L_bak = arrayList;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (listQueryInstrumentation_L_bak != null) {
            return listQueryInstrumentation_L_bak;
        }
        try {
            return new ArrayList();
        } catch (Throwable th2) {
            th2.printStackTrace();
            return listQueryInstrumentation_L_bak;
        }
    }

    public static List<InstrumentationInfo> queryInstrumentation_L_bak(Object obj, String str, int i) {
        log("queryInstrumentation_L_bak", obj, str, Integer.valueOf(i));
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
        return queryInstrumentation_L_copy(obj, str, i);
    }

    public static List<InstrumentationInfo> queryInstrumentation_L_copy(Object obj, String str, int i) {
        log("queryInstrumentation_L_copy", obj, str, Integer.valueOf(i));
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
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object queryInstrumentation_bak(Object obj, String str, int i) {
        log("queryInstrumentation_bak", obj, str, Integer.valueOf(i));
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
        return queryInstrumentation_copy(obj, str, i);
    }

    public static Object queryInstrumentation_copy(Object obj, String str, int i) {
        log("queryInstrumentation_copy", obj, str, Integer.valueOf(i));
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentActivities(Object obj, Intent intent, String str, int i, int i2) {
        Object objQueryIntentActivities_bak = null;
        log("queryIntentActivities", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        Object obj2 = null;
        try {
            objQueryIntentActivities_bak = queryIntentActivities_bak(obj, intent, str, i, i2);
        } catch (Throwable th) {
            th = th;
        }
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryIntentActivities_bak, new Object[0]));
                int i13 = 0;
                boolean z = false;
                while (i13 < arrayList.size()) {
                    ResolveInfo resolveInfo = (ResolveInfo) arrayList.get(i13);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        arrayList.remove(resolveInfo);
                        i13--;
                        log("queryIntentActivities.已移除", obj, intent, componentInfo.packageName);
                        z = true;
                    }
                    i13++;
                }
                if (z) {
                    objQueryIntentActivities_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        if (objQueryIntentActivities_bak != null) {
            return objQueryIntentActivities_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th3) {
            th3.printStackTrace();
            return objQueryIntentActivities_bak;
        }
    }

    public static Object queryIntentActivities_2(Object obj, Intent intent, String str, int i, int i2) {
        Object objQueryIntentActivities_2_bak = null;
        log("queryIntentActivities_2", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object obj2 = null;
        try {
            objQueryIntentActivities_2_bak = queryIntentActivities_2_bak(obj, intent, str, i, i2);
        } catch (Throwable th) {
            th = th;
        }
        try {
            if (isHookAntiDetection()) {
                ArrayList arrayList = new ArrayList((List) objQueryIntentActivities_2_bak);
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ResolveInfo resolveInfo = (ResolveInfo) arrayList.get(i7);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        arrayList.remove(resolveInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryIntentActivities_2_bak = arrayList;
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
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
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
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
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return objQueryIntentActivities_2_bak;
    }

    public static Object queryIntentActivities_2_bak(Object obj, Intent intent, String str, int i, int i2) {
        try {
            log("queryIntentActivities_2_bak", obj);
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
        return queryIntentActivities_2_copy(obj, intent, str, i, i2);
    }

    public static Object queryIntentActivities_2_copy(Object obj, Intent intent, String str, int i, int i2) {
        try {
            log("queryIntentActivities_2_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static List<ResolveInfo> queryIntentActivities_L(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentActivities_L", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        List<ResolveInfo> listQueryIntentActivities_L_bak = queryIntentActivities_L_bak(obj, intent, str, i, i2);
        if (listQueryIntentActivities_L_bak != null) {
            try {
                if (isHookAntiDetection()) {
                    List<ResolveInfo> arrayList = new ArrayList<>(listQueryIntentActivities_L_bak);
                    int i7 = 0;
                    boolean z = false;
                    while (i7 < arrayList.size()) {
                        ResolveInfo resolveInfo = arrayList.get(i7);
                        ComponentInfo componentInfo = resolveInfo.activityInfo;
                        if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                            componentInfo = null;
                        }
                        if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                            arrayList.remove(resolveInfo);
                            i7--;
                            log("queryIntentActivities.已移除", obj, intent, componentInfo.packageName);
                            z = true;
                        }
                        i7++;
                    }
                    if (z) {
                        listQueryIntentActivities_L_bak = arrayList;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (listQueryIntentActivities_L_bak != null) {
            return listQueryIntentActivities_L_bak;
        }
        try {
            return new ArrayList();
        } catch (Throwable th2) {
            th2.printStackTrace();
            return listQueryIntentActivities_L_bak;
        }
    }

    public static List<ResolveInfo> queryIntentActivities_L_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentActivities_L_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryIntentActivities_L_copy(obj, intent, str, i, i2);
    }

    public static List<ResolveInfo> queryIntentActivities_L_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentActivities_L_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ResolveInfo> queryIntentActivities_S(Object obj, Intent intent, String str, int i, int i2, int i3) {
        log("queryIntentActivities_S", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<ResolveInfo> listQueryIntentActivities_S_bak = queryIntentActivities_S_bak(obj, intent, str, i, i2, i3);
        try {
            if (isHookAntiDetection()) {
                int i6 = 0;
                while (i6 < listQueryIntentActivities_S_bak.size()) {
                    ResolveInfo resolveInfo = listQueryIntentActivities_S_bak.get(i6);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        listQueryIntentActivities_S_bak.remove(resolveInfo);
                        i6--;
                        log("queryIntentActivities_S.已移除", obj, intent, componentInfo.packageName);
                    }
                    i6++;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (listQueryIntentActivities_S_bak != null) {
            return listQueryIntentActivities_S_bak;
        }
        try {
            return new ArrayList();
        } catch (Throwable th2) {
            th2.printStackTrace();
            return listQueryIntentActivities_S_bak;
        }
    }

    public static List<ResolveInfo> queryIntentActivities_S_bak(Object obj, Intent intent, String str, int i, int i2, int i3) {
        log("queryIntentActivities_S_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return queryIntentActivities_S_copy(obj, intent, str, i, i2, i3);
    }

    public static List<ResolveInfo> queryIntentActivities_S_copy(Object obj, Intent intent, String str, int i, int i2, int i3) {
        log("queryIntentActivities_S_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentActivities_bak(Object obj, Intent intent, String str, int i, int i2) {
        try {
            log("queryIntentActivities_bak", obj);
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
        return queryIntentActivities_copy(obj, intent, str, i, i2);
    }

    public static Object queryIntentActivities_copy(Object obj, Intent intent, String str, int i, int i2) {
        try {
            log("queryIntentActivities_copy", obj);
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentActivityOptions(Object obj, ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) {
        log("queryIntentActivityOptions", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object objQueryIntentActivityOptions_bak = queryIntentActivityOptions_bak(obj, componentName, intentArr, strArr, intent, str, i, i2);
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryIntentActivityOptions_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ResolveInfo resolveInfo = (ResolveInfo) arrayList.get(i7);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        arrayList.remove(resolveInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryIntentActivityOptions_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (objQueryIntentActivityOptions_bak != null) {
            return objQueryIntentActivityOptions_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryIntentActivityOptions_bak;
        }
    }

    public static List<ResolveInfo> queryIntentActivityOptions_L(Object obj, ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) {
        int i3 = 0;
        log("queryIntentActivityOptions_L", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        List<ResolveInfo> listQueryIntentActivityOptions_L_bak = queryIntentActivityOptions_L_bak(obj, componentName, intentArr, strArr, intent, str, i, i2);
        if (listQueryIntentActivityOptions_L_bak != null) {
            try {
                if (isHookAntiDetection()) {
                    List<ResolveInfo> arrayList = new ArrayList<>(listQueryIntentActivityOptions_L_bak);
                    boolean z = false;
                    while (i3 < arrayList.size()) {
                        ResolveInfo resolveInfo = arrayList.get(i3);
                        ComponentInfo componentInfo = resolveInfo.activityInfo;
                        if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                            componentInfo = null;
                        }
                        if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                            arrayList.remove(resolveInfo);
                            i3--;
                            z = true;
                        }
                        i3++;
                    }
                    if (z) {
                        listQueryIntentActivityOptions_L_bak = arrayList;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (listQueryIntentActivityOptions_L_bak != null) {
            return listQueryIntentActivityOptions_L_bak;
        }
        try {
            return new ArrayList();
        } catch (Throwable th2) {
            th2.printStackTrace();
            return listQueryIntentActivityOptions_L_bak;
        }
    }

    public static List<ResolveInfo> queryIntentActivityOptions_L_bak(Object obj, ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) {
        log("queryIntentActivityOptions_L_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryIntentActivityOptions_L_copy(obj, componentName, intentArr, strArr, intent, str, i, i2);
    }

    public static List<ResolveInfo> queryIntentActivityOptions_L_copy(Object obj, ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) {
        log("queryIntentActivityOptions_L_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentActivityOptions_bak(Object obj, ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) {
        log("queryIntentActivityOptions_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            stringBuffer3.append("#" + obj);
            stringBuffer3.toString();
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return queryIntentActivityOptions_copy(obj, componentName, intentArr, strArr, intent, str, i, i2);
    }

    public static Object queryIntentActivityOptions_copy(Object obj, ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) {
        log("queryIntentActivityOptions_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            stringBuffer3.append("#" + obj);
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
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentContentProviders(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentContentProviders", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object objQueryIntentContentProviders_bak = queryIntentContentProviders_bak(obj, intent, str, i, i2);
        try {
            if (isHookAntiDetection()) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryIntentContentProviders_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ResolveInfo resolveInfo = (ResolveInfo) arrayList.get(i7);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        arrayList.remove(resolveInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryIntentContentProviders_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (objQueryIntentContentProviders_bak != null) {
            return objQueryIntentContentProviders_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryIntentContentProviders_bak;
        }
    }

    public static List<ResolveInfo> queryIntentContentProviders_L(Object obj, Intent intent, String str, int i, int i2) {
        int i3 = 0;
        log("queryIntentContentProviders_L", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
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
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        List<ResolveInfo> listQueryIntentContentProviders_L_bak = queryIntentContentProviders_L_bak(obj, intent, str, i, i2);
        if (listQueryIntentContentProviders_L_bak != null) {
            try {
                if (isHookAntiDetection()) {
                    List<ResolveInfo> arrayList = new ArrayList<>(listQueryIntentContentProviders_L_bak);
                    boolean z = false;
                    while (i3 < arrayList.size()) {
                        ResolveInfo resolveInfo = arrayList.get(i3);
                        ComponentInfo componentInfo = resolveInfo.activityInfo;
                        if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                            componentInfo = null;
                        }
                        if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                            arrayList.remove(resolveInfo);
                            i3--;
                            z = true;
                        }
                        i3++;
                    }
                    if (z) {
                        listQueryIntentContentProviders_L_bak = arrayList;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (listQueryIntentContentProviders_L_bak != null) {
            return listQueryIntentContentProviders_L_bak;
        }
        try {
            return new ArrayList();
        } catch (Throwable th2) {
            th2.printStackTrace();
            return listQueryIntentContentProviders_L_bak;
        }
    }

    public static List<ResolveInfo> queryIntentContentProviders_L_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentContentProviders_L_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryIntentContentProviders_L_copy(obj, intent, str, i, i2);
    }

    public static List<ResolveInfo> queryIntentContentProviders_L_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentContentProviders_L_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i3 = 0; i3 < 100; i3 = i3 + 1 + 1) {
            }
            for (int i4 = 0; i4 < 100; i4 = i4 + 1 + 1) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentContentProviders_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentContentProviders_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
        return queryIntentContentProviders_copy(obj, intent, str, i, i2);
    }

    public static Object queryIntentContentProviders_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentContentProviders_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentReceivers(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentReceivers", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object objQueryIntentReceivers_bak = queryIntentReceivers_bak(obj, intent, str, i, i2);
        try {
            if (isHookAntiDetection() && objQueryIntentReceivers_bak != null) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryIntentReceivers_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ResolveInfo resolveInfo = (ResolveInfo) arrayList.get(i7);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        arrayList.remove(resolveInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryIntentReceivers_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (objQueryIntentReceivers_bak != null) {
            return objQueryIntentReceivers_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryIntentReceivers_bak;
        }
    }

    public static Object queryIntentReceivers_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentReceivers_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
        return queryIntentReceivers_copy(obj, intent, str, i, i2);
    }

    public static Object queryIntentReceivers_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentReceivers_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
            stringBuffer2.append("#" + obj);
            stringBuffer2.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
            }
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static Object queryIntentServices(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentServices", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        Object objQueryIntentServices_bak = queryIntentServices_bak(obj, intent, str, i, i2);
        try {
            if (isHookAntiDetection() && objQueryIntentServices_bak != null) {
                Method declaredMethod = parceledListSliceClass.getDeclaredMethod("getList", new Class[0]);
                declaredMethod.setAccessible(true);
                ArrayList arrayList = new ArrayList((List) declaredMethod.invoke(objQueryIntentServices_bak, new Object[0]));
                int i7 = 0;
                boolean z = false;
                while (i7 < arrayList.size()) {
                    ResolveInfo resolveInfo = (ResolveInfo) arrayList.get(i7);
                    ComponentInfo componentInfo = resolveInfo.activityInfo;
                    if (componentInfo == null && (componentInfo = resolveInfo.serviceInfo) == null && (componentInfo = resolveInfo.providerInfo) == null) {
                        componentInfo = null;
                    }
                    if (componentInfo != null && isPackageDetected(componentInfo.packageName)) {
                        arrayList.remove(resolveInfo);
                        i7--;
                        z = true;
                    }
                    i7++;
                }
                if (z) {
                    objQueryIntentServices_bak = parceledListSliceClass.getConstructor(List.class).newInstance(arrayList);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (objQueryIntentServices_bak != null) {
            return objQueryIntentServices_bak;
        }
        try {
            return parceledListSliceClass.getConstructor(List.class).newInstance(new ArrayList());
        } catch (Throwable th2) {
            th2.printStackTrace();
            return objQueryIntentServices_bak;
        }
    }

    public static Object queryIntentServices_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentServices_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
        return queryIntentServices_copy(obj, intent, str, i, i2);
    }

    public static Object queryIntentServices_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("queryIntentServices_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
            stringBuffer4.append("#" + obj);
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
            stringBuffer5.append("#" + obj);
            stringBuffer5.toString();
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
            }
            return null;
        } catch (Exception e5) {
            e5.printStackTrace();
            return null;
        }
    }

    public static ResolveInfo resolveIntent(Object obj, Intent intent, String str, int i, int i2) {
        log("resolveIntent", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        if (isHookAntiDetection()) {
            if (isPackageDetected(intent.getPackage())) {
                intent.setPackage("" + System.currentTimeMillis());
            }
            ComponentName component = intent.getComponent();
            if (component != null && isPackageDetected(component.getPackageName())) {
                intent.setComponent(new ComponentName("" + System.currentTimeMillis(), component.getClassName()));
            }
        }
        return resolveIntent_bak(obj, intent, str, i, i2);
    }

    public static ResolveInfo resolveIntent_S(Object obj, Intent intent, String str, int i, int i2, int i3, boolean z, int i4) {
        log("resolveIntent_S", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z), Integer.valueOf(i4));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
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
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
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
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (isHookAntiDetection()) {
            if (isPackageDetected(intent.getPackage())) {
                intent.setPackage("" + System.currentTimeMillis());
            }
            ComponentName component = intent.getComponent();
            if (component != null && isPackageDetected(component.getPackageName())) {
                intent.setComponent(new ComponentName("" + System.currentTimeMillis(), component.getClassName()));
            }
        }
        try {
            return resolveIntent_S_bak(obj, intent, str, i, i2, i3, z, i4);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static ResolveInfo resolveIntent_S_bak(Object obj, Intent intent, String str, int i, int i2, int i3, boolean z, int i4) {
        log("resolveIntent_S_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z), Integer.valueOf(i4));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
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
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
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
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
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
            for (int i11 = 0; i11 < 100; i11 = i11 + 1 + 1) {
            }
            for (int i12 = 0; i12 < 100; i12 = i12 + 1 + 1) {
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
            for (int i13 = 0; i13 < 100; i13 = i13 + 1 + 1) {
            }
            for (int i14 = 0; i14 < 100; i14 = i14 + 1 + 1) {
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
        return resolveIntent_S_copy(obj, intent, str, i, i2, i3, z, i4);
    }

    public static ResolveInfo resolveIntent_S_copy(Object obj, Intent intent, String str, int i, int i2, int i3, boolean z, int i4) {
        log("resolveIntent_S_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z), Integer.valueOf(i4));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
            stringBuffer.toString();
            for (int i5 = 0; i5 < 100; i5 = i5 + 1 + 1) {
            }
            for (int i6 = 0; i6 < 100; i6 = i6 + 1 + 1) {
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
            for (int i7 = 0; i7 < 100; i7 = i7 + 1 + 1) {
            }
            for (int i8 = 0; i8 < 100; i8 = i8 + 1 + 1) {
            }
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static ResolveInfo resolveIntent_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("resolveIntent_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
        return resolveIntent_copy(obj, intent, str, i, i2);
    }

    public static ResolveInfo resolveIntent_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("resolveIntent_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static ResolveInfo resolveService(Object obj, Intent intent, String str, int i, int i2) {
        log("resolveService", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#");
            stringBuffer.append("#" + obj);
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
        if (isHookAntiDetection()) {
            if (isPackageDetected(intent.getPackage())) {
                intent.setPackage("" + System.currentTimeMillis());
            }
            ComponentName component = intent.getComponent();
            if (component != null && isPackageDetected(component.getPackageName())) {
                intent.setComponent(new ComponentName("" + System.currentTimeMillis(), component.getClassName()));
            }
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
        try {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#");
            stringBuffer4.append("#" + obj);
            stringBuffer4.toString();
            for (int i9 = 0; i9 < 100; i9 = i9 + 1 + 1) {
            }
            for (int i10 = 0; i10 < 100; i10 = i10 + 1 + 1) {
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            return resolveService_bak(obj, intent, str, i, i2);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static ResolveInfo resolveService_bak(Object obj, Intent intent, String str, int i, int i2) {
        log("resolveService_bak", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
        return resolveService_copy(obj, intent, str, i, i2);
    }

    public static ResolveInfo resolveService_copy(Object obj, Intent intent, String str, int i, int i2) {
        log("resolveService_copy", obj, intent, str, Integer.valueOf(i), Integer.valueOf(i2));
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
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }
}
