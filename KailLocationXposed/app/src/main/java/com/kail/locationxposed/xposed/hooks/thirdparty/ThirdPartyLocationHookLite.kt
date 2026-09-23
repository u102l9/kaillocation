package com.kail.locationxposed.xposed.hooks.thirdparty

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.kail.locationxposed.xposed.utils.KailLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.core.FakeLocState
import java.lang.reflect.Modifier

internal object ThirdPartyLocationHookLite {
    fun hook(classLoader: ClassLoader) {
        hookAMapNetworkLocationManager(classLoader)
        hookBaiduLocationClient(classLoader)
        hookTencentNlpManager(classLoader)
    }

    private fun hookAMapNetworkLocationManager(classLoader: ClassLoader) {
        val cNetworkLocationManager = XposedHelpers.findClassIfExists(
            "com.amap.android.location.NetworkLocationManager",
            classLoader
        ) ?: return

        XposedBridge.hookAllMethods(cNetworkLocationManager, "onSendExtraCommand", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null) return
                if (!FakeLocState.isEnabled()) return
                val cmd = param.args.getOrNull(0) as? String
                val extras = param.args.getOrNull(1) as? Bundle
                KailLog.d(null, "XPOSED", "高德 onSendExtraCommand 命令=$cmd 附加=$extras", isHighFrequency = true)
                param.result = false
            }
        })

        val boolFields = cNetworkLocationManager.declaredFields
            .filter { it.type == Boolean::class.java && !Modifier.isStatic(it.modifiers) }
        boolFields.forEach { it.isAccessible = true }

        val listenerFields = cNetworkLocationManager.declaredFields
            .filter { it.type == LocationListener::class.java && !Modifier.isStatic(it.modifiers) }
        listenerFields.forEach { it.isAccessible = true }

        XposedBridge.hookAllMethods(cNetworkLocationManager, "requestLocationUpdates", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null) return
                if (!FakeLocState.isEnabled()) return
                if (param.args.size > 1 && param.args[1] is LocationListener) {
                    param.args[1] = null
                }
                val thiz = param.thisObject ?: return
                listenerFields.forEach { f ->
                    kotlin.runCatching { f.set(thiz, null) }
                }
                boolFields.forEach { f ->
                    kotlin.runCatching { f.setBoolean(thiz, false) }
                }
            }
        })

        XposedBridge.hookAllMethods(cNetworkLocationManager, "updateOffLocEnable", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null) return
                param.result = Unit
            }
        })
    }

    private fun hookBaiduLocationClient(classLoader: ClassLoader) {
        val cLocationClient = XposedHelpers.findClassIfExists(
            "com.baidu.location.LocationClient",
            classLoader
        ) ?: return

        XposedBridge.hookAllMethods(cLocationClient, "requestNLPNormal", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                if (param == null) return
                if (!FakeLocState.isEnabled()) return
                val bdLoc = param.result ?: return
                val loc = FakeLocState.injectInto(null) ?: return
                kotlin.runCatching {
                    XposedHelpers.callMethod(bdLoc, "setLatitude", loc.latitude)
                    XposedHelpers.callMethod(bdLoc, "setLongitude", loc.longitude)
                    XposedHelpers.callMethod(bdLoc, "setBuildingID", "")
                    XposedHelpers.callMethod(bdLoc, "setAddrStr", "")
                    KailLog.d(null, "XPOSED", "百度 requestNLPNormal 注入位置 纬度=${loc.latitude} 经度=${loc.longitude}", isHighFrequency = true)
                }
            }
        })

        blindHookLocation(cLocationClient)
    }

    private fun hookTencentNlpManager(classLoader: ClassLoader) {
        val cTencentNlpManager = XposedHelpers.findClassIfExists(
            "com.tencent.geolocation.nlp.TencentNLPManager",
            classLoader
        ) ?: return
        blindHookLocation(cTencentNlpManager)
    }

    private fun blindHookLocation(c: Class<*>) {
        c.declaredMethods.forEach { m ->
            val hasLocationParam = m.parameterTypes.any { Location::class.java.isAssignableFrom(it) } ||
                m.parameterTypes.any { List::class.java.isAssignableFrom(it) }
            if (!hasLocationParam) return@forEach
            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    if (param == null) return
                    if (!FakeLocState.isEnabled()) return
                    val args = param.args ?: return
                    for (i in args.indices) {
                        when (val v = args[i]) {
                            is Location -> {
                                args[i] = FakeLocState.injectInto(v)
                            }
                            is List<*> -> {
                                val list = v.filterIsInstance<Location>()
                                if (list.isNotEmpty()) {
                                    args[i] = list.map { FakeLocState.injectInto(it) }
                                }
                            }
                        }
                    }
                }
            })
        }
    }
}

