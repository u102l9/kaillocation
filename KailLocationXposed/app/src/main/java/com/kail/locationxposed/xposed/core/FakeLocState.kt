package com.kail.locationxposed.xposed.core

import android.location.Location
import android.location.LocationManager
import com.kail.locationxposed.xposed.utils.KailLog
import java.io.File
import java.util.concurrent.atomic.AtomicReference

internal object FakeLocState {
    private const val TAG = "FakeLocState"
    private const val OFFSETS_FILE = "/data/local/tmp/kail_sensor_offsets.txt"
    
    private val enabledRef = AtomicReference(false)
    private val locationRef = AtomicReference<Location?>(null)
    private val speedRef = AtomicReference(0f)
    private val bearingRef = AtomicReference(0f)
    private val altitudeRef = AtomicReference(0.0)
    private val accuracyRef = AtomicReference(2.5f)
    private val stepEnabledRef = AtomicReference(false)
    private var stepCadenceSpm = 120f
    private var stepGaitMode = 0
    private var stepSimScheme = 0
    internal var nativeLibraryLoaded = false
    private var moduleApkPath: String? = null
    private var pendingWriteOffset: String? = null
    private var pendingConvertOffset: String? = null
    
    fun setModuleApkPath(path: String) {
        moduleApkPath = path
        KailLog.i(null, TAG, "Module APK path set: $path")
    }
    
    @Synchronized
    fun isNativeLibraryLoaded(): Boolean = nativeLibraryLoaded

    @Synchronized
    fun ensureNativeLibraryLoaded(): Boolean {
        KailLog.i(null, TAG, ">>> ensureNativeLibraryLoaded START, nativeLibraryLoaded=$nativeLibraryLoaded")
        if (nativeLibraryLoaded) {
            KailLog.i(null, TAG, ">>> Native library already loaded, skipping")
            return true
        }
        
        KailLog.i(null, TAG, ">>> ensureNativeLibraryLoaded: loading SO")
        
        val soPath = "/data/local/kail-lib/libkail_native_hook.so"
        val soFile = java.io.File(soPath)
        
        if (!soFile.exists()) {
            KailLog.e(null, TAG, ">>> SO file not found at: $soPath")
            return false
        }
        
        KailLog.i(null, TAG, ">>> Attempting System.load: $soPath")
        try {
            System.load(soPath)
            nativeLibraryLoaded = true
            KailLog.i(null, TAG, ">>> System.load succeeded, nativeLibraryLoaded=true")
            
            KailLog.i(null, TAG, ">>> Calling loadAndApplyOffsets()")
            loadAndApplyOffsets()
            KailLog.i(null, TAG, ">>> loadAndApplyOffsets() returned")
            
            return true
        } catch (e: Throwable) {
            KailLog.e(null, TAG, ">>> System.load failed: ${e.message}")
            return false
        }
    }
    
    private fun loadAndApplyOffsets() {
        try {
            KailLog.i(null, TAG, ">>> loadAndApplyOffsets: attempting to read offsets file")
            val offsets = readOffsetsFromFile()
            if (offsets != null) {
                val (writeOffset, convertOffset) = offsets
                KailLog.i(null, TAG, ">>> Loaded offsets from file: write=$writeOffset, convert=$convertOffset")
                if (writeOffset.isNotEmpty()) {
                    KailLog.i(null, TAG, ">>> Applying write offset: $writeOffset")
                    setWriteOffset(writeOffset)
                } else {
                    KailLog.w(null, TAG, ">>> write offset is empty, skipping")
                }
                if (convertOffset.isNotEmpty()) {
                    KailLog.i(null, TAG, ">>> Applying convert offset: $convertOffset")
                    setConvertOffset(convertOffset)
                } else {
                    KailLog.w(null, TAG, ">>> convert offset is empty, skipping")
                }
            } else {
                KailLog.w(null, TAG, ">>> No offsets loaded from file (readOffsetsFromFile returned null)")
            }
        } catch (e: Exception) {
            KailLog.e(null, TAG, ">>> loadAndApplyOffsets failed: ${e.message}")
        }
    }
    
