package com.kail.locationxposed.xposed.hooks.miui

import com.kail.locationxposed.xposed.base.BaseLocationHook
import com.kail.locationxposed.xposed.utils.KailLog

object MiuiLocationManagerHook: BaseLocationHook() {
    private const val TAG = "MiuiLocationManagerHook"

    operator fun invoke(classLoader: ClassLoader) {
        // 目前无额外 hook，保留入口并打点便于确认调用链覆盖。
        KailLog.d(null, TAG, "invoke: no extra hooks for this surface")
    }
}
