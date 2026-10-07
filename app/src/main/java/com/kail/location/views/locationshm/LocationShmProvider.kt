package com.kail.location.views.locationshm

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.SharedMemory
import com.kail.location.inject.utils.LocationShm
import com.kail.location.utils.KailLog

/**
 * root 模式位置模拟的共享内存 fd 通道。
 *
 * <p>App 侧 [com.kail.location.service.Root.ServiceGoRoot] 创建一块 ashmem
 * （[SharedMemory]）并注册到这里；注入进 system_server 的
 * [com.kail.location.inject.utils.RootLocationControl] 通过
 * `ContentResolver.call` 拿到同一个 [SharedMemory]（Parcelable，binder 会自动
 * 传递底层 fd）再 mmap。走的是标准 ContentProvider binder 通道，绕开 SELinux
 * 对 app 直读 /data/kail-loc 文件的限制，system_server 调 app provider 始终可用。
 */
class LocationShmProvider : ContentProvider() {

    companion object {
        private const val TAG = "LocationShmProvider"

        val AUTHORITY = LocationShm.PROVIDER_AUTHORITY
        val METHOD_GET_SHM = LocationShm.PROVIDER_METHOD_GET_SHM
        val METHOD_GET_CONFIG = LocationShm.PROVIDER_METHOD_GET_CONFIG
        val KEY_SHM = LocationShm.PROVIDER_KEY_SHM
        val KEY_WIFI_CONFIG = LocationShm.PROVIDER_KEY_WIFI_CONFIG
        val KEY_CELL_CONFIG = LocationShm.PROVIDER_KEY_CELL_CONFIG
        val KEY_ALLOW_CONFIG = LocationShm.PROVIDER_KEY_ALLOW_CONFIG
        val KEY_HIDE_CONFIG = LocationShm.PROVIDER_KEY_HIDE_CONFIG
        val KEY_ANTIDETECT_CONFIG = LocationShm.PROVIDER_KEY_ANTIDETECT_CONFIG
        val KEY_STEP_CONFIG = LocationShm.PROVIDER_KEY_STEP_CONFIG
        val KEY_CAMERA_CONFIG = LocationShm.PROVIDER_KEY_CAMERA_CONFIG
        val URI: Uri = Uri.parse(LocationShm.PROVIDER_URI)

        @Volatile
        private var sharedMemory: SharedMemory? = null

        private val configs = java.util.concurrent.ConcurrentHashMap<String, String>()

        fun setSharedMemory(memory: SharedMemory?) {
            sharedMemory = memory
            KailLog.i(null, TAG, "setSharedMemory ${if (memory == null) "<cleared>" else "ok"}")
        }

        fun getSharedMemory(): SharedMemory? = sharedMemory

        /** 下发一份配置文本（null/空 表示清除该配置）。 */
        fun setConfig(key: String, value: String?) {
            if (value.isNullOrEmpty()) {
                configs.remove(key)
                KailLog.i(null, TAG, "[provider-write] $key = <cleared>")
            } else {
                configs[key] = value
                KailLog.i(null, TAG, "[provider-write] $key len=${value.length} head=${value.lineSequence().firstOrNull() ?: ""}")
            }
        }

        fun getConfig(key: String): String? = configs[key]

        fun setWifiConfig(text: String?) = setConfig(KEY_WIFI_CONFIG, text)
        fun setCellConfig(text: String?) = setConfig(KEY_CELL_CONFIG, text)
    }

    override fun onCreate(): Boolean {
        KailLog.i(null, TAG, "provider onCreate")
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        when (method) {
            METHOD_GET_SHM -> {
                val memory = sharedMemory
                if (memory == null) {
                    KailLog.w(null, TAG, "[provider-call] get_shm -> no shm registered")
                    return null
                }
                KailLog.i(null, TAG, "[provider-call] get_shm -> ok")
                return Bundle().apply { putParcelable(KEY_SHM, memory) }
            }
            METHOD_GET_CONFIG -> {
                KailLog.d(null, TAG, "[provider-call] get_config -> keys=${configs.keys}")
                return Bundle().apply {
                    for ((key, value) in configs) {
                        putString(key, value)
                    }
                }
            }
        }
        KailLog.w(null, TAG, "[provider-call] unknown method=$method")
        return super.call(method, arg, extras)
    }

    override fun getType(uri: Uri): String = "application/octet-stream"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
