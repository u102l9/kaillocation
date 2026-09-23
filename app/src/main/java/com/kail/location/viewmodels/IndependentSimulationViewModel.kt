package com.kail.location.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.kail.location.auth.UsageManager
import com.kail.location.utils.KailLog

/**
 * 独立模拟页面的 ViewModel。
 *
 * 职责很简单：维护一份"目标应用白名单"。当用户在这里选好应用并开启独立模拟后，
 * 之后在 位置/路线/WiFi/基站/步频 等页面点"开始模拟"时，模拟数据 **只对白名单
 * 里的应用生效**，其它应用一律读到真实数据。
 *
 * 底层实现：白名单通过 [com.kail.location.service.Root.ServiceGoRoot] 下发到
 * FakeLocation 注入层的 `setAllowMockPackages`（oem_location /
 * oem_wifi）。白名单为空时 FakeLocation 默认对所有应用生效（isAllMock）。
 */
class IndependentSimulationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    companion object {
        private const val TAG = "IndependentSimulationVM"
        const val KEY_INDEPENDENT_ENABLED = "independent_enabled"
        const val KEY_INDEPENDENT_TARGET_PACKAGES = "independent_target_packages"
    }

    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_INDEPENDENT_ENABLED, false))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _targetPackages = MutableStateFlow(prefs.getString(KEY_INDEPENDENT_TARGET_PACKAGES, "") ?: "")
    val targetPackages: StateFlow<String> = _targetPackages.asStateFlow()

    fun setEnabled(value: Boolean) {
        if (value) {
            viewModelScope.launch {
                val app = getApplication<Application>()
                if (!UsageManager.canStartSimulation(app)) {
                    return@launch
                }
                if (!UsageManager.consumeSimulation(app)) {
                    return@launch
                }
                doSetEnabled(true)
            }
        } else {
            doSetEnabled(false)
        }
    }

    private fun doSetEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_INDEPENDENT_ENABLED, value).apply()
        _isEnabled.value = value
        applyAllowListToInjection(value)
    }

    fun setTargetPackages(value: String) {
        prefs.edit().putString(KEY_INDEPENDENT_TARGET_PACKAGES, value).apply()
        _targetPackages.value = value
        // If independent mode is already running, push the updated allow-list live.
        if (_isEnabled.value) applyAllowListToInjection(true)
    }

    /** The currently selected target packages as a clean list. */
    val targetPackageList: List<String>
        get() = _targetPackages.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * Push the target-app allow-list to the FakeLocation injection layer if a
     * ServiceGoRoot mock is already running. The selection is always persisted
     * to prefs first, and [ServiceGoRoot] re-reads it on every mock start, so
     * the allow-list reliably applies even if the service isn't running yet —
     * this live push only updates an in-flight session.
     *
     * We deliberately do NOT start the service just to set the allow-list: a
     * foreground service started with only a control action (no mock) would
     * have to be torn down immediately and risks the "startForeground not
     * called" crash. Persisting to prefs is enough for correctness.
     */
    private fun applyAllowListToInjection(enabled: Boolean) {
        try {
            val ctx = getApplication<Application>().applicationContext
            if (!com.kail.location.service.Root.ServiceGoRoot.isRunning) return
            val pkgs = if (enabled) targetPackageList else emptyList()
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                    com.kail.location.service.Root.ServiceGoRoot.CONTROL_SET_ALLOW_PACKAGES
                )
                putStringArrayListExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_ALLOW_PACKAGES,
                    ArrayList(pkgs)
                )
            }
            ctx.startService(intent)
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "applyAllowListToInjection failed", e)
        }
    }
}
