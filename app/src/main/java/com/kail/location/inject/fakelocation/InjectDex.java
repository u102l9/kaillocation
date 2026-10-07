package com.kail.location.inject.fakelocation;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import com.kail.location.inject.utils.HiddenApiBypass;
import com.kail.location.inject.utils.PackageSignatureVerifier;
import com.kail.location.inject.utils.RootLocationControl;
import com.kail.location.lib.lhooker.LHooker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.kail.location.inject.fakelocation.hook.phone.PhoneInterfaceManagerHook;
import com.kail.location.inject.fakelocation.hook.app.AppProcessHook;

/**
 * 注入体系在 Java 层的总入口（Dex 版）。
 *
 * native 侧 libfakeloc 系列库反射调用本类的三个静态入口：
 *   - libfakeloc_initzygote -> {@link #initZygote(Object)}（zygote 进程）
 *   - libfakeloc_init       -> {@link #init(Object)}（system_server）
 *   - libfakeloc_apphook    -> {@link #hookApplication(Object)}（各 app 进程）
 *
 * 职责：绕过 hidden API -> 按 ABI 加载 liblhooker.so -> 挂载对应的 hook 链，
 * 并把产生的 mock 服务（模拟定位/模拟 WiFi/反检测/隐藏 Root/原生捕获）
 * 注册进进程的 ServiceManager。
 */
public class InjectDex {

    /** bootstrap 状态文件路径：记录注入每个阶段的进度，供 root 侧排障。 */
    private static final String BOOTSTRAP_STATE_PATH = "/data/system/kail-loc/injectdex_state.txt";

    /** 当前已成功挂载的 hook 集合（线程安全，供调试/上层查询）。 */
    public static List<?> activeHooks = Collections.synchronizedList(new ArrayList());

    /** 初始化回调列表：handler/主线程就绪后依次回调 onInitialized()。 */
    static List<InitializationCallback> initializationCallbacks = Collections.synchronizedList(new ArrayList());

    /** 主线程 Handler（在 {@link #initializeMainThread} 中创建）。 */
    private static Handler mainHandler;

    /** 全局 application Context（在 {@link #init} / {@link #hookApplication} 时赋值）。 */
    private static Context applicationContext;

    /**
     * 设置 hook 库的会话路径（由 native 侧 apphook 调用）。
     * 让 LHooker 的会话携带本次注入的 hooker 库路径，供后续 fork 出的
     * 应用进程在原生层注入时复用，并落一条持久日志。
     *
     * @param libraryPath 当前进程注入所用的 hooker 库文件路径
     */
    public static void setHookLibraryPath(String libraryPath) {
        LHooker.setSessionLibraryPath(libraryPath);
        com.kail.location.inject.utils.InjectLog.persist("InjectDex", "hook library path=", libraryPath);
    }

    /**
     * 初始化回调接口：当主线程 Handler 就绪时被逐一触发。
     */
    public interface InitializationCallback {
        void onInitialized();
    }

