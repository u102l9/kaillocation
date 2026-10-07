package com.kail.location.inject.utils;

/**
 * 模拟控制通道的路径。
 *
 * 历史版本曾按 App versionName 生成 location_control_<version>.txt / ack 文件，
 * 以隔离“App 升级后旧注入仍缓存旧版本文件”的协议差异；但会在 /data/system/kail-loc
 * 里堆积大量旧版本残留文件且从不清理。
 *
 * 现简化为固定文件名，写入侧每次下发配置都用 printf > 覆盖重建文件（内容即最新），
 * 并在服务启动时把旧版本残留一并删除，避免目录持续膨胀。
 */
public final class RootControlPaths {
    public static final String RUNTIME_DIR = "/data/system/kail-loc";
    public static final String LEGACY_CONTROL_PATH = RUNTIME_DIR + "/location_control.txt";
    public static final String LEGACY_ACK_PATH = RUNTIME_DIR + "/location_control_ack.txt";

    private RootControlPaths() {
    }

    public static String controlPath(android.content.Context context) {
        return LEGACY_CONTROL_PATH;
    }

    public static String ackPath(android.content.Context context) {
        return LEGACY_ACK_PATH;
    }
}