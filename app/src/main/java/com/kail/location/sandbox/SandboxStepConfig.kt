package com.kail.location.sandbox

import android.content.Context
import com.kail.location.utils.KailLog
import org.json.JSONObject
import java.io.File

/**
 * 沙盒步频模拟配置管理。
 * 用于在宿主应用和沙盒应用之间传递步频模拟参数。
 */
object SandboxStepConfig {
    private const val TAG = "SandboxStepConfig"
    private const val CONFIG_FILE = "sandbox_step_config.json"
    private const val CONFIG_TIMEOUT_MS = 5 * 60 * 1000L

    /**
     * 写入步频模拟配置。
     *
     * @param context 上下文。
     * @param enabled 是否启用步频模拟。
     * @param spm     步频（步/分钟）。
     */
    fun writeConfig(context: Context, enabled: Boolean, spm: Float) {
        try {
            val file = File(context.filesDir, CONFIG_FILE)
            val json = JSONObject().apply {
                put("enabled", enabled)
                put("spm", spm.toDouble())
                put("timestamp", System.currentTimeMillis())
            }
            file.writeText(json.toString())
        } catch (e: Exception) {
            KailLog.e(context, TAG, "Failed to write config", e)
        }
    }

    /**
     * 读取步频模拟配置。
     *
     * @param context 上下文。
     * @return Pair(enabled, spm)。
     */
    fun readConfig(context: Context): Pair<Boolean, Float> {
        return try {
            val file = File(context.filesDir, CONFIG_FILE)
            if (!file.exists()) {
                return Pair(false, 120f)
            }
            val json = JSONObject(file.readText())
            val timestamp = json.optLong("timestamp", 0)
            if (System.currentTimeMillis() - timestamp > CONFIG_TIMEOUT_MS) {
                return Pair(false, 120f)
            }
            val enabled = json.optBoolean("enabled", false)
            val spm = json.optDouble("spm", 120.0).toFloat()
            Pair(enabled, spm)
        } catch (e: Exception) {
            KailLog.e(context, TAG, "Failed to read config", e)
            Pair(false, 120f)
        }
    }

    /**
     * 清除步频模拟配置。
     *
     * @param context 上下文。
     */
    fun clearConfig(context: Context) {
        try {
            val file = File(context.filesDir, CONFIG_FILE)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            KailLog.e(context, TAG, "Failed to clear config", e)
        }
    }
}
