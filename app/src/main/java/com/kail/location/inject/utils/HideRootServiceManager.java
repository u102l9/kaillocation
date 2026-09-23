package com.kail.location.inject.utils;

import java.util.List;

/**
 * 目标进程读取"Root与应用隐藏"配置的入口。
 *
 * <p>{@code oem_integrity} binder 在本机被 SELinux 拦截（addService/find 均拒），
 * 已弃用；配置统一走 {@link HideConfigFile}（Provider 优先 + 文件兜底）。
 */
public class HideRootServiceManager {

    private static final class Holder {
        static HideRootServiceManager instance = new HideRootServiceManager();
    }

    private HideRootServiceManager() {
    }

    public static HideRootServiceManager getInstance() {
        return Holder.instance;
    }

    public List<String> getHiddenPackages() {
        return HideConfigFile.getPackages();
    }

    public List<String> getHiddenProcesses() {
        return null;
    }

    public boolean isHideRootEnabled() {
        return HideConfigFile.isEnabled();
    }

    public boolean isHideAppListEnabled() {
        return HideConfigFile.isHideAppListEnabled();
    }

    /** 配置由 App 写 {@link HideConfigFile}（Provider + 文件）驱动，此处无需处理。 */
    public void disableHideRoot() {
    }
}
