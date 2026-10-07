package com.kail.location.inject.utils;

import android.app.ActivityManager;
import android.os.Binder;
import com.kail.location.inject.fakelocation.InjectDex;
import java.util.List;

public class CallingProcessUtils {
    private static ActivityManager cachedActivityManager;
    private static List<ActivityManager.RunningAppProcessInfo> cachedRunningProcesses;
    private static long processCacheTimeMillis;

    public static String getPackageNameForPid(int pid) {
        return getPackageNameForPidOrUid(pid, -1);
    }

    public static String getPackageNameForPidOrUid(int pid, int uid) {
        if (uid != -1) {
            String[] packagesForUid = new String[0];
            try {
                packagesForUid = InjectDex.getApplicationContext().getPackageManager().getPackagesForUid(uid);
            } catch (Throwable th) {
                th.printStackTrace();
            }
            if (packagesForUid != null && packagesForUid.length > 0) {
                return packagesForUid[0];
            }
        }
        String processName = getProcessNameForPidOrUid(pid, uid);
        return (processName == null || !processName.contains(":")) ? processName : processName.split(":")[0];
    }

    public static String getProcessNameForPidOrUid(int pid, int uid) {
        try {
            if (cachedRunningProcesses == null || System.currentTimeMillis() - processCacheTimeMillis > 5000) {
                if (cachedActivityManager == null) {
                    cachedActivityManager = (ActivityManager) InjectDex.getApplicationContext().getSystemService("activity");
                }
                cachedRunningProcesses = cachedActivityManager.getRunningAppProcesses();
                processCacheTimeMillis = System.currentTimeMillis();
            }
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = cachedRunningProcesses;
            if (runningProcesses == null) {
                return null;
            }
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.pid == pid || (pid == -1 && processInfo.uid == uid)) {
                    return processInfo.processName;
                }
            }
            return null;
        } catch (Throwable unused) {
            return null;
        }
    }

    public static boolean isCallingShellUid() {
        return Binder.getCallingUid() == 2000;
    }

    public static boolean isCallingSystemUid() {
        return Binder.getCallingUid() < 10000;
    }

    public static boolean isSystemUid(int uid) {
        return uid < 10000;
    }
}
