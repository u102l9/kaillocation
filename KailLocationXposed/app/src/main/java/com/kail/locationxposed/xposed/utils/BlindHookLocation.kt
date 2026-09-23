package com.kail.locationxposed.xposed.utils

import android.location.Location
import de.robv.android.xposed.XposedBridge
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog

object BlindHookLocation: BaseLocationHook() {
    operator fun invoke(clazz: Class<*>, classLoader: ClassLoader): Int {
        return BlindHook(clazz, classLoader) { method, location: Location? ->
            if (location == null || !FakeLoc.enable) return@BlindHook location

            val newLoc = injectLocation(location)

            if (FakeLoc.enableDebugLog) {
                KailLog.d(null, "Kail_Xposed", "${method.name} injected: $newLoc")
            }

            newLoc
        }
    }
}