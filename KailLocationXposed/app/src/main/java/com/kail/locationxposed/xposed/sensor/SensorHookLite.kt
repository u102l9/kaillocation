package com.kail.locationxposed.xposed.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import com.kail.locationxposed.xposed.core.FakeLocState
import com.kail.locationxposed.xposed.utils.KailLog
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

internal object SensorHookLite {
    private const val TAG = "SensorHookLite"
    private val stepListeners = ConcurrentHashMap.newKeySet<Any>()
    @Volatile private var sensorRef: Sensor? = null
    @Volatile private var stepThread: Thread? = null
    @Volatile private var sensorEventConstructor: Constructor<*>? = null

    fun hook(classLoader: ClassLoader) {
        val cSSM = XposedHelpers.findClassIfExists("android.hardware.SystemSensorManager", classLoader)
        if (cSSM == null) {
            KailLog.w(null, TAG, "hook skipped: SystemSensorManager not found")
            return
        }
        XposedBridge.hookAllMethods(cSSM, "registerListener", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                val args = param?.args ?: return
                val listener = args.getOrNull(0) ?: return
                val sensor = args.getOrNull(1) as? Sensor ?: return
                val type = sensor.type
                if (type == Sensor.TYPE_STEP_DETECTOR || type == Sensor.TYPE_STEP_COUNTER) {
                    stepListeners.add(listener)
                    sensorRef = sensor
                    KailLog.i(null, TAG, "step listener registered: type=$type listener=${listener.javaClass.name}")
                    ensureThread()
                }
            }
        })
        XposedBridge.hookAllMethods(cSSM, "unregisterListener", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                val args = param?.args ?: return
                val listener = args.getOrNull(0) ?: return
                stepListeners.remove(listener)
            }
        })
        KailLog.i(null, TAG, "hook installed on SystemSensorManager")
    }

    private fun ensureThread() {
        if (stepThread?.isAlive == true) return
        val t = Thread {
            var counter = 0f
            while (true) {
                try {
                    if (!FakeLocState.isEnabled() || !FakeLocState.isStepEnabled()) {
                        Thread.sleep(200)
                        continue
                    }
                    val spm = FakeLocState.getStepCadenceSpm()
                    if (spm <= 0f) {
                        Thread.sleep(200)
                        continue
                    }
                    val sensor = sensorRef
                    if (sensor == null) {
                        Thread.sleep(200)
                        continue
                    }
                    val ev = createEvent(sensor)
                    if (sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                        ev.values[0] = 1f
                    } else {
                        counter += 1f
                        ev.values[0] = counter
                    }
                    ev.timestamp = System.nanoTime()
                    stepListeners.forEach { l ->
                        kotlin.runCatching {
                            XposedHelpers.callMethod(l, "onSensorChanged", ev)
                        }
                    }
                    KailLog.v(null, TAG, "fed step event value=${ev.values[0]} to ${stepListeners.size} listener(s)")
                    val intervalMs = (60000.0 / spm).toLong().coerceAtLeast(1L)
                    Thread.sleep(intervalMs)
                } catch (_: InterruptedException) {
                    break
                } catch (t: Throwable) {
                    KailLog.w(null, TAG, "step feeder iteration error: ${t.message}")
                }
            }
        }
        t.isDaemon = true
        t.name = "KailStepHook"
        t.start()
        stepThread = t
    }

    private fun createEvent(sensor: Sensor): SensorEvent {
        val cons = sensorEventConstructor ?: synchronized(this) {
            sensorEventConstructor ?: XposedHelpers.findClass("android.hardware.SensorEvent", sensor.javaClass.classLoader)
                .getDeclaredConstructor(Int::class.javaPrimitiveType)
                .apply { isAccessible = true }
                .also { sensorEventConstructor = it }
        }
        val ev = cons.newInstance(1) as SensorEvent
        XposedHelpers.setObjectField(ev, "sensor", sensor)
        return ev
    }
}