    /**
     * 应用进程 hook 入口（由 native 侧 libfakeloc_apphook 反射调用）。
     *
     * 每个 app 进程启动（fork 后或服务进程创建）都会执行：
     * 1. 绕过 hidden API 限制；
     * 2. 按目标进程 ABI 加载对应的 liblhooker(64/x/x64).so；
     * 3. LHooker 若未初始化则中止；
     * 4. 暂停所有线程后挂载 hook：com.android.phone 走
     *    PhoneInterfaceManagerHook，其余应用走 AppProcessHook。
     *
     * @param contextObject 进程的 Context（通常为 system_server context）
     * @return 始终返回 null（native 侧忽略返回值）
     */
    public static Object[] hookApplication(Object contextObject) {
        String message;
        applicationContext = (Context) contextObject;
        log("App: " + contextObject);
        try {
            HiddenApiBypass.bypassHiddenApiRestrictions();
            String packageName = ((Context) contextObject).getPackageName();
            if (packageName.equals("com.android.phone")) {
                if (!LHooker.isDeviceX86_64()) {
                    if (!LHooker.isDeviceX86()) {
                        if (LHooker.isDeviceArm64()) {
                            LHooker.loadHookLibrary("/data/kail-loc/liblhooker64.so");
                        } else {
                            LHooker.loadHookLibrary("/data/kail-loc/liblhooker.so");
                        }
                    }
                    LHooker.loadHookLibrary("/data/kail-loc/liblhookerx.so");
                }
                LHooker.loadHookLibrary("/data/kail-loc/liblhookerx64.so");
            } else {
                String nativeLibraryAbi = new File("" + ((Context) contextObject).getPackageManager().getApplicationInfo(packageName.split(":")[0], 0).nativeLibraryDir).getName();
                log("App abi: " + nativeLibraryAbi);
                if (LHooker.isX86_64Abi(nativeLibraryAbi)) {
                    LHooker.loadHookLibrary("/data/kail-loc/liblhookerx64.so");
                } else if (LHooker.isX86Abi(nativeLibraryAbi)) {
                    LHooker.loadHookLibrary("/data/kail-loc/liblhookerx.so");
                } else {
                    if (LHooker.isArm64Abi(nativeLibraryAbi)) {
                        LHooker.loadHookLibrary("/data/kail-loc/liblhooker64.so");
                    }
                    LHooker.loadHookLibrary("/data/kail-loc/liblhooker.so");
                }
            }
            if (!LHooker.initialized) {
                com.kail.location.inject.utils.InjectLog.e("InjectDex", "hookApplication aborted: LHooker not initialized");
                return null;
            }
            LHooker.suspendAll();
            if (packageName.equals("com.android.phone")) {
                PhoneInterfaceManagerHook.hook(((Context) contextObject).getClassLoader());
                message = "App finished.";
            } else {
                AppProcessHook.applyHookToApp((Context) contextObject, packageName);
                message = "App[" + packageName + "] finished.";
            }
            log(message);
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            com.kail.location.inject.utils.InjectLog.e("InjectDex", "hookApplication error", th);
            return null;
        }
    }

    /**
     * system_server 内初始化入口（由 native 侧 libfakeloc_init 反射调用）。
     *
     * 按顺序完成注入体系的搭建：
     * 1. 主线程 Handler 就绪并触发初始化回调（{@link #initializeMainThread}）；
     * 2. 绕过 hidden API 限制；
     * 3. 按设备 ABI 加载 liblhooker(64/x/x64).so；
     * 4. 启动 RootLocationControl；
     * 5. 做包签名校验（com.kail.location / oem_manager / oem_bluetooth）；
     * 6. 通过 ServiceManagerBridge 向系统注册 oem_location / oem_wifi /
     *    oem_security / oem_integrity / oem_native 五个 mock 服务；
     * 7. LHooker 未初始化则中止；否则挂载全 app 的 unified hook；
     * 8. 启动反检测配置轮询线程（{@link #startAntiDetectConfigPoller}）。
     *
     * 每一步的关键事件都会写入 bootstrap 状态文件以便排障。
     *
     * @param contextObject system_server 的 Context
     * @return 始终返回 null
     */
    public static Object[] init(Object contextObject) {
        Context context = (Context) contextObject;
        applicationContext = context;
        writeBootstrapState("entered context=" + contextObject, null);
        com.kail.location.inject.utils.InjectLog.persist("InjectDex", "init: ", contextObject);
        try {
            initializeMainThread(context);
            writeBootstrapState("main_thread_ready", null);
            HiddenApiBypass.bypassHiddenApiRestrictions();
            writeBootstrapState("hidden_api_bypassed", null);
            LHooker.loadHookLibrary(LHooker.isDeviceX86_64() ? "/data/kail-loc/liblhookerx64.so" : LHooker.isDeviceX86() ? "/data/kail-loc/liblhookerx.so" : LHooker.isDeviceArm64() ? "/data/kail-loc/liblhooker64.so" : "/data/kail-loc/liblhooker.so");
            com.kail.location.inject.utils.InjectLog.persist("InjectDex", "LHooker loaded initialized=", LHooker.initialized);
            writeBootstrapState("lhooker_loaded initialized=" + LHooker.initialized, null);
            RootLocationControl.start(context);
            writeBootstrapState("root_location_control_start_called", null);
            PackageSignatureVerifier.verifyPackageSignature(context, "com.kail.location", "oem_manager");
            // oem_location / oem_wifi / oem_security / oem_integrity / oem_native
            // 等 binder 服务在本机被 SELinux 拒绝注册（addService denied）且被
            // 目标进程 find 拦截，已彻底弃用。模拟状态统一走：
            //   App(Provider + 文件通道) -> system_server RootLocationControl
            //   -> MockLocationHookManager / MockWifiConfigManager（同进程直调）
            // 目标进程侧由 MockLocationServiceManager 走 Provider + 文件通道读取。
            com.kail.location.inject.utils.InjectLog.persist("InjectDex",
                    "oem_* binder services disabled (SELinux); using provider+file channels");
            PackageSignatureVerifier.verifyPackageSignature(context, "com.kail.location", "oem_bluetooth");
            if (!LHooker.initialized) {
                com.kail.location.inject.utils.InjectLog.e("InjectDex", "init aborted: LHooker not initialized");
                writeBootstrapState("aborted_lhooker_not_initialized", null);
                return null;
            }
            LHooker.suspendAll();
            com.kail.location.inject.utils.InjectLog.persist("InjectDex", "init finished, all services registered");
            writeBootstrapState("finished", null);
            startAntiDetectConfigPoller(context);
            return null;
        } catch (RuntimeException th) {
            com.kail.location.inject.utils.InjectLog.e("InjectDex", "init runtime error", th);
            writeBootstrapState("runtime_error", th);
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            com.kail.location.inject.utils.InjectLog.e("InjectDex", "init error", th);
            writeBootstrapState("error", th);
            return null;
        }
    }

