package com.kail.locationxposed.xposed.sensor

import com.kail.locationxposed.xposed.utils.KailLog

internal object NativeSensorHook {
    private const val TAG = "NativeSensorHook"
    private var initCalled = false

    @Synchronized
    fun init(cadence: Float, mode: Int, scheme: Int, enabled: Boolean) {
        KailLog.i(null, TAG, ">>> [JNI] init called: cadence=$cadence, mode=$mode, scheme=$scheme, enabled=$enabled, initCalled=$initCalled")
        if (initCalled) {
            KailLog.i(null, TAG, ">>> [JNI] Already initialized, calling nativeSetGaitParams only")
            try {
                KailLog.i(null, TAG, ">>> [JNI] Calling nativeSetGaitParams($cadence, $mode, $scheme, $enabled)")
                nativeSetGaitParams(cadence, mode, scheme, enabled)
                KailLog.i(null, TAG, ">>> [JNI] nativeSetGaitParams succeeded")
            } catch (e: Throwable) {
                KailLog.e(null, TAG, ">>> [JNI] nativeSetGaitParams failed: ${e.message}")
                e.printStackTrace()
            }
            return
        }
        try {
            KailLog.i(null, TAG, ">>> [JNI] Calling nativeInit($cadence, $mode, $scheme, $enabled)")
            nativeInit(cadence, mode, scheme, enabled)
            initCalled = true
            KailLog.i(null, TAG, ">>> [JNI] nativeInit succeeded, initCalled=true")
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> [JNI] nativeInit failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Synchronized
    fun setStepSimEnabled(enabled: Boolean) {
        KailLog.i(null, TAG, ">>> [JNI] setStepSimEnabled called: enabled=$enabled")
        try {
            KailLog.i(null, TAG, ">>> [JNI] Calling nativeSetStepSimEnabled($enabled)")
            nativeSetStepSimEnabled(enabled)
            KailLog.i(null, TAG, ">>> [JNI] nativeSetStepSimEnabled succeeded")
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> [JNI] nativeSetStepSimEnabled failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Synchronized
    fun setRouteSimulation(active: Boolean, spm: Float, mode: Int) {
        KailLog.i(null, TAG, ">>> [JNI] setRouteSimulation called: active=$active, spm=$spm, mode=$mode")
        try {
            nativeSetRouteSimulation(active, spm, mode)
            KailLog.i(null, TAG, ">>> [JNI] nativeSetRouteSimulation succeeded")
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> [JNI] nativeSetRouteSimulation failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Synchronized
    fun setWriteOffset(offset: Long) {
        KailLog.i(null, TAG, ">>> [JNI] setWriteOffset called: $offset")
        try {
            nativeSetWriteOffset(offset)
            KailLog.i(null, TAG, ">>> [JNI] nativeSetWriteOffset succeeded")
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> [JNI] nativeSetWriteOffset failed: ${e.message}")
        }
    }

    @Synchronized
    fun setConvertOffset(offset: Long) {
        KailLog.i(null, TAG, ">>> [JNI] setConvertOffset called: $offset")
        try {
            nativeSetConvertOffset(offset)
            KailLog.i(null, TAG, ">>> [JNI] nativeSetConvertOffset succeeded")
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> [JNI] nativeSetConvertOffset failed: ${e.message}")
        }
    }

    @Synchronized
    fun reset() {
        KailLog.i(null, TAG, ">>> [JNI] reset called")
        initCalled = false
        try {
            nativeReset()
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> [JNI] nativeReset failed: ${e.message}")
            setStepSimEnabled(false)
        }
    }

    private external fun nativeInit(cadence: Float, mode: Int, scheme: Int, enabled: Boolean)
    private external fun nativeSetStepSimEnabled(enabled: Boolean)
    private external fun nativeSetRouteSimulation(active: Boolean, spm: Float, mode: Int)
    private external fun nativeSetGaitParams(cadence: Float, mode: Int, scheme: Int, enabled: Boolean)
    private external fun nativeSetWriteOffset(offset: Long)
    private external fun nativeSetConvertOffset(offset: Long)
    private external fun nativeReset()
}
