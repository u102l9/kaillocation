package com.kail.location.utils.service

import android.os.SystemClock
import com.baidu.mapapi.model.LatLng
import com.kail.location.geo.GeoMath
import com.kail.location.utils.KailLog
import com.kail.location.utils.MapUtils

/**
 * 负责路线点管理、沿路线前进、距离计算与进度汇报。
 *
 * 另含「防检测绕行」：沿主路线跑时，每隔一段随机时间随机朝左侧或右侧
 * 斜着跑出去一段距离、保持平行跑一会、再斜着跑回主路线，避免轨迹是一个
 * 完美的闭合圈。
 */
class RouteEngine {

    private companion object {
        const val TAG = "RouteEngine"
    }

    private val routePoints: MutableList<Pair<Double, Double>> = mutableListOf()
    private val routeCumulativeDistances: MutableList<Double> = mutableListOf()
    /**
     * 每个途经点的等待时长（秒），与 routePoints 下标一一对应；0 表示不停留。
     */
    private val routeWaitTimes: MutableList<Double> = mutableListOf()
    private var totalDistance: Double = 0.0
    private var routeIndex = 0
    private var routeLoop = false
    private var segmentProgressMeters = 0.0
    /**
     * True once a non-looping route has reached its end. The engine then parks
     * at the final point (currentLat/Lng stay at the destination) instead of
     * clearing — so location keeps reporting the last simulated spot rather
     * than snapping back to the route's origin.
     */
    private var routeFinished = false

    /**
     * 是否正在某个途经点原地等待（等待期间位置保持在该点）。
     */
    var isWaiting: Boolean = false
        private set
    private var waitRemainingMs: Long = 0L
    private var waitStartedAtMs: Long = 0L

    // ------------------------------------------------------------------
    // 防检测绕行（detour）
    //
    // 一次绕行分三段：
    //   OUT  以 devAngle 偏角斜着跑，直到横向偏移达到目标偏离距离
    //   HOLD 偏角归零，平行于主路线跑一段（保持偏离）
    //   BACK 以 -devAngle 偏角斜着跑，直到横向偏移回到 0（精确汇回主线）
    //
    // 关键：每 tick 的位移都拆成「沿主线 d·cos(θ)」+「侧向 d·sin(θ)」，
    // 合成位移恒等于 d，所以绕行不会让配速读数变快，只有方向在变 ——
    // 这正是真人绕开障碍物的样子。
    // ------------------------------------------------------------------
    private var detourEnabled: Boolean = false
    private var detourDistMinM: Float = 30f
    private var detourDistMaxM: Float = 80f
    private var detourHoldMinSec: Float = 15f
    private var detourHoldMaxSec: Float = 45f
    private var detourGapMinSec: Float = 60f
    private var detourGapMaxSec: Float = 180f

    private const val PHASE_NONE = 0
    private const val PHASE_OUT = 1
    private const val PHASE_HOLD = 2
    private const val PHASE_BACK = 3

    /** 已完成圈数；0 表示还在第一圈（第一圈不绕行）。 */
    private var lapCount: Int = 0
    private var detourPhase: Int = PHASE_NONE
    private var nextDetourAtMs: Long = 0L
    /** 本次绕行的目标偏离距离（米）。 */
    private var detourTargetM: Double = 0.0
    /** 本次绕行 HOLD 段的时长（毫秒）。 */
    private var detourHoldMs: Long = 0L
    private var detourHoldStartMs: Long = 0L
    /** 本次绕行的偏向：+1 右 / -1 左。 */
    private var detourSideSign: Double = 1.0
    /** 本次绕行的偏角（弧度，恒为正；实际方向乘 sideSign）。 */
    private var detourAngleRad: Double = 0.0
    /** 当前离主路线的垂直距离（米），带符号：正 = 沿行进方向的右侧。 */
    private var detourOffsetM: Double = 0.0

