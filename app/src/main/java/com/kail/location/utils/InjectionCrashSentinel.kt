package com.kail.location.utils

import android.content.Context
import android.os.SystemClock

/**
 * InjectionCrashSentinel —— 注入崩溃哨兵。
 *
 * 解决「点击开始模拟后设备崩溃重启，但日志里查不到原因」这个最棘手的问题。
 *
 * 为什么普通诊断报告救不了这种场景：
 *   ptrace 注入若把 system_server 搞崩，整机会被 watchdog 重启，app 进程随即
 *   被杀。[SimulationDiagnostics.finish] 是在注入流程末尾「一次性」落盘的，
 *   崩溃发生时它根本来不及执行，于是整块报告丢失——用户发来的日志自然不完整。
 *
 * 哨兵机制（跨重启取证）：
 *   1. 注入前 [arm]：把一条「待确认」记录<b>同步</b>写到 root 可读写的
 *      /data/kail-loc/inject_pending.txt，并记下当前的「开机纪元」(boot epoch =
 *      wallclock - elapsedRealtime)。
 *   2. 注入并确认系统健康后 [disarm]：删除该记录。
 *   3. 下次 app 启动时 [checkAndReport]：若记录<b>仍然存在</b>，说明上次注入后
 *      没能走到 disarm——大概率是崩溃了。再比对开机纪元：若已变化(设备重启过)，
 *      就<b>确证</b>「上次开始模拟把系统搞崩重启了」，并输出对应诊断。
 *
 * 文件放在 /data/kail-loc（注入部署目录，0777，主进程与 system_server 都能写），
 * 这样即使应用私有目录因重装/清数据丢失，root 取证仍在。
 */
object InjectionCrashSentinel {

    private const val TAG = "InjectCrash"
    private const val SENTINEL_PATH = "/data/kail-loc/inject_pending.txt"

    /**
     * 读取内核启动时间戳(btime, Unix 秒)。只有真正重启才会变化，不受深睡/时钟漂移
     * 影响，比用 wallclock-elapsedRealtime 推算开机纪元更可靠。读不到返回 -1。
     */
    private fun kernelBootTimeSec(): Long {
        return runCatching {
            val out = ShellUtils.executeCommand("cat /proc/stat 2>/dev/null | grep '^btime'").trim()
            // 形如 "btime 1733040000"
            out.split(Regex("\\s+")).getOrNull(1)?.toLongOrNull() ?: -1L
        }.getOrDefault(-1L)
    }

    /**
     * 注入前布防：同步写入待确认记录。必须在真正调用 kail_inject 之前调用，
     * 且要同步（不能丢给后台线程），否则崩溃时记录可能还没落盘。
     *
     * @param scenario 场景（location/route/wifi/cell）
     */
    fun arm(scenario: String, sdkInt: Int, device: String) {
        val payload = buildString {
            append("scenario=").append(scenario).append('\n')
            append("kernel_btime_sec=").append(kernelBootTimeSec()).append('\n')
            append("uptime_ms=").append(SystemClock.elapsedRealtime()).append('\n')
            append("wallclock_ms=").append(System.currentTimeMillis()).append('\n')
            append("sdk=").append(sdkInt).append('\n')
            append("device=").append(device).append('\n')
        }
        // 用 su 同步落盘到 root 目录，并 sync 刷写，最大化崩溃前落盘成功率。
        val escaped = payload.replace("'", "'\\''")
        runCatching {
            ShellUtils.executeCommand(
                "mkdir -p /data/kail-loc && printf '%s' '$escaped' > $SENTINEL_PATH && chmod 666 $SENTINEL_PATH && sync"
            )
        }
    }

    /** 注入完成且系统确认健康后撤防：删除待确认记录。 */
    fun disarm() {
        runCatching { ShellUtils.executeCommand("rm -f $SENTINEL_PATH") }
    }

    /**
     * App 启动时调用：检测上一次注入是否导致了崩溃/重启，若是则输出一条
     * 醒目的诊断，并附上系统崩溃缓冲区(logcat -b crash)的相关行。
     *
     * 安全：本方法只读哨兵文件 + 读 logcat，不做任何破坏性操作。无 root 时静默跳过。
     */
    fun checkAndReport(context: Context) {
        runCatching {
            val raw = ShellUtils.executeCommand("cat $SENTINEL_PATH 2>/dev/null").trim()
            if (raw.isBlank()) return  // 没有待确认记录：上次正常 disarm 了，或从未注入

            val fields = raw.lineSequence()
                .mapNotNull { line ->
                    val i = line.indexOf('=')
                    if (i > 0) line.substring(0, i) to line.substring(i + 1) else null
                }
                .toMap()

            val prevBootTime = fields["kernel_btime_sec"]?.toLongOrNull()
            val scenario = fields["scenario"] ?: "?"
            val prevUptimeMs = fields["uptime_ms"]?.toLongOrNull() ?: -1L
            val nowBootTime = kernelBootTimeSec()
            // 内核 btime 变化即代表设备重启过（容许 ±3s 抖动，btime 偶有秒级误差）。
            val rebooted = prevBootTime != null && prevBootTime > 0 && nowBootTime > 0 &&
                kotlin.math.abs(nowBootTime - prevBootTime) > 3L

            val report = StringBuilder()
            report.append("检测到上一次「开始模拟」未正常收尾（注入哨兵仍存在）。\n")
            report.append("场景=$scenario 上次开机后约 ${prevUptimeMs / 1000}s 触发注入。\n")
            if (rebooted) {
                report.append("判定：⚠️ 设备在那次注入后发生了重启——极可能是注入 system_server 导致崩溃重启。\n")
                report.append("（内核启动时间已变化：旧btime=$prevBootTime 新btime=$nowBootTime）\n")
                report.append("建议：开机后等系统就绪(满阈值)再开始模拟；若必现，把本段+下方崩溃日志发开发者。\n")
            } else {
                report.append("判定：注入流程异常中断但设备未重启（可能 app 被杀/ANR/用户强退）。\n")
            }

            // 附上系统崩溃缓冲区里与 system_server / 我们的 so 相关的行。
            val crashTail = runCatching {
                ShellUtils.executeCommand(
                    "logcat -d -b crash -t 200 2>/dev/null | " +
                        "grep -iE 'system_server|Fatal signal|liblhooker|libfakeloc|libkail|kail_inject|JitCodeCache|signal 11|signal 6' " +
                        "| tail -40"
                ).trim()
            }.getOrDefault("")
            if (crashTail.isNotBlank()) {
                report.append("—— 系统崩溃缓冲区(相关行) ——\n")
                report.append(crashTail).append('\n')
            } else {
                report.append("（崩溃缓冲区无相关行——重启后该缓冲区可能已被清空）\n")
            }

            KailLog.persistBlock(context, "注入崩溃取证", report.toString().trimEnd())
            KailLog.persist(context, TAG,
                if (rebooted) "上次开始模拟疑似导致系统崩溃重启（场景=$scenario）" else "上次注入异常中断（场景=$scenario）",
                'w')

            // 取证完成，清掉哨兵，避免重复报告。
            disarm()
        }
    }
}