    /**
     * zygote 入口（由 native 侧 libfakeloc_initzygote 反射调用）。
     *
     * zygote 里没有 Activity 上下文，这里只做最轻量的准备：
     * 1. 绕过 hidden API 限制；
     * 2. 按设备 ABI 加载 liblhooker(64/x/x64).so（32/64 位兼容时分别加载）；
     * 3. 把 AppProcessHook 挂到系统 ClassLoader 上——这样之后 fork 出来的
     *    所有应用进程都自动继承 hook 链（后续 dp_*_hook 覆盖各别 app）。
     *
     * @param startupParam native 传入的启动参数（当前未使用）
     * @return 始终返回 null
     */
    public static Object[] initZygote(Object startupParam) {
        String libraryPath;
        String processLibraryPath;
        com.kail.location.inject.utils.InjectLog.i("InjectDex", "initZygote: " + startupParam);
        HiddenApiBypass.bypassHiddenApiRestrictions();
        if (LHooker.isDeviceX86_64() || LHooker.isDeviceX86()) {
            libraryPath = "/data/kail-loc/liblhookerx.so";
            if (Build.VERSION.SDK_INT >= 23 && Process.is64Bit()) {
                processLibraryPath = "/data/kail-loc/liblhookerx64.so";
                LHooker.loadHookLibrary(processLibraryPath);
            }
            LHooker.loadHookLibrary(libraryPath);
        } else {
            libraryPath = "/data/kail-loc/liblhooker.so";
            if (Build.VERSION.SDK_INT >= 23 && Process.is64Bit()) {
                processLibraryPath = "/data/kail-loc/liblhooker64.so";
                LHooker.loadHookLibrary(processLibraryPath);
            }
            LHooker.loadHookLibrary(libraryPath);
        }
        AppProcessHook.hook(ClassLoader.getSystemClassLoader());
        com.kail.location.inject.utils.InjectLog.i("InjectDex", "initZygote finished, initialized=" + LHooker.initialized);
        return null;
    }

    /**
     * 获取注入体系持有的全局 application Context
     * （在 {@link #init} / {@link #hookApplication} 时被赋值）。
     *
     * @return 当前进程的 Context，未初始化时为 null
     */
    public static Context getApplicationContext() {
        return applicationContext;
    }

