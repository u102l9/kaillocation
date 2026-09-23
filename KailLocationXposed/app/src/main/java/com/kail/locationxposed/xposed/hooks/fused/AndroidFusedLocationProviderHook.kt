package com.kail.locationxposed.xposed.hooks.fused

import android.location.Location
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.BlindHookLocation
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.hookMethodAfter
import com.kail.locationxposed.xposed.utils.toClass

object AndroidFusedLocationProviderHook: BaseLocationHook() {
    operator fun invoke(classLoader: ClassLoader) {
        val cFusedLocationProvider = "com.android.location.fused.FusedLocationProvider".toClass(classLoader)
        if (cFusedLocationProvider == null) {
            KailLog.w(null, "Kail_Xposed", "Failed to find FusedLocationProvider")
            return
        }

        if(!initDivineService("AndroidFusedLocationProvider")) {
            KailLog.e(null, "Kail_Xposed", "Failed to init DivineService in AndroidFusedLocationProvider")
            return
        }

        cFusedLocationProvider.hookMethodAfter("chooseBestLocation", Location::class.java, Location::class.java) {
            if (result == null) return@hookMethodAfter

            if (FakeLoc.enable) {
                result = injectLocation(result as Location)
            }
        }

//        cFusedLocationProvider.hookMethodBefore("reportBestLocationLocked") {
//
//        }

        val cChildLocationListener = "com.android.location.fused.FusedLocationProvider\$ChildLocationListener".toClass(classLoader)
        if (cChildLocationListener == null) {
            KailLog.w(null, "Kail_Xposed", "Failed to find ChildLocationListener")
            return
        }

        BlindHookLocation(cChildLocationListener, classLoader)
    }
}