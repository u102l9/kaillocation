package com.kail.location.views.joystick

import android.content.Context
import android.content.SharedPreferences
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.font.FontWeight
import androidx.preference.PreferenceManager
import com.kail.location.R
import com.kail.location.viewmodels.JoystickViewModel
import com.kail.location.viewmodels.SettingsViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Composable function for the joystick overlay UI.
 * Provides controls for movement, speed selection, and opening other windows.
 *
 * @param onMoveInfo Callback for joystick movement updates.
 *                   Parameters: auto (Boolean), angle (Double), r (Double).
 * @param onSpeedChange Callback when the speed mode changes.
 * @param onWindowDrag Callback when the window is dragged.
 * @param onOpenMap Callback to open the map window.
 * @param onOpenHistory Callback to open the history window.
 * @param onClose Callback to close the joystick (unused in current UI).
 */
@Composable
fun JoyStickOverlay(
    viewModel: JoystickViewModel,
    onMoveInfo: (Boolean, Double, Double) -> Unit, // auto, angle, r
    onWindowDrag: (Float, Float) -> Unit,
    onMinimize: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    
    // Settings
    val joystickType = remember { prefs.getString(SettingsViewModel.KEY_JOYSTICK_TYPE, "0") ?: "0" }
    val speed by viewModel.speed.collectAsState()
    var showSpeedSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color(0xCC000000), RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onWindowDrag(dragAmount.x, dragAmount.y)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Speed Settings, History, Map
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    CircleIconButton(
                        iconRes = R.drawable.ic_menu_settings,
                        contentDescription = "Speed Settings",
                        onClick = { showSpeedSettings = !showSpeedSettings }
                    )
                    CircleIconButton(
                        iconRes = R.drawable.ic_history,
                        contentDescription = "History",
                        onClick = { viewModel.setWindowType(JoystickViewModel.WindowType.HISTORY) }
                    )
                    CircleIconButton(
                        iconRes = R.drawable.ic_map,
                        contentDescription = "Map",
                        onClick = { viewModel.setWindowType(JoystickViewModel.WindowType.MAP) }
                    )
                }

                // Right Side: Rocker or Buttons
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (joystickType == "0") {
                        Rocker(
                            modifier = Modifier.size(100.dp),
                            onUpdate = onMoveInfo
                        )
                    } else {
                        DirectionalButtons(
                            onUpdate = onMoveInfo
                        )
                    }
                }
            }

            // Minimize button (top-right corner of the overlay)
            CircleIconButton(
                iconRes = R.drawable.ic_minimize,
                contentDescription = "Minimize",
                onClick = onMinimize,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-1).dp, y = (-9).dp)
            )
        }

        if (showSpeedSettings) {
            Spacer(modifier = Modifier.height(8.dp))
            SpeedSettingsPanel(
                speed = speed,
                onSpeedChange = { newSpeed ->
                    viewModel.setSpeed(newSpeed)
                    // Persist to SharedPreferences so ViewModel can listen or just update via VM
                    prefs.edit().putString(SettingsViewModel.KEY_JOYSTICK_SPEED, newSpeed.toString()).apply()
                },
                onClose = { showSpeedSettings = false }
            )
        }
    }
}

/**
 * Minimized joystick ball shown after tapping the minimize button.
 * Draggable; tap to restore the full joystick overlay.
 *
 * @param onOpen Callback to restore the full overlay.
 * @param onWindowDrag Callback when the ball is dragged.
 */
@Composable
fun MinimizedJoystickBall(
    onOpen: () -> Unit,
    onWindowDrag: (Float, Float) -> Unit
) {
    Row(
        modifier = Modifier
            .size(52.dp)
            .background(Color(0xCC000000), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onWindowDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(onClick = onOpen),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_move),
            contentDescription = "Open Joystick",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun SpeedSettingsPanel(
    speed: Double, // in m/s
    onSpeedChange: (Double) -> Unit,
    onClose: () -> Unit
) {
    // Convert m/s to km/h for the UI slider
    val speedKmh = (speed * 3.6).toFloat()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC000000)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(240.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.joystick_speed, String.format("%.1f", speedKmh)),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = speedKmh.coerceIn(0f, 300f),
                onValueChange = { newValueKmh ->
                    // Convert km/h back to m/s for internal logic
                    val newValueMs = newValueKmh.toDouble() / 3.6
                    onSpeedChange(newValueMs)
                },
                valueRange = 0f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                Text("300", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            }
        }
    }
}

/**
 * Composable function for the rocker (joystick) control.
 *
 * @param modifier Modifier for the layout.
 * @param onUpdate Callback for joystick updates.
 *                 Parameters: isAuto (Boolean), angle (Double), r (Double).
 */
