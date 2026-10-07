package com.kail.location.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.kail.location.utils.KailLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Root与应用隐藏"（Root & App Hiding）页面的 ViewModel。
 *
 * 与 [IndependentSimulationViewModel] 思路一致：维护一份"目标应用"列表 + 两个开关
 * （隐藏 Root / 隐藏应用列表）。开启后，对选中的应用隐藏 Root、Magisk、Xposed 等痕迹，
 * 并可选地隐藏本机已安装的其它应用；不在列表里的应用不受影响。
 *
 * 底层实现：配置通过 [com.kail.location.service.Root.ServiceGoRoot] 下发到 FakeLocation
 * 注入层的 oem_integrity（IHideRootManager）。ServiceGoRoot 会 stage 注入、推送
 * 隐藏配置，并对每个目标应用进程执行 app-hook 注入，使 RootHideHook / LAntiDetect 在
 * 这些进程里生效。停止则清空配置。
 */
class RootAppHideViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    companion object {
        private const val TAG = "RootAppHideViewModel"
        const val KEY_HIDE_ENABLED = "root_hide_enabled"
        const val KEY_HIDE_ROOT = "root_hide_hide_root"
        const val KEY_HIDE_APPLIST = "root_hide_hide_applist"
        const val KEY_HIDE_TARGET_PACKAGES = "root_hide_target_packages"
    }

    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_HIDE_ENABLED, false))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _hideRoot = MutableStateFlow(prefs.getBoolean(KEY_HIDE_ROOT, true))
    val hideRoot: StateFlow<Boolean> = _hideRoot.asStateFlow()

    private val _hideAppList = MutableStateFlow(prefs.getBoolean(KEY_HIDE_APPLIST, false))
    val hideAppList: StateFlow<Boolean> = _hideAppList.asStateFlow()

    private val _targetPackages = MutableStateFlow(prefs.getString(KEY_HIDE_TARGET_PACKAGES, "") ?: "")
    val targetPackages: StateFlow<String> = _targetPackages.asStateFlow()

    /** The currently selected target packages as a clean list. */
    val targetPackageList: List<String>
        get() = _targetPackages.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun setHideRoot(value: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_ROOT, value).apply()
        _hideRoot.value = value
        if (_isEnabled.value) pushHideConfig(true)
    }

    fun setHideAppList(value: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_APPLIST, value).apply()
        _hideAppList.value = value
        if (_isEnabled.value) pushHideConfig(true)
    }

    fun setTargetPackages(value: String) {
        prefs.edit().putString(KEY_HIDE_TARGET_PACKAGES, value).apply()
        _targetPackages.value = value
        if (_isEnabled.value) pushHideConfig(true)
    }

    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_ENABLED, value).apply()
        _isEnabled.value = value
        pushHideConfig(value)
    }

    /**
     * Push the hide config to ServiceGoRoot. When enabling, the service is
     * started in HIDE_ONLY mode (no location mock) so it stages the inject,
     * pushes the config into oem_integrity, and injects the target apps.
     * When disabling, a stop_hide control action tears the config down.
     */
    private fun pushHideConfig(enabled: Boolean) {
        try {
            val ctx = getApplication<Application>().applicationContext
            val svc = com.kail.location.service.Root.ServiceGoRoot::class.java
            if (enabled) {
                val intent = android.content.Intent(ctx, svc).apply {
                    if (com.kail.location.service.Root.ServiceGoRoot.isRunning) {
                        putExtra(
                            com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                            com.kail.location.service.Root.ServiceGoRoot.CONTROL_SET_HIDE
                        )
                    } else {
                        putExtra(com.kail.location.service.Root.ServiceGoRoot.EXTRA_HIDE_ONLY, true)
                    }
                    putExtra(com.kail.location.service.Root.ServiceGoRoot.EXTRA_HIDE_ROOT, _hideRoot.value)
                    putExtra(com.kail.location.service.Root.ServiceGoRoot.EXTRA_HIDE_APPLIST, _hideAppList.value)
                    putStringArrayListExtra(
                        com.kail.location.service.Root.ServiceGoRoot.EXTRA_HIDE_PACKAGES,
                        ArrayList(targetPackageList)
                    )
                }
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    ctx.startForegroundService(intent)
                } else {
                    ctx.startService(intent)
                }
            } else {
                // 停止：UI 进程直接清 Provider + 文件配置，不依赖 ServiceGoRoot 是否
                // 存活/被 ROM 冻结。否则服务一旦被冻结/卡死，onStartCommand 不执行，
                // 配置永远停在 enabled=1，表现为"点停止没反应、目标应用继续隐藏"。
                clearHideConfigDirectly()
                // 再通知服务 force-stop 目标进程（best effort；服务不可用不影响已清配置）。
                val intent = android.content.Intent(ctx, svc).apply {
                    putExtra(
                        com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                        com.kail.location.service.Root.ServiceGoRoot.CONTROL_STOP_HIDE
                    )
                }
                if (com.kail.location.service.Root.ServiceGoRoot.isRunning) {
                    ctx.startService(intent)
                } else if (android.os.Build.VERSION.SDK_INT >= 26) {
                    ctx.startForegroundService(intent)
                } else {
                    ctx.startService(intent)
                }
            }
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "pushHideConfig failed", e)
        }
    }

    /**
     * 停止隐藏时，由 UI 进程直接清掉 Provider + 文件里的隐藏配置。
     *
     * 目标进程的 [com.kail.location.inject.utils.HideConfigFile] /
     * [com.kail.location.inject.utils.AntiDetectConfigFile] 都是 Provider 优先、
     * 文件兜底，两边都写 enabled=0 才能保证立即停止隐藏。Provider 是同进程静态表，
     * 直接清即可；文件写入走 su，放后台线程避免卡主线程。
     */
    private fun clearHideConfigDirectly() {
        runCatching {
            com.kail.location.views.locationshm.LocationShmProvider.setConfig(
                com.kail.location.inject.utils.LocationShm.PROVIDER_KEY_HIDE_CONFIG, null
            )
            com.kail.location.views.locationshm.LocationShmProvider.setConfig(
                com.kail.location.inject.utils.LocationShm.PROVIDER_KEY_ANTIDETECT_CONFIG, null
            )
        }.onFailure { KailLog.e(getApplication(), TAG, "clearHideConfig(provider): ${it.message}") }
        Thread({
            runCatching {
                com.kail.location.utils.ShellUtils.executeCommand(
                    "mkdir -p /data/kail-loc && chmod 777 /data/kail-loc && " +
                        "printf 'enabled=0\\n' > /data/kail-loc/hide_config.txt && " +
                        "chmod 644 /data/kail-loc/hide_config.txt && " +
                        "printf 'hook_enabled=0\\n' > /data/kail-loc/antidetect_config.txt && " +
                        "chmod 644 /data/kail-loc/antidetect_config.txt"
                )
            }.onFailure { KailLog.e(getApplication(), TAG, "clearHideConfig(file): ${it.message}") }
        }, "RootAppHideClearConfig").start()
    }
}
