package com.kail.location.geo

import kotlin.math.cos
import kotlin.math.sin

object GeoPredict {
    fun randomInRangeWithMean(min: Double, max: Double, mean: Double): Double {
        require(mean in min..max) { "平均值必须在 min 和 max 之间" }
        return if (kotlin.random.Random.nextBoolean()) {
            kotlin.random.Random.nextDouble(min, mean)
        } else {
            kotlin.random.Random.nextDouble(mean, max)
        }
    }

    fun nextByDisplacementKm(lng: Double, lat: Double, dLngKm: Double, dLatKm: Double): Pair<Double, Double> {
        val deltaLng = GeoMath.deltaLngKm(dLngKm, lat)
        val deltaLat = GeoMath.deltaLatKm(dLatKm)
        return Pair(lng + deltaLng, lat + deltaLat)
    }

    fun nextBySpeedMps(lng: Double, lat: Double, speedMps: Double, angleDeg: Double, dtMillis: Long, intensity: Double = 1.0): Pair<Double, Double> {
        val seconds = dtMillis / 1000.0
        val distanceKm = (speedMps * seconds * intensity) / 1000.0
        val dxKm = distanceKm * cos(angleDeg * 2.0 * Math.PI / 360.0)
        val dyKm = distanceKm * sin(angleDeg * 2.0 * Math.PI / 360.0)
        return nextByDisplacementKm(lng, lat, dxKm, dyKm)
    }
}
