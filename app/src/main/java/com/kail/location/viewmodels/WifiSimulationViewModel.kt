package com.kail.location.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.kail.location.models.WifiInfo
import com.kail.location.utils.KailLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import com.kail.location.auth.UsageManager

/**
 * WiFi模拟页面的 ViewModel
 * 支持多选历史记录作为模拟列表，独立开始/停止模拟
 */
class WifiSimulationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val wifiManager = application.applicationContext.getSystemService(WifiManager::class.java)

    companion object {
        private const val TAG = "WifiSimulationVM"
        const val KEY_WIFI_ENABLED = "wifi_sim_enabled"
        const val KEY_WIFI_LIST = "wifi_sim_list"
        const val KEY_WIFI_SELECTED_IDS = "wifi_sim_selected_ids" // comma-separated
        const val KEY_WIFI_IS_SIMULATING = "wifi_sim_is_simulating"
    }

    // Whether currently simulating (active state) - the only switch needed
    private val _isSimulating = MutableStateFlow(prefs.getBoolean(KEY_WIFI_IS_SIMULATING, false))
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _wifiList = MutableStateFlow(loadWifiList())
    val wifiList: StateFlow<List<WifiInfo>> = _wifiList.asStateFlow()

    // Multi-select: set of IDs that are actively being simulated
    private val _selectedIds = MutableStateFlow(loadSelectedIds())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    // Nearby scan results
    private val _nearbyResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val nearbyResults: StateFlow<List<ScanResult>> = _nearbyResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    /** The list of WiFis currently selected for simulation */
    val activeWifiList: List<WifiInfo>
        get() = _wifiList.value.filter { it.id in _selectedIds.value }

    private fun loadWifiList(): List<WifiInfo> {
        val json = prefs.getString(KEY_WIFI_LIST, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<WifiInfo>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(WifiInfo(
                    id = obj.optString("id", ""),
                    name = obj.optString("name", ""),
                    ssid = obj.optString("ssid", ""),
                    bssid = obj.optString("bssid", ""),
                    rssi = obj.optInt("rssi", -50),
                    frequency = obj.optInt("frequency", 2412),
                    linkSpeed = obj.optInt("linkSpeed", 65),
                    capabilities = obj.optString("capabilities", "")
                ))
            }
            list
        } catch (_: Exception) { emptyList() }
    }

    private fun saveWifiList(list: List<WifiInfo>) {
        val array = JSONArray()
        list.forEach { wifi ->
            val obj = JSONObject().apply {
                put("id", wifi.id)
                put("name", wifi.name)
                put("ssid", wifi.ssid)
                put("bssid", wifi.bssid)
                put("rssi", wifi.rssi)
                put("frequency", wifi.frequency)
                put("linkSpeed", wifi.linkSpeed)
                put("capabilities", wifi.capabilities)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_WIFI_LIST, array.toString()).apply()
    }

    private fun loadSelectedIds(): Set<String> {
        val csv = prefs.getString(KEY_WIFI_SELECTED_IDS, "") ?: ""
        return csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private fun saveSelectedIds(ids: Set<String>) {
        prefs.edit().putString(KEY_WIFI_SELECTED_IDS, ids.joinToString(",")).apply()
    }

    fun setSimulating(value: Boolean) {
        if (value) {
            viewModelScope.launch {
                val app = getApplication<Application>()
                if (!UsageManager.canStartSimulation(app)) {
                    return@launch
                }
                if (!UsageManager.consumeSimulation(app)) {
                    return@launch
                }
                doSetSimulating(true)
            }
        } else {
            doSetSimulating(false)
        }
    }

    private fun doSetSimulating(value: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_IS_SIMULATING, value).apply()
        _isSimulating.value = value
        writeWifiConfigToFile(value)
        if (value) {
            startServiceGoRootWifiMode()
        } else {
            stopServiceGoRootWifiMode()
        }
    }

    /**
     * 从 prefs 重新加载列表/选择/模拟状态。
     *
     * WifiPickerActivity 用自己独立的 ViewModel 实例添加 WiFi 并写入 prefs，
     * 返回本页时本页 ViewModel 不会自动感知，导致「模拟中添加的 WiFi 下方不显示」。
     * 在 Activity.onResume 调用本方法即可刷新。
     */
    fun reloadFromPrefs() {
        _wifiList.value = loadWifiList()
        _selectedIds.value = loadSelectedIds()
        _isSimulating.value = prefs.getBoolean(KEY_WIFI_IS_SIMULATING, false)
    }

    /**
     * 模拟进行中时，把最新的 WiFi 列表推给正在运行的 ServiceGoRoot，立即生效
     * （无需停止再开始）。未在模拟时不做任何事。
     */
    private fun pushWifiListToService() {
        if (!_isSimulating.value) return
        try {
            val ctx = getApplication<Application>().applicationContext
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                    com.kail.location.service.Root.ServiceGoRoot.CONTROL_SET_WIFI
                )
                putParcelableArrayListExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_WIFI_LIST,
                    ArrayList(activeWifiList)
                )
            }
            ctx.startService(intent)
            KailLog.i(ctx, TAG, "pushWifiListToService: ${activeWifiList.size} networks")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "pushWifiListToService failed", e)
        }
    }

    private fun stopServiceGoRootWifiMode() {
        try {
            val ctx = getApplication<Application>().applicationContext
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                    com.kail.location.service.Root.ServiceGoRoot.CONTROL_STOP_WIFI
                )
            }
            ctx.startService(intent)
            KailLog.i(ctx, TAG, "stopServiceGoRootWifiMode: sent CONTROL_STOP_WIFI")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "stopServiceGoRootWifiMode failed", e)
        }
    }

    private fun startServiceGoRootWifiMode() {
        try {
            val ctx = getApplication<Application>().applicationContext
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(com.kail.location.service.Root.ServiceGoRoot.EXTRA_WIFI_ONLY, true)
                putExtra(com.kail.location.views.locationpicker.LocationPickerActivity.LAT_MSG_ID, com.kail.location.service.Root.ServiceGoRoot.DEFAULT_LAT)
                putExtra(com.kail.location.views.locationpicker.LocationPickerActivity.LNG_MSG_ID, com.kail.location.service.Root.ServiceGoRoot.DEFAULT_LNG)
                putExtra(com.kail.location.views.locationpicker.LocationPickerActivity.ALT_MSG_ID, prefs.getString("setting_altitude", "55.0")?.toDoubleOrNull() ?: 55.0)
                // Pass the selected WiFi networks so ServiceGoRoot can push
                // them into the FakeLocation injection layer (oem_wifi).
                putParcelableArrayListExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_WIFI_LIST,
                    ArrayList(activeWifiList)
                )
            }
            ctx.startService(intent)
            KailLog.i(ctx, TAG, "startServiceGoRootWifiMode: started with ${activeWifiList.size} networks")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "startServiceGoRootWifiMode failed", e)
        }
    }

    private fun writeWifiConfigToFile(simulating: Boolean) {
        try {
            val effectiveEnabled = simulating
            val array = JSONArray()
            activeWifiList.forEach { wifi ->
                val obj = JSONObject().apply {
                    put("id", wifi.id)
                    put("name", wifi.name)
                    put("ssid", wifi.ssid)
                    put("bssid", wifi.bssid)
                    put("rssi", wifi.rssi)
                    put("frequency", wifi.frequency)
                    put("linkSpeed", wifi.linkSpeed)
                    put("capabilities", wifi.capabilities)
                }
                array.put(obj)
            }
            val wifiConfig = """{"enabled":$effectiveEnabled,"list":$array}"""
            val wifiFile = "/data/local/kail-lib/kail_wifi.json"
            com.kail.location.utils.ShellUtils.executeCommand("echo '$wifiConfig' > $wifiFile")
            com.kail.location.utils.ShellUtils.executeCommand("chmod 777 $wifiFile")

            // Also update kail_location.conf wifi_mock field
            val confFile = "/data/local/kail-lib/kail_location.conf"
            val checkResult = com.kail.location.utils.ShellUtils.executeCommand("[ -f $confFile ] && echo yes || echo no").trim()
            if (checkResult == "yes") {
                // Use sed to update wifi_mock line, or append if not exists
                com.kail.location.utils.ShellUtils.executeCommand("sed -i 's/^wifi_mock=.*/wifi_mock=$effectiveEnabled/' $confFile || echo 'wifi_mock=$effectiveEnabled' >> $confFile")
                // Also ensure enabled=false for light mode (WiFi is not location simulation)
                com.kail.location.utils.ShellUtils.executeCommand("sed -i 's/^enabled=.*/enabled=false/' $confFile || echo 'enabled=false' >> $confFile")
            } else {
                // Create minimal config
                val content = """enabled=false
mode=global
wifi_mock=$effectiveEnabled
cell_mock=false
gnss_mock=false
lat=36.667662
lon=117.027707
alt=55.0
speed=0.0
bearing=0.0
accuracy=25.0
target_packages="""
                com.kail.location.utils.ShellUtils.executeCommand("echo '$content' > $confFile")
                com.kail.location.utils.ShellUtils.executeCommand("chmod 777 $confFile")
            }
            KailLog.i(getApplication(), TAG, "writeWifiConfigToFile: enabled=$effectiveEnabled networks=${activeWifiList.size}")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "writeWifiConfigToFile failed", e)
        }
    }

    /** Toggle selection of a WiFi in the simulation list */
    fun toggleSelection(id: String) {
        val current = _selectedIds.value.toMutableSet()
        if (id in current) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
        saveSelectedIds(current)
        pushWifiListToService()
    }

    /** Select all history items */
    fun selectAll() {
        val allIds = _wifiList.value.map { it.id }.toSet()
        _selectedIds.value = allIds
        saveSelectedIds(allIds)
    }

    /** Deselect all */
    fun deselectAll() {
        _selectedIds.value = emptySet()
        saveSelectedIds(emptySet())
    }

    fun addWifi(info: WifiInfo) {
        val newInfo = if (info.id.isEmpty()) info.copy(id = UUID.randomUUID().toString()) else info
        val finalInfo = if (newInfo.name.isBlank()) newInfo.copy(name = newInfo.ssid) else newInfo
        val newList = _wifiList.value + finalInfo
        _wifiList.value = newList
        saveWifiList(newList)
        // Auto-select newly added item
        toggleSelection(finalInfo.id)
    }

    fun addFromScanResult(result: ScanResult) {
        val info = WifiInfo(
            id = UUID.randomUUID().toString(),
            name = result.SSID.ifBlank { "Unknown" },
            ssid = result.SSID,
            bssid = result.BSSID,
            rssi = result.level,
            frequency = result.frequency,
            linkSpeed = 65,
            capabilities = result.capabilities
        )
        addWifi(info)
    }

    fun updateWifi(info: WifiInfo) {
        val newList = _wifiList.value.map { if (it.id == info.id) info else it }
        _wifiList.value = newList
        saveWifiList(newList)
    }

    fun renameWifi(id: String, newName: String) {
        val newList = _wifiList.value.map {
            if (it.id == id) it.copy(name = newName) else it
        }
        _wifiList.value = newList
        saveWifiList(newList)
    }

    fun deleteWifi(id: String) {
        val newList = _wifiList.value.filter { it.id != id }
        _wifiList.value = newList
        saveWifiList(newList)
        // Also remove from selection
        if (id in _selectedIds.value) {
            toggleSelection(id)
        }
    }

    fun scanNearbyWifi() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val cached = wifiManager?.scanResults
                if (!cached.isNullOrEmpty()) {
                    _nearbyResults.value = cached
                }
                @Suppress("DEPRECATION")
                wifiManager?.startScan()
                kotlinx.coroutines.delay(1500)
                val updated = wifiManager?.scanResults
                if (!updated.isNullOrEmpty()) {
                    _nearbyResults.value = updated
                }
                KailLog.i(getApplication(), TAG, "scanNearbyWifi: ${updated?.size ?: 0} results")
            } catch (e: Exception) {
                KailLog.e(getApplication(), TAG, "scanNearbyWifi failed", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

}
