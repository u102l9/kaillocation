package com.kail.locationxposed.xposed.hooks.telephony.miui

import com.kail.locationxposed.xposed.hooks.telephony.BaseTelephonyHook

object MiuiTelephonyManagerHook: BaseTelephonyHook() {
    operator fun invoke(classLoader: ClassLoader) {
//        val cMiuiTelephonyManager = XposedHelpers.findClassIfExists("com.miui.internal.telephony.BaseTelephonyManagerAndroidImpl", classLoader)
//        val cTelephonyManagerEx = XposedHelpers.findClassIfExists("miui.telephony.TelephonyManagerEx", classLoader)
//
//        if (FakeLocationConfig.DEBUG) {
//            println("[Kail] MiuiTelephonyManager: $cMiuiTelephonyManager")
//            println("[Kail] MiuiTelephonyManagerEx: $cTelephonyManagerEx")
//        }
//
//        cMiuiTelephonyManager?.let { clazz ->
//            println("[Kail] found " + clazz.declaredMethods.mapNotNull {
//                if (it.returnType == CellLocation::class.java) {
//                    XposedBridge.hookMethod(it, hookGetCellLocation)
//                } else null
//            }.size + " methods to hook in MiuiTelephonyManager")
//        }
//
//        cTelephonyManagerEx?.let { clazz ->
//            println("[Kail] found " + clazz.declaredMethods.mapNotNull {
//                if (it.returnType == CellLocation::class.java) {
//                    XposedBridge.hookMethod(it, hookGetCellLocation)
//                } else null
//            }.size + " methods to hook in MiuiTelephonyManagerEx")
//
//            var sizeGetNeighboringCellInfoMethod = XposedBridge.hookAllMethods(clazz, "getNeighboringCellInfo", hookGetNeighboringCellInfoList).size
//            sizeGetNeighboringCellInfoMethod += XposedBridge.hookAllMethods(clazz, "getNeighboringCellInfoForSlot", hookGetNeighboringCellInfoList).size
//            sizeGetNeighboringCellInfoMethod += XposedBridge.hookAllMethods(clazz, "getNeighboringCellInfoForSubscription", hookGetNeighboringCellInfoList).size
//            println("[Kail] found $sizeGetNeighboringCellInfoMethod methods to hook in MiuiTelephonyManagerEx")
//        }
    }
}