@Composable
fun Rocker(
    modifier: Modifier = Modifier,
    onUpdate: (Boolean, Double, Double) -> Unit
) {
    // Constants
    val outerCircleColor = Color.Gray.copy(alpha = 0.7f)
    val innerCircleColor = Color.LightGray.copy(alpha = 0.7f)
    
    var isAuto by remember { mutableStateOf(false) }
    var innerCenter by remember { mutableStateOf(Offset.Zero) }
    var viewCenter by remember { mutableStateOf(Offset.Zero) }
    var outerRadius by remember { mutableStateOf(0f) }
    var innerRadius by remember { mutableStateOf(0f) }
    
    val lockCloseIcon = painterResource(R.drawable.ic_lock_close)
    val lockOpenIcon = painterResource(R.drawable.ic_lock_open)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val position = change.position
                        
                        when (event.type) {
                            androidx.compose.ui.input.pointer.PointerEventType.Press -> {
                                // Check if inside inner circle (current position)
                                val dist = (position - innerCenter).getDistance()
                                if (dist <= innerRadius * 2) { // Allow some margin or check against innerRadius
                                    // Start dragging
                                }
                            }
                            androidx.compose.ui.input.pointer.PointerEventType.Move -> {
                                change.consume()
                                // Calculate new inner center
                                val dist = (position - viewCenter).getDistance()
                                val maxDist = outerRadius - innerRadius
                                
                                if (dist < maxDist) {
                                    innerCenter = position
                                } else {
                                    // Constrain to outer circle
                                    val ratio = maxDist / dist
                                    innerCenter = viewCenter + (position - viewCenter) * ratio
                                }
                                
                                // Calculate angle and r
                                val dx = innerCenter.x - viewCenter.x
                                val dy = innerCenter.y - viewCenter.y
                                val angle = Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())) - 90
                                val r = sqrt(dx.pow(2) + dy.pow(2)) / maxDist
                                
                                onUpdate(true, angle, r.toDouble())
                            }
                            androidx.compose.ui.input.pointer.PointerEventType.Release -> {
                                // Check for click (toggle auto)
                                // Simplified click detection: if little movement?
                                // Actually, RockerView toggles auto on UP if isClick is true.
                                // Here we can just check if we want to toggle.
                                // For now, let's just reset if not auto.
                                
                                if (!isAuto) {
                                    innerCenter = viewCenter
                                    onUpdate(false, 0.0, 0.0) // Stop
                                }
                                
                                // Toggle auto logic: RockerView does it on UP if it was a "click" (no move)
                                // Let's implement a simple click listener on the inner circle via Box?
                                // No, pointerInput consumes everything.
                            }
                        }
                    }
                }
            }
            .clickable { 
                // Toggle auto
                isAuto = !isAuto
                // Reset bitmap logic handled by drawing
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            viewCenter = center
            outerRadius = size.minDimension / 2
            innerRadius = outerRadius * 0.4f // Approx ratio
            
            if (innerCenter == Offset.Zero) {
                innerCenter = viewCenter
            }

            // Draw Outer Circle
            drawCircle(
                color = outerCircleColor,
                radius = outerRadius,
                center = viewCenter
            )

            // Draw Inner Circle
            drawCircle(
                color = innerCircleColor,
                radius = innerRadius,
                center = innerCenter
            )
            
            // Draw Icon
            // This is a bit tricky with Canvas, easier to use Image composable overlaid
        }
        
        // Overlay Icon
        val density = LocalDensity.current
        Image(
            painter = if (isAuto) lockCloseIcon else lockOpenIcon,
            contentDescription = null,
            modifier = Modifier
                .offset { 
                    IntOffset(
                        (innerCenter.x - innerRadius).toInt(),
                        (innerCenter.y - innerRadius).toInt()
                    )
                }
                .size(with(density) { (innerRadius * 2).toDp() })
                .alpha(0.8f)
        )
    }
}
/**
 * Simplified Rocker implementation (alternative).
 *
 * @param modifier Modifier for the layout.
 * @param onUpdate Callback for joystick updates.
 */
