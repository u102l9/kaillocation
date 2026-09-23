package com.kail.location.utils

import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.preference.PreferenceManager
import com.kail.location.auth.UsageManager

/**
 * SimulationDiagnostics —— 模拟启动诊断收集器。
 *
 * 目标：用户每次模拟（失败）后，导出的日志里都有<b>一整块、自包含、人话</b>的诊断报告，
 * 让人一眼锁定「为什么模拟没生效」，而不用在几百行散乱日志里大海捞针。
 *
 * 设计要点：
 *  - <b>不静默丢失</b>：通过 [KailLog.persistBlock] 始终输出到 Logcat；文件落盘仍
 *    遵循 setting_log_enabled，避免关闭日志时持续写磁盘。
 *  - <b>分步门控（step）</b>：每一步启动前置条件（root / 注入 / binder / AppOps ...）
 *    都记一条 PASS/FAIL/INFO + 原因，失败步会被高亮成 ❌。
 *  - <b>环境快照</b>：开机时长、运行模式、设备/系统版本、SO 文件是否就位等。
 *  - <b>最终判定（verdict）</b>：用一句话给出「成功 / 回退到测试Provider / 失败」+ 根因。
 *
 * 用法（在 ServiceGoRoot 的启动链路里）：
 * ```
 * val diag = SimulationDiagnostics.begin(ctx, mode = "root", scenario = "route")
 * diag.step("ROOT 权限", ok, if (ok) "su 可用" else "su 调用失败：未授权？")
 * ...
 * diag.verdict(success, "FakeLocation binder 已就绪，注入生效")
 * diag.finish()   // 一次性落盘整块报告
 * ```
 */
