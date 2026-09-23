package com.kail.location.views.wifisimulation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.R
import com.kail.location.models.WifiInfo
import com.kail.location.viewmodels.WifiSimulationViewModel
import com.kail.location.views.base.BaseActivity
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim
import com.kail.location.views.theme.locationTheme

class WifiPickerActivity : BaseActivity() {
    private val viewModel: WifiSimulationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            locationTheme {
                WifiPickerScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiPickerScreen(
    viewModel: WifiSimulationViewModel,
    onBackClick: () -> Unit
) {
    val nearbyResults by viewModel.nearbyResults.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    var showManualDialog by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    Box {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wifi_sim_picker_title)) },
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
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            BadgedControl(show = showHelp, number = 2) {
                FloatingActionButton(
                    onClick = { showManualDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.wifi_sim_manual_add))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scan Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                BadgedControl(show = showHelp, number = 3, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.scanNearbyWifi() },
                        enabled = !isScanning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isScanning) stringResource(R.string.wifi_sim_scanning) else stringResource(R.string.wifi_sim_scan_nearby)
                        )
                    }
                }
            }

            HorizontalDivider()

            if (nearbyResults.isEmpty() && !isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wifi),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.wifi_sim_scan_prompt),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.wifi_sim_scan_prompt2),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(nearbyResults) { index, result ->
                        NearbyWifiItem(
                            ssid = result.SSID.ifBlank { "Hidden Network" },
                            bssid = result.BSSID,
                            rssi = result.level,
                            frequency = result.frequency,
                            capabilities = result.capabilities,
                            onAdd = {
                                viewModel.addFromScanResult(result)
                                onBackClick()
                            },
                            showHelp = showHelp && index == 0,
                            badgeNumber = 4
                        )
                    }
                }
            }
        }
    }
        HelpOverlayScrim(
            showHelp = showHelp,
            entries = listOf(
                1 to R.string.help_wifi_picker_back,
                2 to R.string.help_wifi_picker_manual_add,
                3 to R.string.help_wifi_picker_scan,
                4 to R.string.help_wifi_picker_item_add
            ),
            onDismiss = { showHelp = false }
        )
        }

    if (showManualDialog) {
        ManualWifiDialog(
            onDismiss = { showManualDialog = false },
            onSave = { info ->
                viewModel.addWifi(info)
                showManualDialog = false
                onBackClick()
            }
        )
    }
}

@Composable
fun NearbyWifiItem(
    ssid: String,
    bssid: String,
    rssi: Int,
    frequency: Int,
    capabilities: String,
    onAdd: () -> Unit,
    showHelp: Boolean = false,
    badgeNumber: Int = 0
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_wifi),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = ssid,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$bssid · ${rssi}dBm · ${frequency}MHz",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (capabilities.isNotBlank()) {
                        Text(
                            text = capabilities,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1
                        )
                    }
                }
            }

            BadgedControl(show = showHelp, number = badgeNumber) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.wifi_sim_add_label), fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualWifiDialog(
    onDismiss: () -> Unit,
    onSave: (WifiInfo) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ssid by remember { mutableStateOf("") }
    var bssid by remember { mutableStateOf("") }
    var rssi by remember { mutableStateOf("-50") }
    var frequency by remember { mutableStateOf("2412") }
    var capabilities by remember { mutableStateOf("[WPA-PSK-CCMP]") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wifi_sim_manual_add)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.wifi_sim_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text(stringResource(R.string.wifi_sim_ssid)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = bssid,
                    onValueChange = { bssid = it },
                    label = { Text(stringResource(R.string.wifi_sim_bssid)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rssi,
                    onValueChange = { rssi = it },
                    label = { Text(stringResource(R.string.wifi_sim_rssi)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text(stringResource(R.string.wifi_sim_frequency)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = capabilities,
                    onValueChange = { capabilities = it },
                    label = { Text(stringResource(R.string.wifi_sim_capabilities)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val info = WifiInfo(
                        name = name.ifBlank { ssid },
                        ssid = ssid,
                        bssid = bssid,
                        rssi = rssi.toIntOrNull() ?: -50,
                        frequency = frequency.toIntOrNull() ?: 2412,
                        capabilities = capabilities
                    )
                    onSave(info)
                },
                enabled = ssid.isNotBlank() && bssid.isNotBlank()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
