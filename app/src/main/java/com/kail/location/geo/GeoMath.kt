package com.kail.location.geo

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object GeoMath {
    fun metersPerDegLat(lat: Double): Double = 110.574 * 1000.0
    fun metersPerDegLng(lat: Double): Double = 111.320 * 1000.0 * cos(abs(lat) * Math.PI / 180.0)
    fun deltaLngKm(disLngKm: Double, atLat: Double): Double = disLngKm / (111.320 * kotlin.math.cos(kotlin.math.abs(atLat) * Math.PI / 180.0))
    fun deltaLatKm(disLatKm: Double): Double = disLatKm / 110.574
    fun bearingDegrees(lng1: Double, lat1: Double, lng2: Double, lat2: Double): Float {
        val dLng = Math.toRadians(lng2 - lng1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val y = sin(dLng) * kotlin.math.cos(rLat2)
        val x = kotlin.math.cos(rLat1) * kotlin.math.sin(rLat2) - kotlin.math.sin(rLat1) * kotlin.math.cos(rLat2) * kotlin.math.cos(dLng)
        val brng = Math.atan2(y, x)
        return Math.toDegrees(brng).toFloat()
    }
}
