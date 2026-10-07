package com.kail.location.views.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.models.HistoryRecord
import com.kail.location.viewmodels.HistoryViewModel
import com.kail.location.R
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim

/**
 * 历史记录屏幕组合项。
 * 显示历史记录列表，支持搜索和删除功能。
 *
 * @param viewModel 持有历史数据和逻辑的 ViewModel。
 * @param onBackClick 返回按钮点击回调。
 * @param onLocationSelect 历史记录选中回调。
 *                         参数为经度、纬度和高度（字符串格式）。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onLocationSelect: (String, String, String) -> Unit
) {
    val historyRecords by viewModel.historyRecords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<HistoryRecord?>(null) }
    var itemToEdit by remember { mutableStateOf<HistoryRecord?>(null) }
    var showHelp by remember { mutableStateOf(false) }

    Box {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    BadgedControl(show = showHelp, number = 1) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    HelpActionButton(showHelp = showHelp) { showHelp = true }
                    BadgedControl(show = showHelp, number = 2) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text(stringResource(R.string.history_search_hint)) }, // Assuming string exists or use generic
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        BadgedControl(show = showHelp, number = 3) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                },
                singleLine = true
            )

            if (historyRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.history_no_record),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyRecords, key = { it.id }) { record ->
                        BadgedControl(show = showHelp, number = 4) {
                            HistoryItem(
                                record = record,
                                onClick = {
                                    val (lon, lat) = viewModel.getFinalCoordinates(record)
                                    onLocationSelect(record.name, lon, lat)
                                },
                                onDeleteClick = { itemToDelete = record },
                                onEditClick = { itemToEdit = record }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
        HelpOverlayScrim(
            showHelp = showHelp,
            entries = listOf(
                1 to R.string.help_history_back,
                2 to R.string.help_history_delete_all,
                3 to R.string.help_history_clear_search,
                4 to R.string.help_history_item
            ),
            onDismiss = { showHelp = false }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(stringResource(R.string.common_warning)) },
            text = { Text(stringResource(R.string.common_delete_all_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(-1)
                        showDeleteAllDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.common_warning)) },
            text = { Text(stringResource(R.string.common_delete_item_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteRecord(it.id) }
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (itemToEdit != null) {
        var newName by remember { mutableStateOf(itemToEdit!!.name) }
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text(stringResource(R.string.nfc_sim_rename_name)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToEdit?.let { viewModel.updateRecordName(it.id, newName) }
                        itemToEdit = null
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

/**
 * Composable function to display a single history item.
 *
 * @param record The history record.
 * @param onClick Callback when the item is clicked.
 * @param onDeleteClick Callback when the delete option is selected.
 * @param onEditClick Callback when the edit option is selected.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    record: HistoryRecord,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = record.displayTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "WGS84: ${record.displayWgs84}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "BD09: ${record.displayBd09}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.common_edit)) },
                onClick = {
                    showMenu = false
                    onEditClick()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.common_delete)) },
                onClick = {
                    showMenu = false
                    onDeleteClick()
                }
            )
        }
    }
}
