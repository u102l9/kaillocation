package com.kail.location.root

import com.kail.location.utils.KailLog

object NativeSensorHook {
    private const val TAG = "NativeSensorHook"

    /** native 库是否成功加载。加载失败时所有 external 调用都会抛 UnsatisfiedLinkError。 */
    @Volatile
    var libraryLoaded = false
        private set

    init {
        try {
            System.loadLibrary("kail_native_hook")
            libraryLoaded = true
            KailLog.i(null, TAG, "libkail_native_hook.so loaded")
        } catch (t: Throwable) {
            libraryLoaded = false
            KailLog.e(null, TAG, "failed to load libkail_native_hook.so", t)
        }
    }

    external fun nativeSetWriteOffset(offset: Long)
    external fun nativeSetConvertOffset(offset: Long)
    external fun nativeSetRouteSimulation(active: Boolean, spm: Float, mode: Int)
    external fun nativeSetGaitParams(spm: Float, mode: Int, scheme: Int, enable: Boolean)
    external fun nativeReloadConfig(): Boolean
    external fun nativeSetMocking(mocking: Int)
    external fun nativeSetAuthorized(authorized: Int)
    external fun nativeSetStepSimEnabled(enabled: Boolean)
    external fun nativeReset()
    external fun nativeInitHook()
}
