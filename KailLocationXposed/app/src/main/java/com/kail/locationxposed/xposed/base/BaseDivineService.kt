package com.kail.locationxposed.xposed.base

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import com.kail.locationxposed.xposed.utils.BinderUtils
import com.kail.locationxposed.xposed.utils.FakeLoc
import com.kail.locationxposed.xposed.utils.KailLog

abstract class BaseDivineService {
    /**
     * if the hook is TelephonyService? or other service?
     * this service may not be in the same space as the `system_server`,
     * so a binder is used to talk.
     */
    protected fun initDivineService(from: String, retryCount: Int = 0): Boolean {
        // kail_location does not use the kail provider divine service model,
        // so we always return true to allow hooks to proceed.
        return true
    }

    /**
     * Synchronize configurations in different processes
     */
    private fun syncConfig(locationManager: LocationManager, randomKey: String) {
        // No-op in kail_location
    }
}
