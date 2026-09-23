package com.kail.location.views.joystick

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.baidu.mapapi.map.MapView
import com.kail.location.R

@Composable
fun FloatingNavigationControlOverlay(
    mapView: MapView?,
    isPaused: Boolean,
    speed: Double,
    progress: Float, // 0.0 to 1.0
    totalDistance: String, // e.g. "7/560" or just "560m"
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSpeedChange: (Double) -> Unit,
    onWindowDrag: (Float, Float) -> Unit,
    onClose: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(speed) }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onWindowDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        // Close Button (Top Right) - placed first to be on top
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .padding(4.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Main Control Row
        Row(
            modifier = Modifier
                .background(Color(0xCC000000), RoundedCornerShape(16.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Control Buttons Column
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Pause/Resume
                IconButton(
                    onClick = onPauseResume,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    if (isPaused) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow_black_24dp),
                            contentDescription = "Resume",
                            tint = Color.White
                        )
                    } else {
                        // Custom Pause Icon
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val w = size.width
                            val h = size.height
                            val barW = w * 0.25f
                            drawRect(color = Color.White, topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(barW, h * 0.7f))
                            drawRect(color = Color.White, topLeft = Offset(w * 0.60f, h * 0.15f), size = Size(barW, h * 0.7f))
                        }
                    }
                }

                // Stop
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_stop_black_24dp),
                        contentDescription = "Stop",
                        tint = Color.White
                    )
                }

                // Settings
                IconButton(
                    onClick = { showSettings = !showSettings },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Settings, // Or sliders icon
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            // Circular Map View
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            ) {
                if (mapView != null) {
                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Settings Panel (Sub-window)
        if (showSettings) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xCC000000)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Title & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.joystick_route_progress, totalDistance),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Close, // Or minimize icon
                            contentDescription = "Minimize",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showSettings = false }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.joystick_progress), color = Color.White, fontSize = 12.sp, modifier = Modifier.width(40.dp))
                        Slider(
                            value = progress,
                            onValueChange = { onSeek(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Yellow,
                                activeTrackColor = Color.Yellow,
                                inactiveTrackColor = Color.Gray
                            )
                        )
                    }

                    // Speed Control
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.joystick_speed_label), color = Color.White, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentSpeed = (currentSpeed - 5).coerceAtLeast(0.0); onSpeedChange(currentSpeed) }) {
                                Icon(painterResource(R.drawable.ic_left), contentDescription = "Decrease", tint = Color.White)
                            }
                            Text(
                                text = "${currentSpeed.toInt()}km/h",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { currentSpeed += 5; onSpeedChange(currentSpeed) }) {
                                Icon(painterResource(R.drawable.ic_right), contentDescription = "Increase", tint = Color.White)
                            }
                        }
                        Button(
                            onClick = { onSpeedChange(currentSpeed) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(stringResource(R.string.joystick_modify), color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
