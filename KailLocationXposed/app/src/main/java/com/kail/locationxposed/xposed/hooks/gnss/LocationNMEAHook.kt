package com.kail.locationxposed.xposed.hooks.gnss

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.onceHookAllMethod
import com.kail.locationxposed.xposed.utils.onceHookBefore
import com.kail.locationxposed.xposed.utils.onceHookMethodBefore
import java.util.Collections

object LocationNMEAHook: BaseLocationHook() {
    operator fun invoke(classILocationManager: Class<*>) {
        hookGnssNmea(classILocationManager)

        val doNothingMethod = object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null || param.args.isEmpty()) return

                if (FakeLoc.enableDebugLog) {
                    KailLog.d(null, "Kail_Xposed", "doNothingMethod: ${param.method.name}")
                }

                if (FakeLoc.enable && !FakeLoc.enableNMEA) {
                    if (FakeLoc.enableDebugLog) {
                        KailLog.d(null, "Kail_Xposed", "${param.method.name}: disable")
                    }
                    param.result = null
                }
            }
        }

        XposedBridge.hookAllMethods(classILocationManager, "addGnssMeasurementsListener", doNothingMethod)
        XposedBridge.hookAllMethods(classILocationManager, "removeGnssMeasurementsListener", doNothingMethod)
        XposedBridge.hookAllMethods(classILocationManager, "addGnssNavigationMessageListener", doNothingMethod)
        XposedBridge.hookAllMethods(classILocationManager, "removeGnssNavigationMessageListener", doNothingMethod)
        XposedBridge.hookAllMethods(classILocationManager, "addGnssAntennaInfoListener", doNothingMethod)
        XposedBridge.hookAllMethods(classILocationManager, "removeGnssAntennaInfoListener", doNothingMethod)
    }

    private fun hookGnssNmea(classILocationManager: Class<*>) {
        val hookedGnssCallback = Collections.synchronizedSet(HashSet<String>())
        val unhooks = classILocationManager.declaredMethods.filter {
            it.name == "registerGnssNmeaCallback" && it.parameterTypes.size > 1
        }.map { method ->
            method.onceHookBefore {
                val cIGnssNmeaCallback = (args[0] ?: return@onceHookBefore).javaClass

                if (hookedGnssCallback.contains(cIGnssNmeaCallback.name)) return@onceHookBefore
                hookedGnssCallback.add(cIGnssNmeaCallback.name)

                if (FakeLoc.enableDebugLog) {
                    KailLog.d(null, "Kail_Xposed", "registerGnssNmeaCallback: $cIGnssNmeaCallback")
                }

                cIGnssNmeaCallback.onceHookMethodBefore("onNmeaReceived", Long::class.java, String::class.java) {
                    if (FakeLoc.enableNMEA && !FakeLoc.enableAGPS) {
                        result = null // disable
                        return@onceHookMethodBefore
                    }

                    val nmea = args[1] as? String ?: return@onceHookMethodBefore
                    args[1] = injectNMEA(nmea) ?: nmea
                }
            }
        }

        if (FakeLoc.enableDebugLog) {
            KailLog.d(null, "Kail_Xposed", "found ${unhooks.size} registerGnssNmeaCallback")
        }
    }
}