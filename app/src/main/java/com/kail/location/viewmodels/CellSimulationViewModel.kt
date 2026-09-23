package com.kail.location.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.kail.location.R
import com.kail.location.models.CellInfo
import com.kail.location.utils.KailLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import com.kail.location.auth.UsageManager

/**
 * 基站模拟页面的 ViewModel
 * 支持多选历史记录作为模拟列表，独立开始/停止模拟
 */
class CellSimulationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    companion object {
        private const val TAG = "CellSimulationVM"
        const val KEY_CELL_LIST = "cell_sim_list"
        const val KEY_CELL_SELECTED_IDS = "cell_sim_selected_ids" // comma-separated
        const val KEY_CELL_IS_SIMULATING = "cell_sim_is_simulating"
    }

    private val _isSimulating = MutableStateFlow(prefs.getBoolean(KEY_CELL_IS_SIMULATING, false))
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _cellList = MutableStateFlow(loadCellList())
    val cellList: StateFlow<List<CellInfo>> = _cellList.asStateFlow()

    // Multi-select: set of IDs that are actively being simulated
    private val _selectedIds = MutableStateFlow(loadSelectedIds())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    /** The list of cells currently selected for simulation */
    val activeCellList: List<CellInfo>
        get() = _cellList.value.filter { it.id in _selectedIds.value }

    // Cell scanning state
    private val _isScanningCell = MutableStateFlow(false)
    val isScanningCell: StateFlow<Boolean> = _isScanningCell.asStateFlow()

    private val _scannedCells = MutableStateFlow<List<CellInfo>>(emptyList())
    val scannedCells: StateFlow<List<CellInfo>> = _scannedCells.asStateFlow()

    // Network fetch state
    private val _isFetchingNetwork = MutableStateFlow(false)
    val isFetchingNetwork: StateFlow<Boolean> = _isFetchingNetwork.asStateFlow()

    private val _networkFetchError = MutableStateFlow<String?>(null)
    val networkFetchError: StateFlow<String?> = _networkFetchError.asStateFlow()

    private fun loadCellList(): List<CellInfo> {
        val json = prefs.getString(KEY_CELL_LIST, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<CellInfo>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(CellInfo(
                    id = obj.optString("id", ""),
                    networkType = obj.optString("networkType", "LTE"),
                    mcc = obj.optInt("mcc", 460),
                    mnc = obj.optInt("mnc", 0),
                    lac = obj.optInt("lac", 0),
                    cid = obj.optLong("cid", 0),
                    psc = obj.optInt("psc", 0),
                    latitude = obj.optDouble("latitude", 0.0),
                    longitude = obj.optDouble("longitude", 0.0),
                    radius = obj.optDouble("radius", 1000.0).toFloat()
                ))
            }
            list
        } catch (_: Exception) { emptyList() }
    }

    private fun saveCellList(list: List<CellInfo>) {
        val array = JSONArray()
        list.forEach { cell ->
            val obj = JSONObject().apply {
                put("id", cell.id)
                put("networkType", cell.networkType)
                put("mcc", cell.mcc)
                put("mnc", cell.mnc)
                put("lac", cell.lac)
                put("cid", cell.cid)
                put("psc", cell.psc)
                put("latitude", cell.latitude)
                put("longitude", cell.longitude)
                put("radius", cell.radius)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CELL_LIST, array.toString()).apply()
    }

    private fun loadSelectedIds(): Set<String> {
        val csv = prefs.getString(KEY_CELL_SELECTED_IDS, "") ?: ""
        return csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private fun saveSelectedIds(ids: Set<String>) {
        prefs.edit().putString(KEY_CELL_SELECTED_IDS, ids.joinToString(",")).apply()
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
        prefs.edit().putBoolean(KEY_CELL_IS_SIMULATING, value).apply()
        _isSimulating.value = value
        writeCellConfigToFile(value)
        if (value) {
            startServiceGoRootCellMode()
        } else {
            stopServiceGoRootCellMode()
        }
    }

    /**
     * 模拟进行中时，把最新的基站列表推给正在运行的 ServiceGoRoot，立即生效
     * （页面内添加/勾选后无需停止再开始）。未在模拟时不做任何事。
     */
    private fun pushCellListToService() {
        if (!_isSimulating.value) return
        try {
            val ctx = getApplication<Application>().applicationContext
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                    com.kail.location.service.Root.ServiceGoRoot.CONTROL_SET_CELL
                )
                putParcelableArrayListExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CELL_LIST,
                    ArrayList(activeCellList)
                )
            }
            ctx.startService(intent)
            KailLog.i(ctx, TAG, "pushCellListToService: ${activeCellList.size} towers")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "pushCellListToService failed", e)
        }
    }

    private fun stopServiceGoRootCellMode() {
        try {
            val ctx = getApplication<Application>().applicationContext
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CONTROL_ACTION,
                    com.kail.location.service.Root.ServiceGoRoot.CONTROL_STOP_CELL
                )
            }
            ctx.startService(intent)
            KailLog.i(ctx, TAG, "stopServiceGoRootCellMode: sent CONTROL_STOP_CELL")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "stopServiceGoRootCellMode failed", e)
        }
    }

    private fun writeCellConfigToFile(simulating: Boolean) {
        try {
            val effectiveEnabled = simulating && activeCellList.isNotEmpty()
            val array = JSONArray()
            activeCellList.forEach { cell ->
                val obj = JSONObject().apply {
                    put("id", cell.id)
                    put("networkType", cell.networkType)
                    put("mcc", cell.mcc)
                    put("mnc", cell.mnc)
                    put("lac", cell.lac)
                    put("cid", cell.cid)
                    put("psc", cell.psc)
                    put("latitude", cell.latitude)
                    put("longitude", cell.longitude)
                    put("radius", cell.radius)
                }
                array.put(obj)
            }
            val cellConfig = """{"enabled":$effectiveEnabled,"list":$array}"""
            val cellFile = "/data/local/kail-lib/kail_cell.json"
            com.kail.location.utils.ShellUtils.executeCommand("echo '$cellConfig' > $cellFile")
            com.kail.location.utils.ShellUtils.executeCommand("chmod 777 $cellFile")

            val confFile = "/data/local/kail-lib/kail_location.conf"
            val checkResult = com.kail.location.utils.ShellUtils.executeCommand("[ -f $confFile ] && echo yes || echo no").trim()
            if (checkResult == "yes") {
                com.kail.location.utils.ShellUtils.executeCommand("sed -i 's/^cell_mock=.*/cell_mock=$effectiveEnabled/' $confFile || echo 'cell_mock=$effectiveEnabled' >> $confFile")
                com.kail.location.utils.ShellUtils.executeCommand("sed -i 's/^enabled=.*/enabled=false/' $confFile || echo 'enabled=false' >> $confFile")
            } else {
                val content = """enabled=false
mode=global
wifi_mock=false
cell_mock=$effectiveEnabled
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
            KailLog.i(getApplication(), TAG, "writeCellConfigToFile: enabled=$effectiveEnabled cells=${activeCellList.size}")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "writeCellConfigToFile failed", e)
        }
    }

    private fun startServiceGoRootCellMode() {
        try {
            val ctx = getApplication<Application>().applicationContext
            val intent = android.content.Intent(ctx, com.kail.location.service.Root.ServiceGoRoot::class.java).apply {
                putExtra(com.kail.location.service.Root.ServiceGoRoot.EXTRA_CELL_ONLY, true)
                putExtra(com.kail.location.views.locationpicker.LocationPickerActivity.LAT_MSG_ID, com.kail.location.service.Root.ServiceGoRoot.DEFAULT_LAT)
                putExtra(com.kail.location.views.locationpicker.LocationPickerActivity.LNG_MSG_ID, com.kail.location.service.Root.ServiceGoRoot.DEFAULT_LNG)
                putExtra(com.kail.location.views.locationpicker.LocationPickerActivity.ALT_MSG_ID, prefs.getString("setting_altitude", "55.0")?.toDoubleOrNull() ?: 55.0)
                // Pass the selected cell towers so ServiceGoRoot can push them
                // into the FakeLocation injection layer (setMockCells).
                putParcelableArrayListExtra(
                    com.kail.location.service.Root.ServiceGoRoot.EXTRA_CELL_LIST,
                    ArrayList(activeCellList)
                )
            }
            ctx.startService(intent)
            KailLog.i(ctx, TAG, "startServiceGoRootCellMode: started with ${activeCellList.size} cells")
        } catch (e: Exception) {
            KailLog.e(getApplication(), TAG, "startServiceGoRootCellMode failed", e)
        }
    }

    /** Toggle selection of a cell in the simulation list */
    fun toggleSelection(id: String) {
        val current = _selectedIds.value.toMutableSet()
        if (id in current) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
        saveSelectedIds(current)
        pushCellListToService()
    }

    fun selectAll() {
        val allIds = _cellList.value.map { it.id }.toSet()
        _selectedIds.value = allIds
        saveSelectedIds(allIds)
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
        saveSelectedIds(emptySet())
    }

    fun addCell(info: CellInfo) {
        val newInfo = if (info.id.isEmpty()) info.copy(id = UUID.randomUUID().toString()) else info
        val newList = _cellList.value + newInfo
        _cellList.value = newList
        saveCellList(newList)
        // Auto-select newly added item
        toggleSelection(newInfo.id)
    }

    fun updateCell(info: CellInfo) {
        val newList = _cellList.value.map { if (it.id == info.id) info else it }
        _cellList.value = newList
        saveCellList(newList)
        pushCellListToService()
    }

    fun deleteCell(id: String) {
        val newList = _cellList.value.filter { it.id != id }
        _cellList.value = newList
        saveCellList(newList)
        if (id in _selectedIds.value) {
            toggleSelection(id)
        }
    }

    // ─── Scan current cell towers ───

    fun scanCurrentCellInfo() {
        viewModelScope.launch {
            _isScanningCell.value = true
            try {
                val telephonyManager = getApplication<Application>().getSystemService(TelephonyManager::class.java)
                val cellInfos = telephonyManager?.allCellInfo
                if (cellInfos.isNullOrEmpty()) {
                    _scannedCells.value = emptyList()
                    return@launch
                }

                val (lat, lng) = getCurrentLocation()
                val results = mutableListOf<CellInfo>()

                for (cellInfo in cellInfos) {
                    val info = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && cellInfo is android.telephony.CellInfoLte -> {
                            val identity = cellInfo.cellIdentity
                            CellInfo(
                                id = UUID.randomUUID().toString(),
                                networkType = "LTE",
                                mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    identity.mccString?.toIntOrNull() ?: 460
                                } else {
                                    @Suppress("DEPRECATION")
                                    identity.mcc.takeIf { it != Integer.MAX_VALUE } ?: 460
                                },
                                mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    identity.mncString?.toIntOrNull() ?: 0
                                } else {
                                    @Suppress("DEPRECATION")
                                    identity.mnc.takeIf { it != Integer.MAX_VALUE } ?: 0
                                },
                                lac = identity.tac.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                cid = identity.ci.takeIf { it != Integer.MAX_VALUE }?.toLong() ?: 0L,
                                psc = identity.pci.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                latitude = lat,
                                longitude = lng,
                                radius = 1000f
                            )
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && cellInfo is android.telephony.CellInfoGsm -> {
                            val identity = cellInfo.cellIdentity
                            CellInfo(
                                id = UUID.randomUUID().toString(),
                                networkType = "GSM",
                                mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    identity.mccString?.toIntOrNull() ?: 460
                                } else {
                                    @Suppress("DEPRECATION")
                                    identity.mcc.takeIf { it != Integer.MAX_VALUE } ?: 460
                                },
                                mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    identity.mncString?.toIntOrNull() ?: 0
                                } else {
                                    @Suppress("DEPRECATION")
                                    identity.mnc.takeIf { it != Integer.MAX_VALUE } ?: 0
                                },
                                lac = identity.lac.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                cid = identity.cid.takeIf { it != Integer.MAX_VALUE }?.toLong() ?: 0L,
                                psc = 0,
                                latitude = lat,
                                longitude = lng,
                                radius = 1000f
                            )
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && cellInfo is android.telephony.CellInfoWcdma -> {
                            val identity = cellInfo.cellIdentity
                            CellInfo(
                                id = UUID.randomUUID().toString(),
                                networkType = "WCDMA",
                                mcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    identity.mccString?.toIntOrNull() ?: 460
                                } else {
                                    @Suppress("DEPRECATION")
                                    identity.mcc.takeIf { it != Integer.MAX_VALUE } ?: 460
                                },
                                mnc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    identity.mncString?.toIntOrNull() ?: 0
                                } else {
                                    @Suppress("DEPRECATION")
                                    identity.mnc.takeIf { it != Integer.MAX_VALUE } ?: 0
                                },
                                lac = identity.lac.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                cid = identity.cid.takeIf { it != Integer.MAX_VALUE }?.toLong() ?: 0L,
                                psc = identity.psc.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                latitude = lat,
                                longitude = lng,
                                radius = 1000f
                            )
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && cellInfo is android.telephony.CellInfoCdma -> {
                            val identity = cellInfo.cellIdentity
                            CellInfo(
                                id = UUID.randomUUID().toString(),
                                networkType = "CDMA",
                                mcc = 460,
                                mnc = identity.systemId.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                lac = identity.networkId.takeIf { it != Integer.MAX_VALUE } ?: 0,
                                cid = identity.basestationId.takeIf { it != Integer.MAX_VALUE }?.toLong() ?: 0L,
                                psc = 0,
                                latitude = lat,
                                longitude = lng,
                                radius = 1000f
                            )
                        }
                        else -> null
                    }
                    info?.let { results.add(it) }
                }
                _scannedCells.value = results
                KailLog.i(getApplication(), TAG, "scanCurrentCellInfo: found ${results.size} cells")
            } catch (e: Exception) {
                KailLog.e(getApplication(), TAG, "scanCurrentCellInfo failed", e)
                _scannedCells.value = emptyList()
            } finally {
                _isScanningCell.value = false
            }
        }
    }

    private fun getCurrentLocation(): Pair<Double, Double> {
        try {
            val locationManager = getApplication<Application>().getSystemService(LocationManager::class.java)
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastGps != null) return Pair(lastGps.latitude, lastGps.longitude)
            val lastNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastNetwork != null) return Pair(lastNetwork.latitude, lastNetwork.longitude)
        } catch (_: Exception) {
        }
        return Pair(0.0, 0.0)
    }

    fun addScannedCells() {
        val scanned = _scannedCells.value
        if (scanned.isEmpty()) return
        val newList = _cellList.value + scanned
        _cellList.value = newList
        saveCellList(newList)
        // Auto-select newly added items
        scanned.forEach { toggleSelection(it.id) }
        _scannedCells.value = emptyList()
    }

    fun clearScannedCells() {
        _scannedCells.value = emptyList()
    }

    // ─── Fetch cells from OpenCellID by lat/lng ───

    fun fetchCellsFromNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            _isFetchingNetwork.value = true
            _networkFetchError.value = null
            try {
                val (lat, lng) = getCurrentWgs84Location()
                KailLog.d(getApplication(), TAG, "fetchCellsFromNetwork: location lat=$lat lon=$lng")
                if (lat == 0.0 && lng == 0.0) {
                    _networkFetchError.value = getApplication<Application>().getString(R.string.cell_fetch_no_location)
                    _isFetchingNetwork.value = false
                    return@launch
                }

                val apiKey = prefs.getString(SettingsViewModel.KEY_OPENCELLID_API_KEY, "") ?: ""
                KailLog.d(getApplication(), TAG, "fetchCellsFromNetwork: apiKey length=${apiKey.length} blank=${apiKey.isBlank()}")
                if (apiKey.isBlank()) {
                    _networkFetchError.value = getApplication<Application>().getString(R.string.cell_fetch_no_key)
                    _isFetchingNetwork.value = false
                    return@launch
                }

                val cells = com.kail.location.network.OpenCellIdClient.fetchCellsInArea(
                    apiKey = apiKey,
                    lat = lat,
                    lon = lng,
                    radiusKm = 0.5
                )
                KailLog.i(getApplication(), TAG, "fetchCellsFromNetwork: fetched ${cells.size} cells")

                if (cells.isEmpty()) {
                    _networkFetchError.value = getApplication<Application>().getString(R.string.cell_fetch_no_data)
                } else {
                    _scannedCells.value = cells
                }
            } catch (e: Exception) {
                KailLog.e(getApplication(), TAG, "fetchCellsFromNetwork failed", e)
                _networkFetchError.value = getApplication<Application>().getString(R.string.cell_fetch_failed, e.message)
            } finally {
                _isFetchingNetwork.value = false
            }
        }
    }

    fun clearNetworkFetchError() {
        _networkFetchError.value = null
    }

    private fun getCurrentWgs84Location(): Pair<Double, Double> {
        try {
            val locationManager = getApplication<Application>().getSystemService(LocationManager::class.java)
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastGps != null) return Pair(lastGps.latitude, lastGps.longitude)
            val lastNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastNetwork != null) return Pair(lastNetwork.latitude, lastNetwork.longitude)
        } catch (_: Exception) {
        }
        return Pair(0.0, 0.0)
    }
}
