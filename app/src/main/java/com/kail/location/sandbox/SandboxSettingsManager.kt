package com.kail.location.sandbox

import android.content.Context
import android.content.SharedPreferences
import com.kail.location.utils.KailLog
import top.niunaijun.blackbox.BlackBoxCore

/**
 * 沙盒设置管理器 - 直接读写 NewBlackbox 的 SharedPreferences。
 */
object SandboxSettingsManager {

    private const val TAG = "SandboxSettings"
    private const val SP_NAME = "AppSharedPreferenceDelegate"
    private const val KEY_HIDE_ROOT = "mHideRoot"
    private const val KEY_DAEMON_ENABLE = "mDaemonEnable"
    private const val KEY_USE_VPN_NETWORK = "mUseVpnNetwork"
    private const val KEY_DISABLE_FLAG_SECURE = "mDisableFlagSecure"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    var hideRoot: Boolean
        get() = prefs.getBoolean(KEY_HIDE_ROOT, false)
        set(value) = prefs.edit().putBoolean(KEY_HIDE_ROOT, value).apply()

    var daemonEnable: Boolean
        get() = prefs.getBoolean(KEY_DAEMON_ENABLE, false)
        set(value) = prefs.edit().putBoolean(KEY_DAEMON_ENABLE, value).apply()

    var useVpnNetwork: Boolean
        get() = prefs.getBoolean(KEY_USE_VPN_NETWORK, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_VPN_NETWORK, value).apply()

    var disableFlagSecure: Boolean
        get() = prefs.getBoolean(KEY_DISABLE_FLAG_SECURE, false)
        set(value) = prefs.edit().putBoolean(KEY_DISABLE_FLAG_SECURE, value).apply()

    val isSupportGms: Boolean
        get() = try {
            BlackBoxCore.get().isSupportGms
        } catch (e: Exception) {
            KailLog.w(null, TAG, "isSupportGms: query GMS support failed: ${e.message}")
            false
        }

    fun sendLogs(listener: BlackBoxCore.LogSendListener) {
        BlackBoxCore.get().sendLogs("Manual Log Upload from Settings", true, listener)
    }
}
