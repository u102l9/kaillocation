package com.kail.location.views.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.R
import com.kail.location.utils.DataTransferManager
import com.kail.location.utils.KailLog
import com.kail.location.utils.ShellUtils
import com.kail.location.viewmodels.SettingsViewModel
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 设置屏幕主界面
 * 展示所有可配置的应用选项，按类别分组显示。
 *
 * @param viewModel 设置界面的 ViewModel，用于读取和更新偏好设置
 * @param onBackClick 返回按钮点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var logCacheSize by remember { mutableStateOf(KailLog.getLogCacheSizeBytes(context)) }
    var showClearLogDialog by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val exportLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val ok = KailLog.exportLogs(context, uri)
            android.widget.Toast.makeText(
                context,
                if (ok) context.getString(R.string.setting_export_log_success) else context.getString(R.string.setting_export_log_failed),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        logCacheSize = KailLog.getLogCacheSizeBytes(context)
    }

    // ===== 数据导出 / 导入 状态 =====
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    // 待导出的类别集合（在导出对话框中确认后传给文件创建回调）
    var pendingExportCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    // 已解析的待导入备份及其类别选择
    var parsedBackup by remember { mutableStateOf<DataTransferManager.ParsedBackup?>(null) }

    val exportDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && pendingExportCategories.isNotEmpty()) {
            val ok = DataTransferManager.export(context, uri, pendingExportCategories)
            android.widget.Toast.makeText(
                context,
                if (ok) context.getString(R.string.data_transfer_export_success) else context.getString(R.string.data_transfer_export_failed),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        pendingExportCategories = emptySet()
    }

    val pickBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val parsed = DataTransferManager.parseBackup(context, uri)
            if (parsed == null) {
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.data_transfer_invalid_file),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                parsedBackup = parsed
                showImportDialog = true
            }
        }
    }

    // State observation
    val joystickType by viewModel.joystickType.collectAsState()
    val walkSpeed by viewModel.walkSpeed.collectAsState()
    val runSpeed by viewModel.runSpeed.collectAsState()
    val bikeSpeed by viewModel.bikeSpeed.collectAsState()
    val altitude by viewModel.altitude.collectAsState()
    val mockSpeed by viewModel.mockSpeed.collectAsState()
    val accuracy by viewModel.accuracy.collectAsState()
    val minSatellites by viewModel.minSatellites.collectAsState()
    val reportInterval by viewModel.reportInterval.collectAsState()
    val randomOffset by viewModel.randomOffset.collectAsState()
    val latOffset by viewModel.latOffset.collectAsState()
    val lonOffset by viewModel.lonOffset.collectAsState()
    val logEnabled by viewModel.logEnabled.collectAsState()
    val debugLogEnabled by viewModel.debugLogEnabled.collectAsState()
    val historyExpiration by viewModel.historyExpiration.collectAsState()
    val baiduMapKey by viewModel.baiduMapKey.collectAsState()
    val mapZoom by viewModel.mapZoom.collectAsState()
    val gpsSatelliteSim by viewModel.gpsSatelliteSim.collectAsState()
    val naturalJitter by viewModel.naturalJitter.collectAsState()
    val stepSimEnabled by viewModel.stepSimEnabled.collectAsState()
    val simScheme by viewModel.simScheme.collectAsState()
    val opencellidApiKey by viewModel.opencellidApiKey.collectAsState()

    // ===== Xposed 模块隐藏状态 =====
    val xposedScope = rememberCoroutineScope()
    var hasRoot by remember { mutableStateOf<Boolean?>(null) }
    var xposedModuleState by remember { mutableStateOf("checking") } // checking | visible | hidden | absent

    fun queryXposedModuleState(): String {
        val visible = ShellUtils.executeCommand("pm list packages com.kail.locationxposed", 15_000L)
        if (visible.contains("package:com.kail.locationxposed")) return "visible"
        val all = ShellUtils.executeCommand("pm list packages -u com.kail.locationxposed", 15_000L)
        return if (all.contains("package:com.kail.locationxposed")) "hidden" else "absent"
    }

    LaunchedEffect(Unit) {
        hasRoot = withContext(Dispatchers.IO) { ShellUtils.hasRoot() }
        xposedModuleState = withContext(Dispatchers.IO) { queryXposedModuleState() }
    }

    Box {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_menu_settings)) },
                    navigationIcon = {
                        BadgedControl(show = showHelp, number = 1) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        HelpActionButton(showHelp = showHelp) { showHelp = true }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // ===== Group: 移动参数 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_move))

            BadgedControl(show = showHelp, number = 2, modifier = Modifier.fillMaxWidth()) {
                ListPreference(
                    title = stringResource(R.string.setting_joystick_type),
                    currentValue = joystickType,
                    entries = stringArrayResource(R.array.array_joystick_type),
                    entryValues = stringArrayResource(R.array.array_joystick_type_values),
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_JOYSTICK_TYPE, it) }
                )
            }

            BadgedControl(show = showHelp, number = 3, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_walk),
                    value = walkSpeed,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_WALK_SPEED, it) }
                )
            }

            BadgedControl(show = showHelp, number = 4, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_run),
                    value = runSpeed,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_RUN_SPEED, it) }
                )
            }

            BadgedControl(show = showHelp, number = 5, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_bike),
                    value = bikeSpeed,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_BIKE_SPEED, it) }
                )
            }

            // ===== Group: 位置模拟参数 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_move))

            BadgedControl(show = showHelp, number = 6, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_altitude),
                    value = altitude,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_ALTITUDE, it) }
                )
            }

            BadgedControl(show = showHelp, number = 7, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_mock_speed),
                    value = mockSpeed,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_MOCK_SPEED, it) }
                )
            }

            BadgedControl(show = showHelp, number = 8, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_accuracy_title),
                    value = accuracy,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_ACCURACY, it) },
                    description = stringResource(R.string.setting_accuracy_summary)
                )
            }

            BadgedControl(show = showHelp, number = 9, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_min_satellites),
                    value = minSatellites,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_MIN_SATELLITES, it) },
                    description = stringResource(R.string.setting_min_satellites_summary)
                )
            }

            BadgedControl(show = showHelp, number = 10, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_report_interval),
                    value = reportInterval,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_REPORT_INTERVAL, it) },
                    description = stringResource(R.string.setting_report_interval_summary)
                )
            }

            // ===== Group: 位置偏移 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_location_offset))

            BadgedControl(show = showHelp, number = 11, modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.setting_random_offset),
                    checked = randomOffset,
                    onCheckedChange = { viewModel.updateBooleanPreference(SettingsViewModel.KEY_RANDOM_OFFSET, it) },
                    summary = stringResource(R.string.setting_random_offset_summary)
                )
            }

            BadgedControl(show = showHelp, number = 12, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_lat_max_offset),
                    value = latOffset,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_LAT_OFFSET, it) }
                )
            }

            BadgedControl(show = showHelp, number = 13, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_lon_max_offset),
                    value = lonOffset,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_LON_OFFSET, it) }
                )
            }

            // ===== Group: 卫星与信号 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_satellite_and_signal))

            BadgedControl(show = showHelp, number = 14, modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.setting_gps_satellite_title),
                    checked = gpsSatelliteSim,
                    onCheckedChange = { viewModel.updateBooleanPreference(SettingsViewModel.KEY_GPS_SATELLITE_SIM, it) },
                    summary = stringResource(R.string.setting_gps_satellite_summary)
                )
            }

            // ===== Group: 步频模拟 =====
            PreferenceCategory(title = stringResource(R.string.settings_step_sim))

            BadgedControl(show = showHelp, number = 15, modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.settings_step_sim_enable),
                    checked = stepSimEnabled,
                    onCheckedChange = { viewModel.updateBooleanPreference(SettingsViewModel.KEY_STEP_SIM_ENABLED, it) },
                    summary = stringResource(R.string.settings_step_sim_summary)
                )
            }

            BadgedControl(show = showHelp, number = 16, modifier = Modifier.fillMaxWidth()) {
                ListPreference(
                    title = stringResource(R.string.settings_step_type),
                    currentValue = simScheme,
                    entries = stringArrayResource(R.array.array_sim_scheme),
                    entryValues = stringArrayResource(R.array.array_sim_scheme_values),
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_SIM_SCHEME, it) }
                )
            }

            BadgedControl(show = showHelp, number = 17, modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.setting_natural_jitter),
                    checked = naturalJitter,
                    onCheckedChange = { viewModel.updateBooleanPreference(SettingsViewModel.KEY_NATURAL_JITTER, it) },
                    summary = stringResource(R.string.setting_natural_jitter_summary)
                )
            }

            // ===== Group: 日志 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_log))

            BadgedControl(show = showHelp, number = 18, modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.setting_enable_log),
                    checked = logEnabled,
                    onCheckedChange = { viewModel.updateBooleanPreference(SettingsViewModel.KEY_LOG_ENABLED, it) },
                    summary = stringResource(R.string.setting_enable_log_summary)
                )
            }

            BadgedControl(show = showHelp, number = 19, modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.setting_enable_debug_log),
                    checked = debugLogEnabled,
                    onCheckedChange = { viewModel.updateBooleanPreference(SettingsViewModel.KEY_DEBUG_LOG_ENABLED, it) },
                    summary = stringResource(R.string.setting_enable_debug_log_summary)
                )
            }

            BadgedControl(show = showHelp, number = 20, modifier = Modifier.fillMaxWidth()) {
                ActionPreference(
                    title = stringResource(R.string.setting_export_log),
                    summary = stringResource(R.string.setting_export_log_summary),
                    onClick = { exportLogLauncher.launch("kail_location_logs.txt") }
                )
            }

            BadgedControl(show = showHelp, number = 21, modifier = Modifier.fillMaxWidth()) {
                ActionPreference(
                    title = stringResource(R.string.setting_clear_log_cache),
                    summary = stringResource(R.string.setting_clear_log_cache_summary, formatBytes(logCacheSize)),
                    onClick = {
                        logCacheSize = KailLog.getLogCacheSizeBytes(context)
                        showClearLogDialog = true
                    }
                )
            }

            // ===== Group: 其他 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_other))

            BadgedControl(show = showHelp, number = 23, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_baidu_key),
                    value = baiduMapKey,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_BAIDU_MAP_KEY, it) }
                )
            }

            BadgedControl(show = showHelp, number = 24, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_opencellid_key),
                    value = opencellidApiKey,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_OPENCELLID_API_KEY, it) },
                    description = stringResource(R.string.setting_opencellid_key_summary)
                )
            }

            BadgedControl(show = showHelp, number = 25, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_history_expiration),
                    value = historyExpiration,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_HISTORY_EXPIRATION, it) }
                )
            }

            // ===== Group: Xposed 模块 =====
            PreferenceCategory(title = stringResource(R.string.setting_xposed_module_group))

            val moduleSummary = when {
                xposedModuleState == "checking" -> "检测中…"
                xposedModuleState == "absent" -> context.getString(R.string.setting_xposed_module_summary)
                hasRoot != true -> context.getString(R.string.setting_xposed_module_summary)
                xposedModuleState == "hidden" -> "已隐藏：桌面图标与 LSPosed 模块页的「打开」按钮不可见，可随时恢复"
                else -> "显示中：桌面图标可见，LSPosed 模块页可一键打开"
            }
            SwitchPreference(
                title = stringResource(R.string.setting_xposed_module_hide),
                checked = xposedModuleState == "hidden",
                enabled = hasRoot == true && xposedModuleState != "checking" && xposedModuleState != "absent",
                summary = moduleSummary,
                onCheckedChange = { hide ->
                    xposedScope.launch(Dispatchers.IO) {
                        val cmd = if (hide) "pm hide com.kail.locationxposed" else "pm unhide com.kail.locationxposed"
                        val out = ShellUtils.executeCommand(cmd, 30_000L)
                        xposedModuleState = queryXposedModuleState()
                        val ok = if (hide) xposedModuleState == "hidden" else xposedModuleState == "visible"
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(
                                context,
                                if (ok) {
                                    if (hide) "Xposed 模块已隐藏（桌面图标与 LSPosed 按钮已消失）"
                                    else "Xposed 模块已恢复显示"
                                } else "操作失败：$out",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )

            // ===== Group: 数据备份 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_data_transfer))

            BadgedControl(show = showHelp, number = 26, modifier = Modifier.fillMaxWidth()) {
                ActionPreference(
                    title = stringResource(R.string.setting_export_data),
                    summary = stringResource(R.string.setting_export_data_summary),
                    onClick = { showExportDialog = true }
                )
            }

            BadgedControl(show = showHelp, number = 27, modifier = Modifier.fillMaxWidth()) {
                ActionPreference(
                    title = stringResource(R.string.setting_import_data),
                    summary = stringResource(R.string.setting_import_data_summary),
                    onClick = { pickBackupLauncher.launch(arrayOf("*/*")) }
                )
            }

            // ===== Group: 个性化 =====
            PreferenceCategory(title = stringResource(R.string.setting_group_personalization))

            BadgedControl(show = showHelp, number = 28, modifier = Modifier.fillMaxWidth()) {
                SizePreference(
                    title = stringResource(R.string.setting_floating_window_size),
                    width = viewModel.floatingWindowWidth.collectAsState().value,
                    height = viewModel.floatingWindowHeight.collectAsState().value,
                    onWidthChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_FLOATING_WINDOW_WIDTH, it) },
                    onHeightChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_FLOATING_WINDOW_HEIGHT, it) }
                )
            }

            BadgedControl(show = showHelp, number = 29, modifier = Modifier.fillMaxWidth()) {
                EditTextPreference(
                    title = stringResource(R.string.setting_map_zoom),
                    value = viewModel.mapZoom.collectAsState().value,
                    onValueChange = { viewModel.updateStringPreference(SettingsViewModel.KEY_MAP_ZOOM, it) },
                    description = stringResource(R.string.setting_map_zoom_summary)
                )
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.setting_current_version)) },
                supportingContent = { Text(viewModel.appVersion) }
            )
        }
    }

        HelpOverlayScrim(
            showHelp = showHelp,
            entries = listOf(
                1 to R.string.help_settings_back,
                2 to R.string.help_settings_joystick,
                3 to R.string.help_settings_walk,
                4 to R.string.help_settings_run,
                5 to R.string.help_settings_bike,
                6 to R.string.help_settings_altitude,
                7 to R.string.help_settings_mock_speed,
                8 to R.string.help_settings_accuracy,
                9 to R.string.help_settings_min_satellites,
                10 to R.string.help_settings_report_interval,
                11 to R.string.help_settings_random_offset,
                12 to R.string.help_settings_lat_offset,
                13 to R.string.help_settings_lon_offset,
                14 to R.string.help_settings_gps_satellite,
                15 to R.string.help_settings_step_sim,
                16 to R.string.help_settings_step_type,
                17 to R.string.help_settings_natural_jitter,
                18 to R.string.help_settings_enable_log,
                19 to R.string.help_settings_debug_log,
                20 to R.string.help_settings_export_log,
                21 to R.string.help_settings_clear_log,
                23 to R.string.help_settings_baidu_key,
                24 to R.string.help_settings_opencellid_key,
                25 to R.string.help_settings_history_expiration,
                26 to R.string.help_settings_export_data,
                27 to R.string.help_settings_import_data,
                28 to R.string.help_settings_window_size,
                29 to R.string.help_settings_map_zoom
            ),
            onDismiss = { showHelp = false }
        )
    }

    if (showExportDialog) {
        DataCategorySelectionDialog(
            title = stringResource(R.string.data_transfer_export_title),
            description = stringResource(R.string.data_transfer_select_categories),
            entryCounts = remember { DataTransferManager.availableEntryCounts(context) },
            confirmText = stringResource(R.string.data_transfer_export_button),
            // 默认只勾选有数据的类别
            initiallySelected = remember {
                DataTransferManager.availableEntryCounts(context)
                    .filterValues { it > 0 }.keys
            },
            onConfirm = { selected ->
                showExportDialog = false
                pendingExportCategories = selected
                val defaultName = context.getString(R.string.data_transfer_default_filename)
                exportDataLauncher.launch("$defaultName.${DataTransferManager.FILE_EXTENSION}")
            },
            onDismiss = { showExportDialog = false }
        )
    }

    if (showImportDialog) {
        val backup = parsedBackup
        if (backup == null) {
            showImportDialog = false
        } else {
            DataCategorySelectionDialog(
                title = stringResource(R.string.data_transfer_import_title),
                description = stringResource(R.string.data_transfer_select_import_categories),
                entryCounts = backup.categoryEntryCounts,
                confirmText = stringResource(R.string.data_transfer_import_button),
                initiallySelected = backup.categoryEntryCounts.keys,
                // 仅展示备份文件中存在的类别
                restrictToKeys = backup.categoryEntryCounts.keys,
                onConfirm = { selected ->
                    showImportDialog = false
                    val imported = DataTransferManager.import(context, backup, selected)
                    parsedBackup = null
                    android.widget.Toast.makeText(
                        context,
                        if (imported.isNotEmpty())
                            context.getString(R.string.data_transfer_import_success, imported.size)
                        else
                            context.getString(R.string.data_transfer_import_failed),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                },
                onDismiss = {
                    showImportDialog = false
                    parsedBackup = null
                }
            )
        }
    }

    if (showClearLogDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogDialog = false },
            title = { Text(stringResource(R.string.setting_clear_log_cache)) },
            text = { Text(stringResource(R.string.setting_clear_log_cache_confirm, formatBytes(logCacheSize))) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = KailLog.clearLogCache(context)
                        logCacheSize = KailLog.getLogCacheSizeBytes(context)
                        showClearLogDialog = false
                        android.widget.Toast.makeText(
                            context,
                            if (ok) context.getString(R.string.setting_clear_log_cache_success) else context.getString(R.string.setting_clear_log_cache_failed),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                ) { Text(stringResource(R.string.setting_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogDialog = false }) {
                    Text(stringResource(R.string.setting_cancel))
                }
            }
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(java.util.Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(java.util.Locale.US, "%.2f MB", mb)
}

/**
 * 数据导出 / 导入的类别选择对话框。
 *
 * 列出每个导航菜单类别（带数据条目数量），允许用户多选要导出或导入的菜单数据。
 *
 * @param entryCounts 类别 id -> 条目数量。
 * @param initiallySelected 默认勾选的类别 id。
 * @param restrictToKeys 若非 null，仅展示这些类别（用于导入时只显示备份中存在的菜单）。
 * @param onConfirm 确认回调，参数为最终选中的类别 id 集合。
 */
@Composable
fun DataCategorySelectionDialog(
    title: String,
    description: String,
    entryCounts: Map<String, Int>,
    confirmText: String,
    initiallySelected: Set<String>,
    restrictToKeys: Set<String>? = null,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val visibleCategories = DataTransferManager.categories.filter {
        restrictToKeys == null || it.id in restrictToKeys
    }
    val selected = remember {
        mutableStateListOf<String>().apply { addAll(initiallySelected) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(description, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                // 按运行模式分组展示菜单类别
                DataTransferManager.ModeGroup.entries.forEach { group ->
                    val groupCategories = visibleCategories.filter { it.group == group }
                    if (groupCategories.isEmpty()) return@forEach
                    Text(
                        text = stringResource(group.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    groupCategories.forEach { category ->
                        val count = entryCounts[category.id] ?: 0
                        val name = stringResource(category.titleRes)
                        val label = if (count > 0) {
                            context.getString(R.string.data_transfer_category_count, name, count)
                        } else {
                            context.getString(R.string.data_transfer_category_empty, name)
                        }
                        val isChecked = category.id in selected
                        val enabled = count > 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = enabled) {
                                    if (isChecked) selected.remove(category.id) else selected.add(category.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = if (enabled) {
                                    { checked -> if (checked) selected.add(category.id) else selected.remove(category.id) }
                                } else null,
                                enabled = enabled
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selected.isEmpty()) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.data_transfer_none_selected),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onConfirm(selected.toSet())
                    }
                }
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.setting_cancel)) }
        }
    )
}

/**
 * 设置类别标题组件
 */
@Composable
fun PreferenceCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

/**
 * 开关类设置项组件
 */
@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        modifier = Modifier.clickable(enabled = enabled) { onCheckedChange(!checked) }
    )
}