    /**
     * 反检测（隐藏应用列表）文件通道的 system_server 侧轮询器。
     *
     * 在 SELinux Enforcing 设备上，oem_security binder 可能 find 不到，配置走 binder
     * 推送不进来。这里像 RootLocationControl 一样，在 system_server 内轮询
     * /data/kail-loc/antidetect_config.txt：一旦 Kail 写好配置，就安装
     * PackageManagerServiceHook 并把这些配置写入 PackageAntiDetectionConfig。
     */
    private static void startAntiDetectConfigPoller(final Context context) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean wasEnabled = false;
                while (true) {
                    try {
                        com.kail.location.inject.utils.AntiDetectConfigFile.Config cfg =
                                com.kail.location.inject.utils.AntiDetectConfigFile.read();
                        if (cfg.hookEnabled) {
                            if (!wasEnabled) {
                                com.kail.location.inject.utils.PackageAntiDetectionConfig.setPackageFilterEnabled(true);
                                com.kail.location.inject.utils.PackageAntiDetectionConfig.setPackageVisibilityFilterEnabled(true);
                                com.kail.location.inject.utils.PackageAntiDetectionConfig.setTargetPackages(cfg.targetPackages);
                                com.kail.location.inject.utils.PackageAntiDetectionConfig.setDetectedPackages(cfg.detectedPackages);
                                // 安装钩子（幂等，packageManagerHooked 守卫）
                                com.kail.location.inject.fakelocation.hook.system.PackageManagerServiceHook
                                        .hook(context.getClassLoader());
                                com.kail.location.inject.utils.InjectLog.persist(
                                        "InjectDex", "antidetect config file applied: detected=", cfg.detectedPackages);
                                wasEnabled = true;
                            }
                        } else if (wasEnabled) {
                            com.kail.location.inject.utils.PackageAntiDetectionConfig.setPackageFilterEnabled(false);
                            com.kail.location.inject.utils.PackageAntiDetectionConfig.setPackageVisibilityFilterEnabled(false);
                            com.kail.location.inject.utils.PackageAntiDetectionConfig.setTargetPackages(null);
                            com.kail.location.inject.utils.PackageAntiDetectionConfig.setDetectedPackages(null);
                            wasEnabled = false;
                        }
                    } catch (Throwable th) {
                        com.kail.location.inject.utils.InjectLog.e("InjectDex", "antidetect config poller error", th);
                    }
                    try {
                        Thread.sleep(400L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, "KailAntiDetectConfig");
        t.setDaemon(true);
        t.start();
    }

    /**
     * DEBUG 级日志的本地封装。
     *
     * @param message 要写入 logcat 的消息
     */
    static void log(String message) {
        com.kail.location.inject.utils.InjectLog.d("InjectDex", message);
    }

    /**
     * 初始化主线程 Handler，并依次触发所有已注册的初始化回调。
     * 回调异常仅打印堆栈，不中断后续回调执行。
     *
     * @param context 用于获取主线程 Looper 的 Context
     */
    private static void initializeMainThread(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        Iterator<InitializationCallback> iterator = initializationCallbacks.iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().onInitialized();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    /**
     * 把当前注入阶段的事件追加写入 /data/system/kail-loc/injectdex_state.txt
     * （root 侧可读，用于诊断注入进度）。
     * 不要求写入者的宿主进程拥有权限；失败时静默忽略。
     *
     * @param event 该阶段的事件描述（如 "hidden_api_bypassed"、"finished"）
     * @param t     可选的异常对象；非 null 时会一并记录栈信息
     */
    private static void writeBootstrapState(String event, Throwable t) {
        try {
            File file = new File(BOOTSTRAP_STATE_PATH);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("event=").append(event).append('\n');
            sb.append("pid=").append(Process.myPid()).append('\n');
            sb.append("time_ms=").append(System.currentTimeMillis()).append('\n');
            sb.append("thread=").append(Thread.currentThread().getName()).append('\n');
            sb.append("lhooker_initialized=").append(LHooker.initialized).append('\n');
            if (t != null) {
                sb.append("throwable=").append(t).append('\n');
                StringWriter writer = new StringWriter();
                t.printStackTrace(new PrintWriter(writer));
                sb.append("stack=").append(writer.toString().replace('\n', '|')).append('\n');
            }
            sb.append("---\n");
            FileOutputStream out = new FileOutputStream(file, true);
            try {
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            } finally {
                out.close();
            }
            file.setReadable(true, false);
            file.setWritable(true, false);
        } catch (Throwable ignored) {
        }
    }
}
