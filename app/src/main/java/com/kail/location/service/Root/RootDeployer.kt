package com.kail.location.service.Root

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import androidx.preference.PreferenceManager
import com.kail.location.inject.utils.RootControlPaths
import com.kail.location.utils.KailLog
import com.kail.location.utils.ShellUtils
import com.kail.location.utils.SimulationDiagnostics
import com.kail.location.viewmodels.SettingsViewModel
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * Helper for the "root" run mode.
 *
 * What this does on every service start:
 *   - Creates the kail staging directories with permissive SELinux labels.
 *   - Copies libkail_native_hook.so out of the APK into /data/local/kail-lib/
 *     so the in-app NativeSensorHook can dlopen it for step-counter mocking.
 *   - Grants the host package the AppOps `android:mock_location` permission
 *     via `appops set` so [com.kail.location.service.Developer.MockLocationProvider]
 *     can register a test provider without the user manually flipping
 *     "select mock location app" in Developer Settings.
 *
 * What this deliberately does NOT do:
 *   - Run kail_inject against system_server. Ptrace-injecting system_server
 *     is extremely fragile on production ROMs: a single mismatch between the
 *     loader's expected ART layout and the running framework version freezes
 *     the entire phone (system_server hangs in ptrace_stop, every UI thread
 *     blocks on it). The kail FakeLocation injection framework lives under
 *     [FAKELOC_DIR] and the injector binary lives at [STAGING_DIR]/kail_inject
 *     for operators who want to run it manually after vetting it on their
 *     specific ROM, but the controller app does not auto-run them.
 *
 * The opt-in helpers [stageInjectionPayloads] and [bootstrapInjection] are
 * provided so a developer can wire them to a manual button in the UI later.
 */
object RootDeployer {
    private const val TAG = "RootDeployer"

    const val STAGING_DIR = "/data/local/kail-lib"
    const val FAKELOC_DIR = "/data/kail-loc"
    const val RUNTIME_DIR = "/data/system/kail-loc"
    const val NATIVE_HOOK_SO = "libkail_native_hook.so"
    const val INJECTOR_BIN = "kail_inject"
    private const val INJECTION_STATE_FILE = "$RUNTIME_DIR/injection_state.txt"
    private const val ZYGOTE_STATE_FILE = "$RUNTIME_DIR/zygote_state.txt"
    private const val BOOTSTRAP_STATE_FILE = "$RUNTIME_DIR/injectdex_state.txt"
    private const val RUNTIME_FAKELOC_INIT_LOG = "$RUNTIME_DIR/fakeloc_init.log"
    private const val RUNTIME_LHOOKER_INIT_LOG = "$RUNTIME_DIR/lhooker_init.log"
    private const val ROOT_SHORT_TIMEOUT_MS = 15_000L
    private const val ROOT_COPY_TIMEOUT_MS = 30_000L
    private const val ROOT_INJECT_TIMEOUT_MS = 135_000L

    /**
     * 部署/注入互斥锁：App 启动时的预热注入（KailPreInject）与 ServiceGoRoot 自身的
     * ensureBaseline( Diagnosed) 可能并发，用同一把锁串行化，避免文件部署/注入互踩。
     */
    private val DEPLOY_LOCK = Any()

    /** FakeLocation loader/hook libraries packaged in the APK under lib/<abi>/. */
    private val FAKELOC_LIBS = listOf(
        "libfakeloc_init.so",
        "libfakeloc_initzygote.so",
        "libfakeloc_apphook.so",
        "liblhooker.so",
        "libStepSensor.so",
        "libantidetect.so"
    )

    /**
     * Idempotent setup that the service runs at every start.
     *
     * Stages the FakeLocation toolchain on disk, and runs `kail_inject` against system_server to
     * register the service_mock_* binders (matching the original FakeLocation
     * behaviour). The injector now has a 5-second watchdog (see
     * cpp/root/inject{,64}.cpp) so a hung remote dlopen detaches the tracee
     * cleanly instead of leaving system_server in ptrace_stop.
     */
    fun ensureBaseline(context: Context): Boolean = synchronized(DEPLOY_LOCK) {
        if (!ShellUtils.hasRoot()) {
            KailLog.w(null, TAG, "ensureBaseline: no root; skipping")
            return@synchronized false
        }

        // ── 1. 内容已过期（某文件缺了/内容哈希不一致）→ 清空两个目录后全量重部署 ──
        if (!isInjectionStaged(context)) {
            val h = currentDeployHash(context)
            KailLog.i(null, TAG, "ensureBaseline: deploying toolchain hash=$h")
            resetDeployDirs()
            syncInjectLogMarkers(context)
            deployNativeHookLib(context)
            deployInjectorBin(context)
            deployFakelocLibs(context)
            deployDexPayload(context)
            KailLog.i(null, TAG, "ensureBaseline: full deploy complete (hash=$h)")
        } else {
            // Files are current — keep inject.dex atomically in sync for
            // per-app injection (which loads the dex on every hookApplication).
            refreshDexPayloadAtomic(context)
        }

        // ── 1.5 补部署 LAntiDetect 库（不受"已就绪"判断影响）──
        ensureAntiDetectLib(context)

        // ── 2. 注入 — 仅当本次开机注入仍"有效"时才跳过；标记匹配但控制线程已无响应时强制重注入 ──
        if (isSystemServerInjectionEffective(context)) {
            KailLog.i(null, TAG, "ensureBaseline: injection already current & control thread alive; skip ptrace")
        } else {
            runCatching {
                if (bootstrapInjection(context)) markSystemServerInjectionCurrent(context)
            }.onFailure { KailLog.w(null, TAG, "bootstrapInjection: ${it.message}") }
        }

        // ── 3. Zygote 注入：在 zygote 里预装 AppProcessHook 链，本开机后续 fork
        //         的 App 进程自动继承；与 system_server 注入相互独立。 ──
        ensureZygoteInjection(context)
        return@synchronized true
    }

    /**
     * 幂等的 zygote 注入：同一开机已注入（boot+版本匹配）则跳过，否则执行
     * [injectZygote]。[diag] 非空时把结果写入诊断步骤。
     */
    private fun ensureZygoteInjection(context: Context, diag: SimulationDiagnostics? = null) {
        if (isZygoteInjectionCurrent(context)) {
            KailLog.i(null, TAG, "ensureBaseline: zygote injection already current this boot; skip ptrace")
            diag?.step("zygote 注入", true, "同一开机已注入过（boot+版本匹配），跳过重复 ptrace")
            return
        }
        val ok = runCatching { injectZygote(context) }.getOrElse {
            KailLog.w(null, TAG, "injectZygote: ${it.message}")
            false
        }
        diag?.step(
            "zygote 注入", ok,
            if (ok) "libfakeloc_initzygote -> zygote 进程 Inject ok，后续 fork 的 App 自动继承 hook"
            else "注入失败（详情见 KailLog）"
        )
    }

