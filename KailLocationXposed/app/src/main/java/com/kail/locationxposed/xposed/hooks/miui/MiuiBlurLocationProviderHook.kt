package com.kail.locationxposed.xposed.hooks.miui

import android.os.Build
import android.telephony.CellIdentity
import android.telephony.CellIdentityCdma
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellSignalStrengthCdma
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.BlindHookLocation
import com.kail.locationxposed.xposed.utils.BinderUtils
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog
import com.kail.locationxposed.xposed.utils.beforeHook
import com.kail.locationxposed.xposed.utils.onceHookAllMethod
import com.kail.locationxposed.xposed.utils.onceHookMethodBefore

object MiuiBlurLocationProviderHook: BaseLocationHook() {
    private const val TAG = "MiuiBlurLocationProviderHook"
    operator fun invoke(classLoader: ClassLoader) {
        val cMiuiBlurLocationManagerImpl = XposedHelpers.findClassIfExists("com.android.server.location.MiuiBlurLocationManagerImpl", classLoader)
        if (cMiuiBlurLocationManagerImpl != null) {
            KailLog.i(null, TAG, "installing MiuiBlurLocationManagerImpl hooks (sdk=${Build.VERSION.SDK_INT})")
            BlindHookLocation(cMiuiBlurLocationManagerImpl, classLoader)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val hooker: XC_MethodHook.MethodHookParam.() -> Unit = {
                    if (FakeLoc.enable && !BinderUtils.isSystemAppsCall()) {
                        result = CellIdentityCdma::class.java.getConstructor(
                            Int::class.java,
                            Int::class.java,
                            Int::class.java,
                            Int::class.java,
                            Int::class.java,
                            String::class.java,
                            String::class.java
                        ).newInstance(
                            Int.MAX_VALUE,
                            Int.MAX_VALUE,
                            Int.MAX_VALUE,
                            (FakeLoc.latitude * 14400.0).toInt(),
                            (FakeLoc.longitude * 14400.0).toInt(),
                            null, null
                        )
                    }
                }
                if(cMiuiBlurLocationManagerImpl.onceHookMethodBefore("getBlurryCellLocation", CellIdentity::class.java) { hooker() } == null) {
                    cMiuiBlurLocationManagerImpl.onceHookMethodBefore("getBlurryCellLocation",
                        CellIdentity::class.java, Int::class.java, String::class.java) { hooker() }
                }
            }

            cMiuiBlurLocationManagerImpl.onceHookAllMethod("getBlurryCellInfos", beforeHook {
                if (FakeLoc.enable && !BinderUtils.isSystemAppsCall()) {
                    val cellInfos = arrayListOf<CellInfo>()
                    val cellInfo = kotlin.runCatching {
                        CellInfoCdma::class.java.getConstructor().newInstance().also {
                            XposedHelpers.callMethod(it, "setRegistered", true)
                            XposedHelpers.callMethod(it, "setTimeStamp", System.nanoTime())
                            XposedHelpers.callMethod(it, "setCellConnectionStatus", 0)
                        }
                    }.getOrElse {
                        CellInfoCdma::class.java.getConstructor(
                            Int::class.java,
                            Boolean::class.java,
                            Long::class.java,
                            CellIdentityCdma::class.java,
                            CellSignalStrengthCdma::class.java
                        ).newInstance(0, true, System.nanoTime(), CellIdentityCdma::class.java.newInstance(), CellSignalStrengthCdma::class.java.newInstance())
                    }
                    cellInfos.add(cellInfo)

                    result = cellInfos
                }
            })
        } else {
            KailLog.d(null, TAG, "MiuiBlurLocationManagerImpl not present; skipping (non-MIUI ROM)")
        }
    }


}