    /** 是否正处于绕行中（OUT / HOLD / BACK 任意一段）。 */
    val isDetouring: Boolean get() = detourPhase != PHASE_NONE
    /** 当前绕行方向：1 右 / -1 左 / 0 未绕行。 */
    val detourSide: Int
        get() = if (detourPhase == PHASE_NONE) 0 else if (detourSideSign >= 0) 1 else -1
    /** 当前偏离主路线的距离（米，绝对值）。 */
    val detourOffsetMeters: Double get() = kotlin.math.abs(detourOffsetM)

    /** 主路线上的真实位置（不含绕行偏移）。 */
    private var mainLng: Double = 0.0
    private var mainLat: Double = 0.0
    private var mainBea: Float = 0.0f

    var currentLng: Double = 0.0
    var currentLat: Double = 0.0
    var currentBea: Float = 0.0f

    val isActive: Boolean get() = routePoints.size >= 2
    /** True only while the route is still progressing (not yet at its end). */
    val isProgressing: Boolean get() = routePoints.size >= 2 && !routeFinished
    val progressRatio: Float
        get() {
            if (routeFinished) return 1f
            if (totalDistance <= 0) return 0f
            val currentDist = if (routeIndex < routeCumulativeDistances.size)
                routeCumulativeDistances[routeIndex] + segmentProgressMeters
            else totalDistance
            return (currentDist / totalDistance).toFloat().coerceIn(0f, 1f)
        }

    fun setupFromArray(routeArray: DoubleArray, coordType: String, waitTimes: DoubleArray? = null) {
        routePoints.clear()
        routeCumulativeDistances.clear()
        routeWaitTimes.clear()
        var i = 0
        while (i + 1 < routeArray.size) {
            val lng = routeArray[i]
            val lat = routeArray[i + 1]
            when (coordType) {
                ServiceConstants.COORD_WGS84 -> routePoints.add(Pair(lng, lat))
                ServiceConstants.COORD_GCJ02 -> {
                    val wgs = MapUtils.gcj02towgs84(lng, lat)
                    routePoints.add(Pair(wgs[0], wgs[1]))
                }
                else -> {
                    val wgs = MapUtils.bd2wgs(lng, lat)
                    routePoints.add(Pair(wgs[0], wgs[1]))
                }
            }
            routeWaitTimes.add(waitTimes?.getOrNull(routePoints.size - 1) ?: 0.0)
            i += 2
        }
        routeIndex = 0
        segmentProgressMeters = 0.0
        routeFinished = false
        isWaiting = false
        waitRemainingMs = 0L
        waitStartedAtMs = 0L
        lapCount = 0
        resetDetourState()
        calculateRouteDistances()
        if (routePoints.isNotEmpty()) {
            val first = routePoints.first()
            mainLng = first.first
            mainLat = first.second
            mainBea = if (routePoints.size >= 2) {
                val second = routePoints[1]
                GeoMath.bearingDegrees(first.first, first.second, second.first, second.second)
            } else {
                0.0f
            }
        }
        syncCurrent()
        KailLog.i(null, TAG, "setupFromArray: ${routePoints.size} points, coordType=$coordType, totalDistance=${"%.1f".format(totalDistance)}m")
    }

    fun setLoop(loop: Boolean) {
        routeLoop = loop
    }

    // ------------------------------------------------------------------
    // 防检测绕行：配置
    // ------------------------------------------------------------------

    /**
     * 配置防检测绕行。
     *
     * @param enabled 是否启用；关闭时立即结束进行中的绕行
     * @param distMinM / distMaxM 偏离主路线的横向距离范围（米），每次绕行在区间内随机
     * @param holdMinSec / holdMaxSec 偏离后平行保持的时长范围（秒），可设 0 表示斜出去立刻斜回来
     * @param gapMinSec / gapMaxSec 两次绕行之间的间隔范围（秒），每次在区间内随机
     */
    fun setDetourConfig(
        enabled: Boolean,
        distMinM: Float,
        distMaxM: Float,
        holdMinSec: Float,
        holdMaxSec: Float,
        gapMinSec: Float,
        gapMaxSec: Float
    ) {
        detourEnabled = enabled
        detourDistMinM = distMinM.coerceAtLeast(1f)
        detourDistMaxM = distMaxM.coerceAtLeast(detourDistMinM)
        detourHoldMinSec = holdMinSec.coerceAtLeast(0f)
        detourHoldMaxSec = holdMaxSec.coerceAtLeast(detourHoldMinSec)
        detourGapMinSec = gapMinSec.coerceAtLeast(1f)
        detourGapMaxSec = gapMaxSec.coerceAtLeast(detourGapMinSec)
        if (!enabled) resetDetourState()
        KailLog.i(
            null, TAG,
            "detour config: enabled=$enabled dist=${detourDistMinM}..${detourDistMaxM}m " +
                "hold=${detourHoldMinSec}..${detourHoldMaxSec}s gap=${detourGapMinSec}..${detourGapMaxSec}s"
        )
    }

