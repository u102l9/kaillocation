package com.kail.location.inject.utils;

import java.nio.ByteBuffer;

/**
 * Root 模式路线模拟的共享内存传输协议（Phase 1：单槽最新值 + seqlock）。
 *
 * <p>背景：root 模式下 App 侧原本每 tick 通过 {@code su} 把当前位置写进
 * {@code /data/system/kail-loc/location_control_*.txt}，注入进 system_server 的
 * {@link RootLocationControl} 再轮询读取。每 tick 一次 shell + 文件写在 Android 16
 * 上开销明显。本类把「最新位置」放进一块 ashmem（由 App 的
 * {@code LocationShmProvider} 经 ContentProvider 把 {@code SharedMemory} 传给
 * system_server），两端 mmap 同一块内存，写端只做内存写、读端只做内存读，
 * 彻底去掉每 tick 的 shell/文件 IPC。
 *
 * <p>同步：单写单读的 seqlock。{@code seq} 为偶数表示稳定、奇数表示写入中；
 * 写端「seq→奇 → 全栅栏 → 写数据 → 全栅栏 → seq→偶」，读端读前后各取一次 seq，
 * 不等或为奇则重试。64 位字段用 volatile 视图保证原子。
 *
 * <p>volatile 访问由 {@link LocationShmAccess}（{@code sun.misc.Unsafe}）实现，
 * 本类在静态初始化时反射加载；失败则 {@link #isAvailable()} 返回 false，调用方
 * 回退文件通道，不会崩溃。
 *
 * <p>本类必须放在 {@code com.kail.location.inject} 前缀下，才会被打进 inject.dex。
 */
public final class LocationShm {

    private LocationShm() {
    }

    public static final int MAGIC = 0x4B4C5348; // 'K''L''S''H'
    /** 布局版本：字段/偏移变更时 +1，双端校验不一致即拒绝共享内存并回退文件通道。 */
    public static final int VERSION = 1;
    /** 共享内存总大小（ashmem）。Phase 1 只用 header + 1 槽，余量留给 Phase 2 ring。 */
    public static final int SIZE = 4096;

    /** 传输共享内存 fd 的 ContentProvider authority / URI / 方法名（两端必须一致）。 */
    public static final String PROVIDER_AUTHORITY = "com.kail.location.locshm";
    public static final String PROVIDER_URI = "content://" + PROVIDER_AUTHORITY + "/shm";
    public static final String PROVIDER_METHOD_GET_SHM = "get_shm";
    public static final String PROVIDER_KEY_SHM = "shm";
    /** 通过同一个 Provider 下发 WiFi / 基站等一次性模拟配置，替代 su 写文件通道。 */
    public static final String PROVIDER_METHOD_GET_CONFIG = "get_config";
    public static final String PROVIDER_KEY_WIFI_CONFIG = "wifi_config";
    public static final String PROVIDER_KEY_CELL_CONFIG = "cell_config";
    public static final String PROVIDER_KEY_ALLOW_CONFIG = "allow_config";
    public static final String PROVIDER_KEY_HIDE_CONFIG = "hide_config";
    public static final String PROVIDER_KEY_ANTIDETECT_CONFIG = "antidetect_config";
    public static final String PROVIDER_KEY_STEP_CONFIG = "step_config";
    public static final String PROVIDER_KEY_CAMERA_CONFIG = "camera_config";
    public static final String ASHMEM_NAME = "kail_loc_shm";

    public static final int HEADER_BYTES = 64;
    public static final int SAMPLE_BYTES = 64;

    // ---- header 偏移 ----
    public static final int OFF_MAGIC = 0;
    public static final int OFF_VERSION = 4;
    public static final int OFF_SEQ = 8;
    public static final int OFF_ENABLED = 16;
    public static final int OFF_FLAGS = 20;
    public static final int OFF_WRITE_TIME = 24;
    public static final int OFF_CAPACITY = 32;
    public static final int OFF_SAMPLE_SIZE = 36;

    // ---- 槽 0 字段绝对偏移 ----
    public static final int SLOT0 = HEADER_BYTES;
    private static final int S_TNANOS = SLOT0;          // 64
    private static final int S_LAT = SLOT0 + 8;         // 72
    private static final int S_LNG = SLOT0 + 16;        // 80
    private static final int S_ALT = SLOT0 + 24;        // 88
    private static final int S_BEARING = SLOT0 + 32;    // 96
    private static final int S_SPEED = SLOT0 + 40;      // 104
    private static final int S_ACCURACY = SLOT0 + 48;   // 112
    private static final int S_PROVIDER = SLOT0 + 56;   // 120
    private static final int S_FLAGS = SLOT0 + 60;      // 124

    /**
     * 底层 volatile 访问抽象。实现类 {@link LocationShmAccess} 用
     * {@code sun.misc.Unsafe}，因此只在初始化时反射加载，失败即视为不可用。
     */
    interface Access {
        long getSeq(ByteBuffer b);

        void setSeq(ByteBuffer b, long value);

        int getInt(ByteBuffer b, int byteOffset);

        void setInt(ByteBuffer b, int byteOffset, int value);

        long getLong(ByteBuffer b, int byteOffset);

        void setLong(ByteBuffer b, int byteOffset, long value);

        double getDouble(ByteBuffer b, int byteOffset);

        void setDouble(ByteBuffer b, int byteOffset, double value);

        void fence();
    }

    private static final Access ACCESS;

    static {
        Access a = null;
        try {
            // sun.misc.Unsafe 属 hidden API，先尝试放行。
            HiddenApiBypass.bypassHiddenApiRestrictions();
            Class<?> clazz = Class.forName("com.kail.location.inject.utils.LocationShmAccess");
            java.lang.reflect.Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            a = (Access) ctor.newInstance();
        } catch (Throwable t) {
            a = null;
            InjectLog.e("LocationShm", "shm access init failed; fallback to control file", t);
        }
        ACCESS = a;
    }

