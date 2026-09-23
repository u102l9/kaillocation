package com.kail.locationxposed.xposed.hooks

import android.location.Location
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.onceHookAllMethod
import com.kail.locationxposed.xposed.utils.onceHookMethod

object LocationManagerHook: BaseLocationHook() {
    operator fun invoke(
        cLocationManager: Class<*>,
    ) {
        val hookGetLastKnownLocation = object: XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                if (param == null || param.hasThrowable() || param.result == null) return

                if (!FakeLoc.enable) return

                if (FakeLoc.enableDebugLog) {
                    KailLog.d(null, "Kail_Xposed", "${param.method.name}: injected!")
                }

                param.result = injectLocation(param.result as Location)
            }
        }
        if(cLocationManager.declaredMethods.filter {
            it.name == "getLastKnownLocation"
        }.map {
            XposedBridge.hookMethod(it, hookGetLastKnownLocation)
        }.isEmpty()) {
            XposedBridge.hookAllMethods(cLocationManager, "getLastLocation", hookGetLastKnownLocation)
        }

        val hookOnLocation = object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.args.isEmpty() || param.args[0] == null) return

                if (!FakeLoc.enable) return

                if (FakeLoc.enableDebugLog) {
                    KailLog.d(null, "Kail_Xposed", "${param.method.name}: injected!")
                }

                when( param.args[0] ) {
                    is Location -> {
                        param.args[0] = injectLocation(param.args[0] as Location)
                    }
                    is List<*> -> {
                        val locations = param.args[0] as List<*>
                        param.args[0] = locations.map { injectLocation(it as Location) }
                    }
                    else -> {
                        KailLog.e(null, "Kail_Xposed", "Unknown method when hook hookOnLocation: ${param.method}")
                    }
                }
            }
        }

        if(cLocationManager.declaredMethods.filter {
                it.name == "requestFlush"
            }.map {
                XposedBridge.hookMethod(it, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        if (param == null || param.args.size > 1 || param.args[1] == null) return

                        val listener = param.args[1]
                        listener.javaClass.onceHookAllMethod("onLocationChanged", hookOnLocation)
                    }
                })
            }.isEmpty()) {
            KailLog.e(null, "Kail_Xposed", "Hook requestFlush failed")
        }

        val hookRequestLocationUpdates = object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null || param.args.isEmpty() || param.args[1] == null) return

                param.args.filterIsInstance<android.location.LocationListener>().also {
                    if (it.isEmpty()) {
                        KailLog.e(null, "Kail_Xposed", "No LocationListener found in requestLocationUpdates: ${param.method}(${
                            param.args?.joinToString { it?.javaClass.toString() }
                        })")
                    }
                }.forEach {
                    it.javaClass.onceHookAllMethod("onLocationChanged", hookOnLocation)
                }
            }
        }
        if(cLocationManager.declaredMethods.filter {
                it.name == "requestLocationUpdates"
            }.map {
                XposedBridge.hookMethod(it, hookRequestLocationUpdates)
            }.isEmpty()) {
            KailLog.e(null, "Kail_Xposed", "Hook requestLocationUpdates failed")
        }

        if(cLocationManager.declaredMethods.filter {
                it.name == "requestSingleUpdate"
            }.map {
                XposedBridge.hookMethod(it, hookRequestLocationUpdates)
            }.isEmpty()) {
            KailLog.e(null, "Kail_Xposed", "Hook requestSingleUpdate failed")
        }

        kotlin.runCatching {
            XposedHelpers.findClass("android.location.LocationManager\$GetCurrentLocationTransport", cLocationManager.classLoader)
        }.onSuccess {
            it.onceHookAllMethod("onLocation", hookOnLocation)
        }.onFailure {
            KailLog.e(null, "Kail_Xposed", "GetCurrentLocationTransport not found: ${it.message}")
        }

        kotlin.runCatching {
            XposedHelpers.findClass("android.location.LocationManager\$BatchedLocationCallbackWrapper", cLocationManager.classLoader)
        }.onSuccess {
            it.onceHookAllMethod("onLocationChanged", hookOnLocation)
        }

        kotlin.runCatching {
            XposedHelpers.findClass("android.location.LocationManager\$LocationListenerTransport", cLocationManager.classLoader)
        }.onSuccess {
            it.onceHookAllMethod("onLocationChanged", hookOnLocation)
        }.onFailure {
            KailLog.e(null, "Kail_Xposed", "LocationListenerTransport not found: ${it.message}")
        }
    }
}