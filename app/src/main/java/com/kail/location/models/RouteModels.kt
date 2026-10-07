package com.kail.location.models

/**
 * 路线信息的数据类。
 *
 * @property id 路线唯一标识。
 * @property startName 起点名称。
 * @property endName 终点名称。
 * @property distance 距离描述（例如 "104米"）。
 */
data class RouteInfo(
    val id: String,
    val startName: String,
    val endName: String,
    val distance: String = "104m",
    val isFavorite: Boolean = false,
    val favoriteTime: Long = 0L,
    val favoriteOrder: Int = 0
)

/**
 * 路线模拟设置的数据类。
 *
 * @property speed 模拟速度基准值（km/h）；未开启随机区间时直接作为上报速度。
 * @property mode 交通方式（如步行、骑行）。
 * @property speedFluctuation 是否启用速度随机区间。
 * @property speedMin 速度随机区间下限（km/h）。
 * @property speedMax 速度随机区间上限（km/h）。
 * @property stepFreqSimulation 是否模拟步频。
 * @property stepCadenceSpm 步频基准值（步/分钟）。
 * @property stepCadenceFluctuation 是否启用步频随机区间。
 * @property stepCadenceMinSpm 步频随机区间下限（步/分钟）。
 * @property stepCadenceMaxSpm 步频随机区间上限（步/分钟）。
 * @property randomIntervalSec 随机取值的变化周期（秒）：每隔这么久重新抽一次值。
 * @property detourEnabled 是否启用防检测绕行（随机偏离主路线再回来）。
 * @property detourDistMinM / detourDistMaxM 绕行横向距离范围（米）。
 * @property detourHoldMinSec / detourHoldMaxSec 单次绕行保持时长范围（秒）。
 * @property detourGapMinSec / detourGapMaxSec 两次绕行之间的间隔范围（秒）。
 * @property isLoop 是否循环模拟。
 */
data class SimulationSettings(
    var speed: Float = 6.5f,
    var mode: TransportMode = TransportMode.Bike,
    var speedFluctuation: Boolean = true,
    var speedMin: Float = 5.0f,
    var speedMax: Float = 8.0f,
    var stepFreqSimulation: Boolean = false,
    var stepCadenceSpm: Float = 120f,
    var stepCadenceFluctuation: Boolean = false,
    var stepCadenceMinSpm: Float = 110f,
    var stepCadenceMaxSpm: Float = 140f,
    var randomIntervalSec: Float = 15f,
    var detourEnabled: Boolean = false,
    var detourDistMinM: Float = 30f,
    var detourDistMaxM: Float = 80f,
    var detourHoldMinSec: Float = 15f,
    var detourHoldMaxSec: Float = 45f,
    var detourGapMinSec: Float = 60f,
    var detourGapMaxSec: Float = 180f,
    var isLoop: Boolean = true
)

/**
 * 模拟使用的交通方式枚举。
 */
enum class TransportMode {
    Walk, Run, Bike, Car, Plane
}
