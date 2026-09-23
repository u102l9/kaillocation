package com.kail.location.inject.utils;

import com.kail.location.inject.fakelocation.model.MockWifiNetwork;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * WiFi 模拟配置的"文件通道"。
 *
 * 背景：SELinux Enforcing 下 oem_wifi binder 注册/查找均被拒绝，MockWifiManagerService
 * 的 startMockWifi / setMockWifiNetworks / stopMockWifi 都无法到达，WiFi 模拟失效。
 * 这里与 {@link AllowMockPackagesConfigFile} / {@link HideConfigFile} 一样补上文件通道：
 * Kail 应用（有 root）把模拟 WiFi 网络列表写到 {@link #PATH}，注入进 system_server 的
 * RootLocationControl 轮询该文件后直接设置 {@link MockWifiConfigManager}。
 *
 * 文件格式（每个网络一个块，块之间空行分隔；第一块可带 enabled 标记）：
 * <pre>
 * enabled=1
 * ssid=MyHome
 * bssid=AA:BB:CC:DD:EE:FF
 * rssi=-50
 * link_speed=65
 * frequency=2412
 * capabilities=[WPA-PSK-CCMP]
 *
 * ssid=Other
 * ...
 * </pre>
 */
public final class WifiMockConfigFile {

    /** 配置文件名：与位置控制的 location_control_*.txt 同目录，system_server 可读。 */
    public static final String PATH = "/data/kail-loc/mock_wifi.txt";

    private static final long CACHE_TTL_MS = 300L;

    private static volatile long lastReadMs;
    private static volatile boolean cachedLoaded;
    private static volatile boolean cachedEnabled;
    private static volatile List<MockWifiNetwork> cachedNetworks = new ArrayList<>();

    public static final class Config {
        public boolean enabled;
        public List<MockWifiNetwork> networks = new ArrayList<>();
    }

    private WifiMockConfigFile() {
    }

    public static Config read() {
        long now = System.currentTimeMillis();
        if (cachedLoaded && now - lastReadMs < CACHE_TTL_MS) {
            Config c = new Config();
            c.enabled = cachedEnabled;
            c.networks = new ArrayList<MockWifiNetwork>(cachedNetworks);
            return c;
        }
        Config cfg = parseFile();
        cachedEnabled = cfg.enabled;
        cachedNetworks = new ArrayList<MockWifiNetwork>(cfg.networks);
        cachedLoaded = true;
        lastReadMs = now;
        return cfg;
    }

    private static Config parseFile() {
        // 与 HideConfigFile 一致：用 FileInputStream(String) 直读，避免 File 构造器
        // 可能被 Hook 的递归风险。
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
        return parse(new String(buf, 0, n, StandardCharsets.UTF_8));
    }

    /** 解析与文件通道相同格式的配置文本（Provider 通道复用）。 */
    public static Config parse(String text) {
        Config cfg = new Config();
        if (text == null || text.isEmpty()) {
            return cfg;
        }
        String[] blocks = text.split("\n\n");
        for (String block : blocks) {
            String[] lines = block.split("\n");
            String ssid = null;
            String bssid = null;
            int rssi = -50;
            int linkSpeed = 65;
            int frequency = 2412;
            String capabilities = "";
            boolean hasNetworkField = false;
            for (String line : lines) {
                int idx = line.indexOf('=');
                if (idx <= 0) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if ("enabled".equals(key)) {
                    cfg.enabled = "1".equals(value) || "true".equalsIgnoreCase(value);
                } else if ("ssid".equals(key)) {
                    ssid = value; hasNetworkField = true;
                } else if ("bssid".equals(key)) {
                    bssid = value; hasNetworkField = true;
                } else if ("rssi".equals(key)) {
                    rssi = parseInt(value, rssi); hasNetworkField = true;
                } else if ("link_speed".equals(key)) {
                    linkSpeed = parseInt(value, linkSpeed); hasNetworkField = true;
                } else if ("frequency".equals(key)) {
                    frequency = parseInt(value, frequency); hasNetworkField = true;
                } else if ("capabilities".equals(key)) {
                    capabilities = value; hasNetworkField = true;
                }
            }
            if (hasNetworkField && ssid != null && bssid != null) {
                cfg.networks.add(new MockWifiNetwork("WIFI", ssid, bssid, rssi, linkSpeed, frequency, capabilities));
            }
        }
        return cfg;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Throwable t) {
            return fallback;
        }
    }
}
