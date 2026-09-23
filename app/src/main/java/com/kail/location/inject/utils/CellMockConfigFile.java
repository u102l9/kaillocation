package com.kail.location.inject.utils;

import android.os.Parcel;
import com.kail.location.inject.fakelocation.model.CellTowerInfo;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基站（Cell）模拟配置的"文件通道"。
 *
 * 背景：SELinux Enforcing 下 oem_location binder 注册/查找均被拒绝，
 * MockLocationManagerService 的 setMockCells 无法到达，基站模拟失效。
 * 这里与 {@link AllowMockPackagesConfigFile} / {@link HideConfigFile} 一样补上
 * 文件通道：Kail 应用（有 root）把模拟小区列表写到 {@link #PATH}，注入进
 * system_server 的 RootLocationControl 轮询该文件后直接设置
 * {@link MockLocationHookManager#setMockCells}。
 *
 * 文件格式（每个小区一个块，块之间空行分隔；第一块可带 enabled 标记）：
 * <pre>
 * enabled=1
 * radio_type=LTE
 * mcc=460
 * mnc=0
 * lac=2084
 * psc=0
 * cell_id=123456
 * lat=35.1
 * lng=103.2
 * accuracy=1000
 *
 * ...
 * </pre>
 */
public final class CellMockConfigFile {

    /** 配置文件名：与位置控制的 location_control_*.txt 同目录，system_server 可读。 */
    public static final String PATH = "/data/kail-loc/mock_cell.txt";

    private static final long CACHE_TTL_MS = 300L;

    private static volatile long lastReadMs;
    private static volatile boolean cachedLoaded;
    private static volatile boolean cachedEnabled;
    private static volatile List<CellTowerInfo> cachedTowers = new ArrayList<>();

    public static final class Config {
        public boolean enabled;
        public List<CellTowerInfo> towers = new ArrayList<>();
    }

    private CellMockConfigFile() {
    }

    public static Config read() {
        long now = System.currentTimeMillis();
        if (cachedLoaded && now - lastReadMs < CACHE_TTL_MS) {
            Config c = new Config();
            c.enabled = cachedEnabled;
            c.towers = new ArrayList<CellTowerInfo>(cachedTowers);
            return c;
        }
        Config cfg = parseFile();
        cachedEnabled = cfg.enabled;
        cachedTowers = new ArrayList<CellTowerInfo>(cfg.towers);
        cachedLoaded = true;
        lastReadMs = now;
        return cfg;
    }

    private static Config parseFile() {
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
            String radioType = null;
            int mcc = 460;
            int mnc = 0;
            int lac = 0;
            int psc = 0;
            long cellId = 0L;
            double lat = 0.0;
            double lng = 0.0;
            float accuracy = 1000f;
            for (String line : lines) {
                int idx = line.indexOf('=');
                if (idx <= 0) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if ("enabled".equals(key)) {
                    cfg.enabled = "1".equals(value) || "true".equalsIgnoreCase(value);
                } else if ("radio_type".equals(key)) {
                    radioType = value.toUpperCase();
                } else if ("mcc".equals(key)) {
                    mcc = parseInt(value, mcc);
                } else if ("mnc".equals(key)) {
                    mnc = parseInt(value, mnc);
                } else if ("lac".equals(key)) {
                    lac = parseInt(value, lac);
                } else if ("psc".equals(key)) {
                    psc = parseInt(value, psc);
                } else if ("cell_id".equals(key)) {
                    cellId = parseLong(value, cellId);
                } else if ("lat".equals(key)) {
                    lat = parseDouble(value, lat);
                } else if ("lng".equals(key)) {
                    lng = parseDouble(value, lng);
                } else if ("accuracy".equals(key)) {
                    accuracy = parseFloat(value, accuracy);
                }
            }
            if (radioType != null) {
                cfg.towers.add(buildTower(radioType, mcc, mnc, lac, psc, cellId, lat, lng, accuracy));
            }
        }
        return cfg;
    }

    /**
     * CellTowerInfo 没有公开构造器（字段私有），与 App 侧 buildCellTowerInfo 相同，
     * 经 Parcel 按字段顺序（radioType, mcc, mnc, lac, psc, cellId, lat, lng, accuracy）
     * 装配。
     */
    private static CellTowerInfo buildTower(String radioType, int mcc, int mnc, int lac,
                                            int psc, long cellId, double lat, double lng, float accuracy) {
        Parcel p = Parcel.obtain();
        try {
            p.writeString(radioType);
            p.writeInt(mcc);
            p.writeInt(mnc);
            p.writeInt(lac);
            p.writeInt(psc);
            p.writeLong(cellId);
            p.writeDouble(lat);
            p.writeDouble(lng);
            p.writeFloat(accuracy);
            p.setDataPosition(0);
            return CellTowerInfo.CREATOR.createFromParcel(p);
        } catch (Throwable t) {
            return null;
        } finally {
            p.recycle();
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Throwable t) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Throwable t) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Throwable t) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (Throwable t) {
            return fallback;
        }
    }
}
