package com.kail.location.utils.service

/**
 * ServiceGoRoot 与 ServiceGoDeveloper 共享的 Intent extra 与 action 常量。
 */
object ServiceConstants {
    const val DEFAULT_LAT = 36.667662
    const val DEFAULT_LNG = 117.027707
    const val DEFAULT_ALT = 55.0
    const val DEFAULT_BEA = 0.0f

    const val EXTRA_ROUTE_POINTS = "EXTRA_ROUTE_POINTS"
    const val EXTRA_ROUTE_WAIT_TIMES = "EXTRA_ROUTE_WAIT_TIMES"
    const val EXTRA_ROUTE_LOOP = "EXTRA_ROUTE_LOOP"
    const val EXTRA_JOYSTICK_ENABLED = "EXTRA_JOYSTICK_ENABLED"
    const val EXTRA_ROUTE_SPEED = "EXTRA_ROUTE_SPEED"
    const val EXTRA_COORD_TYPE = "EXTRA_COORD_TYPE"
    const val EXTRA_CONTROL_ACTION = "EXTRA_CONTROL_ACTION"
    const val EXTRA_SPEED_FLUCTUATION = "EXTRA_SPEED_FLUCTUATION"
    const val EXTRA_NATURAL_JITTER = "EXTRA_NATURAL_JITTER"
    const val EXTRA_SEEK_RATIO = "EXTRA_SEEK_RATIO"
    const val EXTRA_ROUTE_APPEND_POINTS = "EXTRA_ROUTE_APPEND_POINTS"
    const val EXTRA_ROUTE_APPEND_WAIT_TIMES = "EXTRA_ROUTE_APPEND_WAIT_TIMES"

    const val CONTROL_PAUSE = "pause"
    const val CONTROL_RESUME = "resume"
    const val CONTROL_STOP = "stop"
    const val CONTROL_SEEK = "seek"
    const val CONTROL_SET_SPEED = "set_speed"
    const val CONTROL_SET_SPEED_FLUCTUATION = "set_speed_fluctuation"
    const val CONTROL_APPEND_ROUTE = "append_route"

    const val COORD_WGS84 = "WGS84"
    const val COORD_BD09 = "BD09"
    const val COORD_GCJ02 = "GCJ02"

    const val ACTION_STATUS_CHANGED = "com.kail.location.service.STATUS_CHANGED"
    const val EXTRA_IS_SIMULATING = "is_simulating"
    const val EXTRA_IS_PAUSED = "is_paused"

    // --- 实时随机状态广播：把当前正在模拟的速度 / 步频回传给 UI ---
    const val ACTION_LIVE_STATE_CHANGED = "com.kail.location.service.LIVE_STATE_CHANGED"
    const val EXTRA_LIVE_SPEED_KMH = "live_speed_kmh"
    const val EXTRA_LIVE_CADENCE_SPM = "live_cadence_spm"
    const val EXTRA_LIVE_SPEED_RANDOM = "live_speed_random"
    const val EXTRA_LIVE_CADENCE_RANDOM = "live_cadence_random"
}
