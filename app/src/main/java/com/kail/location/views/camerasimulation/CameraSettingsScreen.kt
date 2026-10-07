package com.kail.location.views.camerasimulation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.R
import com.kail.location.viewmodels.CameraSimulationViewModel
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSettingsScreen(
    viewModel: CameraSimulationViewModel,
    onBackClick: () -> Unit
) {
    val rotationOffset by viewModel.rotationOffset.collectAsState()
    val photoFake by viewModel.photoFake.collectAsState()
    val targetPackages by viewModel.targetPackages.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val videoSound by viewModel.videoSound.collectAsState()
    val randomPlay by viewModel.randomPlay.collectAsState()
    val replaceMode by viewModel.replaceMode.collectAsState()
    val micMode by viewModel.micMode.collectAsState()
    val mediaSource by viewModel.mediaSource.collectAsState()
    val streamUrl by viewModel.streamUrl.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val hasImage by viewModel.hasImage.collectAsState()
    val hasAudio by viewModel.hasAudio.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showHelp by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onAudioSelected(it) }
    }

    Box {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.camera_sim_settings), color = Color.White) },
                navigationIcon = {
                    BadgedControl(show = showHelp, number = 1) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    HelpActionButton(showHelp = showHelp) { showHelp = true }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Media source: local / stream ----
            Text(stringResource(R.string.camera_sim_media_source), fontSize = 14.sp)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgedControl(show = showHelp, number = 2) {
                    FilterChip(
                        selected = mediaSource == "local",
                        onClick = { viewModel.setMediaSource("local") },
                        label = { Text(stringResource(R.string.camera_sim_source_local), fontSize = 12.sp) }
                    )
                }
                BadgedControl(show = showHelp, number = 3) {
                    FilterChip(
                        selected = mediaSource == "stream",
                        onClick = { viewModel.setMediaSource("stream") },
                        label = { Text(stringResource(R.string.camera_sim_source_stream), fontSize = 12.sp) }
                    )
                }
            }
            if (mediaSource == "stream") {
                BadgedControl(show = showHelp, number = 4) {
                    OutlinedTextField(
                        value = streamUrl,
                        onValueChange = { viewModel.setStreamUrl(it) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        placeholder = { Text(stringResource(R.string.camera_sim_stream_url_hint), fontSize = 12.sp) },
                        singleLine = true
                    )
                }
            }

            // ---- Replace mode: video / image ----
            if (mediaSource == "local") {
                Text(stringResource(R.string.camera_sim_replace_mode), fontSize = 14.sp)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadgedControl(show = showHelp, number = 5) {
                        FilterChip(
                            selected = replaceMode == "video",
                            onClick = { viewModel.setReplaceMode("video") },
                            label = { Text(stringResource(R.string.camera_sim_mode_video), fontSize = 12.sp) }
                        )
                    }
                    BadgedControl(show = showHelp, number = 6) {
                        FilterChip(
                            selected = replaceMode == "image",
                            onClick = { viewModel.setReplaceMode("image") },
                            label = { Text(stringResource(R.string.camera_sim_mode_image), fontSize = 12.sp) }
                        )
                    }
                }
                if (replaceMode == "image") {
                    BadgedControl(show = showHelp, number = 7) {
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (hasImage) stringResource(R.string.camera_sim_image_repick)
                                else stringResource(R.string.camera_sim_image_pick),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ---- Rotation offset ----
            Text(stringResource(R.string.camera_sim_rotation), fontSize = 14.sp)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 90, 180, 270).forEachIndexed { index, deg ->
                    BadgedControl(show = showHelp, number = 8 + index) {
                        FilterChip(
                            selected = rotationOffset == deg,
                            onClick = { viewModel.setRotationOffset(deg) },
                            label = { Text("${deg}°", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // ---- Toggles ----
            BadgedControl(show = showHelp, number = 12) {
                SettingsToggleRow(stringResource(R.string.camera_sim_photo_fake), photoFake) {
                    viewModel.setPhotoFake(it)
                }
            }
            BadgedControl(show = showHelp, number = 13) {
                SettingsToggleRow(stringResource(R.string.camera_sim_video_sound), videoSound) {
                    viewModel.setVideoSound(it)
                }
            }
            if (mediaSource == "local" && replaceMode == "video") {
                BadgedControl(show = showHelp, number = 14) {
                    SettingsToggleRow(stringResource(R.string.camera_sim_random_play), randomPlay) {
                        viewModel.setRandomPlay(it)
                    }
                }
            }

            // ---- Mic mode ----
            Text(stringResource(R.string.camera_sim_mic_mode), fontSize = 14.sp)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("off", "mute", "replace", "video_sync").forEachIndexed { index, mode ->
                    BadgedControl(show = showHelp, number = 15 + index) {
                        FilterChip(
                            selected = micMode == mode,
                            onClick = { viewModel.setMicMode(mode) },
                            label = {
                                Text(
                                    when (mode) {
                                        "off" -> stringResource(R.string.camera_sim_mic_off)
                                        "mute" -> stringResource(R.string.camera_sim_mic_mute)
                                        "replace" -> stringResource(R.string.camera_sim_mic_replace)
                                        else -> stringResource(R.string.camera_sim_mic_sync)
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
            if (micMode == "replace") {
                BadgedControl(show = showHelp, number = 19) {
                    OutlinedButton(
                        onClick = { audioPicker.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (hasAudio) stringResource(R.string.camera_sim_audio_repick)
                            else stringResource(R.string.camera_sim_audio_pick),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ---- Control surfaces ----
            BadgedControl(show = showHelp, number = 20) {
                SettingsToggleRow(stringResource(R.string.camera_sim_notification_toggle), notificationEnabled) {
                    viewModel.setNotificationEnabled(it)
                }
            }
            BadgedControl(show = showHelp, number = 21) {
                SettingsToggleRow(stringResource(R.string.camera_sim_overlay_toggle), overlayEnabled) {
                    viewModel.setOverlayEnabled(it)
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ---- Target app picker ----
            Text(
                stringResource(R.string.camera_sim_targets, targetPackages.size),
                fontSize = 14.sp
            )
            BadgedControl(show = showHelp, number = 22) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    placeholder = { Text(stringResource(R.string.camera_sim_search_apps), fontSize = 13.sp) },
                    singleLine = true
                )
            }
            val filtered = remember(installedApps, searchQuery) {
                if (searchQuery.isBlank()) installedApps
                else installedApps.filter {
                    it.label.contains(searchQuery, true) || it.packageName.contains(searchQuery, true)
                }
            }
            LazyColumn(Modifier.fillMaxWidth().height(240.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    BadgedControl(show = showHelp, number = 23) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleTarget(app.packageName) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.packageName in targetPackages,
                                onCheckedChange = { viewModel.toggleTarget(app.packageName) }
                            )
                            Column {
                                Text(app.label, fontSize = 14.sp)
                                Text(app.packageName, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
        HelpOverlayScrim(
            showHelp = showHelp,
            entries = listOf(
                1 to R.string.help_camera_settings_back,
                2 to R.string.help_camera_settings_source_local,
                3 to R.string.help_camera_settings_source_stream,
                4 to R.string.help_camera_settings_stream_url,
                5 to R.string.help_camera_settings_mode_video,
                6 to R.string.help_camera_settings_mode_image,
                7 to R.string.help_camera_settings_pick_image,
                8 to R.string.help_camera_settings_rotation_0,
                9 to R.string.help_camera_settings_rotation_90,
                10 to R.string.help_camera_settings_rotation_180,
                11 to R.string.help_camera_settings_rotation_270,
                12 to R.string.help_camera_settings_photo_fake,
                13 to R.string.help_camera_settings_video_sound,
                14 to R.string.help_camera_settings_random_play,
                15 to R.string.help_camera_settings_mic_off,
                16 to R.string.help_camera_settings_mic_mute,
                17 to R.string.help_camera_settings_mic_replace,
                18 to R.string.help_camera_settings_mic_sync,
                19 to R.string.help_camera_settings_pick_audio,
                20 to R.string.help_camera_settings_notification,
                21 to R.string.help_camera_settings_overlay,
                22 to R.string.help_camera_settings_search_apps,
                23 to R.string.help_camera_settings_target_apps
            ),
            onDismiss = { showHelp = false }
        )
    }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, fontSize = 14.sp)
    }
}