class SimulationDiagnostics private constructor(
    private val appContext: Context?,
    private val mode: String,
    private val scenario: String,
) {
    private val sb = StringBuilder()
    private val startElapsedMs = SystemClock.elapsedRealtime()
    @Volatile private var firstFailure: String? = null
    @Volatile private var finished = false

    companion object {
        const val TAG = "SimDiag"

        fun begin(context: Context?, mode: String, scenario: String): SimulationDiagnostics {
            val diag = SimulationDiagnostics(context?.applicationContext ?: context, mode, scenario)
            diag.writeHeader()
            return diag
        }
    }

    private fun writeHeader() {
        val uptimeSec = SystemClock.elapsedRealtime() / 1000
        sb.append("场景=$scenario 运行模式=$mode\n")
        sb.append("设备=${Build.MANUFACTURER} ${Build.MODEL} 系统=Android ${Build.VERSION.RELEASE}(API ${Build.VERSION.SDK_INT})\n")
        sb.append("开机时长=${uptimeSec}s ABI=${Build.SUPPORTED_ABIS.joinToString(",")}\n")
    }

    /** 记录一步门控结果。ok=true -> ✅；ok=false -> ❌（并记为首个失败原因）。 */
    fun step(name: String, ok: Boolean, detail: String = ""): SimulationDiagnostics {
        val mark = if (ok) "✅" else "❌"
        val line = "$mark $name${if (detail.isNotEmpty()) " — $detail" else ""}"
        sb.append(line).append('\n')
        if (!ok && firstFailure == null) {
            firstFailure = "$name：$detail"
        }
        // 增量落盘：即使后续某步把进程/整机搞崩、finish() 跑不到，已完成的步骤
        // 也已在磁盘上，不会「记录不了为什么失败」。
        flushLine(line)
        return this
    }

    /** 记录一条中性信息（不计入成功/失败判定）。 */
    fun info(name: String, detail: String = ""): SimulationDiagnostics {
        sb.append("ℹ️ $name${if (detail.isNotEmpty()) " — $detail" else ""}").append('\n')
        return this
    }

    /** 记录一条警告（不直接判失败，但提示潜在问题）。 */
    fun warn(name: String, detail: String = ""): SimulationDiagnostics {
        sb.append("⚠️ $name${if (detail.isNotEmpty()) " — $detail" else ""}").append('\n')
        return this
    }

    /** 记录一次异常。 */
    fun error(name: String, t: Throwable): SimulationDiagnostics {
        val line = "❌ $name — 异常：${t.javaClass.simpleName}: ${t.message}"
        sb.append(line).append('\n')
        if (firstFailure == null) firstFailure = "$name：${t.message}"
        flushLine(line)
        return this
    }

    /**
     * 增量输出一行。Logcat 始终输出；文件落盘遵循日志开关。
     * 这样注入崩溃/进程被杀时，开启日志的设备仍能保留「走到哪一步」的现场。
     */
    private fun flushLine(line: String) {
        runCatching {
            KailLog.persist(appContext, "$TAG/step", "[$scenario] $line", 'i')
        }
    }

    /**
     * 给出最终判定。
     * @param success 模拟是否真正按预期生效（root 模式应为注入生效，而非回退测试Provider）
     * @param summary 一句话说明
     */
    fun verdict(success: Boolean, summary: String): SimulationDiagnostics {
        val took = SystemClock.elapsedRealtime() - startElapsedMs
        sb.append("──────────\n")
        if (success) {
            sb.append("结果：✅ 成功 — $summary（耗时 ${took}ms）\n")
        } else {
            val reason = firstFailure ?: summary
            sb.append("结果：❌ 未生效 — $summary（耗时 ${took}ms）\n")
            sb.append("根因：$reason\n")
            val advice = adviceFor(reason)
            if (advice.isNotEmpty()) sb.append("建议：$advice\n")
        }
        return this
    }

    /**
     * 根据首个失败原因给出「人话」修复建议，让用户/开发者一眼知道下一步怎么办。
     * 用关键词匹配，覆盖最常见的失败模式。
     */
    private fun adviceFor(reason: String): String {
        val r = reason
        return when {
            r.contains("ROOT") || r.contains("su ") || r.contains("授权") ->
                "在 KernelSU/Magisk 管理器里给 KailLocation 授予 ROOT 权限后重试。"
            r.contains("开机") || r.contains("就绪") ->
                "刚开机系统未稳定，等开机满 ${UsageManager.bootReadyThresholdSeconds()}s 后再开始模拟。"
            r.contains("watchdog") || r.contains("超时") ->
                "注入超时多因 system_server 繁忙或刚开机；稍候重试，仍失败则重启设备后再试。"
            r.contains("缺失") || r.contains("部署") ->
                "SO/dex 部署失败，检查存储空间与 /data/kail-loc 权限，或重装应用。"
            r.contains("binder") || r.contains("注入未生效") ->
                "注入未注册 oem_location；确认 ROOT 已授权、ArtMethod 布局已识别（见上方 native 探测），必要时重启设备。"
            r.contains("ART") || r.contains("布局") ->
                "ART ArtMethod 布局未识别（系统版本过新？），把本段日志发给开发者。"
            r.contains("权限") || r.contains("ACCESS_FINE_LOCATION") ->
                "授予应用定位权限后重试。"
            else -> "把这整段「模拟诊断」日志导出发给开发者，可据此定位。"
        }
    }

    /**
     * 折叠 native LHooker 的 ArtMethod 探测结果到诊断块里（从
     * /data/kail-loc/lhooker_init.log 读回的摘要逐行）。让 ART 布局识别
     * 结果和模拟诊断在同一块报告里，便于一眼对照。
     */
    fun recordNativeProbe(lines: List<String>) {
        if (lines.isEmpty()) {
            info("ArtMethod 探测", "无 native 探测日志（注入未运行或未落盘）")
            return
        }
        sb.append("— ArtMethod 运行时探测 —\n")
        lines.forEach { sb.append("  ").append(it).append('\n') }
    }

    /** 折叠 libfakeloc_init.so 的 native loader 阶段日志到诊断块里。 */
    fun recordLoaderTrace(lines: List<String>) {
        if (lines.isEmpty()) {
            info("注入 loader", "无 fakeloc_init 日志（doRun 可能未执行，或注入器只返回了假成功）")
            return
        }
        sb.append("— fakeloc_init loader —\n")
        lines.forEach { sb.append("  ").append(it).append('\n') }
    }

    /** 折叠 InjectDex.init 在 system_server 内写出的 Java bootstrap 状态。 */
    fun recordBootstrapState(lines: List<String>) {
        if (lines.isEmpty()) {
            info("InjectDex Java bootstrap", "无 injectdex_state（Java 入口可能未执行，或 system_server 无法写运行时目录）")
            return
        }
        sb.append("— InjectDex Java bootstrap —\n")
        lines.forEach { sb.append("  ").append(it).append('\n') }
    }

    /** 折叠 system_server 注入态关键 logcat，覆盖文件日志被 SELinux 拒写的场景。 */
    fun recordInjectedLogcat(lines: List<String>) {
        if (lines.isEmpty()) {
            info("注入态 logcat", "无 InjectDex/RootLocationControl/native loader 日志")
            return
        }
        sb.append("— 注入态 logcat —\n")
        lines.forEach { sb.append("  ").append(it).append('\n') }
    }

    /** 一次性输出整块报告；文件落盘遵循日志开关。可重复调用，仅首次生效。 */
    fun finish() {
        if (finished) return
        finished = true
        KailLog.persistBlock(appContext, "模拟诊断", sb.toString().trimEnd())
    }

    // ------------------------------------------------------------------
    // 常用环境探测（便于各 step 复用，统一口径）。
    // ------------------------------------------------------------------

    /** 开机就绪：root 模式注入要求开机已超过阈值，否则 system_server 未稳定易卡死。 */
    fun checkBootReady(): Boolean {
        val (ready, remainSec) = UsageManager.systemReadiness()
        step(
            "开机就绪",
            ready,
            if (ready) "开机已超过 ${UsageManager.bootReadyThresholdSeconds()}s"
            else "刚开机，还需等待约 ${remainSec}s（此时注入易卡死/重启）"
        )
        return ready
    }

    /** 运行模式快照。 */
    fun recordRunMode(context: Context) {
        val rm = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("setting_run_mode", "developer") ?: "developer"
        info("当前运行模式", rm)
    }

    /**
     * 采集 system_server 的 PID（通过 su）。注入前后各取一次对比：若 PID 变化，
     * 说明注入把 system_server 搞崩并被 watchdog 重启了——这是「开机就崩溃重启」
     * 类问题最直接的证据。返回 PID 字符串（失败为空）。
     */
    fun sampleSystemServerPid(label: String): String {
        val pid = runCatching {
            com.kail.location.utils.ShellUtils.executeCommand("pgrep -f system_server").trim()
                .lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: ""
        }.getOrDefault("")
        info("system_server PID($label)", if (pid.isNotEmpty()) pid else "未取到")
        return pid
    }

    /**
     * 对比注入前后的 system_server PID。若变化，记为失败（注入导致重启）。
     */
    fun checkSystemServerStable(before: String, after: String) {
        if (before.isNotEmpty() && after.isNotEmpty() && before != after) {
            step("system_server 稳定性", false,
                "PID 由 $before 变为 $after —— 注入导致 system_server 崩溃重启")
        } else if (before.isNotEmpty() && before == after) {
            step("system_server 稳定性", true, "PID 未变（$after），未崩溃")
        }
    }
}