    private fun resetDetourState() {
        detourPhase = PHASE_NONE
        detourOffsetM = 0.0
        detourTargetM = 0.0
        detourHoldMs = 0L
        detourHoldStartMs = 0L
        detourSideSign = 1.0
        detourAngleRad = 0.0
        nextDetourAtMs = 0L
    }

    private fun randomRange(min: Float, max: Float): Float {
        return if (max <= min) min else min + kotlin.random.Random.nextFloat() * (max - min)
    }

    /**
     * 推进绕行状态机，返回本 tick 应使用的偏角（弧度；0 表示平行于主路线）。
     * 调用方需把位移 d 拆成 主线 d·cos(θ) + 侧向 d·sin(θ)。
     */
    private fun updateDetour(nowMs: Long): Double {
        if (!detourEnabled) return 0.0

        when (detourPhase) {
            PHASE_NONE -> {
                // 第一圈不绕行
                if (lapCount < 1) return 0.0
                if (nextDetourAtMs == 0L) {
                    nextDetourAtMs = nowMs + (randomRange(detourGapMinSec, detourGapMaxSec) * 1000f).toLong()
                    return 0.0
                }
                if (nowMs < nextDetourAtMs) return 0.0
                detourTargetM = randomRange(detourDistMinM, detourDistMaxM).toDouble()
                detourHoldMs = (randomRange(detourHoldMinSec, detourHoldMaxSec) * 1000f).toLong()
                detourSideSign = if (kotlin.random.Random.nextBoolean()) 1.0 else -1.0
                // 偏角随机 32~52 度，避免每次绕行的形状完全一样
                detourAngleRad = Math.toRadians(randomRange(32f, 52f).toDouble())
                detourPhase = PHASE_OUT
                KailLog.i(
                    null, TAG,
                    "detour START: side=${if (detourSideSign >= 0) "right" else "left"} " +
                        "dist=${"%.1f".format(detourTargetM)}m hold=${detourHoldMs}ms " +
                        "angle=${"%.1f".format(Math.toDegrees(detourAngleRad))}deg lap=$lapCount"
                )
                return detourAngleRad * detourSideSign
            }

            PHASE_OUT -> {
                if (kotlin.math.abs(detourOffsetM) >= detourTargetM) {
                    detourPhase = PHASE_HOLD
                    detourHoldStartMs = nowMs
                    KailLog.i(null, TAG, "detour HOLD: offset=${"%.1f".format(detourOffsetM)}m for ${detourHoldMs}ms")
                    return 0.0
                }
                return detourAngleRad * detourSideSign
            }

            PHASE_HOLD -> {
                if (nowMs - detourHoldStartMs >= detourHoldMs) {
                    detourPhase = PHASE_BACK
                    return -detourAngleRad * detourSideSign
                }
                return 0.0
            }

            PHASE_BACK -> {
                if (detourOffsetM * detourSideSign <= 0.0) {
                    KailLog.i(null, TAG, "detour END: back on route")
                    detourOffsetM = 0.0
                    detourPhase = PHASE_NONE
                    nextDetourAtMs = nowMs + (randomRange(detourGapMinSec, detourGapMaxSec) * 1000f).toLong()
                    return 0.0
                }
                return -detourAngleRad * detourSideSign
            }
        }
        return 0.0
    }

