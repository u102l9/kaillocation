package com.kail.location.inject.utils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 独立模拟"目标应用白名单"的文件通道。
 *
 * 背景：在 SELinux Enforcing（Android S+/14+）的设备上，system_server 动态注册
 * oem_location / oem_wifi 服务被拒绝（`avc: denied { add }`），App 进程 find
 * 这些服务也被拦截，导致 IMockLocationManager.setAllowMockPackages /
 * IMockWifiManager.setAllowMockPackages 两个 binder 通道失效，独立模拟的白名单
 * 永远无法下发，模拟对所有应用无差别生效。
 *
 * 结构：Kail 应用（有 root）把白名单写到 {@link #PATH}（world-readable），注入
 * 进 system_server 的 RootLocationControl 线程轮询该文件并直接调用
 * {@link MockLocationHookManager#setAllowMockPackages} 与
 * {@link MockWifiConfigManager#setAllowMockPackages}，完全绕开 service_manager 的
 * SELinux 拦截。与 binder 通道并存：binder 可用时仍走 binder（带授权 gating），
 * 解析不到时文件通道兜底。
 */
public final class AllowMockPackagesConfigFile {

    /** 配置文件名：与位置控制的 location_control_*.txt 同目录，system_server 可读。 */
    public static final String PATH = "/data/kail-loc/allow_mock_packages.txt";

    private static final long CACHE_TTL_MS = 300L;

    private static volatile long lastReadMs;
    private static volatile boolean cachedLoaded;
    private static volatile boolean cachedEnabled;
    private static volatile List<String> cachedPackages = new ArrayList<>();

    public static final class Config {
        public boolean enabled;
        public List<String> packages = new ArrayList<>();
    }

    private AllowMockPackagesConfigFile() {
    }

    public static Config read() {
        long now = System.currentTimeMillis();
        if (cachedLoaded && now - lastReadMs < CACHE_TTL_MS) {
            Config c = new Config();
            c.enabled = cachedEnabled;
            c.packages = new ArrayList<String>(cachedPackages);
            return c;
        }
        Config cfg = parseFile();
        cachedEnabled = cfg.enabled;
        cachedPackages = new ArrayList<String>(cfg.packages);
        cachedLoaded = true;
        lastReadMs = now;
        return cfg;
    }

    private static Config parseFile() {
        // Provider 通道优先（App 的 LocationShmProvider.get_config）。
        String text = ConfigProviderChannel.get(LocationShm.PROVIDER_KEY_ALLOW_CONFIG);
        if (text == null) {
            // 文件通道兜底：与 HideConfigFile 保持一致，避免 File/FileReader 构造器
            // （可能被 Hook）引起的递归风险，用 FileInputStream(String) 直读。
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
                // 文件不存在/读不到：保持未启用（mock 全部应用），保持安静。
                return new Config();
            }
            if (n <= 0) {
                return new Config();
            }
            text = new String(buf, 0, n, StandardCharsets.UTF_8);
        }
        return parse(text);
    }

    /** 解析与文件通道相同格式的配置文本（Provider 通道复用）。 */
    public static Config parse(String text) {
        Config cfg = new Config();
        if (text == null || text.isEmpty()) {
            return cfg;
        }
        String[] lines = text.split("\n");
        for (String line : lines) {
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if ("enabled".equals(key)) {
                cfg.enabled = "1".equals(value) || "true".equalsIgnoreCase(value);
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