    private fun readOffsetsFromFile(): Pair<String, String>? {
        val file = File(OFFSETS_FILE)
        KailLog.i(null, TAG, ">>> readOffsetsFromFile: checking file $OFFSETS_FILE, exists=${file.exists()}, canRead=${file.canRead()}")
        if (!file.exists()) {
            KailLog.w(null, TAG, ">>> Offsets file not found: $OFFSETS_FILE")
            return null
        }
        
        var sendObjectsOffset = ""
        var convertToSensorEventOffset = ""
        
        try {
            val lines = file.readLines()
            KailLog.i(null, TAG, ">>> File has ${lines.size} lines")
            lines.forEachIndexed { index, line ->
                KailLog.i(null, TAG, ">>> Line $index: '$line'")
                val trimmed = line.trim()
                if (trimmed.startsWith("send_objects=")) {
                    sendObjectsOffset = trimmed.substringAfter("send_objects=").trim()
                    KailLog.i(null, TAG, ">>> Parsed send_objects offset: '$sendObjectsOffset'")
                } else if (trimmed.startsWith("convert_to_sensor_event=")) {
                    convertToSensorEventOffset = trimmed.substringAfter("convert_to_sensor_event=").trim()
                    KailLog.i(null, TAG, ">>> Parsed convert_to_sensor_event offset: '$convertToSensorEventOffset'")
                }
            }
            KailLog.i(null, TAG, ">>> Final parsed offsets: send_objects='$sendObjectsOffset', convert_to_sensor_event='$convertToSensorEventOffset'")
            return Pair(sendObjectsOffset, convertToSensorEventOffset)
        } catch (e: Exception) {
            KailLog.e(null, TAG, ">>> Failed to read offsets file: ${e.message}")
            return null
        }
    }
    
    @Synchronized
    fun markLoaded() {
        nativeLibraryLoaded = true
        KailLog.i(null, TAG, "Native library marked as loaded externally")
        KailLog.i(null, TAG, ">>> markLoaded: calling loadAndApplyOffsets()")
        loadAndApplyOffsets()
        KailLog.i(null, TAG, ">>> markLoaded: loadAndApplyOffsets() returned")
    }

    fun isEnabled(): Boolean = enabledRef.get()

    fun getAltitude(): Double = altitudeRef.get()

    fun getSpeed(): Float = speedRef.get()

    fun getBearing(): Float = bearingRef.get()

    fun setEnabled(enabled: Boolean) {
        enabledRef.set(enabled)
    }

    fun setSpeed(speed: Float) {
        speedRef.set(speed)
    }

    fun setBearing(bearing: Float) {
        bearingRef.set(bearing)
    }

    fun setAltitude(altitude: Double) {
        altitudeRef.set(altitude)
    }

    fun getAccuracy(): Float = accuracyRef.get()

    fun setAccuracy(accuracy: Float) {
        accuracyRef.set(accuracy)
    }

    fun setStepEnabled(enabled: Boolean) {
        stepEnabledRef.set(enabled)
        if (!ensureNativeLibraryLoaded()) return
    }

    fun isStepEnabled(): Boolean = stepEnabledRef.get()

    fun setStepCadenceSpm(spm: Float) { stepCadenceSpm = spm }
    fun getStepCadenceSpm(): Float = stepCadenceSpm
    fun setStepGaitMode(mode: Int) { stepGaitMode = mode }
    fun getGaitMode(): Int = stepGaitMode
    fun setStepSimScheme(scheme: Int) { stepSimScheme = scheme }
    fun getSimScheme(): Int = stepSimScheme

    fun getLatitude(): Double = locationRef.get()?.latitude ?: 0.0
    fun getLongitude(): Double = locationRef.get()?.longitude ?: 0.0

    fun updateLocation(lat: Double, lon: Double) {
        KailLog.i(null, "DEBUG", "=== updateLocation called: lat=$lat lon=$lon")
        val loc = Location(LocationManager.GPS_PROVIDER)
        loc.latitude = lat
        loc.longitude = lon
        loc.altitude = altitudeRef.get()
        loc.time = System.currentTimeMillis()
        loc.speed = speedRef.get()
        loc.bearing = bearingRef.get()
        locationRef.set(loc)
        KailLog.i(null, "DEBUG", "=== locationRef updated to: ${loc.latitude}, ${loc.longitude}")
    }

    fun injectInto(origin: Location?): Location? {
        val enabled = isEnabled()
        val current = locationRef.get()
        KailLog.i(null, "DEBUG", "=== injectInto: enabled=$enabled, current=$current, origin=$origin")
        if (!enabled) {
            KailLog.i(null, "DEBUG", "=== injectInto skipped: enabled=false")
            return origin
        }
        if (current == null) {
            KailLog.i(null, "DEBUG", "=== injectInto skipped: current is null")
            return origin
        }
        val out = Location(origin ?: current)
        out.latitude = current.latitude
        out.longitude = current.longitude
        out.altitude = current.altitude
        out.time = System.currentTimeMillis()
        out.speed = speedRef.get()
        out.bearing = bearingRef.get()
        KailLog.i(null, "DEBUG", "=== injectInto result: ${out.latitude}, ${out.longitude}")
        return out
    }