    /** false 表示当前运行时没有可用的 VarHandle（低版本/加载失败），调用方应回退文件通道。 */
    public static boolean isAvailable() {
        return ACCESS != null;
    }

    private static int intGet(ByteBuffer b, int off) {
        return ACCESS.getInt(b, off);
    }

    private static void intSet(ByteBuffer b, int off, int value) {
        ACCESS.setInt(b, off, value);
    }

    private static long longGet(ByteBuffer b, int off) {
        return ACCESS.getLong(b, off);
    }

    private static void longSet(ByteBuffer b, int off, long value) {
        ACCESS.setLong(b, off, value);
    }

    private static double doubleGet(ByteBuffer b, int off) {
        return ACCESS.getDouble(b, off);
    }

    private static void doubleSet(ByteBuffer b, int off, double value) {
        ACCESS.setDouble(b, off, value);
    }

    private static long seqGet(ByteBuffer b) {
        return ACCESS.getSeq(b);
    }

    private static void seqSet(ByteBuffer b, long value) {
        ACCESS.setSeq(b, value);
    }

    // ------------------------------------------------------------------
    // header
    // ------------------------------------------------------------------

    /** 写端初始化 header。必须在共享给读端之前调用。 */
    public static void initHeader(ByteBuffer b, int capacity) {
        if (!isAvailable() || b == null || b.capacity() < HEADER_BYTES + SAMPLE_BYTES) return;
        intSet(b, OFF_MAGIC, MAGIC);
        intSet(b, OFF_VERSION, VERSION);
        seqSet(b, 0L);
        intSet(b, OFF_FLAGS, 0);
        longSet(b, OFF_WRITE_TIME, 0L);
        intSet(b, OFF_CAPACITY, capacity);
        intSet(b, OFF_SAMPLE_SIZE, SAMPLE_BYTES);
        intSet(b, OFF_ENABLED, 1);
    }

    /** 读端校验。magic/version/容量不匹配即拒绝。 */
    public static boolean validate(ByteBuffer b) {
        if (!isAvailable() || b == null) return false;
        if (b.capacity() < HEADER_BYTES + SAMPLE_BYTES) return false;
        return intGet(b, OFF_MAGIC) == MAGIC
                && intGet(b, OFF_VERSION) == VERSION
                && intGet(b, OFF_CAPACITY) >= 1
                && intGet(b, OFF_SAMPLE_SIZE) == SAMPLE_BYTES;
    }

    public static void setEnabled(ByteBuffer b, boolean enabled) {
        if (!isAvailable() || b == null) return;
        intSet(b, OFF_ENABLED, enabled ? 1 : 0);
    }

    public static boolean isEnabled(ByteBuffer b) {
        if (!isAvailable() || b == null) return false;
        return intGet(b, OFF_ENABLED) == 1;
    }

    // ------------------------------------------------------------------
    // 样本读写
    // ------------------------------------------------------------------

    /**
     * seqlock 写入最新样本。返回 false 表示共享内存不可用（调用方回退文件通道）。
     */
    public static boolean writeSample(ByteBuffer b,
                                      long tNanos,
                                      double lat,
                                      double lng,
                                      double alt,
                                      double bearing,
                                      double speed,
                                      double accuracy,
                                      int provider,
                                      int flags) {
        if (!isAvailable() || b == null) return false;
        long seq = seqGet(b);
        seqSet(b, seq + 1L); // odd: 写入中
        ACCESS.fence();
        longSet(b, S_TNANOS, tNanos);
        doubleSet(b, S_LAT, lat);
        doubleSet(b, S_LNG, lng);
        doubleSet(b, S_ALT, alt);
        doubleSet(b, S_BEARING, bearing);
        doubleSet(b, S_SPEED, speed);
        doubleSet(b, S_ACCURACY, accuracy);
        intSet(b, S_PROVIDER, provider);
        intSet(b, S_FLAGS, flags);
        longSet(b, OFF_WRITE_TIME, System.currentTimeMillis());
        ACCESS.fence();
        seqSet(b, seq + 2L); // even: 发布
        return true;
    }

    /** 样本快照。 */
    public static final class Sample {
        public long tNanos;
        public double lat;
        public double lng;
        public double alt;
        public double bearing;
        public double speed;
        public double accuracy;
        public int provider;
        public int flags;
    }

    /**
     * seqlock 读取最新样本。
     *
     * @return 成功时返回本次读到的 seq（单调递增，可用于判断是否有新样本）；
     *         共享内存不可用/校验失败返回 -1；尚无样本返回 0。
     */
    public static long readSample(ByteBuffer b, Sample out) {
        if (!isAvailable() || b == null || out == null) return -1L;
        for (int attempt = 0; attempt < 64; attempt++) {
            long s1 = seqGet(b);
            if ((s1 & 1L) != 0L) {
                continue;
            }
            out.tNanos = longGet(b, S_TNANOS);
            out.lat = doubleGet(b, S_LAT);
            out.lng = doubleGet(b, S_LNG);
            out.alt = doubleGet(b, S_ALT);
            out.bearing = doubleGet(b, S_BEARING);
            out.speed = doubleGet(b, S_SPEED);
            out.accuracy = doubleGet(b, S_ACCURACY);
            out.provider = intGet(b, S_PROVIDER);
            out.flags = intGet(b, S_FLAGS);
            long s2 = seqGet(b);
            if (s1 == s2) {
                return s1;
            }
        }
        return -1L;
    }
}
