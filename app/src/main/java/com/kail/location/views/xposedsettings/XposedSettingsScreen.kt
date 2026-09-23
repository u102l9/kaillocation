package com.kail.location.views.xposedsettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.kail.location.R
import com.kail.location.views.common.AppDrawer
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XposedSettingsScreen(
    onNavigate: (Int) -> Unit,
    appVersion: String,
    runMode: String,
    onRunModeChange: (String) -> Unit,
    onDeveloperModeSelected: () -> Unit = {},
    onXposedSettingsSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val scrollState = rememberScrollState()
    val drawerState = remember { androidx.compose.material3.DrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed) }
    val scope = rememberCoroutineScope()

    var showHelp by remember { mutableStateOf(false) }

    var enableMockGnss by remember { mutableStateOf(prefs.getBoolean("setting_gps_satellite_sim", true)) }
    var disableFused by remember { mutableStateOf(prefs.getBoolean("setting_disable_fused", false)) }
    var hideMock by remember { mutableStateOf(prefs.getBoolean("setting_hide_mock", false)) }
    var disableWifiScan by remember { mutableStateOf(prefs.getBoolean("setting_disable_wifi_scan", false)) }
    var downgradeCdma by remember { mutableStateOf(prefs.getBoolean("setting_downgrade_cdma", false)) }
    var antiPullback by remember { mutableStateOf(prefs.getBoolean("setting_anti_pullback", false)) }
    var minSatellites by remember { mutableStateOf(prefs.getString("setting_min_satellites", "12") ?: "12") }
    var reportInterval by remember { mutableStateOf(prefs.getString("setting_report_interval", "200") ?: "200") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                currentScreen = "XposedSettings",
                onNavigate = onNavigate,
                appVersion = appVersion,
                runMode = runMode,
                onRunModeChange = onRunModeChange,
                onDeveloperModeSelected = onDeveloperModeSelected,
                onXposedSettingsSelected = onXposedSettingsSelected,
                scope = scope
            )
        }
    ) {
        Box {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.drawer_xposed_settings)) },
                        navigationIcon = {
                            BadgedControl(show = showHelp, number = 1) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        actions = {
                            HelpActionButton(showHelp = showHelp) { showHelp = true }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                ) {
                    PreferenceCategory(title = stringResource(R.string.setting_group_satellite_and_signal))

                    BadgedControl(show = showHelp, number = 2, modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.setting_gps_satellite_title),
                            checked = enableMockGnss,
                            onCheckedChange = {
                                enableMockGnss = it
                                prefs.edit().putBoolean("setting_gps_satellite_sim", it).apply()
                            },
                            summary = stringResource(R.string.setting_gps_satellite_summary)
                        )
                    }

                    BadgedControl(show = showHelp, number = 3, modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.setting_disable_fused),
                            checked = disableFused,
                            onCheckedChange = {
                                disableFused = it
                                prefs.edit().putBoolean("setting_disable_fused", it).apply()
                            },
                            summary = stringResource(R.string.setting_disable_fused_summary)
                        )
                    }

                    BadgedControl(show = showHelp, number = 4, modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.setting_hide_mock),
                            checked = hideMock,
                            onCheckedChange = {
                                hideMock = it
                                prefs.edit().putBoolean("setting_hide_mock", it).apply()
                            },
                            summary = stringResource(R.string.setting_hide_mock_summary)
                        )
                    }

                    BadgedControl(show = showHelp, number = 5, modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.setting_disable_wifi_scan),
                            checked = disableWifiScan,
                            onCheckedChange = {
                                disableWifiScan = it
                                prefs.edit().putBoolean("setting_disable_wifi_scan", it).apply()
                            },
                            summary = stringResource(R.string.setting_disable_wifi_scan_summary)
                        )
                    }

                    BadgedControl(show = showHelp, number = 6, modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.setting_downgrade_cdma),
                            checked = downgradeCdma,
                            onCheckedChange = {
                                downgradeCdma = it
                                prefs.edit().putBoolean("setting_downgrade_cdma", it).apply()
                            },
                            summary = stringResource(R.string.setting_downgrade_cdma_summary)
                        )
                    }

                    PreferenceCategory(title = stringResource(R.string.setting_group_anti_detect))

                    BadgedControl(show = showHelp, number = 7, modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.setting_anti_pullback),
                            checked = antiPullback,
                            onCheckedChange = {
                                antiPullback = it
                                prefs.edit().putBoolean("setting_anti_pullback", it).apply()
                            },
                            summary = stringResource(R.string.setting_anti_pullback_summary)
                        )
                    }

                    PreferenceCategory(title = stringResource(R.string.setting_group_intercept))

                    BadgedControl(show = showHelp, number = 8, modifier = Modifier.fillMaxWidth()) {
                        EditTextPreference(
                            title = stringResource(R.string.setting_min_satellites),
                            value = minSatellites,
                            onValueChange = {
                                minSatellites = it
                                prefs.edit().putString("setting_min_satellites", it).apply()
                            },
                            description = stringResource(R.string.setting_min_satellites_summary)
                        )
                    }

                    BadgedControl(show = showHelp, number = 9, modifier = Modifier.fillMaxWidth()) {
                        EditTextPreference(
                            title = stringResource(R.string.setting_report_interval),
                            value = reportInterval,
                            onValueChange = {
                                reportInterval = it
                                prefs.edit().putString("setting_report_interval", it).apply()
                            },
                            description = stringResource(R.string.setting_report_interval_summary)
                        )
                    }
                }
            }

            HelpOverlayScrim(
                showHelp = showHelp,
                entries = listOf(
                    1 to R.string.help_xposed_menu,
                    2 to R.string.help_xposed_gps_satellite,
                    3 to R.string.help_xposed_disable_fused,
                    4 to R.string.help_xposed_hide_mock,
                    5 to R.string.help_xposed_disable_wifi_scan,
                    6 to R.string.help_xposed_downgrade_cdma,
                    7 to R.string.help_xposed_anti_pullback,
                    8 to R.string.help_xposed_min_satellites,
                    9 to R.string.help_xposed_report_interval
                ),
                onDismiss = { showHelp = false }
            )
        }
    }
}

@Composable
fun PreferenceCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

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
                Text(value.ifEmpty { stringResource(R.string.xposed_not_set) })
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