@Composable
fun ActionPreference(
    title: String,
    summary: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        modifier = Modifier.clickable { onClick() }
    )
}

/**
 * 文本编辑类设置项组件
 */
@Composable
fun EditTextPreference(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    description: String = ""
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Column {
                Text(value.ifEmpty { stringResource(R.string.setting_not_set) })
                if (description.isNotEmpty()) {
                    Text(description, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        var tempValue by remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = { tempValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                    if (description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempValue.isNotBlank()) {
                            onValueChange(tempValue)
                        }
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.setting_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.setting_cancel))
                }
            }
        )
    }
}

/**
 * 浮窗大小设置项组件（弹窗含宽/高两个输入框）
 */
@Composable
fun SizePreference(
    title: String,
    width: String,
    height: String,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempW by remember { mutableStateOf(width) }
    var tempH by remember { mutableStateOf(height) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text("${width} x ${height}") },
        modifier = Modifier.clickable { tempW = width; tempH = height; showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempW,
                        onValueChange = { tempW = it },
                        label = { Text(stringResource(R.string.setting_floating_window_width)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempH,
                        onValueChange = { tempH = it },
                        label = { Text(stringResource(R.string.setting_floating_window_height)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempW.isNotBlank() && tempH.isNotBlank()) {
                        onWidthChange(tempW)
                        onHeightChange(tempH)
                    }
                    showDialog = false
                }) { Text(stringResource(R.string.setting_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.setting_cancel)) }
            }
        )
    }
}

/**
 * 列表选择类设置项组件
 */
@Composable
fun ListPreference(
    title: String,
    currentValue: String,
    entries: Array<String>,
    entryValues: Array<String>,
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val index = entryValues.indexOf(currentValue)
    val displayValue = if (index >= 0 && index < entries.size) entries[index] else currentValue

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(displayValue) },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    entries.forEachIndexed { i, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(entryValues[i])
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (entryValues[i] == currentValue),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = entry)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.setting_cancel))
                }
            }
        )
    }
}
