package com.kail.locationxposed.xposed.hooks.fused

import android.location.LocationListener
import android.os.Bundle
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.BlindHookLocation
import com.kail.locationxposed.xposed.utils.BlindHookLocation.invoke
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.hookMethodAfter
import com.kail.locationxposed.xposed.utils.hookMethodBefore
import com.kail.locationxposed.xposed.utils.onceHookMethodBefore
import com.kail.locationxposed.xposed.utils.toClass
import java.lang.reflect.Modifier

object ThirdPartyLocationHook: BaseLocationHook() {
    operator fun invoke(classLoader: ClassLoader) {
        if(!initDivineService("ThirdPartyLocationHook")) {
            KailLog.e(null, "Kail_Xposed", "Failed to init DivineService in ThirdPartyLocationHook")
            return
        }

        // There is a certain probability that the positioning SDK will exchange data directly with these services
        // instead of ... through the system
        hookAMapNetLocManager(classLoader)
        hookBDMap(classLoader)
        hookTencent(classLoader)
    }

    private fun hookAMapNetLocManager(classLoader: ClassLoader) {
        val cNetworkLocationManager = "com.amap.android.location.NetworkLocationManager".toClass(classLoader)
        if (cNetworkLocationManager == null) {
            KailLog.w(null, "Kail_Xposed", "Failed to find NetworkLocationManager (amap service)")
            return
        }

        cNetworkLocationManager.onceHookMethodBefore("onSendExtraCommand") {
            if (FakeLoc.enableDebugLog) {
                val cmd = args[0] as String
                val extras = args[1] as Bundle

                KailLog.d(null, "Kail_Xposed", "NetworkLocationManager.onSendExtraCommand: $cmd, $extras")
            }
            if (FakeLoc.enable) {
                this.result = false
            }
        }

        val boolFields = cNetworkLocationManager.declaredFields
            .filter { it.type == Boolean::class.java && !Modifier.isStatic(it.modifiers) }
        boolFields.forEach {
            it.isAccessible = true
        }
        val listenerField = cNetworkLocationManager.declaredFields
            .filter { it.type == LocationListener::class.java && !Modifier.isStatic(it.modifiers) }
        listenerField.forEach {
            it.isAccessible = true
        }
        cNetworkLocationManager.onceHookMethodBefore("requestLocationUpdates") {
            if (FakeLoc.enable) {
                if(this.args[1] is LocationListener) {
                    this.args[1] = null // 拒绝高德地图注册私有listener
                }
                listenerField.forEach { it.set(thisObject, null) }
                boolFields.forEach { it.setBoolean(thisObject, false) }
            }
        }

        cNetworkLocationManager.hookMethodBefore("updateOffLocEnable") {
            this.result = Unit
        }

        BlindHookLocation(cNetworkLocationManager, classLoader)
    }

    private fun hookBDMap(classLoader: ClassLoader) {
        run {
            val bdLocationClient = "com.baidu.location.LocationClient".toClass(classLoader)
            if (bdLocationClient == null) {
                KailLog.w(null, "Kail_Xposed", "Failed to find LocationClient (baidu service)")
                return@run
            }

            bdLocationClient.hookMethodAfter("requestNLPNormal") {
                if (FakeLoc.enable) {
                    val bdLocation = result ?: return@hookMethodAfter

                    val jitterLat = FakeLoc.jitterLocation()
                    XposedHelpers.callMethod(bdLocation, "setLatitude", jitterLat.first)
                    XposedHelpers.callMethod(bdLocation, "setLongitude", jitterLat.second)
                    XposedHelpers.callMethod(bdLocation, "setBuildingID", "")
                    XposedHelpers.callMethod(bdLocation, "setAddrStr", "")
                }
            }

            BlindHookLocation(bdLocationClient, classLoader)
        }


    }

    private fun hookTencent(classLoader: ClassLoader) {
        run {
            val cTencentNLPManager = "com.tencent.geolocation.nlp.TencentNLPManager".toClass(classLoader)
            if (cTencentNLPManager == null) {
                KailLog.w(null, "Kail_Xposed", "Failed to find TencentNLPManager (tencent service)")
                return@run
            }

            BlindHookLocation(cTencentNLPManager, classLoader)
        }

//        run {
//            val cTencentLocationManager = "com.tencent.map.geolocation.TencentLocationManager".toClass(classLoader)
//                ?: return@run
//
//
//        }
    }
}