    /**
     * 带诊断的 ensureBaseline：每一步都记入 [diag]，便于用户排障时一眼定位失败点。
     * 返回是否成功跑完注入引导（不代表 binder 一定就绪，后续由调用方核对）。
     */
    fun ensureBaselineDiagnosed(context: Context, diag: SimulationDiagnostics): Boolean = synchronized(DEPLOY_LOCK) {
        val rooted = ShellUtils.hasRoot()
        diag.step(
            "ROOT 权限",
            rooted,
            if (rooted) "su 可用"
            else "su 调用失败——应用未获 ROOT 授权（请在 KernelSU/Magisk 管理器里授权）"
        )
        if (!rooted) {
            KailLog.w(null, TAG, "ensureBaseline: no root; skipping")
            return@synchronized false
        }

        ensureAntiDetectLib(context)

        if (isSystemServerInjectionEffective(context)) {
            diag.step("system_server 注入活性", true, "控制线程 ack 心跳正常，跳过重新注入")
            diag.step("ptrace 注入 system_server", true, "同一开机/system_server PID 已注入过，跳过部署和重复 ptrace")
            ensureZygoteInjection(context, diag)
            return@synchronized true
        }
        diag.step("system_server 注入活性", false, "控制线程无响应或注入标记失效，强制重新注入")

        runCatching { resetDeployDirs() }
            .onSuccess { diag.step("准备目录", true, "$STAGING_DIR / $FAKELOC_DIR (重置→部署)") }
            .onFailure { diag.error("准备目录", it) }

        syncInjectLogMarkers(context)

        val nativeOk = runCatching { deployNativeHookLib(context) }.getOrDefault(false)
        diag.step("部署 native hook 库", nativeOk, NATIVE_HOOK_SO)

        val injectorOk = runCatching { deployInjectorBin(context) }.getOrDefault(false)
        diag.step("部署注入器", injectorOk, INJECTOR_BIN)

        val loaderOk = runCatching { deployFakelocLibs(context) }.getOrDefault(false)
        diag.step("部署 FakeLocation 注入库", loaderOk, "libfakeloc_init.so 等 ${FAKELOC_LIBS.size} 个")

        val dexOk = runCatching { deployDexPayload(context) }.getOrDefault(false)
        diag.step("部署 inject.dex", dexOk, "libfakeloc.so (slim dex)")

        val (injected, injectDetail) = runCatching { bootstrapInjectionVerbose(context) }.getOrElse {
            diag.error("ptrace 注入 system_server", it)
            false to "注入抛异常：${it.message}"
        }
        if (injected) markSystemServerInjectionCurrent(context)
        diag.step("ptrace 注入 system_server", injected, injectDetail)
        ensureZygoteInjection(context, diag)
        return@synchronized injected
    }

    /** 注入态日志标记目录（与 InjectLog 中的常量保持一致）。 */
    private const val INJECT_LOG_DIR = "/sdcard/Documents/KailLocation/logs"

    /**
     * 把宿主的日志开关同步成注入进程可读的标记文件。
     *
     * 注入态 Hook 运行在目标 App 进程里，读不到本应用的 SharedPreferences，
     * 因此用公共目录下的标记文件传递开关（见 [com.kail.location.inject.utils.InjectLog]）：
     *   .kail_debug    -> 启用日志
     *   .kail_log_file -> 额外落盘
     *   .kail_verbose  -> 启用高频(V)详细日志
     */
    fun syncInjectLogMarkers(context: Context) {
        runCatching {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val logEnabled = prefs.getBoolean(SettingsViewModel.KEY_LOG_ENABLED, false)
            val debugEnabled = prefs.getBoolean(SettingsViewModel.KEY_DEBUG_LOG_ENABLED, false)
            // 详细调试隐含开启基础日志。
            val enabled = logEnabled || debugEnabled
            val clearPublicLogs = "rm -f $INJECT_LOG_DIR/kail_log_* $INJECT_LOG_DIR/*.log"
            val clearDownloadLogcat =
                "rm -f /sdcard/Download/logs/${context.packageName}_logcat*.txt " +
                    "/sdcard/Downloads/logs/${context.packageName}_logcat*.txt"
            val cmd = if (enabled) {
                listOf(
                    clearDownloadLogcat,
                    "mkdir -p $INJECT_LOG_DIR",
                    "chmod 777 $INJECT_LOG_DIR",
                    if (logEnabled) ":" else clearPublicLogs,
                    markerCommand("$INJECT_LOG_DIR/.kail_debug", true),
                    markerCommand("$INJECT_LOG_DIR/.kail_log_file", logEnabled),
                    markerCommand("$INJECT_LOG_DIR/.kail_verbose", debugEnabled)
                ).joinToString(" && ")
            } else {
                "$clearDownloadLogcat && rm -f $INJECT_LOG_DIR/.kail_debug $INJECT_LOG_DIR/.kail_log_file $INJECT_LOG_DIR/.kail_verbose $INJECT_LOG_DIR/kail_log_* $INJECT_LOG_DIR/*.log"
            }
            rootCmd(cmd)
            KailLog.i(context, TAG, "syncInjectLogMarkers: enabled=$enabled file=$logEnabled verbose=$debugEnabled")
        }.onFailure { KailLog.w(context, TAG, "syncInjectLogMarkers: ${it.message}") }
    }

    private fun markerCommand(path: String, on: Boolean): String {
        return if (on) "touch $path && chmod 666 $path" else "rm -f $path"
    }

    private fun rootCmd(command: String, timeoutMs: Long = ROOT_SHORT_TIMEOUT_MS): String {
        return ShellUtils.executeCommand(command, timeoutMs)
    }

    /**
     * Run kail_inject against system_server to register the FakeLocation
     * service_mock_* binders.
     *
     * The injector has a 5-second watchdog (see cpp/root/inject{,64}.cpp) that
     * trips PTRACE_DETACH if a remote function hangs — typically when the
     * remote dlopen blocks on a linker mutex held by a sibling thread. With
     * the watchdog, a hung inject leaves system_server runnable instead of
     * permafrozen, at the cost of an "Inject fail" return.
     */
    fun bootstrapInjection(): Boolean {
        return bootstrapInjectionVerbose(null).first
    }

    fun bootstrapInjection(context: Context): Boolean {
        return bootstrapInjectionVerbose(context).first
    }

    /**
     * Like [bootstrapInjection] but also returns the injector's raw stdout/stderr
     * so the diagnostics layer can surface the exact failure (watchdog trip,
     * remote dlopen hang, "Inject fail", missing files, …) instead of a generic
     * guess. Returns (success, humanReadableDetail).
     */
    fun bootstrapInjectionVerbose(): Pair<Boolean, String> {
        return bootstrapInjectionVerbose(null)
    }