@Composable
fun RockerSimple(
    modifier: Modifier = Modifier,
    onUpdate: (Boolean, Double, Double) -> Unit
) {
    var isAuto by remember { mutableStateOf(false) }
    var innerOffset by remember { mutableStateOf(Offset.Zero) } // Offset from center
    
    val outerRadius = 70.dp
    val innerRadius = 30.dp
    val maxDist = with(LocalDensity.current) { (outerRadius - innerRadius).toPx() }
    
    Box(
        modifier = modifier
            .size(outerRadius * 2)
            .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!isAuto) {
                            innerOffset = Offset.Zero
                            onUpdate(false, 0.0, 0.0)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = innerOffset + Offset(dragAmount.x, dragAmount.y)
                        val dist = newOffset.getDistance()
                        
                        innerOffset = if (dist > maxDist) {
                            newOffset * (maxDist / dist)
                        } else {
                            newOffset
                        }
                        
                        // Calculate angle/r
                        // Note: Android coordinates: Y down.
                        // RockerView: atan2(x, y) - 90.
                        // x is horizontal, y is vertical.
                        val angle = Math.toDegrees(atan2(innerOffset.x.toDouble(), innerOffset.y.toDouble())) - 90
                        val r = dist / maxDist
                        onUpdate(true, angle, r.toDouble())
                    }
                )
            }
    ) {
        // Inner Circle (Joystick Handle)
        Box(
            modifier = Modifier
                .size(innerRadius * 2)
                .offset { androidx.compose.ui.unit.IntOffset(innerOffset.x.toInt(), innerOffset.y.toInt()) }
                .align(Alignment.Center)
                .background(Color.LightGray.copy(alpha = 0.8f), CircleShape)
                .clickable { isAuto = !isAuto },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(if (isAuto) R.drawable.ic_lock_close else R.drawable.ic_lock_open),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


/**
 * Composable function for directional buttons control.
 *
 * @param onUpdate Callback for direction updates.
 */
@Composable
fun DirectionalButtons(
    onUpdate: (Boolean, Double, Double) -> Unit
) {
    // State to track locking (Center button state)
    var isLocked by remember { mutableStateOf(true) }
    // State to track currently active direction (only when locked)
    var activeDirection by remember { mutableStateOf<Double?>(null) }

    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.secondary
    val defaultColor = Color.White

    // Helper to handle direction clicks
    fun onDirectionClick(angle: Double) {
        if (isLocked) {
            if (activeDirection == angle) {
                // Stop current direction
                activeDirection = null
                onUpdate(false, angle, 0.0)
            } else {
                // Start new direction
                activeDirection = angle
                onUpdate(true, angle, 1.0)
            }
        } else {
            // Manual mode: single step
            onUpdate(false, angle, 1.0)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: NW, N, NE
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DirectionButton(
                iconRes = R.drawable.ic_left_up,
                isActive = isLocked && activeDirection == 135.0,
                onClick = { onDirectionClick(135.0) }
            )
            DirectionButton(
                iconRes = R.drawable.ic_up,
                isActive = isLocked && activeDirection == 90.0,
                onClick = { onDirectionClick(90.0) }
            )
            DirectionButton(
                iconRes = R.drawable.ic_right_up,
                isActive = isLocked && activeDirection == 45.0,
                onClick = { onDirectionClick(45.0) }
            )
        }
        // Row 2: W, Center, E
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DirectionButton(
                iconRes = R.drawable.ic_left,
                isActive = isLocked && activeDirection == 180.0,
                onClick = { onDirectionClick(180.0) }
            )
            // Center Button
            IconButton(
                onClick = {
                    if (isLocked) {
                        // Unlock
                        isLocked = false
                        activeDirection = null
                        onUpdate(false, 0.0, 0.0) // Stop everything
                    } else {
                        // Lock
                        isLocked = true
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open),
                    contentDescription = "Lock/Unlock",
                    tint = if (isLocked) accentColor else defaultColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            DirectionButton(
                iconRes = R.drawable.ic_right,
                isActive = isLocked && activeDirection == 0.0,
                onClick = { onDirectionClick(0.0) }
            )
        }
        // Row 3: SW, S, SE
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DirectionButton(
                iconRes = R.drawable.ic_left_down,
                isActive = isLocked && activeDirection == 225.0,
                onClick = { onDirectionClick(225.0) }
            )
            DirectionButton(
                iconRes = R.drawable.ic_down,
                isActive = isLocked && activeDirection == 270.0,
                onClick = { onDirectionClick(270.0) }
            )
            DirectionButton(
                iconRes = R.drawable.ic_right_down,
                isActive = isLocked && activeDirection == 315.0,
                onClick = { onDirectionClick(315.0) }
            )
        }
    }
}

/**
 * Composable for a single direction button.
 *
 * @param iconRes Icon resource ID.
 * @param isActive Whether the button is active.
 * @param onClick Callback when clicked.
 */
@Composable
fun DirectionButton(
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isActive) MaterialTheme.colorScheme.secondary else Color.White
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Composable for a circular icon button used in the joystick menu.
 *
 * @param iconRes Icon resource ID.
 * @param contentDescription Content description.
 * @param onClick Callback when clicked.
 * @param modifier Modifier.
 * @param isSelected Whether the button is selected.
 */
@Composable
fun CircleIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.1f)
    val tint = Color.White

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
