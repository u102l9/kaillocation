package com.kail.location.inject.utils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 反检测 / "隐藏应用列表" 配置的"文件通道"。
 *
 * 背景与 HideConfigFile 相同：在 SELinux Enforcing 设备上，Kail 应用无法 find
 * oem_security binder，导致 PackageAntiDetectionConfig（位于注入的 system_server 内）
 * 只能靠 binder 下发配置而可能收不到。这里补一个文件通道：Kail 用 root 把配置写进
 * {@link #PATH}（world-readable），system_server 内的 PackageAntiDetectionConfig 读它。
 *
 * 与 HideConfigFile 的区别：本文件被 system_server 内的钩子读取（读的是普通文件，
 * 不走挂钩的 File 构造器，但为稳妥仍用 FileInputStream(String) 避免与 RootHideHook
 * 冲突）。
 */
public final class AntiDetectConfigFile {

    private static final String PATH = "/data/kail-loc/antidetect_config.txt";
    private static final long CACHE_TTL_MS = 300L;

    private static volatile long lastReadMs;
    private static volatile boolean cachedLoaded;
    private static volatile boolean cachedHookEnabled;
    private static volatile boolean cachedVisibilityFilterEnabled;
    private static volatile boolean cachedFilterEnabled;
    private static volatile List<String> cachedDetected = new ArrayList<>();
    private static volatile List<String> cachedTargets = new ArrayList<>();

    public static final class Config {
        public boolean hookEnabled;
        public boolean visibilityFilterEnabled;
        public boolean filterEnabled;
        public List<String> detectedPackages = new ArrayList<>();
        public List<String> targetPackages = new ArrayList<>();
    }

    private AntiDetectConfigFile() {
    }

    public static boolean isHookEnabled() {
        return read().hookEnabled;
    }

    public static boolean isVisibilityFilterEnabled() {
        return read().visibilityFilterEnabled;
    }

    public static boolean isFilterEnabled() {
        return read().filterEnabled;
    }

    /** 要"从结果里过滤掉"的已检测包（空则 null，语义与 binder 一致）。 */
    public static List<String> getDetectedPackages() {
        List<String> list = read().detectedPackages;
        return list.isEmpty() ? null : list;
    }

    public static List<String> getTargetPackages() {
        List<String> list = read().targetPackages;
        return list.isEmpty() ? null : list;
    }

    public static Config read() {
        long now = System.currentTimeMillis();
        if (cachedLoaded && now - lastReadMs < CACHE_TTL_MS) {
            Config c = new Config();
            c.hookEnabled = cachedHookEnabled;
            c.visibilityFilterEnabled = cachedVisibilityFilterEnabled;
            c.filterEnabled = cachedFilterEnabled;
            c.detectedPackages = new ArrayList<String>(cachedDetected);
            c.targetPackages = new ArrayList<String>(cachedTargets);
            return c;
        }
        Config cfg = parseFile();
        cachedHookEnabled = cfg.hookEnabled;
        cachedVisibilityFilterEnabled = cfg.visibilityFilterEnabled;
        cachedFilterEnabled = cfg.filterEnabled;
        cachedDetected = new ArrayList<String>(cfg.detectedPackages);
        cachedTargets = new ArrayList<String>(cfg.targetPackages);
        cachedLoaded = true;
        lastReadMs = now;
        return cfg;
    }

    private static Config parseFile() {
        // Provider 通道优先（App 的 LocationShmProvider.get_config）。
        String text = ConfigProviderChannel.get(LocationShm.PROVIDER_KEY_ANTIDETECT_CONFIG);
        if (text == null) {
            // 文件通道兜底：与 HideConfigFile 相同，用 FileInputStream(String)
            // 绕开 RootHideHook 对 File 构造器的挂钩，避免递归。
            byte[] buf = new byte[4096];
            int n;
            try {
                FileInputStream fis = new FileInputStream(PATH);
                try {
                    n = fis.read(buf);
                } finally {
                    fis.close();
                }
            } catch (Throwable t) {
                return new Config();
            }
            if (n <= 0) {
                return new Config();
            }
            text = new String(buf, 0, n, StandardCharsets.UTF_8);
        }
        Config cfg = new Config();
        for (String line : text.split("\n")) {
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if ("hook_enabled".equals(key)) {
                cfg.hookEnabled = "1".equals(value) || "true".equalsIgnoreCase(value);
            } else if ("visibility_filter".equals(key)) {
                cfg.visibilityFilterEnabled = "1".equals(value) || "true".equalsIgnoreCase(value);
            } else if ("filter_enabled".equals(key)) {
                cfg.filterEnabled = "1".equals(value) || "true".equalsIgnoreCase(value);
            } else if ("detected_packages".equals(key)) {
                addAll(cfg.detectedPackages, value);
            } else if ("target_packages".equals(key)) {
                addAll(cfg.targetPackages, value);
            }
        }
        return cfg;
    }

    private static void addAll(List<String> out, String csv) {
        for (String part : csv.split(",")) {
            String pkg = part.trim();
            if (pkg.length() > 0 && !out.contains(pkg)) {
                out.add(pkg);
            }
        }
    }
}
