package com.kail.location.inject.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * InjectLog —— 注入态（运行在被 hook 的目标 App 进程内）专用日志工具。
 *
 * <p>背景：inject 包里的 Hook 类原本各自有一个空的 {@code log(Object... objArr)} 桩方法
 * （反编译产物），导致数百处埋点全部失效。本类提供统一实现，让这些埋点重新生效，
 * 并与 Java/Kotlin 侧的 KailLog 保持一致的「文件:行号#方法」定位风格。
 *
 * <p>注入进程读不到 com.kail.location 的 SharedPreferences，因此开关通过
 * <b>公共目录下的标记文件</b> 控制（无需重新打包、无需 IPC，对所有目标进程统一生效）：
 * <ul>
 *   <li>{@code /sdcard/Documents/KailLocation/logs/.kail_debug}    —— 开启日志（Logcat/XposedBridge 输出）</li>
 *   <li>{@code /sdcard/Documents/KailLocation/logs/.kail_log_file} —— 额外落盘到文件</li>
 *   <li>{@code /sdcard/Documents/KailLocation/logs/.kail_verbose}  —— 开启高频(V)详细日志</li>
 * </ul>
 * 标记文件每 {@link #MARKER_TTL_MS} 毫秒检查一次（带缓存），因此热点路径开销极小。
 * 也可由宿主通过 {@link #setEnabled(boolean)} / {@link #setFileEnabled(boolean)} 等强制覆盖。
 *
 * <p>策略：W/E 级别始终输出（便于定位严重问题）；高频/V 日志限流「少量保存」，
 * 被丢弃条数会在下次输出时以 {@code (+N suppressed)} 标注。
 *
 * <p>注意：不使用 SimpleDateFormat，避免在 native/系统线程触发 ICU 崩溃。
 */
public final class InjectLog {

    private InjectLog() {
    }

    private static final String TAG_PREFIX = "KailLog/";
    private static final String LOG_DIR = "/sdcard/Documents/KailLocation/logs";
    private static final String FILE_MARKER = ".kail_log_file";
    private static final long MARKER_TTL_MS = 5000L;
    private static final long HIGH_FREQ_INTERVAL_MS = 1000L;
    private static final int MAX_THROTTLE_KEYS = 512;

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final ConcurrentHashMap<String, Long> HIGH_FREQ_LAST = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> HIGH_FREQ_SUPPRESSED = new ConcurrentHashMap<>();

    // 标记文件解析出的状态（带 TTL 缓存）。
    private static volatile boolean enabled = false;
    private static volatile boolean fileEnabled = false;
    private static volatile boolean verbose = false;
    private static volatile long markerCheckedAt = 0L;

    // 宿主强制覆盖（非空时优先于标记文件）。
    private static volatile Boolean enabledOverride = null;
    private static volatile Boolean fileOverride = null;
    private static volatile Boolean verboseOverride = null;

    private static volatile Method xposedLog;
    private static volatile boolean xposedResolved = false;
    private static volatile String cachedProcess = null;

    // 取调用者信息时的重入保护。某些被 hook 的方法（如 StackTraceElement.getClassName）
    // 自身会打日志，链路为：log -> emit -> callerInfo -> Thread.getStackTrace -> getClassName
    // （又被 hook）-> log …… 自指成环。用本线程标记打断：重入时直接返回 "Unknown"，不再取栈。
    private static final ThreadLocal<Boolean> IN_CALLER_INFO = new ThreadLocal<Boolean>();

    // 日志整体重入保护：被 hook 的方法（File.exists / ClassLoader.loadClass /
    // StackTraceElement.getClassName 等）内部会再打日志，若日志组件恰好调用了这些方法
    // 就会自指成环。同一线程重入时直接丢弃这条日志，从根上断掉所有此类环。
    private static final ThreadLocal<Boolean> IN_EMIT = new ThreadLocal<Boolean>();

    // 各 Hook 类埋点总开关（默认关）。被 hook 的方法（File.exists/list、
    // ClassLoader.loadClass、SystemProperties.get*、PackageManagerService 查询等）
    // 会被目标 App 极高频调用，逐条打日志会刷屏并拖慢目标进程，故默认关闭。
    public static volatile boolean hookLogEnabled = false;

    // ------------------------------------------------------------------
    // 宿主可调用的强制开关（可选）。
    // ------------------------------------------------------------------
    public static void setEnabled(boolean value) {
        enabledOverride = value;
    }

    public static void setFileEnabled(boolean value) {
        fileOverride = value;
    }

    public static void setVerbose(boolean value) {
        verboseOverride = value;
    }

    // ------------------------------------------------------------------
    // 对外日志方法。
    // ------------------------------------------------------------------

    /** 通用调试日志（等同 d）。兼容原 Hook 类里的 {@code log(Object...)} 桩。 */
    public static void log(String tag, Object... parts) {
        emit(tag, 'd', false, parts);
    }

    public static void d(String tag, Object... parts) {
        emit(tag, 'd', false, parts);
    }

    public static void i(String tag, Object... parts) {
        emit(tag, 'i', false, parts);
    }

    public static void w(String tag, Object... parts) {
        emit(tag, 'w', false, parts);
    }

    public static void e(String tag, Object... parts) {
        emit(tag, 'e', false, parts);
    }

    /** 高频详细日志：仅在 verbose 开启时输出并限流。 */
    public static void v(String tag, Object... parts) {
        emit(tag, 'v', true, parts);
    }

    /**
     * 重要的一次性诊断日志：无论开关如何，<b>始终</b>输出到 Logcat。
     *
     * <p>用于低频、高价值、需要事后排查的关键状态（如 ArtMethod 布局自动探测结果、
     * 注入初始化结论）。这类信息在我们手头没有的设备上排查问题时尤为重要，因此不能
     * 文件落盘仍依赖 {@code .kail_log_file}，避免关闭日志后继续写外部目录。
     */
    public static void persist(String tag, Object... parts) {
        emitAlways(tag, 'i', parts);
    }

    /** 记录异常（附带完整堆栈）。 */
    public static void e(String tag, String message, Throwable tr) {
        emit(tag, 'e', false, message + "\n" + stackTrace(tr));
    }

    public static void w(String tag, String message, Throwable tr) {
        emit(tag, 'w', false, message + "\n" + stackTrace(tr));
    }

    // ------------------------------------------------------------------
    // 核心实现。
    // ------------------------------------------------------------------
    private static void emit(String tag, char level, boolean highFrequency, Object... parts) {
        // 日志重入保护：被 hook 的方法内部又触发日志时直接丢弃，避免自指死循环。
        if (IN_EMIT.get() != null) return;
        IN_EMIT.set(Boolean.TRUE);
        try {
            refreshFlags();

            boolean warnOrError = level == 'w' || level == 'e';
            boolean verboseLike = highFrequency || level == 'v';

            if (!warnOrError) {
                if (!enabled) return;
                if (verboseLike && !verbose) return;
            }

            String caller = callerInfo();
            String suffix = "";
            if (verboseLike && !warnOrError) {
                int dropped = highFreqGate(tag + "@" + caller);
                if (dropped < 0) return;
                if (dropped > 0) suffix = " (+" + dropped + " suppressed)";
            }

            String body = join(parts);
            String thread = Thread.currentThread().getName();
            String logcatTag = TAG_PREFIX + tag;
            String logcatMessage = "[" + thread + "] " + caller + " | " + body + suffix;

            Method m = xposedLogMethod();
            if (m != null) {
                try {
                    m.invoke(null, logcatTag + ": " + logcatMessage);
                } catch (Throwable ignored) {
                }
            }

            switch (level) {
                case 'v': Log.v(logcatTag, logcatMessage); break;
                case 'i': Log.i(logcatTag, logcatMessage); break;
                case 'w': Log.w(logcatTag, logcatMessage); break;
                case 'e': Log.e(logcatTag, logcatMessage); break;
                default:  Log.d(logcatTag, logcatMessage); break;
            }

            // 文件落盘严格跟随宿主日志开关；关闭日志时不再写外部 logs 目录。
            if (fileEnabled) {
                String fileMessage = Character.toUpperCase(level) + " [" + processName() + "/" + thread + "] "
                        + tag + " " + caller + " | " + body + suffix;
                writeFile(fileMessage);
            }
        } finally {
            IN_EMIT.remove();
        }
    }

    /**
     * 与 {@link #emit} 类似，但<b>无视输出开关</b>：始终输出到 Logcat（及 Xposed）。
     * 仅供 {@link #persist} 使用，承载低频高价值的一次性诊断信息；文件落盘仍受开关控制。
     */
    private static void emitAlways(String tag, char level, Object... parts) {
        // 同 emit：日志重入保护。
        if (IN_EMIT.get() != null) return;
        IN_EMIT.set(Boolean.TRUE);
        try {
            refreshFlags();

            String caller = callerInfo();
            String body = join(parts);
            String thread = Thread.currentThread().getName();
            String logcatTag = TAG_PREFIX + tag;
            String logcatMessage = "[" + thread + "] " + caller + " | " + body;

            Method m = xposedLogMethod();
            if (m != null) {
                try {
                    m.invoke(null, logcatTag + ": " + logcatMessage);
                } catch (Throwable ignored) {
                }
            }

            switch (level) {
                case 'w': Log.w(logcatTag, logcatMessage); break;
                case 'e': Log.e(logcatTag, logcatMessage); break;
                default:  Log.i(logcatTag, logcatMessage); break;
            }

            if (fileEnabled) {
                String fileMessage = Character.toUpperCase(level) + " [" + processName() + "/" + thread + "] "
                        + tag + " " + caller + " | " + body;
                writeFile(fileMessage);
            }
        } finally {
            IN_EMIT.remove();
        }
    }

    private static String join(Object... parts) {
        if (parts == null || parts.length == 0) return "";
        if (parts.length == 1) return String.valueOf(parts[0]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(String.valueOf(parts[i]));
        }
        return sb.toString();
    }

    private static String stackTrace(Throwable tr) {
        if (tr == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(tr);
        for (StackTraceElement el : tr.getStackTrace()) {
            sb.append("\n\tat ").append(el);
        }
        Throwable cause = tr.getCause();
        if (cause != null && cause != tr) {
            sb.append("\nCaused by: ").append(stackTrace(cause));
        }
        return sb.toString();
    }

    /** 返回调用方「文件名:行号#方法名」，跳过本类与 Thread 帧。 */
    private static String callerInfo() {
        // 重入保护：getClassName() 等被 hook 的方法内部可能再次触发日志，若继续取栈会无限递归。
        if (IN_CALLER_INFO.get() != null) {
            return "Unknown";
        }
        IN_CALLER_INFO.set(Boolean.TRUE);
        try {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String self = InjectLog.class.getName();
            for (int i = 3; i < stack.length; i++) {
                StackTraceElement f = stack[i];
                if (!f.getClassName().equals(self) && !f.getClassName().contains("java.lang.Thread")) {
                    String file = f.getFileName();
                    if (file == null) file = "Unknown";
                    int line = f.getLineNumber();
                    return line > 0 ? (file + ":" + line + "#" + f.getMethodName())
                                    : (file + "#" + f.getMethodName());
                }
            }
            return "Unknown";
        } finally {
            IN_CALLER_INFO.remove();
        }
    }

    /**
     * 高频限流门：同一 key 在 {@link #HIGH_FREQ_INTERVAL_MS} 内只放行一条。
     * @return -1 表示被丢弃；>=0 表示放行，值为期间被丢弃的条数。
     */
    private static int highFreqGate(String key) {
        long now = System.currentTimeMillis();
        if (HIGH_FREQ_LAST.size() > MAX_THROTTLE_KEYS) {
            HIGH_FREQ_LAST.clear();
            HIGH_FREQ_SUPPRESSED.clear();
        }
        Long last = HIGH_FREQ_LAST.get(key);
        if (last != null && now - last < HIGH_FREQ_INTERVAL_MS) {
            HIGH_FREQ_SUPPRESSED.merge(key, 1, Integer::sum);
            return -1;
        }
        HIGH_FREQ_LAST.put(key, now);
        Integer dropped = HIGH_FREQ_SUPPRESSED.remove(key);
        return dropped == null ? 0 : dropped;
    }

    private static void refreshFlags() {
        long now = System.currentTimeMillis();
        if (now - markerCheckedAt < MARKER_TTL_MS) {
            applyOverrides();
            return;
        }
        markerCheckedAt = now;
        try {
            File dir = new File(LOG_DIR);
            enabled = new File(dir, ".kail_debug").exists();
            fileEnabled = new File(dir, FILE_MARKER).exists();
            verbose = new File(dir, ".kail_verbose").exists();
        } catch (Throwable ignored) {
        }
        applyOverrides();
    }

    private static void applyOverrides() {
        if (enabledOverride != null) enabled = enabledOverride;
        if (fileOverride != null) fileEnabled = fileOverride;
        if (verboseOverride != null) verbose = verboseOverride;
    }

    private static Method xposedLogMethod() {
        if (xposedResolved) return xposedLog;
        synchronized (InjectLog.class) {
            if (!xposedResolved) {
                try {
                    xposedLog = Class.forName("de.robv.android.xposed.XposedBridge")
                            .getDeclaredMethod("log", String.class);
                } catch (Throwable ignored) {
                    xposedLog = null;
                }
                xposedResolved = true;
            }
            return xposedLog;
        }
    }

    private static String processName() {
        String name = cachedProcess;
        if (name != null) return name;
        try {
            String cmdline = new String(readSmall("/proc/self/cmdline")).trim();
            int nul = cmdline.indexOf('\0');
            if (nul >= 0) cmdline = cmdline.substring(0, nul);
            cmdline = cmdline.trim();
            if (!cmdline.isEmpty()) {
                cachedProcess = cmdline;
                return cmdline;
            }
        } catch (Throwable ignored) {
        }
        cachedProcess = "?";
        return cachedProcess;
    }

    private static byte[] readSmall(String path) throws Exception {
        File f = new File(path);
        try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
            byte[] buf = new byte[256];
            int n = in.read(buf);
            if (n <= 0) return new byte[0];
            byte[] out = new byte[n];
            System.arraycopy(buf, 0, out, 0, n);
            return out;
        }
    }

    private static void writeFile(final String message) {
        final long ts = System.currentTimeMillis();
        IO.execute(() -> {
            if (!new File(LOG_DIR, FILE_MARKER).exists()) {
                return;
            }
            String fileName = "kail_log_" + (ts / 86_400_000L) + ".txt";
            String entry = formatTime(ts) + " " + message + "\n";
            try {
                File dir = new File(LOG_DIR);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file, true)) {
                    fos.write(entry.getBytes());
                }
            } catch (Throwable t) {
                suAppend(LOG_DIR + "/" + fileName, entry);
            }
        });
    }

    /** 写入失败（权限问题）时的 su 兜底。 */
    private static void suAppend(String path, String payload) {
        try {
            File file = new File(path);
            String parent = file.getParent() == null ? "/sdcard" : file.getParent();
            String escaped = payload
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("$", "\\$")
                    .replace("`", "\\`");
            String cmd = "mkdir -p \"" + parent + "\" && printf \"%s\" \"" + escaped + "\" >> \"" + path + "\"";
            Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
        } catch (Throwable ignored) {
        }
    }

    /** HH:mm:ss.SSS（本地时区），不依赖 SimpleDateFormat。 */
    private static String formatTime(long ts) {
        long dayMs = 86_400_000L;
        long localMs = (((ts + TimeZone.getDefault().getOffset(ts)) % dayMs) + dayMs) % dayMs;
        long hour = localMs / 3_600_000L;
        long minute = (localMs % 3_600_000L) / 60_000L;
        long second = (localMs % 60_000L) / 1000L;
        long ms = localMs % 1000L;
        StringBuilder sb = new StringBuilder(12);
        pad(sb, hour, 2);
        sb.append(':');
        pad(sb, minute, 2);
        sb.append(':');
        pad(sb, second, 2);
        sb.append('.');
        pad(sb, ms, 3);
        return sb.toString();
    }

    private static void pad(StringBuilder sb, long value, int width) {
        String text = Long.toString(value);
        for (int i = text.length(); i < width; i++) sb.append('0');
        sb.append(text);
    }
}
