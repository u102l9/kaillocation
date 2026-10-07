package com.kail.location.views.navigationsimulation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.kail.location.R
import com.kail.location.utils.KailLog
import com.kail.location.utils.MapUtils
import com.kail.location.viewmodels.NavigationSimulationViewModel
import com.kail.location.views.common.BadgedControl
import com.kail.location.views.common.HelpActionButton
import com.kail.location.views.common.HelpOverlayScrim
import com.kail.location.views.routesimulation.WaypointWaitDialog
import com.kail.location.views.routesimulation.buildWaitBadgeBitmap
import android.graphics.Color as AndroidColor

/**
 * 导航模拟的规划页：
 * - 中心准星常驻，点一次落起点、再点一次落终点；
 * - 已规划（蓝色路线线出现）后，再点则把等待点吸附到蓝色线上最近的点，
 *   并通过"等待"按钮为该点设置停留秒数（模拟到该处会等待）。
 *
 * @param mapView 地图视图
 * @param onBackClick 返回回调
 * @param onConfirmClick 确认回调，参数为起点、终点（BD09）与等待点（路线下标 → 秒）
 * @param initialStart 已存在的起点（BD09），为空则无预填
 * @param initialEnd 已存在的终点（BD09），为空则无预填
 * @param plannedRoutePoints 已规划好的路线途经点（BD09），用于画蓝色规划线
 * @param routeWaits 已存在的等待点（路线下标 → 秒），用于预填
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationPlanScreen(
    mapView: MapView?,
    onBackClick: () -> Unit,
    onConfirmClick: (LatLng, LatLng, Map<Int, Int>) -> Unit,
    viewModel: NavigationSimulationViewModel,
    onLocateClick: (() -> Unit)? = null,
    initialStart: LatLng? = null,
    initialEnd: LatLng? = null,
    plannedRoutePoints: List<LatLng>? = null,
    routeWaits: Map<Int, Int> = emptyMap(),
    startLocked: Boolean = false,
    endLocked: Boolean = false,
    startPinned: Boolean = false,
    endPinned: Boolean = false
) {
    val context = LocalContext.current
    val route = plannedRoutePoints
    var startPoint by remember { mutableStateOf(initialStart) }
    var endPoint by remember { mutableStateOf(initialEnd) }
    val waitPoints = remember { mutableStateListOf<LatLng>() }
    val waitSecs = remember { mutableStateListOf<Int>() }
    var startMarkerOverlay by remember { mutableStateOf<Overlay?>(null) }
    var endMarkerOverlay by remember { mutableStateOf<Overlay?>(null) }
    var plannedLineOverlay by remember { mutableStateOf<Overlay?>(null) }
    val waitMarkersOverlays = remember { mutableListOf<Overlay>() }

    var showWaitDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showHelp by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()
    val canConfirm = startPoint != null && endPoint != null

    fun tryDrop(center: LatLng): Boolean {
        when {
            !startLocked && !startPinned && startPoint == null -> {
                startPoint = center
                return true
            }
            !endLocked && !endPinned && endPoint == null -> {
                endPoint = center
                return true
            }
        }
        return false
    }

    fun nearestIndex(target: LatLng): Int {
        if (route == null || route.isEmpty()) return -1
        var best = 0
        var bestDist = Double.MAX_VALUE
        route.forEachIndexed { i, p ->
            val dlat = p.latitude - target.latitude
            val dlng = p.longitude - target.longitude
            val d = dlat * dlat + dlng * dlng
            if (d < bestDist) {
                bestDist = d
                best = i
            }
        }
        return best
    }

    fun redraw() {
        val map = mapView?.map ?: return
        startMarkerOverlay?.remove()
        startMarkerOverlay = null
        endMarkerOverlay?.remove()
        endMarkerOverlay = null
        plannedLineOverlay?.remove()
        plannedLineOverlay = null
        waitMarkersOverlays.forEach { runCatching { it.remove() } }
        waitMarkersOverlays.clear()

        // 蓝色规划线（百度 API 规划的路线）
        if (route != null && route.size >= 2) {
            plannedLineOverlay = map.addOverlay(
                PolylineOptions().width(8).color(AndroidColor.BLUE).points(route)
            )
        }
        val sp = startPoint
        if (sp != null) {
            val sd = MapUtils.bitmapDescriptorFromVector(context, R.drawable.icon_gcoding, AndroidColor.GREEN)
            if (sd != null) {
                startMarkerOverlay = map.addOverlay(MarkerOptions().position(sp).icon(sd).zIndex(8).draggable(false))
            }
        }
        val ep = endPoint
        if (ep != null) {
            val ed = MapUtils.bitmapDescriptorFromVector(context, R.drawable.icon_gcoding, AndroidColor.RED)
            if (ed != null) {
                endMarkerOverlay = map.addOverlay(MarkerOptions().position(ep).icon(ed).zIndex(8).draggable(false))
            }
        }
        // 等待点：蓝色秒数气泡（吸附在蓝色线上），标出等待秒数
        waitPoints.forEachIndexed { i, p ->
            val badge = buildWaitBadgeBitmap(context, waitSecs[i])
            waitMarkersOverlays.add(
                map.addOverlay(
                    MarkerOptions()
                        .position(p)
                        .icon(BitmapDescriptorFactory.fromBitmap(badge))
                        .anchor(0.5f, 1f)
                        .zIndex(9)
                        .draggable(false)
                )
            )
        }
    }

    // 预填已存在的等待点
    LaunchedEffect(Unit) {
        routeWaits.forEach { (idx, sec) ->
            route?.getOrNull(idx)?.let {
                waitPoints.add(it)
                waitSecs.add(sec)
            }
        }
        redraw()
    }

    LaunchedEffect(mapView) {
        try {
            val map = mapView?.map ?: return@LaunchedEffect
            map.clear()
            map.isMyLocationEnabled = true
            map.setMyLocationConfiguration(
                com.baidu.mapapi.map.MyLocationConfiguration(
                    com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL,
                    true,
                    null
                )
            )
            map.setMapStatus(MapStatusUpdateFactory.zoomTo(15f))
        } catch (e: Exception) {
            KailLog.e(context, "NavigationPlanScreen", "map init error: ${e.message}")
        }
    }

    LaunchedEffect(mapView, startPoint, endPoint, waitPoints.toList(), waitSecs.toList()) {
        redraw()
    }

    if (showWaitDialog && waitPoints.isNotEmpty()) {
        WaypointWaitDialog(
            waypointIndex = waitPoints.size,
            currentWaitSeconds = waitSecs.last(),
            onDismiss = { showWaitDialog = false },
            onConfirm = { seconds ->
                waitSecs[waitSecs.lastIndex] = seconds
                showWaitDialog = false
                redraw()
            }
        )
    }

    Box {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_sim_plan_title), color = Color.White) },
                navigationIcon = {
                    BadgedControl(show = showHelp, number = 1) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    HelpActionButton(showHelp = showHelp) { showHelp = true }
                    BadgedControl(show = showHelp, number = 2) {
                        IconButton(onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                searchQuery = ""
                                viewModel.clearSearchResults()
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (mapView != null) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }

            // Center crosshair: always visible, marks where the next point will drop
            Image(
                painter = painterResource(id = R.drawable.icon_gcoding),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp),
                colorFilter = ColorFilter.tint(
                    if (startPoint == null && endPoint == null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            )

            // Right control column
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                BadgedControl(show = showHelp, number = 3) {
                    NavPlanMapButton(R.drawable.ic_home_position) { onLocateClick?.invoke() }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BadgedControl(show = showHelp, number = 4) {
                    NavPlanMapButton(R.drawable.ic_zoom_in) { mapView?.map?.setMapStatus(MapStatusUpdateFactory.zoomIn()) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BadgedControl(show = showHelp, number = 5) {
                    NavPlanMapButton(R.drawable.ic_zoom_out) { mapView?.map?.setMapStatus(MapStatusUpdateFactory.zoomOut()) }
                }
            }

            // Bottom-right FABs
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Undo: 先撤等待点，再撤终点/起点（锁定点不可撤）
                BadgedControl(show = showHelp, number = 6) {
                    SmallFloatingActionButton(
                        onClick = {
                            when {
                                waitPoints.isNotEmpty() -> {
                                    waitPoints.removeAt(waitPoints.lastIndex)
                                    waitSecs.removeAt(waitSecs.lastIndex)
                                    redraw()
                                }
                                endPoint != null && !endLocked && !endPinned -> {
                                    endPoint = null
                                    redraw()
                                }
                                startPoint != null && !startLocked && !startPinned -> {
                                    startPoint = null
                                    redraw()
                                }
                            }
                        },
                        modifier = Modifier.alpha(if (startPoint != null || endPoint != null || waitPoints.isNotEmpty()) 1f else 0f),
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_left), contentDescription = null)
                    }
                }

                // Wait time for the latest wait point
                BadgedControl(show = showHelp, number = 7) {
                    FloatingActionButton(
                        onClick = { if (waitPoints.isNotEmpty()) showWaitDialog = true },
                        containerColor = if (showWaitDialog) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = CircleShape,
                        modifier = Modifier.alpha(if (waitPoints.isNotEmpty()) 1f else 0f)
                    ) {
                        Text(
                            text = stringResource(R.string.route_plan_wait_btn),
                            color = if (showWaitDialog) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Drop point: 1st tap = start, 2nd = end, then wait points snapped to the line
                BadgedControl(show = showHelp, number = 8) {
                    FloatingActionButton(
                        onClick = {
                            val center = mapView?.map?.mapStatus?.target ?: return@FloatingActionButton
                            when {
                                tryDrop(center) -> {
                                    redraw()
                                    KailLog.i(context, "NavigationPlanScreen", "point -> $center (start=$startPoint end=$endPoint)")
                                }
                                route != null && route.size >= 2 -> {
                                    val idx = nearestIndex(center)
                                    if (idx >= 0) {
                                        waitPoints.add(route[idx])
                                        waitSecs.add(0)
                                        redraw()
                                        KailLog.i(context, "NavigationPlanScreen", "wait point snapped to route[$idx]")
                                    }
                                }
                                (startLocked || startPinned) && (endLocked || endPinned) ->
                                    android.widget.Toast.makeText(context, R.string.nav_sim_both_locked, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_position),
                            contentDescription = "Drop Point",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Confirm
                BadgedControl(show = showHelp, number = 9) {
                    FloatingActionButton(
                        onClick = {
                            val sp = startPoint
                            val ep = endPoint
                            if (sp != null && ep != null) {
                                val waitsMap = mutableMapOf<Int, Int>()
                                waitPoints.forEachIndexed { i, p ->
                                    val idx = nearestIndex(p)
                                    if (idx >= 0) waitsMap[idx] = waitSecs[i]
                                }
                                onConfirmClick(sp, ep, waitsMap)
                            }
                        },
                        containerColor = if (canConfirm) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.alpha(if (canConfirm) 1f else 0.35f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (canConfirm) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Search panel
            if (isSearchActive) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.search(it)
                            },
                            placeholder = { Text(stringResource(R.string.route_plan_search_hint)) },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    BadgedControl(show = showHelp, number = 10) {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            viewModel.clearSearchResults()
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (searchResults.isNotEmpty()) {
                            LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                                items(searchResults.size) { index ->
                                    val item = searchResults[index]
                                    val name = item[NavigationSimulationViewModel.POI_NAME].toString()
                                    val address = item[NavigationSimulationViewModel.POI_ADDRESS].toString()
                                    BadgedControl(show = showHelp, number = 11) {
                                        ListItem(
                                            headlineContent = { Text(name) },
                                            supportingContent = { Text(address) },
                                            modifier = Modifier.clickable {
                                                val lat = item[NavigationSimulationViewModel.POI_LATITUDE] as Double
                                                val lng = item[NavigationSimulationViewModel.POI_LONGITUDE] as Double
                                                val pt = LatLng(lat, lng)
                                                if (tryDrop(pt)) {
                                                    redraw()
                                                    mapView?.map?.animateMapStatus(MapStatusUpdateFactory.newLatLng(pt))
                                                }
                                                isSearchActive = false
                                                searchQuery = ""
                                                viewModel.clearSearchResults()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        HelpOverlayScrim(
            showHelp = showHelp,
            entries = listOf(
                1 to R.string.help_nav_plan_back,
                2 to R.string.help_nav_plan_search,
                3 to R.string.help_nav_plan_locate,
                4 to R.string.help_nav_plan_zoom_in,
                5 to R.string.help_nav_plan_zoom_out,
                6 to R.string.help_nav_plan_undo,
                7 to R.string.help_nav_plan_wait,
                8 to R.string.help_nav_plan_drop,
                9 to R.string.help_nav_plan_confirm,
                10 to R.string.help_nav_plan_clear,
                11 to R.string.help_nav_plan_result
            ),
            onDismiss = { showHelp = false }
        )
    }
}

/**
 * 地图控制按钮（圆形）。
 */
@Composable
private fun NavPlanMapButton(iconRes: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 4.dp,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