    fun bootstrapInjectionVerbose(context: Context?): Pair<Boolean, String> {
        // 不改动 SELinux：不 setenforce 0，也不 setenforce 1，保持系统原本状态。
        return try {
            if (!ShellUtils.hasRoot()) return false to "su 不可用（未授权 ROOT）"
            val injector: File
            val initLoader: File
            if (context != null) {
                injector = File(STAGING_DIR, INJECTOR_BIN)
                initLoader = File(FAKELOC_DIR, "libfakeloc_init.so")
            } else {
                injector = File(STAGING_DIR, INJECTOR_BIN)
                initLoader = File(FAKELOC_DIR, "libfakeloc_init.so")
            }
            if (!injector.exists()) {
                val msg = "注入器缺失：${injector.absolutePath}（部署失败？）"
                KailLog.e(null, TAG, "bootstrapInjection: $msg")
                return false to msg
            }
            if (!initLoader.exists()) {
                val msg = "加载器缺失：${initLoader.absolutePath}（部署失败？）"
                KailLog.e(null, TAG, "bootstrapInjection: $msg")
                return false to msg
            }
            disableStaleRootControls()
            clearInjectionRuntimeFiles(context)

            // --- 先试 ptrace 注入 ---
            val sessionLHooker = prepareSessionLHooker(context)
            val sessionArg = if (sessionLHooker.isNullOrBlank()) "" else " -a ${shellQuote(sessionLHooker)}"
            val cmd = "${injector.absolutePath} -P system_server -l ${initLoader.absolutePath} -n com.kail.location$sessionArg"
            val out = rootCmd(cmd, ROOT_INJECT_TIMEOUT_MS).trim()
            KailLog.i(null, TAG, "kail_inject -> $out")
            val injectorOk = out.contains("Inject ok")
            val bootstrapSignal = if (injectorOk) waitForJavaBootstrapSignal(context) else null
            val ok = injectorOk && bootstrapSignal != null
            if (ok) {
                val detail = "kail_inject 返回 Inject ok；$bootstrapSignal"
                KailLog.i(null, TAG, "bootstrapInjection: ptrace 注入成功")
                return true to detail
            }

            val ptraceDetail = when {
                injectorOk ->
                    "kail_inject 返回 Inject ok，但未看到 Java bootstrap/control ack；这次不记录已注入，避免下次跳过 ptrace。原始输出：$out"
                out.contains("watchdog", ignoreCase = true) ->
                    "注入超时：远程函数未返回，watchdog 触发（system_server 繁忙/刚开机未就绪）。原始输出：$out"
                out.contains("fail", ignoreCase = true) ->
                    "注入器返回失败。原始输出：$out"
                out.isBlank() ->
                    "注入器无输出（su 被拒/进程被杀？）"
                else -> "注入未确认成功。原始输出：$out"
            }
            KailLog.w(null, TAG, "bootstrapInjection: ptrace 注入失败，尝试 Xposed 桥接")

            // --- ptrace 失败，回退到 Xposed 桥接 ---
            val fallbackResult = tryXposedBridgeInjection(context)
            if (fallbackResult) {
                KailLog.i(null, TAG, "bootstrapInjection: Xposed 桥接成功 (ptrace fallback)")
                return true to "Xposed 桥接加载 inject.dex 成功（ptrace 失败后回退）"
            }
            KailLog.w(null, TAG, "bootstrapInjection: Xposed 桥接也不可用")
            false to "ptrace 注入失败（$ptraceDetail），Xposed 桥接也不可用"
        } finally {
            // 不改动 SELinux。
        }
    }

    private fun clearInjectionRuntimeFiles(context: Context?) {
        val controlFile = context?.let { RootControlPaths.controlPath(it) } ?: RootControlPaths.LEGACY_CONTROL_PATH
        val ackFile = context?.let { RootControlPaths.ackPath(it) } ?: RootControlPaths.LEGACY_ACK_PATH
        rootCmd(
            "mkdir -p $RUNTIME_DIR && chmod 777 $RUNTIME_DIR && " +
                "rm -f $RUNTIME_FAKELOC_INIT_LOG $BOOTSTRAP_STATE_FILE " +
                "$controlFile $ackFile $RUNTIME_LHOOKER_INIT_LOG " +
                "$INJECTION_STATE_FILE $FAKELOC_DIR/fakeloc_init.log"
        )
    }

    private fun disableStaleRootControls() {
        val payload = "enabled=0\n"
        rootCmd(
            "mkdir -p $RUNTIME_DIR && chmod 777 $RUNTIME_DIR && " +
                "for f in $RUNTIME_DIR/location_control*.txt; do " +
                "[ -e \"\$f\" ] || continue; " +
                "case \"\$f\" in *location_control_ack*) continue;; esac; " +
                "printf '%s' ${shellQuote(payload)} > \"\$f\"; " +
                "chmod 666 \"\$f\"; " +
                "chcon u:object_r:system_data_file:s0 \"\$f\" 2>/dev/null || true; " +
                "done",
            timeoutMs = 1500L
        )
        // 删除历史版本按 versionName 生成的 location_control_*.txt / ack 残留
        // （现在是固定文件名 location_control.txt，见 RootControlPaths）。
        rootCmd(
            "rm -f $RUNTIME_DIR/location_control_*.txt $RUNTIME_DIR/location_control_ack_*.txt 2>/dev/null || true",
            timeoutMs = 1500L
        )
    }

    /**
     * Fallback when ptrace injection fails: use the Xposed module's
     * "load_dex" command (via sendExtraCommand, same IPC channel as
     * [ServiceGoXposed]) to load InjectDex.init() inside system_server.
     *
     * Returns true when the module loaded the dex and the "oem_location"
     * binder subsequently appeared in ServiceManager.
     */
    private fun tryXposedBridgeInjection(context: Context?): Boolean {
        val ctx = context ?: return false
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        // Step 1: exchange key (probes Xposed module + gets auth token)
        val key = runCatching {
            val extras = Bundle()
            if (!lm.sendExtraCommand("kail", "exchange_key", extras)) return@runCatching null
            extras.getString("key")
        }.getOrNull() ?: run {
            KailLog.w(null, TAG, "tryXposedBridge: exchange_key failed — Xposed module not responding")
            return false
        }
        KailLog.i(null, TAG, "tryXposedBridge: key exchanged")

        // Step 2: call load_dex
        val dexPath = File(FAKELOC_DIR, "libfakeloc.so").absolutePath
        val className = "com.kail.location.inject.fakelocation.InjectDex"
        val nativeLibDir = FAKELOC_DIR
        val loadOk = runCatching {
            val extras = Bundle()
            extras.putString("command_id", "load_dex")
            extras.putString("dex_path", dexPath)
            extras.putString("class_name", className)
            extras.putString("native_lib_dir", nativeLibDir)
            lm.sendExtraCommand("kail", key, extras)
        }.getOrDefault(false)

        if (!loadOk) {
            KailLog.w(null, TAG, "tryXposedBridge: load_dex command rejected")
            return false
        }
        KailLog.i(null, TAG, "tryXposedBridge: load_dex sent, waiting for injectdex_state")

        // Step 3: wait for InjectDex.init() to write bootstrap state.
        // oem_location binder may be registered but SELinux blocks find from
        // untrusted_app on Android 14+, so check the state file instead.
        val deadline = System.currentTimeMillis() + 7000L
        while (System.currentTimeMillis() < deadline) {
            val state = rootCmd("cat $BOOTSTRAP_STATE_FILE 2>/dev/null", 1500L).trim()
            if (state.contains("finished")) {
                KailLog.i(null, TAG, "tryXposedBridge: injectdex_state shows finished")
                return true
            }
            if (state.contains("error") || state.contains("aborted")) {
                KailLog.w(null, TAG, "tryXposedBridge: injectdex_state shows failure: ${state.take(200)}")
                return false
            }
            Thread.sleep(300)
        }
        KailLog.w(null, TAG, "tryXposedBridge: injectdex_state did not appear within 7s")
        return false
    }

