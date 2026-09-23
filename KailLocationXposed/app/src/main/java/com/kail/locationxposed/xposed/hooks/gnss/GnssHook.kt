package com.kail.locationxposed.xposed.hooks.gnss

import android.location.Location
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.BlindHookLocation
import com.kail.locationxposed.xposed.utils.BlindHookLocation.invoke
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.beforeHook
import com.kail.locationxposed.xposed.utils.hookAllMethods
import com.kail.locationxposed.xposed.utils.onceHookAllMethod
import java.util.Collections

object GnssHook: BaseLocationHook() {
    operator fun invoke(classLoader: ClassLoader) {
        hookFrameworkGnss(classLoader)
        hookHALGnss(classLoader)
    }

    private fun hookFrameworkGnss(classLoader: ClassLoader) {
        val cGnssManagerService = XposedHelpers.findClassIfExists("com.android.server.location.GnssManagerService", classLoader) ?: return

        val doNothingMethod = object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null || param.args.isEmpty()) return

                if (FakeLoc.enableDebugLog) {
                    KailLog.d(null, "Kail_Xposed", "doNothingMethod: ${param.method.name}")
                }

                if (FakeLoc.enableMockGnss && !FakeLoc.enableAGPS) {
                    if (FakeLoc.enableDebugLog) {
                        KailLog.d(null, "Kail_Xposed", "${param.method.name}: disable")
                    }
                    param.result = null
                }
            }
        }

        // AGPS Listener?
        XposedBridge.hookAllMethods(cGnssManagerService, "addGnssAntennaInfoListener", doNothingMethod)
        XposedBridge.hookAllMethods(cGnssManagerService, "addGnssMeasurementsListener", doNothingMethod)
        XposedBridge.hookAllMethods(cGnssManagerService, "addGnssNavigationMessageListener", doNothingMethod)
        XposedBridge.hookAllMethods(cGnssManagerService, "removeGnssAntennaInfoListener", doNothingMethod)
        XposedBridge.hookAllMethods(cGnssManagerService, "removeGnssMeasurementsListener", doNothingMethod)
        XposedBridge.hookAllMethods(cGnssManagerService, "removeGnssNavigationMessageListener", doNothingMethod)

        run {
            val hookedGnssCallback = Collections.synchronizedSet(HashSet<String>())
            val unhooks = cGnssManagerService.declaredMethods.filter {
                it.name == "registerGnssNmeaCallback" && it.parameterTypes.size > 1
            }.map { method ->
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        if (param == null || param.args[0] == null) return
                        val classListener = param.args[0].javaClass

                        if (hookedGnssCallback.contains(classListener.name)) return
                        hookedGnssCallback.add(classListener.name)

                        if (FakeLoc.enableDebugLog)
                            KailLog.d(null, "Kail_Xposed", "registerGnssNmeaCallback: $classListener")
                        kotlin.runCatching {
                            XposedHelpers.findAndHookMethod(classListener, "onNmeaReceived", Long::class.java, String::class.java, object: XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    if (!FakeLoc.enableMockGnss) return
                                    
                                    if (FakeLoc.enableAGPS) return
                                    
                                    if (FakeLoc.enableDebugLog)
                                        KailLog.d(null, "Kail_Xposed", "GnssManagerService.onNmeaReceived: disable")
                                    param.result = null
                                }
                            })
                        }.onFailure {
                            KailLog.e(null, "Kail_Xposed", "[onNmeaReceived hook failed: ${it.message} from GnssManagerService, please issue?")
                        }
                    }
                })
            }

            if (FakeLoc.enableDebugLog)
                KailLog.d(null, "Kail_Xposed", "found ${unhooks.size} registerGnssNmeaCallback")
        }
    }

    private fun hookHALGnss(classLoader: ClassLoader) {
//        hookGnssHALAnyVersion(XposedHelpers.findClassIfExists("android.hardware.gnss.IGnss\$Stub", classLoader))
//        hookGnssHALAnyVersion(XposedHelpers.findClassIfExists("android.hardware.gnss.V1_0.IGnss\$Stub", classLoader))
//        hookGnssHALAnyVersion(XposedHelpers.findClassIfExists("android.hardware.gnss.V1_1.IGnss\$Stub", classLoader))
//        hookGnssHALAnyVersion(XposedHelpers.findClassIfExists("android.hardware.gnss.V2_1.IGnss\$Stub", classLoader))
//        hookGnssHALAnyVersion(XposedHelpers.findClassIfExists("android.hardware.gnss.V2_0.IGnss\$Stub", classLoader))
        var hookedGnssLocationProvider = 0
        val cGnssLocationProviderImpl = XposedHelpers.findClassIfExists("com.android.server.location.gnss.GnssLocationProviderImpl", classLoader)
        if (cGnssLocationProviderImpl != null) {
            BlindHookLocation(cGnssLocationProviderImpl, classLoader)
            hookedGnssLocationProvider = 1
        }

        if (hookedGnssLocationProvider == 0) {
            val cGnssLocationProvider = XposedHelpers.findClassIfExists("com.android.server.location.gnss.GnssLocationProvider", classLoader)
            if (cGnssLocationProvider != null) {
                BlindHookLocation(cGnssLocationProvider, classLoader)
                hookedGnssLocationProvider = 2
            }
        }

        if (hookedGnssLocationProvider == 0) {
            KailLog.e(null, "Kail_Xposed", "Failed to find GnssLocationProvider!")
        } else {
            KailLog.i(null, "Kail_Xposed", "GnssLocationProvider done, status: $hookedGnssLocationProvider")
        }

        val cGnssNative = XposedHelpers.findClassIfExists("com.android.server.location.gnss.hal.GnssNative", classLoader)
        if (cGnssNative != null) {
            BlindHookLocation(cGnssNative, classLoader)

            cGnssNative.hookAllMethods("reportLocationBatch", beforeHook {
                if (FakeLoc.enable && args.size > 1 && args[0] is Array<*>) {
                    val locations = args[0] as Array<Location>
                    locations.forEachIndexed { index, location ->
                        locations[index] = injectLocation(location)
                    }
                }
            })
        } else {
            KailLog.e(null, "Kail_Xposed", "GnssNative not found")
        }
    }

    private fun hookGnssHALAnyVersion(cIGnssStub: Class<*>?) {
        if (cIGnssStub != null) {
            cIGnssStub.hookAllMethods("onTransact", beforeHook {
                val clazz = thisObject.javaClass
                clazz.onceHookAllMethod("injectBestLocation", beforeHook {
                    if (FakeLoc.enable) {
                        result = Unit
                    }

                    if (FakeLoc.enableDebugLog) {
                        KailLog.d(null, "Kail_Xposed", "injectLocation: $method")
                    }
                })

                clazz.onceHookAllMethod("injectLocation", beforeHook {
                    if (FakeLoc.enable) {
                        result = Unit
                    }

                    if (FakeLoc.enableDebugLog) {
                        KailLog.d(null, "Kail_Xposed", "injectLocation: $method")
                    }
                })
            })
        } else {
            KailLog.e(null, "Kail_Xposed", "IGnss\$Stub not found")
        }
    }
}