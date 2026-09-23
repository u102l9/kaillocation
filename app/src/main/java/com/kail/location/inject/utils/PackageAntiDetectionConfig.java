package com.kail.location.inject.utils;

import com.kail.location.inject.fakelocation.InjectDex;
import java.util.List;
import com.kail.location.inject.fakelocation.hook.system.PackageManagerServiceHook;

public class PackageAntiDetectionConfig {
    private static boolean packageManagerHookEnabled;
    private static boolean packageFilterEnabled;
    private static boolean packageVisibilityFilterEnabled;
    private static List<String> targetPackages;
    private static List<String> detectedPackages;
    private static final Object configLock = new Object();
    private static List<String> scopedPackageRules;

    public static List<String> getTargetPackages() {
        List<String> fileTargets = AntiDetectConfigFile.getTargetPackages();
        if (fileTargets != null) {
            return fileTargets;
        }
        return targetPackages;
    }

    public static List<String> getDetectedPackages() {
        List<String> fileDetected = AntiDetectConfigFile.getDetectedPackages();
        if (fileDetected != null) {
            return fileDetected;
        }
        return detectedPackages;
    }

    public static List<String> getScopedPackageRules() {
        synchronized (configLock) {
            return scopedPackageRules;
        }
    }

    private static boolean hasNoPackageFilters() {
        List<String> list;
        List<String> list2 = targetPackages;
        return (list2 == null || list2.isEmpty()) && ((list = scopedPackageRules) == null || list.isEmpty());
    }

    public static boolean isPackageAllowedForPidUid(int pid, int uid) {
        return isPackageAllowed(CallingProcessUtils.getPackageNameForPidOrUid(pid, uid));
    }

    public static boolean isPackageAllowed(String packageName) {
        if (packageName == null) {
            return false;
        }
        synchronized (configLock) {
            if (hasNoPackageFilters()) {
                return true;
            }
            List<String> list = scopedPackageRules;
            if (list != null && !list.isEmpty() && !ScopedListFilter.isAllowed(scopedPackageRules, packageName, "j")) {
                return false;
            }
            List<String> list2 = targetPackages;
            if (list2 == null || list2.isEmpty()) {
                return true;
            }
            return targetPackages.contains(packageName);
        }
    }

    public static boolean isPackageManagerHookEnabled() {
        // Enforcing 下 binder 可能推不进，退到文件通道。
        return packageManagerHookEnabled || AntiDetectConfigFile.isHookEnabled();
    }

    public static boolean isPackageVisibilityFilteringEnabled() {
        if (packageFilterEnabled && packageVisibilityFilterEnabled) {
            return true;
        }
        return AntiDetectConfigFile.isFilterEnabled() && AntiDetectConfigFile.isVisibilityFilterEnabled();
    }

    public static boolean isDetectedPackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        synchronized (configLock) {
            List<String> list = getDetectedPackages();
            if (list != null && !list.isEmpty()) {
                return list.contains(packageName);
            }
            return false;
        }
    }

    public static boolean isPackageFilterEnabled() {
        return packageFilterEnabled || AntiDetectConfigFile.isFilterEnabled();
    }

    public static void setTargetPackages(List<String> list) {
        synchronized (configLock) {
            targetPackages = list;
        }
    }

    public static void setDetectedPackages(List<String> list) {
        synchronized (configLock) {
            detectedPackages = list;
        }
    }

    public static void setPackageManagerHookEnabled(boolean enabled) {
        packageManagerHookEnabled = enabled;
        if (enabled) {
            PackageManagerServiceHook.hook(InjectDex.getApplicationContext().getClassLoader());
        }
    }

    public static void setPackageVisibilityFilterEnabled(boolean enabled) {
        packageVisibilityFilterEnabled = enabled;
    }

    public static void setScopedPackageRules(List<String> list) {
        synchronized (configLock) {
            scopedPackageRules = list;
        }
    }

    public static void setPackageFilterEnabled(boolean enabled) {
        packageFilterEnabled = enabled;
    }
}