    private fun waitForJavaBootstrapSignal(context: Context?, timeoutMs: Long = 4000L): String? {
        val ackFile = context?.let { RootControlPaths.ackPath(it) } ?: RootControlPaths.LEGACY_ACK_PATH
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val ack = rootCmd("cat $ackFile 2>/dev/null", 1500L).trim()
            if (ack.isNotBlank()) {
                val status = parseKeyValue(ack)["status"] ?: "unknown"
                return "控制线程 ack=$status"
            }

            val state = rootCmd("cat $BOOTSTRAP_STATE_FILE 2>/dev/null", 1500L).trim()
            if (state.isNotBlank()) {
                val events = state.lineSequence()
                    .mapNotNull { line -> line.removePrefix("event=").takeIf { it != line } }
                    .toList()
                val lastEvent = events.lastOrNull().orEmpty()
                if (events.any { it.contains("root_location_control_start_called") || it.startsWith("add_service") || it == "finished" }) {
                    return "Java bootstrap 已到达 $lastEvent"
                }
                if (lastEvent.contains("error", ignoreCase = true) || lastEvent.contains("aborted", ignoreCase = true)) {
                    KailLog.w(null, TAG, "Java bootstrap reported failure: $lastEvent")
                    return null
                }
            }
            Thread.sleep(250L)
        }
        return null
    }

    private fun parseKeyValue(raw: String): Map<String, String> {
        return raw.lineSequence().mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
    }

    /**
     * Inject the FakeLocation app-hook loader into an arbitrary running
     * process by name. Used to bring the cell-tower pull APIs
     * (TelephonyManager.getAllCellInfo / getCellLocation, served by
     * com.android.phone's PhoneInterfaceManager) under
     * [com.kail.location.inject.fakelocation.hook.phone.PhoneInterfaceManagerHook].
     *
     * The system_server inject (registering the service_mock_* binders) is a
     * prerequisite — the app-hook InjectDex.hookApplication path reads mock
     * state through those binders. Safe to call repeatedly; injecting an
     * already-hooked process just re-runs hookApplication which no-ops the
     * already-installed hooks.
     */
    fun injectAppProcess(context: Context, processName: String): Boolean {
        if (!ShellUtils.hasRoot()) return false
        val injector = File(STAGING_DIR, INJECTOR_BIN)
        // libfakeloc_apphook.so -> InjectDex.hookApplication (installs the
        // per-process hooks, including PhoneInterfaceManagerHook for phone).
        val appLoader = File(FAKELOC_DIR, "libfakeloc_apphook.so")
        if (!injector.exists() || !appLoader.exists()) {
            KailLog.e(null, TAG, "injectAppProcess: injector or apphook loader missing")
            return false
        }
        val cmd = "${injector.absolutePath} -P $processName -l ${appLoader.absolutePath} -n com.kail.location"
        val out = rootCmd(cmd, ROOT_INJECT_TIMEOUT_MS)
        KailLog.i(null, TAG, "kail_inject ($processName) -> $out")
        if (out.contains("Inject ok")) return true

        // The by-name path (findPidByProcessName, inject64.cpp:69) scans /proc
        // and attaches to the FIRST exact-cmdline match. When the target is in
        // the middle of a restart (post force-stop relaunch), that match can be
        // racy/wrong and the injector exits with 0 bytes. Fall back to
        // enumerating the package's exact-cmdline PIDs and injecting each by
        // PID (-p) until one succeeds.
        KailLog.w(null, TAG, "injectAppProcess: by-name failed for $processName; trying per-PID fallback")
        for (pid in resolveProcessPids(processName)) {
            val pidCmd = "${injector.absolutePath} -p $pid -l ${appLoader.absolutePath} -n com.kail.location"
            val pidOut = rootCmd(pidCmd, ROOT_INJECT_TIMEOUT_MS)
            KailLog.i(null, TAG, "kail_inject ($processName pid=$pid) -> $pidOut")
            if (pidOut.contains("Inject ok")) return true
        }
        return false
    }

    /**
     * Resolve a process's PIDs for the per-PID injection fallback.
     *
     * 优先用 `pidof`：按进程名精确匹配、毫秒级返回，覆盖绝大多数情况。只有 `pidof`
     * 拿不到时才退回 `exactCmdlinePids` 的 /proc cmdline 扫描——那个 shell 循环在进程多
     * 或系统繁忙时容易超过 15s 超时（之前 app-hook 注入失败的元凶）。
     */
    private fun resolveProcessPids(processName: String): List<Int> {
        val pids = LinkedHashSet<Int>()
        runCatching {
            rootCmd("pidof $processName 2>/dev/null", 3000L).trim()
                .split(Regex("\\s+"))
                .mapNotNull { it.toIntOrNull() }
                .forEach { pids.add(it) }
        }
        if (pids.isNotEmpty()) return pids.toList()
        return exactCmdlinePids(processName)
    }

    /**
     * Inject libfakeloc_initzygote into the zygote process, so the hook chain
     * (InjectDex.initZygote -> AppProcessHook.hook on handleBindApplication)
     * is inherited by every subsequently forked app process — no per-app
     * ptrace needed after a reboot.
     *
     * Targets the zygote flavor matching the device's primary ABI
     * (zygote64 on 64-bit devices), driven by PID (-p) which bypasses the
     * injector's exact-cmdline name match. The zygote injection is independent
     * of the system_server one; it may run even if the latter failed.
     */
    fun injectZygote(context: Context): Boolean {
        if (!ShellUtils.hasRoot()) return false
        val injector = File(STAGING_DIR, INJECTOR_BIN)
        val zygoteLoader = File(FAKELOC_DIR, "libfakeloc_initzygote.so")
        if (!injector.exists() || !zygoteLoader.exists()) {
            KailLog.e(null, TAG, "injectZygote: injector or zygote loader missing")
            return false
        }
        // The injector and the loader are deployed for the device's primary ABI
        // only, so target the matching zygote flavor (zygote64 on 64-bit
        // devices). PTRACE across bitness would corrupt regs reads.
        val name = zygoteProcessName()
        val pid = findZygotePid(name) ?: run {
            KailLog.w(null, TAG, "injectZygote: no $name process found")
            return false
        }
        val cmd = "${injector.absolutePath} -Z -p $pid -l ${zygoteLoader.absolutePath} -n com.kail.location"
        val out = rootCmd(cmd, ROOT_INJECT_TIMEOUT_MS)
        KailLog.i(null, TAG, "kail_inject ($name pid=$pid) -> $out")
        if (!out.contains("Inject ok")) {
            KailLog.w(null, TAG, "injectZygote: failed: $out")
            return false
        }
        markZygoteInjectionCurrent(context, listOf(pid))
        return true
    }

    private fun findZygotePid(name: String): String? {
        return rootCmd("pidof $name 2>/dev/null", 1500L).trim()
            .split(Regex("\\s+"))
            .firstOrNull { it.isNotBlank() && it.all(Char::isDigit) }
    }

    private fun isZygoteInjectionCurrent(context: Context): Boolean {
        val raw = rootCmd("cat $ZYGOTE_STATE_FILE 2>/dev/null", 1500L).trim()
        if (raw.isBlank()) return false
        val values = raw.lineSequence().mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        val boot = values["kernel_btime_sec"]?.toLongOrNull() ?: return false
        val deployHash = values["deploy_hash"]?.trim() ?: return false
        val recordedPids = values["zygote_pid"]?.trim()?.split(",")?.map { it.trim() }.orEmpty()
        val currentHash = currentDeployHash(context)
        val current = boot > 0 &&
            boot == kernelBootTimeSec() &&
            deployHash.isNotEmpty() &&
            deployHash == currentHash &&
            recordedPids.isNotEmpty() &&
            recordedPids.any { it == findZygotePid(zygoteProcessName()) }
        if (!current) {
            KailLog.i(
                null, TAG,
                "zygote state stale: state=$values boot=$boot hash=$deployHash currentHash=$currentHash " +
                    "currentZygote=${findZygotePid(zygoteProcessName())} recorded=${recordedPids.joinToString(",")}"
            )
        }
        return current
    }

    /** zygote 进程名：按设备主 ABI 匹配（与 [injectZygote] 一致）。 */
    private fun zygoteProcessName(): String =
        if (preferredAbi() == "arm64-v8a" || preferredAbi() == "x86_64") "zygote64" else "zygote"

    private fun markZygoteInjectionCurrent(context: Context, zygotePids: List<String>) {
        if (zygotePids.isEmpty()) return
        val boot = kernelBootTimeSec()
        if (boot <= 0) return
        val deployHash = currentDeployHash(context)
        val payload = "kernel_btime_sec=$boot\n" +
            "zygote_pid=${zygotePids.joinToString(",")}\n" +
            "deploy_hash=$deployHash\n" +
            "wallclock_ms=${System.currentTimeMillis()}\n"
        rootCmd(
            "printf '%s' ${shellQuote(payload)} > $ZYGOTE_STATE_FILE && " +
                "chmod 666 $ZYGOTE_STATE_FILE && chcon u:object_r:system_data_file:s0 $ZYGOTE_STATE_FILE 2>/dev/null || true"
        )
        KailLog.i(
            null, TAG,
            "zygote injection marked current: boot=$boot pids=${zygotePids.joinToString(",")} hash=$deployHash"
        )
    }

    /**
     * Root helper that lists /proc for processes whose cmdline equals
     * [processName] exactly (the "main" process of a package — child
     * processes carry a ':name' suffix and are excluded). Returns PIDs in
     * ascending order.
     */
    private fun exactCmdlinePids(processName: String): List<Int> {
        if (processName.isEmpty()) return emptyList()
        return runCatching {
            val script = "for d in /proc/[0-9]*; do " +
                "c=\$(tr '\\0' ' ' < \"\$d/cmdline\" 2>/dev/null); " +
                "case \" \$c \" in \" $processName \"*) p=\"\${d#/proc/}\"; echo \"\$p\";; esac; done"
            rootCmd(script, ROOT_SHORT_TIMEOUT_MS)
                .lineSequence()
                .mapNotNull { it.trim().toIntOrNull() }
                .distinct()
                .sorted()
                .toList()
        }.getOrElse {
            KailLog.w(null, TAG, "exactCmdlinePids failed: ${it.message}")
            emptyList()
        }
    }

    /**
     * Side-effect free check for whether the ptrace-injection prerequisites
     * have already been staged on disk. 现场取哈希对比：每个关键文件与 APK 内源
     * 的内容 md5 一致才算就绪；任一不一致 → 全量重部署。
     */
    fun isInjectionStaged(context: Context): Boolean {
        val checks = listOf(
            "$FAKELOC_DIR/libfakeloc.so" to payloadHash(context),
            "$FAKELOC_DIR/libfakeloc_init.so" to deployHash(context, "libfakeloc_init.so"),
            "$FAKELOC_DIR/libfakeloc_initzygote.so" to deployHash(context, "libfakeloc_initzygote.so"),
            "$STAGING_DIR/$INJECTOR_BIN" to deployHash(context, "libkail_inject.so")
        )
        for ((path, expected) in checks) {
            if (expected.isEmpty()) return false
            if (fileMd5OnDevice(path) != expected) return false
        }
        return true
    }

    private data class InjectionState(
        val bootTimeSec: Long,
        val systemServerPid: String,
        val deployHash: String
    )

    // ------------------------------------------------------------------
    // Building blocks
    // ------------------------------------------------------------------

    fun deployNativeHookLib(context: Context): Boolean {
        val src = File(context.applicationInfo.nativeLibraryDir, NATIVE_HOOK_SO)
        val versionedDst = File(STAGING_DIR, NATIVE_HOOK_SO)
        val ok = copyAndChmod(context, src, "lib/${preferredAbi()}/$NATIVE_HOOK_SO", versionedDst)
        // Also stage a copy under FAKELOC_DIR so it can be System.load()ed from
        // inside system_server by the inject. system_server may already have the
        // file mapped, so only write it when missing/stale (hash mismatch) —
        // truncating a mapped file crashes the process on the next page fault.
        runCatching {
            val fakelocDst = File(FAKELOC_DIR, NATIVE_HOOK_SO)
            val expected = deployHash(context, NATIVE_HOOK_SO)
            val current = fileMd5OnDevice(fakelocDst.absolutePath)
            if (fakelocDst.length() <= 0L || expected.isNotEmpty() && current != expected) {
                rootCmd("cp -f ${versionedDst.absolutePath} ${fakelocDst.absolutePath}", ROOT_COPY_TIMEOUT_MS)
                rootCmd("chmod 644 ${fakelocDst.absolutePath}")
                rootCmd("chcon u:object_r:system_file:s0 ${fakelocDst.absolutePath} 2>/dev/null || true")
            } else {
                KailLog.i(null, TAG, "deployNativeHookLib: keep existing mapped-safe copy ${fakelocDst.absolutePath}")
            }
        }.onFailure { KailLog.w(null, TAG, "stage native hook into FAKELOC_DIR: ${it.message}") }
        return ok
    }

    fun deployInjectorBin(context: Context): Boolean {
        val abi = preferredAbi()
        val src = File(context.applicationInfo.nativeLibraryDir, "libkail_inject.so")
        val versioned = File(STAGING_DIR, INJECTOR_BIN)
        val ok = copyAndChmod(context, src, "lib/$abi/libkail_inject.so", versioned)
        if (ok) rootCmd("chmod 755 ${versioned.absolutePath}")
        return ok
    }

    /**
     * 确保 LAntiDetect 依赖的 libantidetect.so / libantidetect64.so 已部署到
     * FAKELOC_DIR（无版本号——LAntiDetect.loadAndInitialize 硬编码
     * System.load("/data/kail-loc/libantidetect64.so")）。
     *
     * 独立于"注入已就绪"判断：版本号不变时全量部署会跳过，但这份库缺了
     * "隐藏应用列表"的原生层就起不来（UnsatisfiedLinkError）。
     */
    fun ensureAntiDetectLib(context: Context): Boolean {
        val plain = File(FAKELOC_DIR, "libantidetect.so")
        val plain64 = File(FAKELOC_DIR, "libantidetect64.so")
        if (plain.exists() && plain.length() > 0L && plain64.exists() && plain64.length() > 0L) {
            return true
        }
        val src = File(context.applicationInfo.nativeLibraryDir, "libantidetect.so")
        if (!src.exists()) {
            KailLog.w(null, TAG, "ensureAntiDetectLib: APK 无 libantidetect.so")
            return false
        }
        rootCmd("mkdir -p $FAKELOC_DIR && chmod 777 $FAKELOC_DIR", ROOT_SHORT_TIMEOUT_MS)
        val ok = copyAndChmod(context, src, "lib/${preferredAbi()}/libantidetect.so", plain)
        if (ok) {
            rootCmd(
                "cp -f ${plain.absolutePath} ${plain64.absolutePath} && " +
                    "chmod 644 ${plain.absolutePath} ${plain64.absolutePath} && " +
                    "chcon u:object_r:system_file:s0 ${plain.absolutePath} ${plain64.absolutePath} 2>/dev/null || true",
                ROOT_COPY_TIMEOUT_MS
            )
            KailLog.i(null, TAG, "ensureAntiDetectLib: staged libantidetect for LAntiDetect")
        }
        return ok
    }

    fun deployFakelocLibs(context: Context): Boolean {        var initLoader = false
        val abi = preferredAbi()
        val isArm64 = abi == "arm64-v8a"
        for (name in FAKELOC_LIBS) {
            val src = File(context.applicationInfo.nativeLibraryDir, name)
            val versioned = File(FAKELOC_DIR, name)
            val ok = copyAndChmod(context, src, "lib/$abi/$name", versioned)
            if (ok && name == "libfakeloc_init.so") initLoader = true

            // InjectDex.java probes both `<name>.so` (arm) and `<name>64.so`
            // (arm64) without checking which side actually exists. Mirror the
            // file under the matching suffix for the active ABI so the lookup
            // succeeds regardless of which path it picks first.
            if (ok && isArm64 && !name.contains("64.so")) {
                val sixtyFour = name.replace(".so", "64.so")
                val mirror = File(FAKELOC_DIR, sixtyFour)
                rootCmd("cp -f ${versioned.absolutePath} ${mirror.absolutePath}", ROOT_COPY_TIMEOUT_MS)
                rootCmd("chmod 777 ${mirror.absolutePath}")
                rootCmd("chcon u:object_r:system_file:s0 ${mirror.absolutePath} 2>/dev/null || true")
            }
            // x86_64: LHooker expects liblhookerx64.so for the entry-point hook
            // engine; mirror the freshly deployed liblhooker.so under that name
            // so the injected bootstrap finds it on x86_64 emulators/devices.
            if (ok && abi == "x86_64" && name == "liblhooker.so") {
                val mirror = File(FAKELOC_DIR, "liblhookerx64.so")
                rootCmd("cp -f ${versioned.absolutePath} ${mirror.absolutePath}", ROOT_COPY_TIMEOUT_MS)
                rootCmd("chmod 777 ${mirror.absolutePath}")
                rootCmd("chcon u:object_r:system_file:s0 ${mirror.absolutePath} 2>/dev/null || true")
            }
            // LAntiDetect 硬编码 System.load("/data/kail-loc/libantidetect64.so")
            // （无版本号，arm 侧是 libantidetect.so），版本化副本满足不了它，
            // 额外放一份无版本号副本，否则"隐藏应用列表"的原生层起不来。
            if (ok && name == "libantidetect.so") {
                val plain = File(FAKELOC_DIR, "libantidetect.so")
                val plain64 = File(FAKELOC_DIR, "libantidetect64.so")
                rootCmd(
                    "cp -f ${versioned.absolutePath} ${plain.absolutePath} && " +
                        "cp -f ${versioned.absolutePath} ${plain64.absolutePath} && " +
                        "chmod 644 ${plain.absolutePath} ${plain64.absolutePath} && " +
                        "chcon u:object_r:system_file:s0 ${plain.absolutePath} ${plain64.absolutePath} 2>/dev/null || true",
                    ROOT_COPY_TIMEOUT_MS
                )
            }
        }
        return initLoader
    }

    /**
     * 把 /data/kail-loc/libfakeloc.so 与 APK 的 assets/inject.dex 保持同步。
     * 用"现场取哈希"判断是否需要更新：先算目标文件 md5，与 APK 内源 md5 不一致才写。
     * 写入走 temp+mv，避免截断一个正在被 mmap 的旧 dex（system_server）。
     */
    fun refreshDexPayloadAtomic(context: Context) {
        runCatching {
            val slim = File(context.cacheDir, "inject.dex")
            context.assets.open("inject.dex").use { input ->
                slim.outputStream().use { input.copyTo(it) }
            }
            if (!slim.exists() || slim.length() <= 0) return@runCatching
            val versioned = File(FAKELOC_DIR, "libfakeloc.so")
            // 现场取哈希：本地 cache 的 slim 与设备上的 payload 对比。
            val localMd5 = fileMd5(slim)
            val dstMd5 = fileMd5OnDevice(versioned.absolutePath)
            if (localMd5.isNotEmpty() && localMd5 == dstMd5) return@runCatching
            val tmp = "${versioned.absolutePath}.new"
            rootCmd("cp -f ${slim.absolutePath} $tmp", ROOT_COPY_TIMEOUT_MS)
            rootCmd("chmod 644 $tmp")
            rootCmd("chcon u:object_r:system_file:s0 $tmp 2>/dev/null || true")
            rootCmd("mv -f $tmp ${versioned.absolutePath}")
            KailLog.i(null, TAG, "refreshDexPayloadAtomic: dex updated (${slim.length()} bytes)")
        }.onFailure { KailLog.w(null, TAG, "refreshDexPayloadAtomic: ${it.message}") }
    }

    fun deployDexPayload(context: Context): Boolean {
        val versioned = File(FAKELOC_DIR, "libfakeloc.so")
        // Prefer the slim inject.dex we ship in assets — it contains only the
        // FakeLocation bootstrap classes (com.kail.location.inject.* +
        // com.kail.location.lib.lhooker.*), about 1-2 MB compared to the full
        // 33 MB APK. system_server's DexClassLoader can verify that small dex
        // in well under our 60 s ptrace watchdog window. The full APK path is
        // only used as a fallback if assets/inject.dex is missing (older builds
        // without the slim-dex Gradle task).
        val slim = runCatching {
            val out = File(context.cacheDir, "inject.dex")
            context.assets.open("inject.dex").use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            out
        }.getOrNull()

        return runCatching {
            if (slim != null && slim.exists() && slim.length() > 0) {
                rootCmd("cp -f ${slim.absolutePath} ${versioned.absolutePath}", ROOT_COPY_TIMEOUT_MS)
                KailLog.i(null, TAG, "deployDexPayload: using slim inject.dex (${slim.length()} bytes)")
            } else {
                val apkPath = context.applicationInfo.sourceDir ?: return@runCatching false
                rootCmd("cp -f $apkPath ${versioned.absolutePath}", ROOT_COPY_TIMEOUT_MS)
                KailLog.w(null, TAG, "deployDexPayload: assets/inject.dex missing; falling back to full APK ($apkPath)")
            }
            rootCmd("chmod 644 ${versioned.absolutePath}")
            rootCmd("chcon u:object_r:system_file:s0 ${versioned.absolutePath} 2>/dev/null || true")
            versioned.exists() && versioned.length() > 0
        }.getOrElse {
            KailLog.e(null, TAG, "deployDexPayload: ${it.message}")
            false
        }
    }

    /**
     * Silently grant the host package the `android:mock_location` AppOps so
     * `LocationManager.addTestProvider` works without the user toggling
     * "select mock location app" in Developer Settings.
     */
    fun grantMockLocationAppOps(context: Context): Boolean {
        val pkg = context.packageName
        return runCatching {
            rootCmd("appops set $pkg android:mock_location allow")
            // Some ROMs accept a numeric op id alias; harmless when it does not exist.
            rootCmd("appops set $pkg 58 allow")
            true
        }.getOrElse {
            KailLog.e(null, TAG, "grantMockLocationAppOps: ${it.message}")
            false
        }
    }

    /** Convenience: revoke the AppOps grant when leaving root mode. */
    fun revokeMockLocationAppOps(context: Context): Boolean {
        val pkg = context.packageName
        return runCatching {
            rootCmd("appops set $pkg android:mock_location default 2>/dev/null || appops set $pkg android:mock_location ignore")
            rootCmd("appops set $pkg 58 default 2>/dev/null || appops set $pkg 58 ignore")
            true
        }.getOrElse { false }
    }

    private fun prepareDirs() {
        runCatching {
            for (d in listOf(STAGING_DIR, FAKELOC_DIR)) {
                rootCmd("mkdir -p $d")
                rootCmd("chmod 777 $d")
                rootCmd("chcon u:object_r:system_file:s0 $d 2>/dev/null || true")
            }
            rootCmd("mkdir -p $RUNTIME_DIR")
            rootCmd("chmod 777 $RUNTIME_DIR")
            rootCmd("chcon u:object_r:system_data_file:s0 $RUNTIME_DIR 2>/dev/null || restorecon -R $RUNTIME_DIR 2>/dev/null || true")
            // libfakeloc_init.cpp uses /data/kail-loc/system_dex as the
            // DexClassLoader optimization output dir. If it doesn't exist
            // before we inject, ART falls back to compiling the 33MB APK in
            // an in-process buffer which can take >10s on cold cache and
            // sometimes never finishes (system_server gets killed by its
            // own watchdog). Pre-create it with permissive SELinux labels.
            for (d in listOf("$FAKELOC_DIR/system_dex", "$FAKELOC_DIR/oat", "$FAKELOC_DIR/zygote_dex")) {
                rootCmd("mkdir -p $d")
                rootCmd("chmod 777 $d")
                rootCmd("chcon u:object_r:system_file:s0 $d 2>/dev/null || true")
            }
        }.onFailure { KailLog.e(null, TAG, "prepareDirs: ${it.message}") }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    // 注入目标是 system_server，必须按设备的原生主 ABI 部署 so/注入器，
    // 而不是硬性优先 arm64 —— 否则在 x86_64 模拟器（SUPPORTED_ABIS 同时含
    // x86_64 与 arm64-v8a）上会错误地部署 arm64 库，导致 dlopen/寄存器不匹配。
    private fun preferredAbi(): String =
        android.os.Build.SUPPORTED_ABIS.firstOrNull()
            ?: "arm64-v8a"

    private fun isSystemServerInjectionCurrent(context: Context): Boolean {
        val state = readInjectionState() ?: return false
        val boot = kernelBootTimeSec()
        val pid = systemServerPid()
        val deployHash = currentDeployHash(context)
        val current = boot > 0 &&
            state.bootTimeSec == boot &&
            pid.isNotBlank() &&
            state.systemServerPid == pid &&
            state.deployHash.isNotEmpty() &&
            state.deployHash == deployHash
        if (!current) {
            KailLog.i(null, TAG, "injection state stale: state=$state boot=$boot pid=$pid hash=$deployHash")
        }
        return current
    }

    /**
     * 注入是否真正"有效"：除了 boot+pid+版本 标记匹配外，还要求注入到 system_server 的
     * 控制线程（RootLocationControl）确实能完整执行"启用模拟"的 apply 路径。长时运行后
     * 该线程可能卡死/退出——一次 apply 死锁即可让注入永久失效，但 system_server PID 不变，
     * 仅靠标记会误判"已注入"而跳过，导致只能重启才能恢复。这里主动探测一次：写入
     * enabled=1 的全量控制内容，等控制线程真正走完 apply（含 MockLocationHookManager/
     * dispatch loop 路径）并回写 status=applied，然后再把原控制内容写回。
     * 注意：必须探测 enable 全路径——只探测"读文件回写 ack"的轻路径会误判：线程活着
     * 但 enable 路径卡死时，轻路径（enabled=0 快路径）必然通过。
     */
    private fun isSystemServerInjectionEffective(context: Context): Boolean {
        if (!isSystemServerInjectionCurrent(context)) return false
        val alive = isInjectedControlThreadAlive(context)
        if (!alive) {
            KailLog.w(null, TAG, "injection marker current but control thread cannot apply enable — invalidating, will re-inject")
            invalidateInjectionState()
        }
        return alive
    }

    /**
     * 探测 system_server 内控制线程能否真正启用模拟（见 [isSystemServerInjectionEffective]）。
     * 用备份恢复原控制文件内容，避免探测本身留下副作用。
     */
    private fun isInjectedControlThreadAlive(context: Context, timeoutMs: Long = 10_000L): Boolean {
        val controlPath = RootControlPaths.controlPath(context)
        val ackPath = RootControlPaths.ackPath(context)
        return runCatching {
            val snapshot = rootCmd("cat $controlPath 2>/dev/null", 1500L)
            val probeContent = "enabled=1\n" +
                "lat=0.0\n" +
                "lng=0.0\n" +
                "alt=55.0\n" +
                "bearing=0.0\n" +
                "speed=0.0\n" +
                "interval=1000\n" +
                "step_enabled=0\n" +
                "step_spm=120.0\n" +
                "step_mode=0\n" +
                "step_scheme=0\n"
            rootCmd(
                "rm -f $ackPath; " +
                    "mkdir -p $RUNTIME_DIR && chmod 777 $RUNTIME_DIR && " +
                    "printf '%s' ${shellQuote(probeContent)} > $controlPath && " +
                    "chmod 666 $controlPath 2>/dev/null || true",
                1500L
            )
            val deadline = System.currentTimeMillis() + timeoutMs
            var alive = false
            while (System.currentTimeMillis() < deadline) {
                val ack = parseKeyValue(rootCmd("cat $ackPath 2>/dev/null", 1500L))
                when (ack["status"]) {
                    "applied" -> {
                        alive = true
                        break
                    }
                    "error" -> break
                }
                Thread.sleep(250L)
            }
            // 恢复原控制内容（快照为空则删掉探测文件，避免遗留 enabled=1）。
            if (snapshot.isNotBlank()) {
                rootCmd(
                    "printf '%s' ${shellQuote(snapshot)} > $controlPath && chmod 666 $controlPath 2>/dev/null || true",
                    1500L
                )
            } else {
                rootCmd("rm -f $controlPath 2>/dev/null || true", 1500L)
            }
            alive
        }.getOrDefault(false)
    }

    private fun invalidateInjectionState() {
        rootCmd("rm -f $INJECTION_STATE_FILE 2>/dev/null || true", 1500L)
    }

    private fun markSystemServerInjectionCurrent(context: Context) {
        val boot = kernelBootTimeSec()
        val pid = systemServerPid()
        if (boot <= 0 || pid.isBlank()) {
            KailLog.w(null, TAG, "mark injection skipped: boot=$boot pid=$pid")
            return
        }
        val deployHash = currentDeployHash(context)
        val payload = "kernel_btime_sec=$boot\n" +
            "system_server_pid=$pid\n" +
            "deploy_hash=$deployHash\n" +
            "wallclock_ms=${System.currentTimeMillis()}\n"
        rootCmd(
            "printf '%s' ${shellQuote(payload)} > $INJECTION_STATE_FILE && " +
                "chmod 666 $INJECTION_STATE_FILE && chcon u:object_r:system_data_file:s0 $INJECTION_STATE_FILE 2>/dev/null || true"
        )
        KailLog.i(null, TAG, "system_server injection marked current: boot=$boot pid=$pid hash=$deployHash")
    }

    private fun readInjectionState(): InjectionState? {
        val raw = rootCmd("cat $INJECTION_STATE_FILE 2>/dev/null", 1500L).trim()
        if (raw.isBlank()) return null
        val values = raw.lineSequence().mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        val boot = values["kernel_btime_sec"]?.toLongOrNull() ?: return null
        val pid = values["system_server_pid"]?.trim() ?: return null
        if (pid.isBlank()) return null
        val deployHash = values["deploy_hash"]?.trim() ?: return null
        return InjectionState(boot, pid, deployHash)
    }

    private fun resetDeployDirs() {
        rootCmd("rm -rf $FAKELOC_DIR $STAGING_DIR 2>/dev/null || true")
        prepareDirs()
    }

    private fun kernelBootTimeSec(): Long {
        return rootCmd("cat /proc/stat 2>/dev/null | grep '^btime'", 1500L).trim()
            .split(Regex("\\s+"))
            .getOrNull(1)
            ?.toLongOrNull()
            ?: -1L
    }

    private fun systemServerPid(): String {
        return rootCmd("pgrep -f system_server 2>/dev/null", 1500L).trim()
            .lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?: ""
    }

    private fun prepareSessionLHooker(context: Context?): String? {
        return runCatching {
            val resolvedName = preferredLHookerName()
            val base = File(FAKELOC_DIR, resolvedName)
            if (!base.exists() || base.length() <= 0) {
                KailLog.w(null, TAG, "prepareSessionLHooker: missing ${base.absolutePath}")
                return@runCatching null
            }
            KailLog.persist(null, TAG, "prepareSessionLHooker: ${base.absolutePath}")
            base.absolutePath
        }.getOrElse {
            KailLog.w(null, TAG, "prepareSessionLHooker: ${it.message}")
            null
        }
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
    }

    private fun preferredLHookerName(): String {
        return when (preferredAbi()) {
            "x86_64" -> "liblhookerx64.so"
            "x86" -> "liblhookerx.so"
            "arm64-v8a" -> "liblhooker64.so"
            else -> "liblhooker.so"
        }
    }

    /**
     * 把目标 so 部署到设备。优先从 APK 内按目标 ABI（preferredAbi()，即
     * system_server 的原生 ABI）的 zip 条目解压；只有解压失败时才回退到 App
     * 进程自身的 nativeLibraryDir。
     *
     * 不能反过来优先 nativeLibraryDir：在带 ARM 翻译的 x86_64 模拟器上，
     * App 进程以 arm64 运行（nativeLibraryDir=.../lib/arm64），但目标
     * system_server 是 x86_64，若把 arm64 注入器/库注入进去会因寄存器与
     * ELF 架构不匹配而失败。
     */
    private fun copyAndChmod(context: Context, src: File, zipEntry: String, dst: File): Boolean {
        return runCatching {
            val ok = if (extractFromApk(context, zipEntry, dst)) {
                true
            } else if (src.exists() && src.length() > 0) {
                rootCmd("cp -f ${src.absolutePath} ${dst.absolutePath}", ROOT_COPY_TIMEOUT_MS)
                true
            } else {
                false
            }
            if (ok) {
                rootCmd("chmod 777 ${dst.absolutePath}")
                rootCmd("chcon u:object_r:system_file:s0 ${dst.absolutePath} 2>/dev/null || true")
            }
            ok && dst.exists() && dst.length() > 0
        }.getOrElse {
            KailLog.e(null, TAG, "copyAndChmod ${dst.name}: ${it.message}")
            false
        }
    }

    private fun extractFromApk(context: Context, zipEntry: String, dst: File): Boolean {
        return runCatching {
            val apkPath = context.applicationInfo.sourceDir ?: return@runCatching false
            ZipFile(apkPath).use { zip ->
                val entry = zip.getEntry(zipEntry) ?: return@runCatching false
                zip.getInputStream(entry).use { input ->
                    dst.outputStream().use { out -> input.copyTo(out) }
                }
            }
            dst.exists() && dst.length() > 0
        }.getOrDefault(false)
    }

    // ------------------------------------------------------------------
    // 现场取哈希（内容校验）
    // ------------------------------------------------------------------

    private fun md5Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }

    /** 目标 so 的内容 md5（源：APK zip 条目优先，否则 nativeLibraryDir 文件）。 */
    private fun deployHash(context: Context, baseName: String): String {
        val abi = preferredAbi()
        val src = File(context.applicationInfo.nativeLibraryDir, baseName)
        val bytes = sourceBytes(context, "lib/$abi/$baseName", src)
        return bytes?.let { md5Hex(it) } ?: ""
    }

    /** 本地文件的内容 md5（不用 root，读 App 自己的文件）。 */
    private fun fileMd5(file: File): String = runCatching {
        md5Hex(file.readBytes())
    }.getOrDefault("")

    /** 设备上任意路径文件的内容 md5（经 root md5sum，读 /data 下部署产物）。 */
    private fun fileMd5OnDevice(path: String): String {
        return rootCmd("md5sum $path 2>/dev/null | cut -d' ' -f1", 5000L).trim()
    }

    /** inject.dex（或回退到整包 APK）的内容 md5。 */
    private fun payloadHash(context: Context): String {
        val bytes = payloadBytes(context) ?: return ""
        return md5Hex(bytes)
    }

    /** 与 [deployDexPayload] / [refreshDexPayloadAtomic] 一致的 payload 字节。 */
    private fun payloadBytes(context: Context): ByteArray? {
        val slim = runCatching {
            val out = File(context.cacheDir, "inject.dex")
            context.assets.open("inject.dex").use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            out
        }.getOrNull()
        if (slim != null && slim.exists() && slim.length() > 0) return slim.readBytes()
        val apkPath = context.applicationInfo.sourceDir ?: return null
        return runCatching { File(apkPath).readBytes() }.getOrNull()
    }

    /**
     * 当前注入载荷（system_server / zygote 通用）的指纹：由 payload + 注入器 + init 加载器
     * 的内容哈希共同决定。任何 so 或 dex 内容一变，指纹即变，强制下次服务启动重新注入。
     */
    private fun currentDeployHash(context: Context): String {
        val parts = listOf(
            payloadHash(context),
            deployHash(context, "libkail_inject.so"),
            deployHash(context, "libfakeloc_init.so"),
            deployHash(context, "libfakeloc_initzygote.so"),
            deployHash(context, "liblhooker.so")
        )
        if (parts.any { it.isEmpty() }) return ""
        return md5Hex(parts.joinToString("|").toByteArray())
    }

    /** 读取源字节：优先 APK zip 条目，否则 nativeLibraryDir 文件（与 copyAndChmod 同源）。 */
    private fun sourceBytes(context: Context, zipEntry: String, src: File): ByteArray? {
        return runCatching {
            val apkPath = context.applicationInfo.sourceDir ?: return null
            ZipFile(apkPath).use { zip ->
                val entry = zip.getEntry(zipEntry) ?: return null
                zip.getInputStream(entry).use { it.readBytes() }
            }
        }.getOrNull() ?: runCatching { src.readBytes() }.getOrNull()
    }
}
