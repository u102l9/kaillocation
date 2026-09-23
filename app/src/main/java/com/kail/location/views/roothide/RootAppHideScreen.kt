package com.kail.location.views.roothide

import com.kail.location.utils.GoUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.R
import com.kail.location.viewmodels.RootAppHideViewModel
import com.kail.location.views.common.AppDrawer
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim
import com.kail.location.views.independentsimulation.AppInfo
import com.kail.location.views.independentsimulation.AppPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootAppHideScreen(
    viewModel: RootAppHideViewModel,
    onBackClick: () -> Unit,
    onNavigate: (Int) -> Unit = {},
    appVersion: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val appPrefs = remember {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    }
    var runMode by remember {
        mutableStateOf(appPrefs.getString("setting_run_mode", "developer") ?: "developer")
    }

    val isEnabled by viewModel.isEnabled.collectAsState()
    val hideRoot by viewModel.hideRoot.collectAsState()
    val hideAppList by viewModel.hideAppList.collectAsState()
    val targetPackages by viewModel.targetPackages.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var selectedPackages by remember {
        mutableStateOf(targetPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet())
    }

    val appInfos = remember(selectedPackages) {
        val pm = context.packageManager
        selectedPackages.mapNotNull { pkg ->
            try {
                val ai = pm.getApplicationInfo(pkg, 0)
                AppInfo(pkg, pm.getApplicationLabel(ai)?.toString() ?: pkg)
            } catch (e: Exception) {
                AppInfo(pkg, pkg)
            }
        }.sortedBy { it.appName }
    }

    val canStart = selectedPackages.isNotEmpty() && (hideRoot || hideAppList)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                currentScreen = "RootAppHide",
                onNavigate = onNavigate,
                appVersion = appVersion,
                runMode = runMode,
                onRunModeChange = { mode ->
                    runMode = mode
                    appPrefs.edit().putString("setting_run_mode", mode).apply()
                },
                onDeveloperModeSelected = {
                    if (GoUtils.isAllowMockLocation(context)) {
                        runMode = "developer"
                        appPrefs.edit().putString("setting_run_mode", "developer").apply()
                    } else {
                        GoUtils.openMockLocationSettings(context)
                    }
                },
                scope = scope
            )
        }
    ) {
        Box {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.root_hide_title)) },
                    navigationIcon = {
                        BadgedControl(show = showHelp, number = 1) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
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
                    .padding(16.dp)
            ) {
            // Target card (like Location Simulation)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isEnabled)
                                stringResource(R.string.root_hide_status_running)
                            else
                                stringResource(R.string.root_hide_status_stopped),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.root_hide_settings_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.root_hide_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Two switches side by side (like the joystick switch style)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SwitchCell(
                                label = stringResource(R.string.root_hide_hide_root),
                                checked = hideRoot,
                                enabled = true,
                                onCheckedChange = { viewModel.setHideRoot(it) },
                                badge = 3,
                                showHelp = showHelp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            SwitchCell(
                                label = stringResource(R.string.root_hide_hide_applist),
                                checked = hideAppList,
                                enabled = hideRoot,
                                onCheckedChange = { viewModel.setHideAppList(it) },
                                badge = 4,
                                showHelp = showHelp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Start/Stop Button
                        BadgedControl(show = showHelp, number = 6) {
                            Button(
                                onClick = {
                                    if (!isEnabled && !canStart) return@Button
                                    viewModel.setEnabled(!isEnabled)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                ),
                                enabled = isEnabled || canStart
                            ) {
                                Text(
                                    text = if (isEnabled)
                                        stringResource(R.string.root_hide_stop)
                                    else
                                        stringResource(R.string.root_hide_start)
                                )
                            }
                        }

                        if (!isEnabled && !canStart) {
                            Text(
                                text = stringResource(R.string.root_hide_no_apps),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // "+" add app button (overlapping top-end, like Location Simulation)
                BadgedControl(
                    show = showHelp,
                    number = 2,
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showAppPicker = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.root_hide_target_apps), tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Added apps header
            Text(
                text = stringResource(R.string.root_hide_added_apps, selectedPackages.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Added apps list (each row deletable)
            if (appInfos.isEmpty()) {
                Text(
                    text = stringResource(R.string.root_hide_target_apps_hint),
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                appInfos.forEachIndexed { index, info ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.appName,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = info.packageName,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            BadgedControl(show = showHelp && index == 0, number = 5) {
                                IconButton(onClick = {
                                    val newSet = selectedPackages - info.packageName
                                    selectedPackages = newSet
                                    viewModel.setTargetPackages(newSet.joinToString(","))
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.root_hide_remove_app),
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (showAppPicker) {
                AppPickerDialog(
                    selectedPackages = selectedPackages,
                    onPackagesChanged = {
                        selectedPackages = it
                        viewModel.setTargetPackages(it.joinToString(","))
                    },
                    onDismiss = { showAppPicker = false }
                )
            }
        }
    }
        HelpOverlayScrim(
            showHelp = showHelp,
            entries = listOf(
                1 to R.string.help_root_hide_menu,
                2 to R.string.help_root_hide_add_app,
                3 to R.string.help_root_hide_hide_root_switch,
                4 to R.string.help_root_hide_hide_applist_switch,
                5 to R.string.help_root_hide_delete_app,
                6 to R.string.help_root_hide_start_stop
            ),
            onDismiss = { showHelp = false }
        )
        }
    }
}

/**
 * 卡片内的开关单元格：上方标签、下方开关，像位置模拟里“摇杆”开关一样。
 */
@Composable
private fun SwitchCell(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: Int,
    showHelp: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            BadgedControl(show = showHelp, number = badge) {
                Switch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.scale(0.85f)
                )
            }
        }
    }
}
