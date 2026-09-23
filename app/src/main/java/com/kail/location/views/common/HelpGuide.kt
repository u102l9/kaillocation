package com.kail.location.views.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kail.location.R
import kotlin.math.roundToInt

/**
 * 帮助模式下给控件叠加数字角标：帮助开启时显示数字，关闭时不显示。
 */
@Composable
fun BadgedControl(
    show: Boolean,
    number: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()
        if (show) {
            NumBadge(
                number = number,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
            )
        }
    }
}

/**
 * 底部按钮说明图例：按编号逐条列出每个按钮的作用。
 * 顶部的拖拽条可以拖动整个面板，方便查看被遮挡的按钮。点击面板任意处关闭。
 */
@Composable
fun HelpLegend(
    entries: List<Pair<Int, Int>>,
    onDismiss: () -> Unit,
    screenSize: IntSize = IntSize.Zero,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    Column(
        modifier = modifier
            .offset {
                IntOffset(
                    dragOffset.x.roundToInt().coerceIn(-screenSize.width / 2, screenSize.width / 2),
                    dragOffset.y.roundToInt().coerceIn(-screenSize.height / 2, screenSize.height / 2)
                )
            }
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 300.dp)
            .background(Color(0xF2202020), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                }
                .padding(bottom = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.7f))
            )
        }
        Text(
            text = stringResource(R.string.help_legend_title),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        entries.forEach { (number, res) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumBadge(number = number)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(res),
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.help_close_hint),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

/**
 * 圆形数字角标，用于标注按钮。
 */
@Composable
private fun NumBadge(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF6D3F)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 顶栏“?”帮助按钮。点击后由页面配合 [HelpOverlayScrim] 展示说明。
 */
@Composable
fun HelpActionButton(
    showHelp: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val helpDesc = stringResource(R.string.loc_sim_help)
    IconButton(
        onClick = { onToggle() },
        modifier = modifier.semantics { contentDescription = helpDesc }
    ) {
        Text(
            text = "?",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 帮助遮罩 + 底部可拖动图例。放在页面最外层 Box 的末尾以覆盖整页。
 * 遮罩可点击关闭；图例面板可拖动、可滚动、点击面板任意处关闭。
 */
@Composable
fun HelpOverlayScrim(
    showHelp: Boolean,
    entries: List<Pair<Int, Int>>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showHelp) return
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    Box(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { screenSize = it }
                .background(Color(0x66000000))
                .pointerInput(Unit) { detectTapGestures { onDismiss() } }
                .zIndex(10f)
        )
        HelpLegend(
            entries = entries,
            onDismiss = onDismiss,
            screenSize = screenSize,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(20f)
        )
    }
}
