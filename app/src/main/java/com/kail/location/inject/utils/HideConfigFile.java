package com.kail.location.inject.utils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 隐藏配置的"文件通道"。
 *
 * 背景：在 SELinux Enforcing（Android S+/14+）的设备上，普通应用（untrusted_app）
 * 无法在 service_manager 里 find/getService 动态注册的 oem_* 服务，导致隐藏配置
 * 只能走 IHideRootManager binder 下发时目标进程读不到。位置模拟能工作是因为它有一个
 * 控制文件通道（RootLocationControl 直接读文件），隐藏没有，所以这里给它补上等价通道。
 *
 * 结构：Kail 应用（有 root）把隐藏配置写到 {@link #PATH}（world-readable），注入进
 * 目标 app 进程的 Hook（HideRootServiceManager / RootHideHook）直接读该文件判断开关，
 * 完全绕开 service_manager 的 SELinux 拦截。与 binder 通道并存：能解析 binder 时仍
 * 走 binder（带授权 gating），解析不到时退到文件通道。
 */
public final class HideConfigFile {

    /** 配置文件名：与位置控制的 location_control_*.txt 同目录，目标进程可读。 */
    public static final String PATH = "/data/kail-loc/hide_config.txt";

    private static final long CACHE_TTL_MS = 300L;

    private static volatile long lastReadMs;
    private static volatile boolean cachedLoaded;
    private static volatile boolean cachedEnabled;
    private static volatile boolean cachedHideAppList;
    private static volatile List<String> cachedPackages = new ArrayList<>();

    public static final class Config {
        public boolean enabled;
        public boolean hideAppList;
        public List<String> packages = new ArrayList<>();
    }

    private HideConfigFile() {
    }

    public static boolean isEnabled() {
        return read().enabled;
    }

    public static boolean isHideAppListEnabled() {
        return read().hideAppList;
    }

    /** 空列表返回 null，与 IHideRootManager.getHiddenPackages() 的空语义对齐。 */
    public static List<String> getPackages() {
        List<String> pkgs = read().packages;
        return pkgs.isEmpty() ? null : pkgs;
    }

    public static Config read() {
        long now = System.currentTimeMillis();
        if (cachedLoaded && now - lastReadMs < CACHE_TTL_MS) {
            Config c = new Config();
            c.enabled = cachedEnabled;
            c.hideAppList = cachedHideAppList;
            c.packages = new ArrayList<String>(cachedPackages);
            return c;
        }
        Config cfg = parseFile();
        cachedEnabled = cfg.enabled;
        cachedHideAppList = cfg.hideAppList;
        cachedPackages = new ArrayList<String>(cfg.packages);
        cachedLoaded = true;
        lastReadMs = now;
        return cfg;
    }

    private static Config parseFile() {
        // Provider 通道优先（App 的 LocationShmProvider.get_config）。
        String text = ConfigProviderChannel.get(LocationShm.PROVIDER_KEY_HIDE_CONFIG);
        if (text == null) {
            text = readFileText();
        }
        if (text == null) {
            return new Config();
        }
        return parse(text);
    }

    /** 文件通道兜底读取。 */
    private static String readFileText() {
        // 注意：不能用 new File(...)/new FileReader(...) 读配置——
        // File 构造器会被 RootHideHook 挂钩，而 hook 判断开关又走到这里读配置，
        // 造成无限递归（StackOverflowError）。FileInputStream(String) 不在钩子范围内。
        byte[] buf = new byte[2048];
        int n;
        try {
            FileInputStream fis = new FileInputStream(PATH);
            try {
                n = fis.read(buf);
            } finally {
                fis.close();
            }
        } catch (Throwable t) {
            // 文件不存在/读不到就当没启用，保持安静。
            return null;
        }
        if (n <= 0) {
            return null;
        }
        return new String(buf, 0, n, StandardCharsets.UTF_8);
    }

    private static Config parse(String text) {
        Config cfg = new Config();
        String[] lines = text.split("\n");
        for (String line : lines) {
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if ("enabled".equals(key)) {
                cfg.enabled = "1".equals(value) || "true".equalsIgnoreCase(value);
            } else if ("hide_app_list".equals(key)) {
                cfg.hideAppList = "1".equals(value) || "true".equalsIgnoreCase(value);
            } else if ("packages".equals(key)) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    String pkg = part.trim();
                    if (pkg.length() > 0 && !cfg.packages.contains(pkg)) {
                        cfg.packages.add(pkg);
                    }
                }
            }
        }
        return cfg;
    }
}