    fun setWriteOffset(offsetString: String) {
        KailLog.i(null, TAG, ">>> setWriteOffset called: input='$offsetString'")
        try {
            val offset = offsetString.toLongOrNull() ?: run {
                if (offsetString.startsWith("0x", ignoreCase = true)) {
                    offsetString.substring(2).toLongOrNull(16)
                } else {
                    null
                }
            }
            KailLog.i(null, TAG, ">>> setWriteOffset: parsed offset=$offset from '$offsetString'")
            if (offset != null) {
                if (nativeLibraryLoaded) {
                    KailLog.i(null, TAG, ">>> nativeLibraryLoaded=true, delegating to NativeSensorHook.setWriteOffset($offset)")
                    com.kail.locationxposed.xposed.sensor.NativeSensorHook.setWriteOffset(offset)
                    KailLog.i(null, TAG, ">>> setWriteOffset succeeded")
                } else {
                    KailLog.w(null, TAG, ">>> nativeLibraryLoaded=false, saving to pending: $offsetString")
                    pendingWriteOffset = offsetString
                }
            } else {
                KailLog.e(null, TAG, ">>> setWriteOffset: failed to parse offset from '$offsetString'")
            }
        } catch (e: Exception) {
            KailLog.e(null, TAG, ">>> setWriteOffset exception: ${e.message}")
        }
    }

    fun setConvertOffset(offsetString: String) {
        KailLog.i(null, TAG, ">>> setConvertOffset called: input='$offsetString'")
        try {
            val offset = offsetString.toLongOrNull() ?: run {
                if (offsetString.startsWith("0x", ignoreCase = true)) {
                    offsetString.substring(2).toLongOrNull(16)
                } else {
                    null
                }
            }
            KailLog.i(null, TAG, ">>> setConvertOffset: parsed offset=$offset from '$offsetString'")
            if (offset != null) {
                if (nativeLibraryLoaded) {
                    KailLog.i(null, TAG, ">>> nativeLibraryLoaded=true, delegating to NativeSensorHook.setConvertOffset($offset)")
                    com.kail.locationxposed.xposed.sensor.NativeSensorHook.setConvertOffset(offset)
                    KailLog.i(null, TAG, ">>> setConvertOffset succeeded")
                } else {
                    KailLog.w(null, TAG, ">>> nativeLibraryLoaded=false, saving to pending: $offsetString")
                    pendingConvertOffset = offsetString
                }
            } else {
                KailLog.e(null, TAG, ">>> setConvertOffset: failed to parse offset from '$offsetString'")
            }
        } catch (e: Exception) {
            KailLog.e(null, TAG, ">>> setConvertOffset exception: ${e.message}")
        }
    }

    fun setMocking(mocking: Boolean) {
    }

    fun setAuthorized(authorized: Boolean) {
    }

    fun loadNativeLibrary(path: String, writeOffset: String = "", convertOffset: String = ""): Pair<Boolean, String> {
        KailLog.i(null, TAG, ">>> loadNativeLibrary called: path=$path, writeOffset=$writeOffset, convertOffset=$convertOffset")
        
        return try {
            val file = File(path)
            if (!file.exists()) {
                KailLog.e(null, TAG, ">>> File not found: $path")
                Pair(false, "File not found: $path")
            } else {
                KailLog.i(null, TAG, ">>> Loading library: $path")
                System.load(path)
                nativeLibraryLoaded = true
                KailLog.i(null, TAG, ">>> Library loaded, nativeLibraryLoaded=$nativeLibraryLoaded")

                pendingWriteOffset?.let {
                    KailLog.i(null, TAG, ">>> Setting pending write offset: $it")
                    setWriteOffset(it)
                    pendingWriteOffset = null
                }

                pendingConvertOffset?.let {
                    KailLog.i(null, TAG, ">>> Setting pending convert offset: $it")
                    setConvertOffset(it)
                    pendingConvertOffset = null
                }

                if (writeOffset.isNotEmpty()) {
                    KailLog.i(null, TAG, ">>> Setting write offset from parameter: $writeOffset")
                    setWriteOffset(writeOffset)
                }

                if (convertOffset.isNotEmpty()) {
                    KailLog.i(null, TAG, ">>> Setting convert offset from parameter: $convertOffset")
                    setConvertOffset(convertOffset)
                }

                loadAndApplyOffsets()

                KailLog.i(null, TAG, ">>> loadNativeLibrary succeeded")
                Pair(true, "Library loaded: $path")
            }
        } catch (e: UnsatisfiedLinkError) {
            KailLog.e(null, TAG, ">>> UnsatisfiedLinkError: ${e.message}")
            Pair(false, "Load failed: ${e.message}")
        } catch (e: Exception) {
            KailLog.e(null, TAG, ">>> Exception: ${e.message}")
            Pair(false, "Error: ${e.message}")
        }
    }

    // No native methods - all native calls delegated to NativeSensorHook
}