    /** 把主路线上位置加上侧向偏移，得到对外暴露的 current*。 */
    private fun syncCurrent() {
        val off = detourOffsetM
        if (off == 0.0) {
            currentLng = mainLng
            currentLat = mainLat
            currentBea = mainBea
            return
        }
        val compass = mainBea + (if (off >= 0) 90.0 else -90.0)
        val math = Math.toRadians(90.0 - compass)
        val dist = kotlin.math.abs(off)
        val dEastKm = dist * kotlin.math.cos(math) / 1000.0
        val dNorthKm = dist * kotlin.math.sin(math) / 1000.0
        currentLng = mainLng + GeoMath.deltaLngKm(dEastKm, mainLat)
        currentLat = mainLat + GeoMath.deltaLatKm(dNorthKm)
        // currentBea 由调用方按实际位移重算（斜跑时朝向应带偏转）
    }

    private fun onLapCompleted() {
        lapCount++
        KailLog.d(null, TAG, "lap completed: lapCount=$lapCount (detour allowed from lap 2)")
    }

    fun clear() {
        routePoints.clear()
        routeCumulativeDistances.clear()
        routeWaitTimes.clear()
        totalDistance = 0.0
        routeIndex = 0
        segmentProgressMeters = 0.0
        routeFinished = false
        isWaiting = false
        waitRemainingMs = 0L
        waitStartedAtMs = 0L
        lapCount = 0
        resetDetourState()
    }

    /**
     * Append extra points to the end of the route while a simulation is
     * running. This lets the user extend a route after (or before) it reaches
     * its original end: the engine keeps whatever progress it already made,
     * and once it runs out of the original segments it continues straight into
     * the newly appended ones. If the route had already finished and parked at
     * the destination, progression is re-activated.
     *
     * @param newPoints additional points (WGS84) to append after the current last point
     * @param newWaitTimes per-point wait seconds (seconds) parallel to newPoints; missing = 0
     */
    fun appendPoints(newPoints: List<Pair<Double, Double>>, newWaitTimes: DoubleArray? = null) {
        if (newPoints.isEmpty()) return
        val wasFinished = routeFinished
        routePoints.addAll(newPoints)
        newPoints.indices.forEach { idx ->
            routeWaitTimes.add(newWaitTimes?.getOrNull(idx) ?: 0.0)
        }
        calculateRouteDistances()
        routeFinished = false
        if (wasFinished) segmentProgressMeters = 0.0
        if (routeIndex >= routePoints.size - 1) {
            routeIndex = (routePoints.size - newPoints.size - 1).coerceAtLeast(0)
        }
        val idx = routeIndex.coerceIn(0, (routePoints.size - 1).coerceAtLeast(0))
        if (idx + 1 < routePoints.size) {
            val a = routePoints[idx]
            val b = routePoints[idx + 1]
            mainBea = GeoMath.bearingDegrees(a.first, a.second, b.first, b.second)
        }
        syncCurrent()
        KailLog.i(null, TAG, "appendPoints: +${newPoints.size} points, total=${routePoints.size}, totalDistance=${"%.1f".format(totalDistance)}m, finished=$wasFinished")
    }

    fun seekToRatio(ratio: Float) {
        if (routePoints.size < 2 || routeCumulativeDistances.isEmpty()) return
        // Seeking back into the route re-activates progression.
        routeFinished = false
        isWaiting = false
        waitRemainingMs = 0L
        waitStartedAtMs = 0L
        val targetDist = totalDistance * ratio.coerceIn(0f, 1f)
        var idx = 0
        for (i in 0 until routeCumulativeDistances.size - 1) {
            if (targetDist >= routeCumulativeDistances[i] && targetDist < routeCumulativeDistances[i + 1]) {
                idx = i
                break
            }
        }
        if (targetDist >= totalDistance) {
            idx = routePoints.size - 2
        }
        routeIndex = idx
        segmentProgressMeters = targetDist - routeCumulativeDistances[idx]

        val a = routePoints[routeIndex]
        val b = routePoints[(routeIndex + 1).coerceAtMost(routePoints.size - 1)]
        val segLen = segmentLengthMeters(a, b)
        val f = if (segLen > 0) (segmentProgressMeters / segLen) else 0.0
        val dLngDeg = b.first - a.first
        val dLatDeg = b.second - a.second
        mainLng = a.first + dLngDeg * f
        mainLat = a.second + dLatDeg * f
        mainBea = GeoMath.bearingDegrees(a.first, a.second, b.first, b.second)
        // seek 是瞬移，继续绕行没有意义，直接结束
        detourPhase = PHASE_NONE
        detourOffsetM = 0.0
        nextDetourAtMs = 0L
        syncCurrent()
        KailLog.d(null, TAG, "seekToRatio: ratio=${"%.3f".format(ratio)} -> index=$routeIndex lat=$currentLat lng=$currentLng")
    }

