package com.kail.location.inject.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.kail.location.inject.fakelocation.InjectDex;

/**
 * 目标进程 / system_server 读取 App 下发配置的 <b>Provider 通道</b>。
 *
 * <p><b>关键约束：绝不能在调用者线程里发 binder 请求。</b>
 * 很多调用方是 system_server 里在持有锁（ActivityManagerService 监视器、PMS 锁等）
 * 的情况下被 hook 触发的；一旦在锁内做 {@code ContentResolver.call}，而 App 端
 * Provider 一时无响应（App 卡顿/被冻结），binder 事务会把系统锁占住几十秒，
 * watchdog 就会判定 “Blocked in monitor com.android.server.am.ActivityManagerService”
 * → system_server 卡死、整机无响应。
 *
 * <p>因此改为：一个常驻后台线程每 {@link #REFRESH_MS} 毫秒去 Provider 拉一次，
 * 结果存进 {@link #cached}；{@link #get(String)} 只读内存缓存，<b>永不发 binder</b>。
 * Provider 不可用/缓存为空时返回 null，调用方走文件兜底（文件用 FileInputStream 读，无 binder）。
 */
public final class ConfigProviderChannel {

    private static final String TAG = "ConfigProviderChannel";
    private static final long REFRESH_MS = 1000L;

    private static volatile Bundle cached;
    /** Provider 连通性，null = 未知。只在状态变化时打 persist 日志，避免刷屏。 */
    private static volatile Boolean lastProviderOk;
    private static volatile boolean started;
    private static final Object START_LOCK = new Object();

    private ConfigProviderChannel() {
    }

    /** 取某个配置键的文本；只读内存缓存，不发 binder；无缓存时返回 null（调用方走文件兜底）。 */
    public static String get(String key) {
        ensureRefreshing();
        Bundle b = cached;
        if (b == null) {
            return null;
        }
        return b.getString(key);
    }

    private static void ensureRefreshing() {
        if (started) {
            return;
        }
        synchronized (START_LOCK) {
            if (started) {
                return;
            }
            started = true;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Bundle reply = fetch();
                            if (reply != null) {
                                cached = reply;
                                if (lastProviderOk == null || !lastProviderOk) {
                                    lastProviderOk = true;
                                    InjectLog.persist(TAG, "[provider-read] online (bg refresh)");
                                }
                            } else if (lastProviderOk == null || lastProviderOk) {
                                lastProviderOk = false;
                                InjectLog.persist(TAG, "[provider-read] unavailable -> file fallback (bg refresh)");
                            }
                        } catch (Throwable t2) {
                            InjectLog.log(TAG, "[provider-read] bg refresh failed: ", t2);
                        }
                        try {
                            Thread.sleep(REFRESH_MS);
                        } catch (InterruptedException ignored) {
                            return;
                        }
                    }
                }
            }, "KailConfigProviderRefresh");
            t.setDaemon(true);
            t.start();
        }
    }

    /** 只在后台刷新线程里调用；会发 binder，但不会占用任何系统锁。 */
    private static Bundle fetch() {
        Context ctx = InjectDex.getApplicationContext();
        if (ctx == null) {
            return null;
        }
        return ctx.getContentResolver().call(
                Uri.parse(LocationShm.PROVIDER_URI),
                LocationShm.PROVIDER_METHOD_GET_CONFIG,
                null,
                null);
    }
}
