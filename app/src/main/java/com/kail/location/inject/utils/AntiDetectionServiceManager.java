package com.kail.location.inject.utils;

import java.util.List;

/**
 * 目标进程读取反检测 / "隐藏应用列表" 配置的入口。
 *
 * <p>{@code oem_security} binder 在本机被 SELinux 拦截，已弃用；配置统一走
 * {@link AntiDetectConfigFile}（Provider 优先 + 文件兜底）。
 */
public class AntiDetectionServiceManager {

    private static final class Holder {
        static AntiDetectionServiceManager instance = new AntiDetectionServiceManager();
    }

    private AntiDetectionServiceManager() {
    }

    public static AntiDetectionServiceManager getInstance() {
        return Holder.instance;
    }

    public List<String> getHookTargetPackages() {
        return AntiDetectConfigFile.getTargetPackages();
    }

    /**
     * 要隐藏的“文件名片段”。
     *
     * <p>不能复用 {@link AntiDetectConfigFile#getDetectedPackages()}：那是**包名**（用于
     * PackageManagerServiceHook 过滤应用列表）。一旦被 RuntimeAntiDetectionHook 的
     * File.exists/list 钩子当成路径片段匹配，就会把目标应用自己的文件（路径里天然含
     * 自己的包名）也一起藏掉，导致它连自己的 APK 都读不到、启动即 ClassNotFoundException。
     *
     * <p>Root 文件隐藏由 {@link RootHideHook}（路径重定向）与 LAntiDetect（native）负责，
     * 这里不再返回包名，返回 null 即让这些 Java 文件钩子成为 no-op。
     */
    public List<String> getHiddenFileNames() {
        return null;
    }

    public List<String> getHookMethodRules() {
        return null;
    }

    public boolean isAntiDetectionEnabled() {
        return AntiDetectConfigFile.isHookEnabled();
    }

    public boolean isFileNameHidingEnabled() {
        return AntiDetectConfigFile.isVisibilityFilterEnabled();
    }

    public boolean isHookRulesEnabled() {
        return AntiDetectConfigFile.isFilterEnabled();
    }
}