    fun advance(distanceMeters: Double) {
        // Once a non-looping route has finished, stay parked at the final point
        // (currentLat/Lng already hold the destination). Do not advance or
        // reset — the location must remain at the last simulated spot.
        if (routeFinished) return

        val nowMs = SystemClock.elapsedRealtime()

        // While waiting at a waypoint, hold the position and only count down
        // the wall-clock wait timer. Once elapsed, resume moving this tick.
        if (isWaiting) {
            val elapsed = nowMs - waitStartedAtMs
            if (elapsed >= waitRemainingMs) {
                isWaiting = false
                waitRemainingMs = 0L
                waitStartedAtMs = 0L
                KailLog.i(null, TAG, "advance: wait finished at waypoint #$routeIndex, resuming")
            } else {
                waitRemainingMs -= elapsed
                waitStartedAtMs = nowMs
                return
            }
        }

        // 防检测绕行：把本 tick 位移拆成「沿主线 d·cos(θ)」+「侧向 d·sin(θ)」，
        // 合成位移仍等于 d，配速读数不受影响。
        val theta = updateDetour(nowMs)
        val prevLat = currentLat
        val prevLng = currentLng
        val mainStep = distanceMeters * kotlin.math.cos(theta)
        if (theta != 0.0) {
            detourOffsetM += distanceMeters * kotlin.math.sin(theta)
        }

        var remaining = mainStep
        while (remaining > 0 && routePoints.size >= 2) {
            val startIdx = routeIndex
            val endIdx = if (startIdx + 1 < routePoints.size) startIdx + 1 else -1
            if (endIdx == -1) {
                if (routeLoop) {
                    routeIndex = 0
                    segmentProgressMeters = 0.0
                    onLapCompleted()
                    continue
                } else {
                    parkAtDestination()
                    break
                }
            }
            val a = routePoints[startIdx]
            val b = routePoints[endIdx]
            val segLen = segmentLengthMeters(a, b)
            if (segLen <= 0.0) {
                routeIndex++
                segmentProgressMeters = 0.0
                if (maybeEnterWait(routeIndex)) return
                if (routeIndex >= routePoints.size - 1) {
                    if (routeLoop) {
                        routeIndex = 0
                        onLapCompleted()
                    } else {
                        parkAtDestination()
                        break
                    }
                }
                continue
            }
            val available = segLen - segmentProgressMeters
            if (remaining >= available) {
                mainLng = b.first
                mainLat = b.second
                mainBea = GeoMath.bearingDegrees(a.first, a.second, b.first, b.second)
                remaining -= available
                routeIndex++
                segmentProgressMeters = 0.0
                // Arrived at waypoint routeIndex; hold there if it has a wait time.
                if (maybeEnterWait(routeIndex)) return
                if (routeIndex >= routePoints.size - 1) {
                    if (routeLoop) {
                        routeIndex = 0
                        onLapCompleted()
                    } else {
                        parkAtDestination()
                        break
                    }
                }
            } else {
                segmentProgressMeters += remaining
                val f = segmentProgressMeters / segLen
                val dLngDeg = b.first - a.first
                val dLatDeg = b.second - a.second
                mainLng = a.first + dLngDeg * f
                mainLat = a.second + dLatDeg * f
                mainBea = GeoMath.bearingDegrees(a.first, a.second, b.first, b.second)
                remaining = 0.0
            }
        }

        syncCurrent()
        // 斜跑阶段（OUT / BACK）朝向由实际位移决定，这样上报的 bearing 也带偏转
        if (theta != 0.0) {
            val movedLat = currentLat - prevLat
            val movedLng = currentLng - prevLng
            if (kotlin.math.abs(movedLat) > 1e-9 || kotlin.math.abs(movedLng) > 1e-9) {
                val b = GeoMath.bearingDegrees(prevLng, prevLat, currentLng, currentLat)
                if (!b.isNaN()) currentBea = b
            }
        }
    }

