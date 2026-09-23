package com.kail.location.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.kail.location.R
import com.kail.location.sandbox.SandboxManager
import com.kail.location.utils.KailLog

/**
 * 沙盒模式的 ViewModel。
 * 负责管理 BlackBox 沙盒中的应用列表、安装、卸载、启动等操作。
 */
class SandboxViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SandboxViewModel"
    }

    private val _sandboxApps = MutableStateFlow<List<SandboxManager.SandboxAppInfo>>(emptyList())
    val sandboxApps: StateFlow<List<SandboxManager.SandboxAppInfo>> = _sandboxApps.asStateFlow()

    private val _systemApps = MutableStateFlow<List<SandboxManager.SystemAppInfo>>(emptyList())
    val systemApps: StateFlow<List<SandboxManager.SystemAppInfo>> = _systemApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadSandboxApps()
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    /**
     * 加载沙盒中已安装的应用列表。
     */
    fun loadSandboxApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val apps = SandboxManager.getSandboxApps()
                withContext(Dispatchers.Main) {
                    _sandboxApps.value = apps
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                KailLog.e(getApplication(), TAG, "loadSandboxApps failed", e)
                withContext(Dispatchers.Main) {
                    _toastMessage.value = getApplication<Application>().getString(R.string.sandbox_loading_failed, e.message)
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * 加载系统中可克隆的应用列表。
     */
    fun loadSystemApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                KailLog.d(getApplication(), TAG, "Loading system apps...")
                _isLoading.value = true
                val apps = SandboxManager.getSystemApps()
                KailLog.d(getApplication(), TAG, "Loaded ${apps.size} system apps")
                withContext(Dispatchers.Main) {
                    _systemApps.value = apps
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                KailLog.e(getApplication(), TAG, "Error loading system apps: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _toastMessage.value = getApplication<Application>().getString(R.string.sandbox_system_apps_failed, e.message)
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * 从系统中克隆安装应用到沙盒。
     */
    fun cloneApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val (success, message) = SandboxManager.cloneApp(packageName)
            withContext(Dispatchers.Main) {
                _toastMessage.value = message
                _isLoading.value = false
                if (success) {
                    loadSandboxApps()
                }
            }
        }
    }

    /**
     * 卸载沙盒中的应用。
     */
    fun uninstallApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = SandboxManager.uninstallApp(packageName)
            withContext(Dispatchers.Main) {
                _toastMessage.value = message
                if (success) {
                    loadSandboxApps()
                }
            }
        }
    }

    /**
     * 启动沙盒中的应用。
     */
    fun launchApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = SandboxManager.launchApp(packageName)
            if (!success) {
                withContext(Dispatchers.Main) {
                    _toastMessage.value = message
                }
            }
        }
    }

    /**
     * 清除沙盒应用数据。
     */
    fun clearAppData(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (_, message) = SandboxManager.clearAppData(packageName)
            withContext(Dispatchers.Main) {
                _toastMessage.value = message
            }
        }
    }

    /**
     * 停止沙盒应用运行。
     */
    fun stopApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val (_, message) = SandboxManager.stopApp(packageName)
            withContext(Dispatchers.Main) {
                _toastMessage.value = message
            }
        }
    }
}
