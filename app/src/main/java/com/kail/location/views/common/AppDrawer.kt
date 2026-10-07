package com.kail.location.views.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    currentScreen: String,
    onNavigate: (Int) -> Unit,
    appVersion: String,
    runMode: String,
    @Suppress("UNUSED_PARAMETER") onRunModeChange: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onDeveloperModeSelected: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onXposedSettingsSelected: () -> Unit = {},
    scope: kotlinx.coroutines.CoroutineScope = rememberCoroutineScope()
) {
    var showEnvDialog by remember { mutableStateOf(false) }
    var showLoginActivity by remember { mutableStateOf(false) }
    var showProfileActivity by remember { mutableStateOf(false) }
    var envMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (showLoginActivity) {
        val intent = Intent(context, com.kail.location.views.auth.LoginActivity::class.java)
        context.startActivity(intent)
        showLoginActivity = false
    }

    if (showProfileActivity) {
        val intent = Intent(context, com.kail.location.views.auth.ProfileActivity::class.java)
        context.startActivity(intent)
        showProfileActivity = false
    }





    suspend fun closeDrawerSmooth() {
        drawerState.animateTo(
            DrawerValue.Closed,
            androidx.compose.animation.core.tween(durationMillis = 140)
        )
    }


    if (showEnvDialog) {
        AlertDialog(
            onDismissRequest = { showEnvDialog = false },
            title = { Text(stringResource(R.string.drawer_env_check)) },
            text = { Text(envMessage) },
            confirmButton = {
                TextButton(onClick = { showEnvDialog = false }) {
                    Text(stringResource(R.string.drawer_ok))
                }
            }
        )
    }




    ModalDrawerSheet {
        LazyColumn {
            item { DrawerHeader(appVersion, onLoginClick = { showLoginActivity = true }, onProfileClick = { showProfileActivity = true }) }
            item { HorizontalDivider() }

            // ===== Group: 模拟 =====
            item {
                Text(
                    text = stringResource(R.string.nav_menu_sim_group),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_location_simulation)) },
                    icon = { Icon(painterResource(R.drawable.ic_position), contentDescription = null) },
                    selected = currentScreen == "LocationSimulation",
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_location_simulation) } }
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_route_simulation)) },
                    icon = { Icon(painterResource(R.drawable.ic_move), contentDescription = null) },
                    selected = currentScreen == "RouteSimulation",
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_route_simulation) } }
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.drawer_nav_sim)) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    selected = currentScreen == "NavigationSimulation",
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_navigation_simulation) } }
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.drawer_nfc_sim)) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = currentScreen == "NfcSimulation",
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_nfc_simulation) } }
                )
            }
            if (runMode == "root") {
                item {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_menu_independent_sim)) },
                        icon = { Icon(painterResource(R.drawable.ic_position), contentDescription = null) },
                        selected = currentScreen == "IndependentSimulation",
                        onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_independent_simulation) } }
                    )
                }
                item {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_menu_root_app_hide)) },
                        icon = { Icon(painterResource(R.drawable.ic_menu_settings), contentDescription = null) },
                        selected = currentScreen == "RootAppHide",
                        onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_root_app_hide) } }
                    )
                }
                item {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_menu_wifi_sim)) },
                        icon = { Icon(painterResource(R.drawable.ic_menu_settings), contentDescription = null) },
                        selected = currentScreen == "WifiSimulation",
                        onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_wifi_simulation) } }
                    )
                }
                item {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_menu_cell_sim)) },
                        icon = { Icon(painterResource(R.drawable.ic_menu_dev), contentDescription = null) },
                        selected = currentScreen == "CellSimulation",
                        onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_cell_simulation) } }
                    )
                }
                item {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.nav_menu_camera_sim)) },
                        icon = { Icon(painterResource(R.drawable.ic_menu_settings), contentDescription = null) },
                        selected = currentScreen == "CameraSimulation",
                        onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_camera_simulation) } }
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ===== Group: 设置 =====
            item {
                Text(
                    text = stringResource(R.string.nav_menu_settings),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_settings)) },
                    icon = { Icon(painterResource(R.drawable.ic_menu_settings), contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_settings) } }
                )
            }
            // 「运行模式」入口已移除：本版本仅保留 ROOT 模式

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ===== Group: 更多 =====
            item {
                Text(
                    text = stringResource(R.string.nav_menu_more),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_contact)) },
                    icon = { Icon(painterResource(R.drawable.ic_contact), contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_contact) } }
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_sponsor)) },
                    icon = { Icon(painterResource(R.drawable.ic_user), contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_sponsor) } }
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_github)) },
                    icon = { Icon(painterResource(R.drawable.ic_menu_dev), contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_source_code) } }
                )
            }
            item {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_menu_faq)) },
                    icon = { Icon(painterResource(R.drawable.ic_menu_feedback), contentDescription = null) },
                    selected = false,
                    onClick = { scope.launch { closeDrawerSmooth(); onNavigate(R.id.nav_faq) } }
                )
            }
        }
    }
}