    /**
     * 到达某个途经点后若配置了等待时长，则原地等待（位置保持在该点）。
     *
     * @return true 表示已进入等待，调用方应停止本次推进
     */
    private fun maybeEnterWait(index: Int): Boolean {
        val waitSeconds = waitSecondsAt(index)
        if (waitSeconds <= 0.0) return false
        isWaiting = true
        waitRemainingMs = (waitSeconds * 1000).toLong().coerceAtLeast(1)
        waitStartedAtMs = SystemClock.elapsedRealtime()
        KailLog.i(null, TAG, "advance: waiting ${waitRemainingMs}ms at waypoint #$index lat=$currentLat lng=$currentLng")
        return true
    }

    private fun waitSecondsAt(index: Int): Double {
        return routeWaitTimes.getOrNull(index) ?: 0.0
    }

    /**
     * Park the engine at the route's final point. Keeps routePoints intact (so
     * isActive stays true and the service keeps reporting this fixed spot) and
     * pins current* to the destination so playback ends at the last simulated
     * location instead of snapping back to the origin.
     */
    private fun parkAtDestination() {
        routeFinished = true
        segmentProgressMeters = 0.0
        detourPhase = PHASE_NONE
        detourOffsetM = 0.0
        if (routePoints.isNotEmpty()) {
            val last = routePoints.last()
            mainLng = last.first
            mainLat = last.second
            routeIndex = (routePoints.size - 1).coerceAtLeast(0)
        }
        syncCurrent()
        KailLog.i(null, TAG, "advance: route finished (no loop), parked at destination lat=$currentLat lng=$currentLng")
    }

    fun buildStatusString(): Pair<String, LatLng>? {
        if (routePoints.isEmpty()) return null
        val currentDist = if (routeFinished) totalDistance
            else if (routeIndex < routeCumulativeDistances.size)
                routeCumulativeDistances[routeIndex] + segmentProgressMeters
            else totalDistance

        val distStr = if (currentDist > 1000) String.format("%.2fkm", currentDist / 1000) else String.format("%.0fm", currentDist)
        val totalDistStr = if (totalDistance > 1000) String.format("%.2fkm", totalDistance / 1000) else String.format("%.0fm", totalDistance)
        val bd = MapUtils.wgs2bd(currentLng, currentLat)
        return "$distStr / $totalDistStr" to LatLng(bd[1], bd[0])
    }

    private fun calculateRouteDistances() {
        routeCumulativeDistances.clear()
        routeCumulativeDistances.add(0.0)
        var total = 0.0
        for (i in 0 until routePoints.size - 1) {
            val a = routePoints[i]
            val b = routePoints[i + 1]
            total += segmentLengthMeters(a, b)
            routeCumulativeDistances.add(total)
        }
        totalDistance = total
    }

    private fun segmentLengthMeters(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
        val midLat = (a.second + b.second) / 2.0
        val dLatDeg = b.second - a.second
        val dLngDeg = b.first - a.first
        val metersPerDegLat = GeoMath.metersPerDegLat(midLat)
        val metersPerDegLng = GeoMath.metersPerDegLng(midLat)
        return kotlin.math.sqrt(
            (dLatDeg * metersPerDegLat) * (dLatDeg * metersPerDegLat) +
            (dLngDeg * metersPerDegLng) * (dLngDeg * metersPerDegLng)
        )
    }
}
