@file:Suppress("UNCHECKED_CAST", "PrivateApi")
package com.kail.locationxposed.xposed.hooks.wlan

import android.net.wifi.WifiInfo
import android.os.Build
import android.util.ArrayMap
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.utils.BinderUtils
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.afterHook
import com.kail.locationxposed.xposed.utils.beforeHook
import com.kail.locationxposed.xposed.utils.hookAllMethods
import com.kail.locationxposed.xposed.utils.hookMethodAfter
import com.kail.locationxposed.xposed.utils.toClass

object WlanHook {
    operator fun invoke(classLoader: ClassLoader) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val cSystemServerClassLoaderFactory = XposedHelpers.findClassIfExists("com.android.internal.os.SystemServerClassLoaderFactory", classLoader)
            if (cSystemServerClassLoaderFactory == null) {
                KailLog.w(null, "Kail_Xposed", "Failed to find SystemServerClassLoaderFactory")
                return
            }
            val sLoadedPaths = XposedHelpers.getStaticObjectField(cSystemServerClassLoaderFactory, "sLoadedPaths") as ArrayMap<String, PathClassLoader>
            val wifiClassLoader = sLoadedPaths.firstNotNullOfOrNull {
                if (it.key.contains("service-wifi.jar")) it.value else null
            }
            if (wifiClassLoader == null) {
                KailLog.w(null, "Kail_Xposed", "Failed to find wifiClassLoader")
                return
            }
            val wifiClazz = "com.android.server.wifi.WifiServiceImpl".toClass(wifiClassLoader)
            if (wifiClazz == null) {
                KailLog.w(null, "Kail_Xposed", "Failed to find WifiServiceImpl class")
                return
            }
            hookWifiServiceImpl(wifiClazz)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cSystemServiceManager = XposedHelpers.findClassIfExists("com.android.server.SystemServiceManager", classLoader)
            if (cSystemServiceManager == null) {
                KailLog.w(null, "Kail_Xposed", "Failed to find SystemServiceManager")
                return
            }
            cSystemServiceManager.hookAllMethods("loadClassFromLoader", afterHook {
                if (args[0] == "com.android.server.wifi.WifiService") {
                    kotlin.runCatching {
                        val classloader = args[1] as PathClassLoader
                        val wifiClazz = classloader.loadClass("com.android.server.wifi.WifiServiceImpl")
                        hookWifiServiceImpl(wifiClazz)
                    }.onFailure {
                        KailLog.e(null, "Kail_Xposed", "Failed to hook WifiService: ${it.message}")
                    }
                }
            })
        }
    }

    private fun hookWifiServiceImpl(wifiClazz: Class<*>) {
        if (!FakeLoc.hookWifi) return

        wifiClazz.hookAllMethods("getConnectionInfo", beforeHook {
            val caller = args.getOrNull(0)
            val packageName = when (caller) {
                is String -> caller
                is Int -> kotlin.runCatching {
                    BinderUtils.getUidPackageNames(uid = caller)?.firstOrNull()
                }.getOrNull() ?: return@beforeHook
                else -> return@beforeHook
            }
            if (FakeLoc.enableDebugLog)
                KailLog.d(null, "Kail_Xposed", "In getConnectionInfo with caller: $packageName, state: ${FakeLoc.enableMockWifi}")

            if (FakeLoc.enableMockWifi && !BinderUtils.isSystemPackages(packageName)) {
                val wifiInfo = WifiInfo::class.java.getConstructor().newInstance()
                XposedHelpers.callMethod(wifiInfo, "setMacAddress", "02:00:00:00:00:00")
                XposedHelpers.callMethod(wifiInfo, "setBSSID", "02:00:00:00:00:00")
                result = wifiInfo
            }
        })

        wifiClazz.hookAllMethods("getScanResults", afterHook {
            val caller = args.getOrNull(0)
            val packageName = when (caller) {
                is String -> caller
                is Int -> kotlin.runCatching {
                    BinderUtils.getUidPackageNames(uid = caller)?.firstOrNull()
                }.getOrNull() ?: return@afterHook
                else -> return@afterHook
            }
            if (packageName.isEmpty()) {
                return@afterHook
            }

            if (FakeLoc.enableDebugLog)
                KailLog.d(null, "Kail_Xposed", "In getScanResults with caller: $packageName, state: ${FakeLoc.enableMockWifi}")

            if(FakeLoc.enableMockWifi) {
                if(result == null) {
                    return@afterHook 
                }
                
                if (result is List<*>) {
                    result = arrayListOf<Any>()
                    return@afterHook
                } // 针对小米系列机型的wifi扫描返回

                if (result is Array<*>) {
                    result = arrayOf<Any>()
                    return@afterHook
                } // 针对一加系列机型的wifi扫描返回

                // 在高于安卓10的版本，Google 引入了 APEX（Android Pony EXpress）文件格式来封装系统组件，包括系统服务~！
                // 上面的代码在高版本将无效导致应用可以通过网络AGPS到正常的位置（现象就是位置拉回）
                // 这里针对一个普通的版本进行一个修复
                val resultClass = result.javaClass
                if (resultClass.name.contains("ParceledListSlice")) runCatching {
                    val constructor = resultClass.getConstructor(List::class.java)
                    if (!constructor.isAccessible) {
                        constructor.isAccessible = true
                    }
                    result = constructor.newInstance(emptyList<Any>())
                    return@afterHook
                }.onFailure {
                    KailLog.e(null, "Kail_Xposed", "getScanResults: ParceledListSlice failed: ${it.message}")
                }

                if (FakeLoc.enableDebugLog) {
                    KailLog.e(null, "Kail_Xposed", "getScanResults: Unknown return type: ${result?.javaClass?.name}")
                }
            }
        })
    }
}
