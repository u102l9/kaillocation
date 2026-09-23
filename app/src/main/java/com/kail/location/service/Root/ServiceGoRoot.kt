package com.kail.location.service.Root

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Process
import android.os.SharedMemory
import android.os.SystemClock
import android.provider.Settings
import android.system.OsConstants
import androidx.preference.PreferenceManager
import com.kail.location.R
import com.kail.location.geo.GeoPredict
import com.kail.location.inject.utils.HideConfigFile
import com.kail.location.inject.utils.LocationShm
import com.kail.location.inject.utils.RootControlPaths
import com.kail.location.root.NativeSensorHook
import com.kail.location.service.Developer.MockLocationProvider
import com.kail.location.utils.GoUtils
import com.kail.location.utils.KailLog
import com.kail.location.utils.MapUtils
import com.kail.location.utils.ShellUtils
import com.kail.location.utils.SimulationDiagnostics
import com.kail.location.utils.InjectionCrashSentinel
import com.kail.location.utils.service.RouteEngine
import com.kail.location.utils.service.ServiceConstants
import com.kail.location.utils.service.ServiceNotificationHelper
import com.kail.location.viewmodels.JoystickViewModel
import com.kail.location.viewmodels.SettingsViewModel
import com.kail.location.views.joystick.JoystickWindowManager
import com.kail.location.views.locationpicker.LocationPickerActivity
import com.kail.location.views.locationshm.LocationShmProvider
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

/**
 * Foreground service for the "root" run mode.
 *
 * Mocks location safely without ptrace-injecting system_server. Concretely:
 *
 *   1. [RootDeployer.ensureBaseline] stages every FakeLocation loader/hook
 *      .so (libfakeloc_init.so / libfakeloc_initzygote.so /
 *      libfakeloc_apphook.so / liblhooker.so / libStepSensor.so /
 *      hook/runtime support libraries into /data/kail-loc/, drops the kail_inject ptrace
 *      injector + libkail_native_hook.so into /data/local/kail-lib/, and
 *      copies the host APK to /data/kail-loc/libfakeloc.so as the dex
 *      payload. The full FakeLocation toolchain is therefore present on the
 *      device for an operator who wants to bootstrap it manually via
 *      [RootDeployer.bootstrapInjection], but the service does NOT run that
 *      step automatically — ptrace-injecting a production system_server
 *      regularly deadlocks the dynamic-linker lock and freezes the phone.
 *   2. The service grants the host package the AppOps `mock_location`
 *      permission via `appops set` so the standard Android test-provider
 *      mechanism works without the user toggling Developer Settings.
 *   3. Location updates are pushed via [MockLocationProvider] (the same code
 *      Developer mode uses) which calls
 *      `LocationManager.setTestProviderLocation` for both GPS and NETWORK
 *      providers. This is exactly the mechanism Developer mode would use,
 *      but root mode handles the AppOps grant for you.
 *   4. Sensor/step-cadence simulation runs through the in-app
 *      [NativeSensorHook] binding to libkail_native_hook.so. The full hook
 *      on system_server still requires the FakeLocation injection chain
 *      above to be online, so step mocking degrades gracefully when only
 *      ensureBaseline ran.
 *
 * The route engine, joystick, foreground notification, and control-action
 * surface mirror [com.kail.location.service.Xposed.ServiceGoXposed] so the
 * existing UI plugs in unchanged.
 */
class ServiceGoRoot : Service() {

    private var mCurLat = ServiceConstants.DEFAULT_LAT
    private var mCurLng = ServiceConstants.DEFAULT_LNG
    private var mCurAlt = ServiceConstants.DEFAULT_ALT
    private var mCurBea = ServiceConstants.DEFAULT_BEA
    private var mSpeed = 1.2

    private lateinit var mLocManager: LocationManager
    private lateinit var mLocHandlerThread: HandlerThread
    private lateinit var mLocHandler: Handler
    private var isStop = false

    private lateinit var mJoystickManager: JoystickWindowManager
    private lateinit var mJoystickViewModel: JoystickViewModel

    private val mBinder = ServiceGoRootBinder()
    private val mRouteEngine = RouteEngine()
    private val mNotificationHelper by lazy {
        ServiceNotificationHelper(
            service = this,
            channelId = "SERVICE_GO_ROOT_NOTE",
            channelName = "SERVICE_GO_ROOT_NOTE",
            noteId = SERVICE_GO_NOTE_ID,
            onShowJoystick = { mJoystickManager.show() },
            onHideJoystick = { mJoystickManager.hide() }
        )
    }

    @Volatile private var locationLoopStarted: Boolean = false
    private var speedFluctuation: Boolean = false
    private var stepEnabled: Boolean = false
    private var stepCadence: Float = 120f
    private var stepMode: Int = 0
    private var stepScheme: Int = 0

    @Volatile private var nativeHookReady: Boolean = false
    @Volatile private var nativeHookAttempted: Boolean = false
    @Volatile private var rootControlActive: Boolean = false
    @Volatile private var rootControlPrepared: Boolean = false
    @Volatile private var rootControlLatestWrite: RootControlWrite? = null
    @Volatile private var rootControlWriterScheduled: Boolean = false
    @Volatile private var lastRootControlAsyncWriteMs: Long = 0L
    @Volatile private var startGeneration: Int = 0
    @Volatile private var bootstrapInProgress: Boolean = false
    @Volatile private var activeRootControlSession: Long = 0L
    private var lastRouteTickElapsedMs: Long = 0L
    private var rootControlFastProcess: java.lang.Process? = null
    private var rootControlFastWriter: BufferedWriter? = null
    private lateinit var mRootControlWriterThread: HandlerThread
    private lateinit var mRootControlWriterHandler: Handler
    @Volatile private var lastStepAckLogMs: Long = 0L
    @Volatile private var stepAckReadInFlight: Boolean = false

    /**
     * Phase 1 共享内存位置传输：App 侧持有 ashmem 与映射，每 tick 把当前位置
     * seqlock 写入；system_server 侧经 LocationShmProvider 拿到 fd 后 mmap 读取。
     * 任一环节失败都为 null，[pushLocationToInjection] 自动回退控制文件通道。
     */
    @Volatile private var locationShmShared: SharedMemory? = null
    @Volatile private var locationShmBuffer: ByteBuffer? = null

    /** Drives Android's standard test-provider mechanism. Same code Developer mode uses. */
    private val mMockLocationProvider by lazy { MockLocationProvider(this, mLocManager) }

    /**
     * Target-app allow-list driven by the "独立模拟" (Independent Simulation)
     * screen. When non-empty, every mock surface (location / GNSS / WiFi /
     * cell) is restricted to ONLY these packages via FakeLocation's
     * setAllowMockPackages; all other apps read real data. Empty means the
     * default FakeLocation behaviour (mock for all apps).
     *
     * Read from prefs on demand so a mock started from any screen always
     * picks up the latest independent-mode selection.
     */
    private val independentAllowPackages: List<String>
        get() = runCatching {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val enabled = prefs.getBoolean("independent_enabled", false)
            if (!enabled) return emptyList()
            (prefs.getString("independent_target_packages", "") ?: "")
                .split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }.getOrDefault(emptyList())

    /**
     * Set true only when the start intent flagged WIFI_ONLY/CELL_ONLY — those
     * modes intentionally do not run the location loop.
     */
    private var modeWifiOnly: Boolean = false
    private var modeCellOnly: Boolean = false

    /**
     * Set true only when the start intent flagged HIDE_ONLY — the "Root与应用
     * 隐藏" (Root & App Hiding) screen. This mode does not mock location; it
     * only pushes the hide allow-list/flags into the FakeLocation
     * oem_integrity binder and injects the target app processes so
     * RootHideHook / LAntiDetect install there.
     */
    private var modeHideOnly: Boolean = false

    /** Root/framework-artifact hiding toggle for the selected target apps. */
    private var hideRootEnabled: Boolean = false

    /** Installed-app-list hiding toggle (requires [hideRootEnabled]). */
    private var hideAppListEnabled: Boolean = false

    /** Packages the hide features apply to. Empty means "no targets". */
    private var pendingHidePackages: List<String> = emptyList()

    /**
     * WiFi / cell networks selected in the UI for spoofing. Populated from the
     * start intent's [EXTRA_WIFI_LIST] / [EXTRA_CELL_LIST] parcelable extras.
     * Pushed into the FakeLocation injection layer via the oem_wifi /
     * oem_location binders once the inject is online.
     */
    private var pendingWifiList: List<com.kail.location.models.WifiInfo> = emptyList()
    private var pendingCellList: List<com.kail.location.models.CellInfo> = emptyList()

    companion object {
        const val DEFAULT_LAT = ServiceConstants.DEFAULT_LAT
        const val DEFAULT_LNG = ServiceConstants.DEFAULT_LNG
        const val DEFAULT_ALT = ServiceConstants.DEFAULT_ALT
        const val DEFAULT_BEA = ServiceConstants.DEFAULT_BEA

        private const val TAG = "ServiceGoRoot"
        private const val HANDLER_MSG_ID = 0
        private const val DEFAULT_LOCATION_UPDATE_INTERVAL_MS = 200L
        private const val SERVICE_GO_HANDLER_NAME = "ServiceGoRootLocation"
        private const val SERVICE_GO_NOTE_ID = 3
        private const val ROOT_RUNTIME_DIR = "/data/system/kail-loc"
        private const val ROOT_INJECTDEX_STATE = "$ROOT_RUNTIME_DIR/injectdex_state.txt"

        const val SERVICE_GO_NOTE_ACTION_JOYSTICK_SHOW = ServiceNotificationHelper.ACTION_JOYSTICK_SHOW
        const val SERVICE_GO_NOTE_ACTION_JOYSTICK_HIDE = ServiceNotificationHelper.ACTION_JOYSTICK_HIDE

        const val EXTRA_ROUTE_POINTS = ServiceConstants.EXTRA_ROUTE_POINTS
        const val EXTRA_ROUTE_WAIT_TIMES = ServiceConstants.EXTRA_ROUTE_WAIT_TIMES
        const val EXTRA_ROUTE_LOOP = ServiceConstants.EXTRA_ROUTE_LOOP
        const val EXTRA_JOYSTICK_ENABLED = ServiceConstants.EXTRA_JOYSTICK_ENABLED
        const val EXTRA_ROUTE_SPEED = ServiceConstants.EXTRA_ROUTE_SPEED
        const val EXTRA_COORD_TYPE = ServiceConstants.EXTRA_COORD_TYPE
        const val EXTRA_CONTROL_ACTION = ServiceConstants.EXTRA_CONTROL_ACTION
        const val EXTRA_SPEED_FLUCTUATION = ServiceConstants.EXTRA_SPEED_FLUCTUATION
        const val EXTRA_SEEK_RATIO = ServiceConstants.EXTRA_SEEK_RATIO
        const val EXTRA_ROUTE_APPEND_POINTS = ServiceConstants.EXTRA_ROUTE_APPEND_POINTS
        const val EXTRA_ROUTE_APPEND_WAIT_TIMES = ServiceConstants.EXTRA_ROUTE_APPEND_WAIT_TIMES

        const val EXTRA_STEP_ENABLED = "EXTRA_STEP_ENABLED"
        const val EXTRA_STEP_FREQ = "EXTRA_STEP_FREQ"
        const val EXTRA_STEP_MODE = "EXTRA_STEP_MODE"
        const val EXTRA_STEP_SCHEME = "EXTRA_STEP_SCHEME"
        const val EXTRA_WIFI_ONLY = "EXTRA_WIFI_ONLY"
        const val EXTRA_CELL_ONLY = "EXTRA_CELL_ONLY"
        const val EXTRA_WIFI_LIST = "EXTRA_WIFI_LIST"
        const val EXTRA_CELL_LIST = "EXTRA_CELL_LIST"
        const val EXTRA_HIDE_ONLY = "EXTRA_HIDE_ONLY"
        const val EXTRA_HIDE_ROOT = "EXTRA_HIDE_ROOT"
        const val EXTRA_HIDE_APPLIST = "EXTRA_HIDE_APPLIST"
        const val EXTRA_HIDE_PACKAGES = "EXTRA_HIDE_PACKAGES"

        const val CONTROL_PAUSE = ServiceConstants.CONTROL_PAUSE
        const val CONTROL_RESUME = ServiceConstants.CONTROL_RESUME
        const val CONTROL_STOP = ServiceConstants.CONTROL_STOP
        const val CONTROL_SEEK = ServiceConstants.CONTROL_SEEK
        const val CONTROL_SET_SPEED = ServiceConstants.CONTROL_SET_SPEED
        const val CONTROL_SET_SPEED_FLUCTUATION = ServiceConstants.CONTROL_SET_SPEED_FLUCTUATION
        const val CONTROL_APPEND_ROUTE = ServiceConstants.CONTROL_APPEND_ROUTE
        const val CONTROL_SET_STEP = "set_step"
        const val CONTROL_STOP_WIFI = "stop_wifi"
        const val CONTROL_SET_WIFI = "set_wifi"
        const val CONTROL_STOP_CELL = "stop_cell"
        const val CONTROL_SET_CELL = "set_cell"
        const val CONTROL_SET_ALLOW_PACKAGES = "set_allow_packages"
        const val EXTRA_ALLOW_PACKAGES = "EXTRA_ALLOW_PACKAGES"
        const val CONTROL_SET_HIDE = "set_hide"
        const val CONTROL_STOP_HIDE = "stop_hide"

        const val COORD_WGS84 = ServiceConstants.COORD_WGS84
        const val COORD_BD09 = ServiceConstants.COORD_BD09
        const val COORD_GCJ02 = ServiceConstants.COORD_GCJ02

        const val ACTION_STATUS_CHANGED = ServiceConstants.ACTION_STATUS_CHANGED
        const val EXTRA_IS_SIMULATING = ServiceConstants.EXTRA_IS_SIMULATING
        const val EXTRA_IS_PAUSED = ServiceConstants.EXTRA_IS_PAUSED

        /**
         * True while a ServiceGoRoot instance is alive. Lets the Independent
         * Simulation screen decide whether to push a live allow-list update
         * (only meaningful when a mock session is actually running).
         */
        @Volatile
        @JvmStatic
        var isRunning: Boolean = false
            private set

        private val ROOT_CONTROL_SESSION_SEQ = AtomicLong(0L)
        private val ROOT_CONTROL_ACTIVE_SESSION = AtomicLong(0L)
        private val ROOT_CONTROL_LOCK = Any()
    }

    override fun onBind(intent: Intent): IBinder = mBinder

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        KailLog.i(this, TAG, "onCreate started")
        runCatching { mNotificationHelper.initAndStartForeground() }
            .onFailure { KailLog.e(this, TAG, "initNotification: ${it.message}") }
        runCatching { mLocManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager }
            .onFailure { KailLog.e(this, TAG, "LocationManager init: ${it.message}") }
        runCatching { initGoLocation() }
            .onFailure { KailLog.e(this, TAG, "initGoLocation: ${it.message}") }
        runCatching { initRootControlWriter() }
            .onFailure { KailLog.e(this, TAG, "initRootControlWriter: ${it.message}") }
        runCatching {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val joystickEnabledPref = prefs.getBoolean("setting_joystick_enabled", false)
            initJoyStick()
            if (joystickEnabledPref) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                    mJoystickManager.show()
                }
            } else {
                mJoystickManager.hide()
            }
        }.onFailure {
            KailLog.e(this, TAG, "init joystick: ${it.message}")
            GoUtils.DisplayToast(applicationContext, getString(R.string.service_overlay_failed, it.message))
        }
        KailLog.i(this, TAG, "onCreate finished")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val ctrl = intent.getStringExtra(EXTRA_CONTROL_ACTION)
            if (!ctrl.isNullOrBlank()) {
                handleControlAction(ctrl, intent)
                return super.onStartCommand(intent, flags, startId)
            }
            speedFluctuation = intent.getBooleanExtra(EXTRA_SPEED_FLUCTUATION, false)
        }

        mNotificationHelper.startForegroundIfReady()

        if (intent != null) {
            modeWifiOnly = intent.getBooleanExtra(EXTRA_WIFI_ONLY, false)
            modeCellOnly = intent.getBooleanExtra(EXTRA_CELL_ONLY, false)
            modeHideOnly = intent.getBooleanExtra(EXTRA_HIDE_ONLY, false)

            // "Root与应用隐藏" start: no location/WiFi/cell mocking, just stage
            // the inject, push the hide config into oem_integrity, and
            // inject the selected app processes so RootHideHook installs there.
            if (modeHideOnly) {
                hideRootEnabled = intent.getBooleanExtra(EXTRA_HIDE_ROOT, false)
                hideAppListEnabled = intent.getBooleanExtra(EXTRA_HIDE_APPLIST, false)
                pendingHidePackages = intent.getStringArrayListExtra(EXTRA_HIDE_PACKAGES) ?: emptyList()
                KailLog.i(this, TAG, "onStartCommand hideOnly hideRoot=$hideRootEnabled hideAppList=$hideAppListEnabled pkgs=${pendingHidePackages.size}")
                Thread({ startHideOnInjection() }, "ServiceGoRootHideBootstrap").start()
                return START_STICKY
            }

            if (bootstrapInProgress && !locationLoopStarted) {
                KailLog.w(this, TAG, "ignore duplicate start while bootstrap is in progress")
                return START_STICKY
            }
            bootstrapInProgress = true

            // Selected WiFi / cell networks (parcelable lists from the UI).
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(EXTRA_WIFI_LIST, com.kail.location.models.WifiInfo::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<com.kail.location.models.WifiInfo>(EXTRA_WIFI_LIST)
                }
            }.getOrNull()?.let { if (it.isNotEmpty()) pendingWifiList = it }
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(EXTRA_CELL_LIST, com.kail.location.models.CellInfo::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<com.kail.location.models.CellInfo>(EXTRA_CELL_LIST)
                }
            }.getOrNull()?.let { if (it.isNotEmpty()) pendingCellList = it }

            val coordType = intent.getStringExtra(EXTRA_COORD_TYPE) ?: COORD_BD09
            mCurLng = intent.getDoubleExtra(LocationPickerActivity.LNG_MSG_ID, DEFAULT_LNG)
            mCurLat = intent.getDoubleExtra(LocationPickerActivity.LAT_MSG_ID, DEFAULT_LAT)
            runCatching {
                when (coordType) {
                    COORD_WGS84 -> { /* keep */ }
                    COORD_GCJ02 -> {
                        val wgs = MapUtils.gcj02towgs84(mCurLng, mCurLat)
                        mCurLng = wgs[0]; mCurLat = wgs[1]
                    }
                    else -> {
                        val wgs = MapUtils.bd2wgs(mCurLng, mCurLat)
                        mCurLng = wgs[0]; mCurLat = wgs[1]
                    }
                }
            }
            mCurAlt = intent.getDoubleExtra(LocationPickerActivity.ALT_MSG_ID, DEFAULT_ALT)
            val joystickEnabled = intent.getBooleanExtra(EXTRA_JOYSTICK_ENABLED, false)
            mSpeed = intent.getFloatExtra(EXTRA_ROUTE_SPEED, mSpeed.toFloat()).toDouble() / 3.6

            val routeArray = intent.getDoubleArrayExtra(EXTRA_ROUTE_POINTS)
            if (routeArray != null && routeArray.size >= 2) {
                mRouteEngine.setupFromArray(
                    routeArray,
                    coordType,
                    intent.getDoubleArrayExtra(EXTRA_ROUTE_WAIT_TIMES)
                )
                mRouteEngine.setLoop(intent.getBooleanExtra(EXTRA_ROUTE_LOOP, false))
                if (mRouteEngine.isActive) {
                    mCurLng = mRouteEngine.currentLng
                    mCurLat = mRouteEngine.currentLat
                    mCurBea = mRouteEngine.currentBea
                }
            }

            stepEnabled = intent.getBooleanExtra(EXTRA_STEP_ENABLED, stepEnabled)
            stepCadence = intent.getFloatExtra(EXTRA_STEP_FREQ, stepCadence)
            stepMode = intent.getIntExtra(EXTRA_STEP_MODE, stepMode)
            stepScheme = intent.getIntExtra(EXTRA_STEP_SCHEME, stepScheme)

            KailLog.i(this, TAG, "onStartCommand lat=$mCurLat lng=$mCurLng wifiOnly=$modeWifiOnly cellOnly=$modeCellOnly step=$stepEnabled spm=$stepCadence")

            val generation = ++startGeneration
            val rootControlSession = ROOT_CONTROL_SESSION_SEQ.incrementAndGet()
            activeRootControlSession = rootControlSession
            ROOT_CONTROL_ACTIVE_SESSION.set(rootControlSession)
            rootControlActive = false
            rootControlLatestWrite = null
            rootControlWriterScheduled = false
            if (this::mRootControlWriterHandler.isInitialized) {
                mRootControlWriterHandler.removeCallbacksAndMessages(null)
            }
            Thread({
                try {
                    // Shell/native setup can take a second or two on real devices.
                    // Keep it off the main thread so the Compose loading indicator
                    // does not freeze immediately after "Start".
                    ensureNativeHookOnce()
                    if (!isCurrentGeneration(generation)) return@Thread
                    val startupOk = startMockLocationOnInjection(generation)
                    // Step mock goes through the oem_location binder, which
                    // only exists after the inject above completes, so apply it
                    // here on the same bootstrap thread rather than on the main
                    // thread before the binder is online.
                    if (isCurrentGeneration(generation) && startupOk) {
                        applyStepSimulation()
                    }
                } catch (t: Throwable) {
                    KailLog.e(this, TAG, "bootstrap failed: ${t.message}", t)
                    if (isCurrentGeneration(generation)) {
                        broadcastStatusStopped()
                        android.os.Handler(mainLooper).post { stopSelf() }
                    }
                } finally {
                    if (generation == startGeneration) {
                        bootstrapInProgress = false
                    }
                }
            }, "ServiceGoRootBootstrap").start()

            runCatching {
                mJoystickViewModel.setCurrentPosition(mCurLng, mCurLat, mCurAlt)
                if (joystickEnabled) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                        if (mRouteEngine.isActive) mJoystickManager.showRouteControl(mSpeed * 3.6)
                        else mJoystickManager.show()
                    } else {
                        GoUtils.DisplayToast(applicationContext, getString(R.string.service_grant_overlay))
                    }
                } else {
                    mJoystickManager.hide()
                }
            }.onFailure { KailLog.e(this, TAG, "joystick show: ${it.message}") }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        KailLog.i(this, TAG, "onDestroy started")
        isRunning = false
        startGeneration++
        bootstrapInProgress = false
        val stoppingRootControlSession = activeRootControlSession
        activeRootControlSession = 0L
        if (stoppingRootControlSession > 0L) {
            ROOT_CONTROL_ACTIVE_SESSION.compareAndSet(stoppingRootControlSession, 0L)
        }
        runCatching {
            broadcastStatusStopped()
            isStop = true
            locationLoopStarted = false
            rootControlActive = false
            rootControlLatestWrite = null
            rootControlWriterScheduled = false
            disableLocationShm()
            if (this::mLocHandler.isInitialized) mLocHandler.removeCallbacksAndMessages(null)
            if (this::mLocHandlerThread.isInitialized) mLocHandlerThread.quitSafely()
            if (this::mRootControlWriterHandler.isInitialized) mRootControlWriterHandler.removeCallbacksAndMessages(null)
            closeRootControlFastShell("service destroying")
            if (this::mRootControlWriterThread.isInitialized) mRootControlWriterThread.quitSafely()
            if (this::mJoystickManager.isInitialized) mJoystickManager.destroy()
            mNotificationHelper.stopForeground()
        }.onFailure { KailLog.e(this, TAG, "onDestroy: ${it.message}") }

        Thread({
            runCatching {
                // Slow binder/provider cleanup must not run on the main thread.
                stopMockLocationOnInjection(retry = false, rootControlSession = stoppingRootControlSession)
                stopHideOnInjection(retry = false)
                RootDeployer.revokeMockLocationAppOps(applicationContext)
                recordCleanupState()
                if (nativeHookReady) {
                    runCatching { NativeSensorHook.nativeSetMocking(0) }
                    runCatching { NativeSensorHook.nativeSetStepSimEnabled(false) }
                    runCatching { NativeSensorHook.nativeSetRouteSimulation(false, 120f, 0) }
                    runCatching { NativeSensorHook.nativeReset() }
                }
            }.onFailure { KailLog.e(applicationContext, TAG, "background cleanup: ${it.message}") }
        }, "ServiceGoRootCleanup").start()

        super.onDestroy()
        KailLog.i(this, TAG, "onDestroy finished")
    }

    // ------------------------------------------------------------------
    // Control intent dispatcher
    // ------------------------------------------------------------------

    private fun handleControlAction(ctrl: String, intent: Intent) {
        when (ctrl) {
            CONTROL_PAUSE -> runCatching {
                isStop = true
                if (this::mJoystickManager.isInitialized) mJoystickManager.setRoutePauseState(true)
                broadcastStatus()
                KailLog.log(this, TAG, "Paused (isStop=true)", isHighFrequency = false)
            }.onFailure { KailLog.e(this, TAG, "Pause: ${it.message}") }

            CONTROL_RESUME -> runCatching {
                isStop = false
                lastRouteTickElapsedMs = SystemClock.elapsedRealtime()
                if (this::mJoystickManager.isInitialized) mJoystickManager.setRoutePauseState(false)
                if (locationLoopStarted && this::mLocHandler.isInitialized && !mLocHandler.hasMessages(HANDLER_MSG_ID)) {
                    mLocHandler.sendEmptyMessage(HANDLER_MSG_ID)
                }
                applyStepSimulation()
                broadcastStatus()
                KailLog.log(this, TAG, "Resumed (isStop=false)", isHighFrequency = false)
            }.onFailure { KailLog.e(this, TAG, "Resume: ${it.message}") }

            CONTROL_STOP -> runCatching {
                stopSelf()
                broadcastStatus()
            }.onFailure { KailLog.e(this, TAG, "stop: ${it.message}") }

            CONTROL_STOP_WIFI -> runCatching {
                // Stop only WiFi spoofing; leave any location/cell session intact.
                stopWifiMockOnInjection()
                modeWifiOnly = false
                pendingWifiList = emptyList()
                KailLog.i(this, TAG, "WiFi mock stopped via control")
                if (!isAnyMockActive()) stopSelf()
            }.onFailure { KailLog.e(this, TAG, "stop_wifi: ${it.message}") }

            CONTROL_STOP_CELL -> runCatching {
                // Stop only cell spoofing. Clearing the cell file (enabled=0) makes
                // system_server 的 applyCellMockConfig 复位 setMockCells/setSafeApps。
                modeCellOnly = false
                pendingCellList = emptyList()
                writeCellMockFile(emptyList())
                KailLog.i(this, TAG, "Cell mock stopped via control")
                if (!isAnyMockActive()) stopSelf()
            }.onFailure { KailLog.e(this, TAG, "stop_cell: ${it.message}") }

            CONTROL_SET_ALLOW_PACKAGES -> runCatching {
                val pkgs = intent.getStringArrayListExtra(EXTRA_ALLOW_PACKAGES) ?: arrayListOf()
                // Run off the main thread: resolving binders + injecting target
                // apps can block briefly.
                Thread({ applyAllowPackages(pkgs) }, "ServiceGoRootAllowPkgs").start()
            }.onFailure { KailLog.e(this, TAG, "set_allow_packages: ${it.message}") }

            CONTROL_SET_WIFI -> runCatching {
                // 模拟进行中动态更新 WiFi 列表（Picker 添加后立即生效）。
                val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(
                        EXTRA_WIFI_LIST, com.kail.location.models.WifiInfo::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<com.kail.location.models.WifiInfo>(EXTRA_WIFI_LIST)
                }
                if (list != null) {
                    pendingWifiList = list
                    if (list.isEmpty()) {
                        runCatching { writeWifiMockFile(emptyList()) }
                    } else {
                        // 重新下发文件 + Provider + binder，off main thread。
                        Thread({ applyWifiMockOnInjection() }, "ServiceGoRootSetWifi").start()
                    }
                    KailLog.i(this, TAG, "set_wifi: ${list.size} networks")
                }
            }.onFailure { KailLog.e(this, TAG, "set_wifi: ${it.message}") }

            CONTROL_SET_CELL -> runCatching {
                // 模拟进行中动态更新基站列表（页面内添加后立即生效）。
                val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(
                        EXTRA_CELL_LIST, com.kail.location.models.CellInfo::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<com.kail.location.models.CellInfo>(EXTRA_CELL_LIST)
                }
                if (list != null) {
                    pendingCellList = list
                    if (list.isEmpty()) {
                        writeCellMockFile(emptyList())
                    } else {
                        // 重新下发 Provider + 文件，off main thread。
                        Thread({ applyCellMockOnInjection() }, "ServiceGoRootSetCell").start()
                    }
                    KailLog.i(this, TAG, "set_cell: ${list.size} towers")
                }
            }.onFailure { KailLog.e(this, TAG, "set_cell: ${it.message}") }

            CONTROL_SET_HIDE -> runCatching {
                hideRootEnabled = intent.getBooleanExtra(EXTRA_HIDE_ROOT, false)
                hideAppListEnabled = intent.getBooleanExtra(EXTRA_HIDE_APPLIST, false)
                pendingHidePackages = intent.getStringArrayListExtra(EXTRA_HIDE_PACKAGES) ?: emptyList()
                // Resolving binders + injecting target apps can block briefly.
                Thread({ startHideOnInjection() }, "ServiceGoRootSetHide").start()
            }.onFailure { KailLog.e(this, TAG, "set_hide: ${it.message}") }

            CONTROL_STOP_HIDE -> runCatching {
                hideRootEnabled = false
                hideAppListEnabled = false
                // stopHideOnInjection 里全是阻塞的 root shell 调用（写配置 + 逐个
                // `am force-stop`）。onStartCommand 在主线程，同步执行会卡住 UI →
                // ANR，表现出来就是"点停止隐藏没反应"。放到后台线程执行。
                Thread({
                    runCatching { stopHideOnInjection() }
                        .onFailure { KailLog.e(this, TAG, "stopHideOnInjection: ${it.message}") }
                    pendingHidePackages = emptyList()
                    KailLog.i(this, TAG, "Hide stopped via control")
                    if (!isAnyMockActive()) stopSelf()
                }, "ServiceGoRootStopHide").start()
            }.onFailure { KailLog.e(this, TAG, "stop_hide: ${it.message}") }

            CONTROL_SEEK -> {
                val ratio = intent.getFloatExtra(EXTRA_SEEK_RATIO, 0f).coerceIn(0f, 1f)
                mRouteEngine.seekToRatio(ratio)
                mCurLng = mRouteEngine.currentLng
                mCurLat = mRouteEngine.currentLat
                mCurBea = mRouteEngine.currentBea
                updateJoystickStatus()
            }

            CONTROL_SET_SPEED -> {
                val kmh = intent.getFloatExtra(EXTRA_ROUTE_SPEED, (mSpeed * 3.6).toFloat())
                mSpeed = kmh.toDouble() / 3.6
            }

            CONTROL_SET_SPEED_FLUCTUATION -> {
                speedFluctuation = intent.getBooleanExtra(EXTRA_SPEED_FLUCTUATION, speedFluctuation)
            }

            CONTROL_APPEND_ROUTE -> runCatching {
                appendRouteFromControl(intent)
            }.onFailure { KailLog.e(this, TAG, "append_route: ${it.message}") }

            CONTROL_SET_STEP -> {
                stepEnabled = intent.getBooleanExtra(EXTRA_STEP_ENABLED, stepEnabled)
                stepCadence = intent.getFloatExtra(EXTRA_STEP_FREQ, stepCadence)
                stepMode = intent.getIntExtra(EXTRA_STEP_MODE, stepMode)
                stepScheme = intent.getIntExtra(EXTRA_STEP_SCHEME, stepScheme)
                val generation = startGeneration
                val rootControlSession = activeRootControlSession
                Thread({
                    ensureNativeHookOnce()
                    applyStepSimulation()
                    if (locationLoopStarted && !modeWifiOnly && !modeCellOnly) {
                        runCatching {
                            writeRootLocationControl(
                                true,
                                timeoutMs = 1500L,
                                generation = generation,
                                rootControlSession = rootControlSession
                            )
                        }
                            .onFailure { KailLog.e(this, TAG, "set step control-file write: ${it.message}") }
                    }
                }, "ServiceGoRootStepControl").start()
            }
        }
    }

    // ------------------------------------------------------------------
    // Native (in-app) sensor hook bridge
    // ------------------------------------------------------------------

    /**
     * One-shot best-effort initialization of the in-app NativeSensorHook
     * bindings against [RootDeployer.NATIVE_HOOK_SO].
     *
     * IMPORTANT — actual sensor hook installation is a no-op when run from
     * the controller app process. The `Dobby` hook installer in
     * native_hook/hook.cpp scans `/proc/self/maps` for libsensor.so /
     * libsensorservice.so and only patches them when they are mapped in the
     * caller. Those libraries live in system_server (and individual apps
     * that consume sensors), not in com.kail.location.
     *
     * For the hook to fire, the SO needs to be loaded into the target
     * process by an external loader — the Xposed companion already does
     * this from inside system_server (see [com.kail.locationxposed]); the
     * FakeLocation injection chain (libfakeloc_init.so / libStepSensor.so)
     * would do it post-injection but we never auto-run kail_inject because
     * it freezes system_server on production ROMs.
     *
     * This method therefore initializes the JNI side so step parameters can
     * be plumbed in, but logs a clear notice that no hook is active from
     * this process. The bool it returns is "JNI bindings loaded", not "hook
     * installed". When the SO is loaded in another process via Xposed/
     * Zygisk, that process picks up its own copy and the hook does install.
     */
    private fun ensureNativeHookOnce(): Boolean {
        if (nativeHookReady) return true
        if (nativeHookAttempted) return false
        nativeHookAttempted = true

        if (!ShellUtils.hasRoot()) {
            KailLog.w(this, TAG, "no root; skipping native hook deploy")
            return false
        }

        runCatching { RootDeployer.deployNativeHookLib(this) }
            .onFailure { KailLog.e(this, TAG, "deployNativeHookLib: ${it.message}") }

        // Probe sensor offsets from libsensor.so / libsensorservice.so on
        // disk so a future loader can pick them up via the JNI setters.
        val (writeOffset, convertOffset) = getOffsetsFromSystem()

        // Drop the probed offsets where the system_server-side NativeStepHook
        // (loaded by the inject) can read them. libkail_native_hook.so installs
        // the Dobby hook on libsensorservice.so::convertToSensorEvent inside
        // system_server using convert_to_sensor_event; send_objects is for
        // libsensor.so (only present in app processes) so may be unused here.
        runCatching {
            val conf = "send_objects=$writeOffset\nconvert_to_sensor_event=$convertOffset\n"
            val f = "/data/local/kail-lib/kail_sensor_offsets.txt"
            ShellUtils.executeCommand("echo '$conf' > $f")
            ShellUtils.executeCommand("chmod 644 $f")
            ShellUtils.executeCommand("chcon u:object_r:system_file:s0 $f")
            KailLog.i(this, TAG, "wrote sensor offsets: write=$writeOffset convert=$convertOffset")
        }.onFailure { KailLog.e(this, TAG, "write sensor offsets: ${it.message}") }

        nativeHookReady = false
        KailLog.i(
            this,
            TAG,
            "NativeSensorHook staged for injected system_server only; app-process native hook disabled. offsets=$writeOffset/$convertOffset"
        )
        return false
    }

    private fun applyStepSimulation() {
        if (stepEnabled && !rootControlActive && !modeWifiOnly && !modeCellOnly) {
            KailLog.w(this, TAG, "step mock deferred: root location control is not active")
            return
        }

        // Step mock 走控制文件通道：step_enabled/step_spm/step_mode/step_scheme
        // 由 buildRootLocationControlContent 写入，system_server 的
        // RootLocationControl.applyStepControl 直接驱动 MockStepSensorManager，
        // 不再依赖被 SELinux 拦截的 oem_location binder。

        // Secondary best-effort path — the in-app NativeSensorHook. Only does
        // anything when the SO is loaded into the consuming process (Xposed/
        // Zygisk); a no-op from the controller process. Kept for parity with
        // ServiceGoXposed.
        if (!nativeHookReady) return
        runCatching {
            if (stepEnabled) {
                NativeSensorHook.nativeSetRouteSimulation(true, stepCadence, stepMode)
                NativeSensorHook.nativeSetGaitParams(stepCadence, stepMode, stepScheme, true)
                NativeSensorHook.nativeSetStepSimEnabled(true)
                NativeSensorHook.nativeSetMocking(1)
            } else {
                NativeSensorHook.nativeSetStepSimEnabled(false)
                NativeSensorHook.nativeSetMocking(0)
                NativeSensorHook.nativeSetRouteSimulation(false, stepCadence, stepMode)
                NativeSensorHook.nativeSetGaitParams(stepCadence, stepMode, stepScheme, false)
                NativeSensorHook.nativeReset()
            }
        }.onFailure { KailLog.e(this, TAG, "applyStepSimulation (native): ${it.message}") }
    }

    // ------------------------------------------------------------------
    // Mock-location bridge
    //
    // Two backends are tried in order:
    //  1. The FakeLocation injection layer (preferred) — only available
    //     after RootDeployer.ensureBaseline has run kail_inject on
    //     system_server and the binder service "oem_location" is
    //     registered.  This is the original FakeLocation hook path.
    //  2. Standard Android test-provider via [MockLocationProvider] — same
    //     code Developer mode uses.  Always works once the AppOps grant
    //     lands; harmless if the inject path also runs.
    // ------------------------------------------------------------------

    private fun isCurrentGeneration(generation: Int): Boolean {
        return isRunning && generation == startGeneration
    }

    private fun shouldAbortBootstrap(generation: Int, diag: SimulationDiagnostics? = null): Boolean {
        if (isCurrentGeneration(generation)) return false
        diag?.warn("启动已取消", "服务已停止或已有新的启动请求，停止旧后台初始化")
        KailLog.i(this, TAG, "bootstrap cancelled: generation=$generation current=$startGeneration running=$isRunning")
        return true
    }

    /**
     * Push the independent-mode target-app allow-list into the FakeLocation
     * injection layer. With a non-empty list, location/GNSS/WiFi/cell mocking
     * only affects those packages (FakeLocation's setAllowMockPackages /
     * MockWifiConfigManager.setAllowMockPackages); every other app reads real
     * data. An empty list clears the restriction (mock for all apps).
     *
     * Called on every mock start so the latest selection always applies, and
     * directly from the CONTROL_SET_ALLOW_PACKAGES action so toggling
     * independent mode takes effect live.
     */
    private fun applyAllowPackages(pkgs: List<String>) {
        val list = ArrayList(pkgs)
        // 文件通道（主通道）：SELinux Enforcing 下 oem_location/oem_wifi binder
        // 注册与 find 均被拦截，白名单通过 /data/kail-loc/allow_mock_packages.txt
        // 被 system_server 内的 RootLocationControl 轮询并直接 setAllowMockPackages。
        writeAllowMockPackagesFile(list)
        KailLog.i(this, TAG, "allowMockPackages applied: ${if (list.isEmpty()) "<all apps>" else list.joinToString()}")

        // Client-side location/cell mirrors install in the target process.
        // Server-side hooks already filter by caller, but app-hook injection is
        // still needed for process-local mirrors.
        if (list.isNotEmpty()) {
            Thread({
                for (pkg in list) {
                    runCatching { RootDeployer.injectAppProcess(applicationContext, pkg) }
                        .onFailure { KailLog.w(this, TAG, "inject $pkg: ${it.message}") }
                }
            }, "ServiceGoRootTargetInject").start()
        }
    }

    /**
     * 把独立模拟白名单写进文件通道 /data/kail-loc/allow_mock_packages.txt。
     * system_server 内的 RootLocationControl 每 250ms 轮询该文件并直接
     * setAllowMockPackages，绕过 SELinux 对 oem_location/oem_wifi binder 的拦截。
     * 空列表写 enabled=0（= 对所有应用生效）；与 hide_config 写入方式完全一致。
     */
    private fun writeAllowMockPackagesFile(pkgs: List<String>) {
        val content = if (pkgs.isEmpty()) {
            "enabled=0\n"
        } else {
            "enabled=1\npackages=${pkgs.joinToString(",")}\n"
        }
        // Provider 通道（主）：system_server 直接 call 取；文件通道兜底。
        runCatching { LocationShmProvider.setConfig(LocationShm.PROVIDER_KEY_ALLOW_CONFIG, content) }
            .onFailure { KailLog.e(this, TAG, "setAllowConfig(provider): ${it.message}") }
        val cmd = "mkdir -p /data/kail-loc && chmod 777 /data/kail-loc && " +
            "printf '%s' ${shellSingleQuote(content)} > /data/kail-loc/allow_mock_packages.txt && chmod 644 /data/kail-loc/allow_mock_packages.txt"
        runCatching { ShellUtils.executeCommand(cmd) }
            .onFailure { KailLog.e(this, TAG, "writeAllowMockPackagesFile: ${it.message}") }
        KailLog.i(this, TAG, "allow mock packages written: enabled=${if (pkgs.isEmpty()) 0 else 1} pkgs=${if (pkgs.isEmpty()) "-" else pkgs.joinToString()} (provider+file)")
    }

    // ------------------------------------------------------------------
    // Root / app-list hiding bridge (oem_integrity)
    //
    // Drives the "Root与应用隐藏" screen. The FakeLocation injection layer
    // registers an IHideRootManager under "oem_integrity"; the per-app
    // RootHideHook / LAntiDetect (installed via AppProcessHook.applyHookToApp)
    // gate entirely on this binder:
    //   isHideRootEnabled()      -> master switch (refreshHideRootEnabled gates
    //                               it on a usable license)
    //   getHiddenPackages()      -> packages the hide applies to (scope "k")
    //   isHideAppListEnabled()   -> also hide the installed-app list (scope "n")
    //
    // Hooks only install in a process AFTER it has been app-hook-injected, so
    // we inject every selected package once the config is pushed.
    // ------------------------------------------------------------------

    /**
     * 把隐藏配置写进文件通道（/data/kail-loc/hide_config.txt）。
     *
     * 目标进程的 Hook（HideRootServiceManager#isHideRootEnabled 等）在解析不到
     * oem_integrity binder（SELinux Enforcing 下 find 被拦截）时会直接读这个文件，
     * 效果与 binder 推送等价：enabled 决定是否隐藏，packages 是目标白名单，
     * hide_app_list 决定是否连已安装列表一起藏。关闭时写 enabled=0。
     */
    private fun writeHideConfigFile(enabled: Boolean) {
        val content = if (enabled) {
            "enabled=1\n" +
                "hide_app_list=${if (hideAppListEnabled) 1 else 0}\n" +
                "packages=${pendingHidePackages.joinToString(",")}\n"
        } else {
            "enabled=0\n"
        }
        val cmd = "mkdir -p /data/kail-loc && chmod 777 /data/kail-loc && " +
            "printf '%s' ${shellSingleQuote(content)} > ${HideConfigFile.PATH} && chmod 644 ${HideConfigFile.PATH}"
        runCatching { ShellUtils.executeCommand(cmd) }
            .onFailure { KailLog.e(this, TAG, "writeHideConfigFile: ${it.message}") }
        // Provider 通道（主）：目标进程的 HideConfigFile 会优先从 Provider 读。
        runCatching { LocationShmProvider.setConfig(LocationShm.PROVIDER_KEY_HIDE_CONFIG, content) }
            .onFailure { KailLog.e(this, TAG, "setHideConfig(provider): ${it.message}") }
        KailLog.i(this, TAG, "hide config written: enabled=$enabled pkgs=${if (enabled) pendingHidePackages.joinToString() else "-"} (provider+file)")
    }

    /**
     * 隐藏应用列表的文件通道：写 /data/kail-loc/antidetect_config.txt。
     *
     * system_server 内 InjectDex 的 KailAntiDetectConfig 轮询器读这个文件来安装
     * PackageManagerServiceHook 并配置过滤。detected 用隐藏白名单里每一个包：
     * 对这些"隐藏目标"（如沙盒）过滤掉别的应用；同时把目标写进 target，让
     * PackageAntiDetectionConfig.isPackageAllowedForPidUid 对沙盒返回 true，
     * 钩子才会对沙盒生效。
     */
    private fun writeAntiDetectConfigFile(enabled: Boolean) {
        val content = if (enabled) {
            // detected_packages = "要从目标应用的列表里移除的包"，即**其它已安装应用**。
            // 绝不能填目标应用自身：PackageManagerServiceHook 会把 detected 包从
            // getInstalledPackages/queryIntentActivities 结果里移除，RuntimeAntiDetectionHook
            // 还会把 detected 当文件路径片段隐藏文件。填成目标应用会导致它把自己藏掉——
            // 连自己的 APK 都读不到，直接 ClassNotFoundException 打不开。
            val hiddenPackages = collectOtherInstalledPackages()
            "hook_enabled=1\n" +
                "filter_enabled=1\n" +
                "visibility_filter=1\n" +
                "detected_packages=${hiddenPackages.joinToString(",")}\n" +
                "target_packages=${pendingHidePackages.joinToString(",")}\n"
        } else {
            "hook_enabled=0\n"
        }
        val cmd = "mkdir -p /data/kail-loc && chmod 777 /data/kail-loc && " +
            "printf '%s' ${shellSingleQuote(content)} > /data/kail-loc/antidetect_config.txt && chmod 644 /data/kail-loc/antidetect_config.txt"
        runCatching { ShellUtils.executeCommand(cmd) }
            .onFailure { KailLog.e(this, TAG, "writeAntiDetectConfigFile: ${it.message}") }
        // Provider 通道（主）：InjectDex 的轮询器会优先从 Provider 读。
        runCatching { LocationShmProvider.setConfig(LocationShm.PROVIDER_KEY_ANTIDETECT_CONFIG, content) }
            .onFailure { KailLog.e(this, TAG, "setAntiDetectConfig(provider): ${it.message}") }
        KailLog.i(this, TAG, "antidetect config written: enabled=$enabled targets=${if (enabled) pendingHidePackages.joinToString() else "-"} (provider+file)")
    }

    /**
     * "隐藏应用列表"要藏掉的包 = 本机已安装的**第三方应用**，去掉目标应用自身。
     * 这些包会被 PackageManagerServiceHook 从目标应用的 getInstalledPackages /
     * queryIntentActivities 等结果里移除，从而对目标应用隐藏"其它应用"。
     */
    private fun collectOtherInstalledPackages(): List<String> {
        return runCatching {
            val targets = pendingHidePackages.toSet()
            packageManager.getInstalledApplications(0)
                .asSequence()
                .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
                .map { it.packageName }
                .filter { it.isNotEmpty() && it !in targets }
                .distinct()
                .toList()
        }.getOrElse {
            KailLog.w(this, TAG, "collectOtherInstalledPackages failed: ${it.message}")
            emptyList()
        }
    }

    /**
     * Stage the inject (so oem_integrity exists), push the hide config,
     * and inject every target app process so RootHideHook installs there.
     */
    private fun startHideOnInjection() {
        runCatching { RootDeployer.ensureBaseline(this) }
            .onFailure { KailLog.e(this, TAG, "RootDeployer.ensureBaseline (hide): ${it.message}") }

        // 文件通道：无论 oem_integrity binder 是否可解析，都先把配置写进
        // /data/kail-loc/hide_config.txt。目标进程的 Hook（HideRootServiceManager）
        // 在 binder 不可用时直接读该文件判断开关，绕开 SELinux 对 find 的拦截。
        writeHideConfigFile(hideRootEnabled && pendingHidePackages.isNotEmpty())
        // 隐藏应用列表的文件通道：system_server 内的轮询器读
        // /data/kail-loc/antidetect_config.txt 来安装 PackageManagerServiceHook。
        writeAntiDetectConfigFile(hideAppListEnabled && hideRootEnabled && pendingHidePackages.isNotEmpty())

        // Provider + 文件通道已下发（hide_config / antidetect_config）。
        // 目标进程的 RootHideHook / PackageManagerServiceHook 会读取；
        // 然后注入目标进程让 Hook 装上。
        if (hideRootEnabled) {
            for (pkg in pendingHidePackages) {
                runCatching { RootDeployer.injectAppProcess(applicationContext, pkg) }
                    .onFailure { KailLog.e(this, TAG, "inject $pkg (hide): ${it.message}") }
            }
        }
    }

    private fun stopHideOnInjection(retry: Boolean = true) {
        writeHideConfigFile(false)
        writeAntiDetectConfigFile(false)
        val pkgsToRestart = pendingHidePackages.toList()
        if (pkgsToRestart.isNotEmpty()) {
            pkgsToRestart.forEach { pkg ->
                if (pkg.matches(Regex("[A-Za-z0-9_.]+"))) {
                    runCatching { ShellUtils.executeCommand("am force-stop $pkg") }
                        .onFailure { KailLog.w(this, TAG, "force-stop $pkg: ${it.message}") }
                }
            }
            KailLog.i(this, TAG, "hide target processes force-stopped for clean property state: ${pkgsToRestart.joinToString()}")
        }
    }

    private fun startMockLocationOnInjection(generation: Int): Boolean {
        // 模拟启动诊断：把每一步前置条件 + 最终判定写成一整块报告。
        // Logcat 始终输出；文件导出仍遵循日志开关，避免关闭日志时持续写盘。
        val scenario = when {
            modeWifiOnly -> "wifi"
            modeCellOnly -> "cell"
            mRouteEngine.isActive -> "route"
            else -> "location"
        }
        val diag = SimulationDiagnostics.begin(this, mode = "root", scenario = scenario)
        if (shouldAbortBootstrap(generation, diag)) {
            diag.finish()
            return false
        }

        // Sample system_server PID BEFORE injection so we can detect a
        // watchdog-induced restart (a crashed inject changes the PID).
        val ssPidBefore = diag.sampleSystemServerPid("注入前")

        // 注入崩溃哨兵布防：注入若把 system_server 搞崩导致整机重启，下面的诊断
        // 报告 finish() 将永远跑不到、报告丢失。所以先同步写一条「待确认」记录，
        // 下次启动 InjectionCrashSentinel.checkAndReport 即可跨重启确证「上次开始
        // 模拟把系统搞崩了」。注入确认健康后再撤防。
        InjectionCrashSentinel.arm(scenario, Build.VERSION.SDK_INT, "${Build.MANUFACTURER} ${Build.MODEL}")

        // Stage the FakeLocation toolchain on disk, run kail_inject (with the
        // 5s watchdog), and grant mock_location AppOps.  RootDeployer is
        // idempotent and never re-runs the inject if the binder is already
        // registered.
        val injected = runCatching { RootDeployer.ensureBaselineDiagnosed(this, diag) }
            .getOrElse {
                KailLog.e(this, TAG, "RootDeployer.ensureBaseline: ${it.message}")
                diag.error("注入引导", it)
                false
            }
        if (shouldAbortBootstrap(generation, diag)) {
            diag.finish()
            return false
        }

        // Fold the native LHooker ArtMethod probe results into THIS diagnostic
        // block (instead of scattering them via separate log lines), and detect
        // whether the inject restarted system_server.
        // 昂贵的诊断镜像（cat /data/system 下的 init/logcat 抓取）各自都要重新起一次
        // su（约 200ms+），非调试模式下跳过，避免拖慢「开始模拟」的转圈；
        // crash 检测用的 pgrep 采样与注入状态小文件仍保留。
        val debugEnabled = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(SettingsViewModel.KEY_DEBUG_LOG_ENABLED, false)
        if (debugEnabled) {
            diag.recordLoaderTrace(mirrorFakelocInitLog())
            diag.recordNativeProbe(mirrorLHookerInitLog())
        }
        diag.recordBootstrapState(mirrorInjectDexState())
        val ssPidAfter = diag.sampleSystemServerPid("注入后")
        diag.checkSystemServerStable(ssPidBefore, ssPidAfter)
        if (debugEnabled) {
            diag.recordInjectedLogcat(mirrorInjectedLogcat(ssPidAfter))
        }

        // 走到这里说明 app 进程没被注入崩溃带走（system_server 至少还能响应 pgrep）。
        // 撤防哨兵：本次注入没有立即把整机搞崩。
        InjectionCrashSentinel.disarm()
        if (shouldAbortBootstrap(generation, diag)) {
            diag.finish()
            return false
        }

        // WiFi-only / cell-only modes must NOT turn on GNSS satellite
        // mocking, and WiFi-only must not start location mocking at all.
        // They still need the inject to have run (so the oem_wifi /
        // oem_location binders exist). We route the selected
        // networks into the FakeLocation injection layer below.
        if (modeWifiOnly) {
            // WiFi spoofing 完全独立于位置模拟：WifiServiceHook 只看
            // MockWifiConfigManager.isMockWifiEnabled()。App 只负责经 Provider +
            // 文件通道下发选中的 WiFi 网络，由 system_server 直接刷入。
            val wifiPushed = runCatching { applyWifiMockOnInjection(); true }.getOrDefault(false)
            diag.step("下发 WiFi 模拟", wifiPushed)
            applyAllowPackages(independentAllowPackages)
            KailLog.i(this, TAG, "wifiOnly: WiFi networks pushed, location+GNSS skipped")
            diag.verdict(wifiPushed, "仅 WiFi 模拟")
            diag.finish()
            return wifiPushed
        }

        if (modeCellOnly) {
            // 基站模拟经 TelephonyRegistryHook / PhoneInterfaceManagerHook，要求
            // MockLocationHookManager.isMocking()==true（由 system_server 的
            // applyCellMockConfig 置位）。App 负责下发配置 + 注入 com.android.phone。
            val cellPushed = runCatching { applyCellMockOnInjection(); true }.getOrDefault(false)
            diag.step("下发基站模拟", cellPushed)
            applyAllowPackages(independentAllowPackages)
            KailLog.i(this, TAG, "cellOnly: cell towers pushed, GNSS skipped")
            diag.verdict(cellPushed, "仅基站模拟")
            diag.finish()
            return cellPushed
        }

        // On Android 16 / OnePlus, system_server is allowed to run our injected
        // code but SELinux denies dynamic ServiceManager.addService(), so the
        // oem_location binder never appears. Use the control file as the primary
        // root-mode location path and keep the binder path only as an optional
        // compatibility bridge for WiFi/cell/older ROMs.
        val controlAck = if (injected) runCatching {
            writeAndWaitRootLocationControl(
                content = buildRootLocationControlContent(true),
                generation = generation
            )
        }.getOrElse {
            KailLog.e(this, TAG, "control-file mock: ${it.message}")
            diag.error("启动控制文件模拟", it)
            null
        } else null
        val controlOk = controlAck?.isAppliedFor(mCurLat, mCurLng) == true
        rootControlActive = controlOk && isCurrentGeneration(generation)
        val controlDetail = when {
            !injected -> "注入未成功，无法走控制文件"
            controlOk -> "system_server 已应用控制文件：${controlAck?.summary()}"
            controlAck != null -> "system_server 控制线程返回异常：${controlAck.summary()}"
            else -> "未收到 system_server 控制线程 ack（控制线程可能未启动或 Hook 初始化失败）"
        }
        if (controlOk) {
            KailLog.i(this, TAG, "control-file mock-location active lat=$mCurLat lng=$mCurLng ack=${controlAck?.summary()}")
        }
        diag.step("控制文件模拟", controlOk, controlDetail)
        val stepControlOk = !stepEnabled || (controlAck?.stepMocking == true && controlAck.stepHookInstalled == true)
        if (stepEnabled) {
            val stepDetail = when {
                !controlOk -> "位置控制未生效，步频未下发"
                controlAck?.stepMocking == true && controlAck.stepHookInstalled == true ->
                    "system_server 已启动全局步频模拟：spm=${controlAck.stepSpm ?: stepCadence} " +
                        "status=${controlAck.stepStatus ?: "running"} " +
                        "synth=${controlAck.stepSynthEvents ?: 0} " +
                        "send=${controlAck.stepSendHook ?: false} " +
                        "convert=${controlAck.stepConvertHook ?: false} " +
                        "handles=${controlAck.stepCounterHandle ?: -1},${controlAck.stepDetectorHandle ?: -1}"
                else ->
                    "未完全生效：status=${controlAck.stepStatus ?: "?"} mocking=${controlAck.stepMocking ?: false} " +
                        "hook=${controlAck.stepHookInstalled ?: false} " +
                        "send=${controlAck.stepSendHook ?: false} convert=${controlAck.stepConvertHook ?: false} " +
                        "${controlAck.stepError?.let { " error=$it" } ?: ""}"
            }
            diag.step("步频模拟", stepControlOk, stepDetail)
        }
        diag.info("数据通道", "Provider(shm/get_config) + 文件通道（oem_* binder 已弃用）")
        val startupOk = controlOk && stepControlOk
        diag.verdict(startupOk,
            when {
                !controlOk -> "ROOT 注入未生效，未启用测试Provider，避免真实定位拉回/被检测"
                stepEnabled && !stepControlOk -> "位置模拟生效，但全局步频 hook 未启动"
                stepEnabled -> "FakeLocation 控制文件路径模拟生效，全局步频模拟已启动"
                else -> "FakeLocation 控制文件路径模拟生效"
            })
        diag.finish()
        // 每次位置模拟启动都重推独立模拟白名单（文件通道 + binder），保证 prefs
        // 中的最新选择生效，也不依赖上次会话的遗留文件状态。
        if (controlOk && isCurrentGeneration(generation) && !modeWifiOnly && !modeCellOnly) {
            applyAllowPackages(independentAllowPackages)
            prepareLocationShm()
            startLocationLoop()
        }

        if (!controlOk) runCatching {
            if (isCurrentGeneration(generation) && !modeWifiOnly && !modeCellOnly) {
                broadcastStatusStopped()
                android.os.Handler(mainLooper).post {
                    GoUtils.DisplayToast(applicationContext,
                        getString(R.string.service_root_fallback_noroot))
                    stopSelf()
                }
            }
        }
        return startupOk
    }

    /**
     * Read the native LHooker init diagnostics that the hook engine dropped at
     * /data/kail-loc/lhooker_init.log (it runs in system_server and can't write
     * to the app's scoped-storage log dir) and mirror them into the app's
     * exportable KailLog. Each injection appends a fresh "===== LHooker init"
     * block; we surface the most recent one so the auto-detected ArtMethod
     * layout is visible when troubleshooting.
     *
     * @return the lines of the most recent init block (empty if none), so the
     *         caller can fold them into the SimulationDiagnostics report.
     */
    private fun mirrorLHookerInitLog(): List<String> {
        return runCatching {
            // Always use su here. /data/system may be non-traversable to the
            // app UID even when the log file itself has permissive bits.
            val text = ShellUtils.executeCommand(
                "cat $ROOT_RUNTIME_DIR/lhooker_init.log 2>/dev/null || " +
                    "cat /data/kail-loc/lhooker_init.log 2>/dev/null || " +
                    "cat /data/local/kail-lib/lhooker_init.log 2>/dev/null"
            )
            if (text.isNullOrBlank()) {
                KailLog.i(this, TAG, "LHooker init log not found yet")
                return@runCatching emptyList<String>()
            }
            // Keep only the last init block to avoid replaying stale sessions.
            val lastBlock = text.trim().split("===== LHooker init")
                .lastOrNull { it.isNotBlank() }
                ?.let { "===== LHooker init$it" }
                ?: text.trim()
            val lines = lastBlock.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotBlank() }
                .toList()
            lines.forEach { KailLog.i(this, TAG, "[native] $it") }
            lines
        }.getOrElse {
            KailLog.w(this, TAG, "mirrorLHookerInitLog: ${it.message}")
            emptyList()
        }
    }

    /** Read libfakeloc_init.so's native loader trace from system_server. */
    private fun mirrorFakelocInitLog(): List<String> {
        return runCatching {
            val out = ShellUtils.executeCommand(
                "cat $ROOT_RUNTIME_DIR/fakeloc_init.log 2>/dev/null || " +
                    "cat /data/kail-loc/fakeloc_init.log 2>/dev/null"
            ).trim()
            if (out.isBlank()) {
                KailLog.w(this, TAG, "fakeloc init log not found yet")
                return@runCatching emptyList<String>()
            }
            out.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotBlank() }
                .toList()
                .also { lines -> lines.forEach { KailLog.i(this, TAG, "[fakeloc-init] $it") } }
        }.getOrElse {
            KailLog.w(this, TAG, "mirrorFakelocInitLog: ${it.message}")
            emptyList()
        }
    }

    /** Read InjectDex.init state that Java writes from inside system_server. */
    private fun mirrorInjectDexState(): List<String> {
        return runCatching {
            val out = ShellUtils.executeCommand("cat $ROOT_INJECTDEX_STATE 2>/dev/null").trim()
            if (out.isBlank()) {
                KailLog.w(this, TAG, "InjectDex state not found yet")
                return@runCatching emptyList<String>()
            }
            out.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotBlank() }
                .toList()
                .takeLast(80)
                .also { lines -> lines.forEach { KailLog.i(this, TAG, "[injectdex-state] $it") } }
        }.getOrElse {
            KailLog.w(this, TAG, "mirrorInjectDexState: ${it.message}")
            emptyList()
        }
    }

    /** Mirror system_server-side injected Java/native logcat lines into the diagnostic block. */
    private fun mirrorInjectedLogcat(systemServerPid: String): List<String> {
        return runCatching {
            val pidArg = systemServerPid.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
            val pidFilter = if (pidArg.matches(Regex("\\d+"))) "--pid=$pidArg" else ""
            val out = ShellUtils.executeCommand(
                "logcat -d -v threadtime -t 300 $pidFilter " +
                    "KailLog/InjectDex:I KailLog/RootLocationControl:I KailLog/ServiceManagerBridge:E " +
                    "KailLog/NativeStepHook:I KailLog/MockStepSensor:I KailLog/LHooker:I " +
                    "NativeSensorHook.native:I MSU:I LINJECT.native:I LINJECT/Injector:I LHooker.Native:I *:S 2>/dev/null"
            ).trim()
            if (out.isBlank()) {
                KailLog.i(this, TAG, "injected logcat not found yet")
                return@runCatching emptyList<String>()
            }
            val lines = out.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotBlank() }
                .toList()
                .takeLast(80)
            lines.forEach { KailLog.i(this, TAG, "[inject-logcat] $it") }
            lines
        }.getOrElse {
            KailLog.w(this, TAG, "mirrorInjectedLogcat: ${it.message}")
            emptyList()
        }
    }

    private fun stopMockLocationOnInjection(retry: Boolean = true, rootControlSession: Long = activeRootControlSession) {
        rootControlActive = false
        disableLocationShm()
        // 控制文件 enabled=0：system_server 的 RootLocationControl.apply 会停止
        // MockLocationHookManager 并复位所有开关（位置/基站/GNSS/白名单）。
        runCatching { writeRootLocationControl(false, rootControlSession = rootControlSession) }
        runCatching { stopWifiMockOnInjection(retry) }
        // 文件通道兜底：清空 WiFi 模拟与基站模拟文件（enabled=0），供下次会话干净起步。
        runCatching { writeCellMockFile(emptyList()) }
        runCatching { mMockLocationProvider.cleanup() }
            .onFailure { KailLog.e(this, TAG, "cleanup providers: ${it.message}") }
    }

    // ------------------------------------------------------------------
    // WiFi spoofing bridge（Provider + 文件通道）
    //
    // oem_wifi binder 被 SELinux 拦截，已弃用。App 把 WiFi 网络列表写进
    // LocationShmProvider.get_config(wifi_config)（主）+ mock_wifi.txt（兜底），
    // system_server 的 RootLocationControl 取回后刷入 MockWifiConfigManager。
    // ------------------------------------------------------------------

    /**
     * True if any spoofing surface is still active (location loop, WiFi, or
     * cell). Used by the granular stop_wifi / stop_cell control actions to
     * decide whether the whole foreground service can shut down.
     */
    private fun isAnyMockActive(): Boolean {
        if (locationLoopStarted) return true
        if (pendingWifiList.isNotEmpty()) return true
        if (pendingCellList.isNotEmpty()) return true
        if (hideRootEnabled && pendingHidePackages.isNotEmpty()) return true
        return false
    }

    /**
     * 把 WiFi 模拟列表写进 Provider（主）+ 文件通道 /data/kail-loc/mock_wifi.txt。
     * system_server 内的 RootLocationControl 取回后直接刷入 MockWifiConfigManager。
     * 空列表写 enabled=0（= 停止 WiFi 模拟）。
     */
    private fun writeWifiMockFile(list: List<com.kail.location.models.WifiInfo>) {
        val sb = StringBuilder()
        if (list.isEmpty()) {
            sb.append("enabled=0\n")
        } else {
            sb.append("enabled=1\n")
            list.forEachIndexed { index, wifi ->
                if (index > 0) sb.append("\n")
                sb.append("ssid=${wifi.ssid}\n")
                sb.append("bssid=${wifi.bssid}\n")
                sb.append("rssi=${wifi.rssi}\n")
                sb.append("link_speed=${wifi.linkSpeed}\n")
                sb.append("frequency=${wifi.frequency}\n")
                sb.append("capabilities=${wifi.capabilities}\n")
            }
        }
        writeMockConfigFile("/data/kail-loc/mock_wifi.txt", sb.toString(), "wifi")
    }

    /**
     * 把基站模拟列表写进文件通道 /data/kail-loc/mock_cell.txt。
     * system_server 内的 RootLocationControl 轮询该文件并直接刷入
     * MockLocationHookManager.setMockCells，绕过 SELinux 对 oem_location binder 的拦截。
     * 空列表写 enabled=0（= 停止基站模拟）。
     */
    private fun writeCellMockFile(list: List<com.kail.location.models.CellInfo>) {
        val sb = StringBuilder()
        if (list.isEmpty()) {
            sb.append("enabled=0\n")
        } else {
            sb.append("enabled=1\n")
            list.forEachIndexed { index, cell ->
                if (index > 0) sb.append("\n")
                sb.append("radio_type=${cell.networkType}\n")
                sb.append("mcc=${cell.mcc}\n")
                sb.append("mnc=${cell.mnc}\n")
                sb.append("lac=${cell.lac}\n")
                sb.append("psc=${cell.psc}\n")
                sb.append("cell_id=${cell.cid}\n")
                sb.append("lat=${cell.latitude}\n")
                sb.append("lng=${cell.longitude}\n")
                sb.append("accuracy=${cell.radius}\n")
            }
        }
        writeMockConfigFile("/data/kail-loc/mock_cell.txt", sb.toString(), "cell")
    }

    private fun writeMockConfigFile(path: String, content: String, what: String) {
        // Provider 通道优先：system_server 直接 ContentResolver.call 取配置，
        // 不依赖 su 写文件与 SELinux 文件权限。文件通道保留为兜底。
        runCatching {
            when (what) {
                "wifi" -> LocationShmProvider.setWifiConfig(content)
                "cell" -> LocationShmProvider.setCellConfig(content)
            }
        }.onFailure { KailLog.e(this, TAG, "setProviderConfig($what): ${it.message}") }
        val cmd = "mkdir -p /data/kail-loc && chmod 777 /data/kail-loc && " +
            "printf '%s' ${shellSingleQuote(content)} > $path && chmod 644 $path"
        runCatching { ShellUtils.executeCommand(cmd) }
            .onFailure { KailLog.e(this, TAG, "write${what}MockFile: ${it.message}") }
        KailLog.i(this, TAG, "$what mock file written: ${content.lineSequence().firstOrNull() ?: ""} (${content.length}b)")
    }

    private fun applyWifiMockOnInjection() {
        if (pendingWifiList.isEmpty()) {
            KailLog.w(this, TAG, "applyWifiMockOnInjection: no WiFi networks selected")
            return
        }
        // Provider + 文件通道（主通道）：system_server 的 RootLocationControl 消费后
        // 直接刷入 MockWifiConfigManager，不再依赖被 SELinux 拦截的 oem_wifi binder。
        writeWifiMockFile(pendingWifiList)
        KailLog.i(this, TAG, "WiFi mock applied: ${pendingWifiList.size} networks (provider+file)")
    }

    private fun stopWifiMockOnInjection(retry: Boolean = true) {
        // 清空 /data/kail-loc/mock_wifi.txt（enabled=0）+ Provider 同步禁用。
        writeWifiMockFile(emptyList())
    }

    // ------------------------------------------------------------------
    // Cell-tower spoofing bridge (oem_location.setMockCells)
    //
    // TelephonyRegistryHook only feeds the spoofed towers when
    // MockLocationHookManager.isMocking() is true, so cell mode arms location
    // mocking + seeds a base fix from the first cell's coordinates, but keeps
    // GNSS satellite mocking OFF and never runs the moving-location loop.
    // App 只负责经 Provider + 文件通道下发小区列表。
    // ------------------------------------------------------------------

    private fun applyCellMockOnInjection() {
        if (pendingCellList.isEmpty()) {
            KailLog.w(this, TAG, "applyCellMockOnInjection: no cells selected")
            return
        }
        // Provider + 文件通道（主通道）：system_server 的 RootLocationControl 消费后
        // 直接 setMockCells / setSafeApps("abhf|*") / startMockLocation / 种子位置，
        // 不再依赖被 SELinux 拦截的 oem_location binder。
        writeCellMockFile(pendingCellList)

        // pull API（TelephonyManager.getAllCellInfo / getCellLocation）由 com.android.phone
        // 进程的 PhoneInterfaceManager 提供，必须把 app-hook 注入该进程才能拦到；
        // 它读取模拟小区走 Provider / 文件通道（见 MockLocationServiceManager）。
        Thread({
            runCatching { RootDeployer.injectAppProcess(applicationContext, "com.android.phone") }
                .onFailure { KailLog.e(this, TAG, "inject com.android.phone: ${it.message}") }
        }, "ServiceGoRootPhoneInject").start()

        KailLog.i(this, TAG, "Cell mock applied: ${pendingCellList.size} towers (provider+file)")
    }

    private data class RootLocationAck(
        val status: String,
        val enabled: Boolean?,
        val lat: Double?,
        val lng: Double?,
        val pid: String?,
        val count: Long?,
        val hookReady: Boolean?,
        val stepEnabled: Boolean?,
        val stepSpm: Float?,
        val stepMocking: Boolean?,
        val stepHookInstalled: Boolean?,
        val stepHookState: Int?,
        val stepSendHook: Boolean?,
        val stepConvertHook: Boolean?,
        val stepCounterHandle: Int?,
        val stepDetectorHandle: Int?,
        val stepSynthEvents: Long?,
        val stepStatus: String?,
        val stepError: String?,
        val raw: String
    ) {
        fun isAppliedFor(expectedLat: Double, expectedLng: Double): Boolean {
            if (status != "applied" || enabled != true) return false
            val ackLat = lat ?: return false
            val ackLng = lng ?: return false
            return kotlin.math.abs(ackLat - expectedLat) < 0.000001 &&
                kotlin.math.abs(ackLng - expectedLng) < 0.000001
        }

        fun summary(): String {
            val step = if (stepEnabled == true) {
                " step=${stepStatus ?: "?"}/mocking=${stepMocking ?: false}/hook=${stepHookInstalled ?: false}" +
                    "/send=${stepSendHook ?: false}/convert=${stepConvertHook ?: false}" +
                    "/handles=${stepCounterHandle ?: -1},${stepDetectorHandle ?: -1}/synth=${stepSynthEvents ?: 0}"
            } else {
                ""
            }
            return "status=$status pid=${pid ?: "?"} count=${count ?: -1} hookReady=${hookReady ?: false} lat=${lat ?: "?"} lng=${lng ?: "?"}$step"
        }
    }

    private data class RootControlWrite(
        val content: String,
        val generation: Int,
        val session: Long
    )

    private fun buildRootLocationControlContent(enabled: Boolean): String {
        // 步频配置同时进 Provider（主），供 system_server 直接 call 取。
        val stepContent = if (enabled) {
            "step_enabled=${if (stepEnabled) 1 else 0}\n" +
                "step_spm=$stepCadence\n" +
                "step_mode=$stepMode\n" +
                "step_scheme=$stepScheme\n"
        } else {
            "step_enabled=0\n"
        }
        runCatching { LocationShmProvider.setConfig(LocationShm.PROVIDER_KEY_STEP_CONFIG, stepContent) }
        return if (enabled) {
            "enabled=1\n" +
                "lat=$mCurLat\n" +
                "lng=$mCurLng\n" +
                "alt=$mCurAlt\n" +
                "bearing=$mCurBea\n" +
                "speed=$mSpeed\n" +
                "interval=${currentLocationUpdateIntervalMs()}\n" +
                stepContent
        } else {
            "enabled=0\n"
        }
    }

    private fun writeRootLocationControl(
        enabled: Boolean,
        timeoutMs: Long = 120_000L,
        generation: Int = startGeneration,
        rootControlSession: Long = activeRootControlSession,
        requireLocationLoop: Boolean = false
    ) {
        if (!enabled) {
            rootControlLatestWrite = null
        }
        writeRootLocationControlGuarded(
            content = buildRootLocationControlContent(enabled),
            enabled = enabled,
            timeoutMs = timeoutMs,
            generation = generation,
            rootControlSession = rootControlSession,
            requireLocationLoop = requireLocationLoop
        )
    }

    private fun isRootControlEnabledWriteCurrent(
        generation: Int,
        rootControlSession: Long,
        requireLocationLoop: Boolean
    ): Boolean {
        return rootControlSession > 0L &&
            generation == startGeneration &&
            rootControlSession == activeRootControlSession &&
            rootControlSession == ROOT_CONTROL_ACTIVE_SESSION.get() &&
            isRunning &&
            (!requireLocationLoop || locationLoopStarted)
    }

    private fun isRootControlDisableAllowed(rootControlSession: Long): Boolean {
        val active = ROOT_CONTROL_ACTIVE_SESSION.get()
        return rootControlSession <= 0L || active == 0L || active == rootControlSession
    }

    private fun writeRootLocationControlGuarded(
        content: String,
        enabled: Boolean,
        timeoutMs: Long = 120_000L,
        generation: Int = startGeneration,
        rootControlSession: Long = activeRootControlSession,
        requireLocationLoop: Boolean = false,
        preferFastShell: Boolean = false
    ) {
        synchronized(ROOT_CONTROL_LOCK) {
            if (enabled && !isRootControlEnabledWriteCurrent(generation, rootControlSession, requireLocationLoop)) {
                KailLog.w(this, TAG, "skip stale enable control-file write: generation=$generation current=$startGeneration session=$rootControlSession active=$activeRootControlSession global=${ROOT_CONTROL_ACTIVE_SESSION.get()} running=$isRunning loop=$locationLoopStarted")
                return
            }
            if (!enabled && !isRootControlDisableAllowed(rootControlSession)) {
                KailLog.w(this, TAG, "skip stale disable control-file write: session=$rootControlSession global=${ROOT_CONTROL_ACTIVE_SESSION.get()}")
                return
            }
            if (preferFastShell && writeRootLocationControlFastLocked(content)) {
                return
            }
            writeRootLocationControlContent(content, timeoutMs)
        }
    }

    private fun writeRootLocationControlContent(content: String, timeoutMs: Long = 120_000L) {
        val controlPath = RootControlPaths.controlPath(applicationContext)
        val cmd = if (rootControlPrepared) {
            "printf '%s' ${shellSingleQuote(content)} > $controlPath"
        } else {
            "mkdir -p $ROOT_RUNTIME_DIR && chmod 777 $ROOT_RUNTIME_DIR && " +
                "printf '%s' ${shellSingleQuote(content)} > $controlPath && chmod 666 $controlPath && " +
                "chcon u:object_r:system_data_file:s0 $controlPath 2>/dev/null || true"
        }
        ShellUtils.executeCommand(cmd, timeoutMs)
        rootControlPrepared = true
    }

    private fun shellSingleQuote(value: String): String {
        return "'" + value.replace("'", "'\\''") + "'"
    }

    private fun isRootControlFastShellAlive(process: java.lang.Process): Boolean {
        return runCatching {
            process.exitValue()
            false
        }.getOrDefault(true)
    }

    private fun drainRootControlStream(stream: InputStream, name: String) {
        Thread({
            runCatching {
                stream.bufferedReader().use { reader ->
                    while (reader.readLine() != null) {
                        // Drain only. The fast shell is intentionally fire-and-forget.
                    }
                }
            }
        }, name).apply {
            isDaemon = true
            start()
        }
    }

    private fun ensureRootControlFastShellLocked(): BufferedWriter? {
        val process = rootControlFastProcess
        val writer = rootControlFastWriter
        if (process != null && writer != null && isRootControlFastShellAlive(process)) {
            return writer
        }
        closeRootControlFastShellLocked(null)

        return runCatching {
            val newProcess = Runtime.getRuntime().exec("su")
            drainRootControlStream(newProcess.inputStream, "ServiceGoRootControlFastOut")
            drainRootControlStream(newProcess.errorStream, "ServiceGoRootControlFastErr")
            val newWriter = BufferedWriter(OutputStreamWriter(newProcess.outputStream))
            rootControlFastProcess = newProcess
            rootControlFastWriter = newWriter

            val controlPath = RootControlPaths.controlPath(applicationContext)
            newWriter.write("mkdir -p $ROOT_RUNTIME_DIR\n")
            newWriter.write("chmod 777 $ROOT_RUNTIME_DIR\n")
            newWriter.write("touch $controlPath\n")
            newWriter.write("chmod 666 $controlPath 2>/dev/null || true\n")
            newWriter.write("chcon u:object_r:system_data_file:s0 $controlPath 2>/dev/null || true\n")
            newWriter.flush()
            rootControlPrepared = true
            KailLog.i(this, TAG, "fast root control shell started path=$controlPath")
            newWriter
        }.getOrElse {
            KailLog.e(this, TAG, "fast root control shell start: ${it.message}")
            closeRootControlFastShellLocked(null)
            null
        }
    }

    private fun writeRootLocationControlFastLocked(content: String): Boolean {
        val writer = ensureRootControlFastShellLocked() ?: return false
        val controlPath = RootControlPaths.controlPath(applicationContext)
        return runCatching {
            writer.write("printf '%s' ${shellSingleQuote(content)} > $controlPath\n")
            writer.flush()
            true
        }.getOrElse {
            KailLog.w(this, TAG, "fast control-file write failed, falling back: ${it.message}")
            closeRootControlFastShellLocked(null)
            false
        }
    }

    private fun closeRootControlFastShell(reason: String? = null) {
        synchronized(ROOT_CONTROL_LOCK) {
            closeRootControlFastShellLocked(reason)
        }
    }

    private fun closeRootControlFastShellLocked(reason: String?) {
        val writer = rootControlFastWriter
        val process = rootControlFastProcess
        rootControlFastWriter = null
        rootControlFastProcess = null
        runCatching {
            writer?.write("exit\n")
            writer?.flush()
        }
        runCatching { writer?.close() }
        runCatching { process?.destroy() }
        if (reason != null && (writer != null || process != null)) {
            KailLog.i(this, TAG, "fast root control shell closed: $reason")
        }
    }

    private fun initRootControlWriter() {
        // daemon：进程被系统要求退出时不要让非守护线程卡住 DestroyJavaVM，
        // 否则会留下"主线程已退出、intent 永远排队"的僵死进程。
        mRootControlWriterThread = HandlerThread("ServiceGoRootControlWriter", Process.THREAD_PRIORITY_BACKGROUND).apply { isDaemon = true }
        mRootControlWriterThread.start()
        mRootControlWriterHandler = Handler(mRootControlWriterThread.looper)
    }

    private fun rootControlAsyncMinIntervalMs(): Long {
        return currentLocationUpdateIntervalMs().coerceAtLeast(200L)
    }

    private fun pushRootLocationControlAsync() {
        val generation = startGeneration
        val rootControlSession = activeRootControlSession
        if (!isRootControlEnabledWriteCurrent(generation, rootControlSession, requireLocationLoop = true)) return
        val content = buildRootLocationControlContent(true)
        rootControlLatestWrite = RootControlWrite(content, generation, rootControlSession)
        if (!this::mRootControlWriterHandler.isInitialized) {
            writeRootLocationControlGuarded(
                content = content,
                enabled = true,
                timeoutMs = 1500L,
                generation = generation,
                rootControlSession = rootControlSession,
                requireLocationLoop = true
            )
            return
        }
        val now = SystemClock.elapsedRealtime()
        val delay = (rootControlAsyncMinIntervalMs() - (now - lastRootControlAsyncWriteMs)).coerceAtLeast(0L)
        scheduleRootControlWriter(delay)
    }

    private fun scheduleRootControlWriter(delayMs: Long) {
        if (rootControlWriterScheduled || !this::mRootControlWriterHandler.isInitialized) return
        rootControlWriterScheduled = true
        mRootControlWriterHandler.postDelayed({
            rootControlWriterScheduled = false
            val write = rootControlLatestWrite ?: return@postDelayed
            rootControlLatestWrite = null
            if (!isRootControlEnabledWriteCurrent(write.generation, write.session, requireLocationLoop = true)) return@postDelayed
            runCatching {
                writeRootLocationControlGuarded(
                    content = write.content,
                    enabled = true,
                    timeoutMs = 1500L,
                    generation = write.generation,
                    rootControlSession = write.session,
                    requireLocationLoop = true,
                    preferFastShell = true
                )
            }.onFailure {
                KailLog.e(this, TAG, "async control-file write: ${it.message}")
            }
            lastRootControlAsyncWriteMs = SystemClock.elapsedRealtime()
            val next = rootControlLatestWrite
            if (next != null && isRootControlEnabledWriteCurrent(next.generation, next.session, requireLocationLoop = true)) {
                scheduleRootControlWriter(rootControlAsyncMinIntervalMs())
            }
        }, delayMs)
    }

    /**
     * 单次 su 会话内完成「清 ack + 写控制文件 + 等 system_server 应用」。
     *
     * 旧的启动路径是三次独立命令、各自重新起一个 su 进程（每次 ~200ms）：
     * clearRootLocationAck() / writeRootLocationControl() / waitForRootLocationAck()
     * （ack 轮询每 250ms 又 cat 一次）。这里合并成一个 shell 命令，内部 100ms 轮询，
     * 把启动期的多次 Runtime.exec("su") 压成一次，显著缩短「开始模拟」的转圈时间。
     * system_server 的 RootLocationControl 线程仍以其自身的 250ms 节奏消费控制文件。
     *
     * @return 最终 ack；超时或 ack 为空则返回 null（与旧 waitForRootLocationAck 语义一致）。
     */
    private fun writeAndWaitRootLocationControl(content: String, generation: Int): RootLocationAck? {
        if (!isCurrentGeneration(generation)) return null
        val controlPath = RootControlPaths.controlPath(applicationContext)
        val ackPath = RootControlPaths.ackPath(applicationContext)
        val prepare = if (rootControlPrepared) "" else "mkdir -p $ROOT_RUNTIME_DIR && chmod 777 $ROOT_RUNTIME_DIR && "
        val cmd = buildString {
            append("rm -f $ackPath; ")
            append(prepare)
            append("printf '%s' ${shellSingleQuote(content)} > $controlPath; ")
            append("chmod 666 $controlPath 2>/dev/null; chcon u:object_r:system_data_file:s0 $controlPath 2>/dev/null; ")
            // 长时运行后 system_server 内的 apply（向各已注册位置监听器派发模拟位置）可能
            // 被冻结/僵尸监听器拖慢数秒，等待窗口给足 12s，避免 4s 上限误报"注入失效"。
            append("i=0; while [ \$i -lt 120 ]; do ")
            append("  [ -s $ackPath ] && grep -q 'status=applied' $ackPath && { echo __ACK__; cat $ackPath; exit 0; }; ")
            append("  sleep 0.1; i=\$((i+1)); done; ")
            append("echo __ACK__; cat $ackPath 2>/dev/null")
        }
        val raw = ShellUtils.executeCommand(cmd, 20_000L).trim()
        rootControlPrepared = true
        val payload = raw.substringAfter("__ACK__", raw).trim()
        if (payload.isBlank()) return null
        return parseRootLocationAck(payload)
    }

    private fun parseRootLocationAck(raw: String): RootLocationAck {
        val values = raw.lineSequence().mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        return RootLocationAck(
            status = values["status"] ?: "unknown",
            enabled = values["enabled"]?.let { it == "1" || it.equals("true", ignoreCase = true) },
            lat = values["lat"]?.toDoubleOrNull(),
            lng = values["lng"]?.toDoubleOrNull(),
            pid = values["pid"],
            count = values["count"]?.toLongOrNull(),
            hookReady = values["mock_initialized"]?.toBooleanStrictOrNull(),
            stepEnabled = values["step_enabled"]?.let { it == "1" || it.equals("true", ignoreCase = true) },
            stepSpm = values["step_spm"]?.toFloatOrNull(),
            stepMocking = values["step_mocking"]?.let { it == "1" || it.equals("true", ignoreCase = true) },
            stepHookInstalled = values["step_hook_installed"]?.let { it == "1" || it.equals("true", ignoreCase = true) },
            stepHookState = values["step_hook_state"]?.toIntOrNull(),
            stepSendHook = values["step_send_hook"]?.let { it == "1" || it.equals("true", ignoreCase = true) },
            stepConvertHook = values["step_convert_hook"]?.let { it == "1" || it.equals("true", ignoreCase = true) },
            stepCounterHandle = values["step_counter_handle"]?.toIntOrNull(),
            stepDetectorHandle = values["step_detector_handle"]?.toIntOrNull(),
            stepSynthEvents = values["step_synth_events"]?.toLongOrNull(),
            stepStatus = values["step_status"],
            stepError = values["step_error"],
            raw = raw
        )
    }

    private fun logStepAckIfDue(nowMs: Long) {
        if (!stepEnabled || nowMs - lastStepAckLogMs < 5000L) return
        lastStepAckLogMs = nowMs
        if (stepAckReadInFlight) return
        stepAckReadInFlight = true
        Thread({
            try {
                val raw = ShellUtils.executeCommand(
                    "cat ${RootControlPaths.ackPath(applicationContext)} 2>/dev/null",
                    timeoutMs = 1000L
                ).trim()
                if (raw.isNotBlank()) {
                    val ack = parseRootLocationAck(raw)
                    KailLog.i(
                        this,
                        TAG,
                        "step ack ${ack.summary()}"
                    )
                }
            } catch (t: Throwable) {
                KailLog.w(this, TAG, "step ack read: ${t.message}")
            } finally {
                stepAckReadInFlight = false
            }
        }, "ServiceGoRootStepAck").start()
    }


    /**
     * 创建/复用 Phase 1 共享内存（ashmem）并注册给 [LocationShmProvider]。
     *
     * 共享内存按「App 进程生命周期」复用：同一进程内多次开始模拟共用同一块
     * ashmem，只重置 header / 置 enabled=1，这样 system_server 侧的映射不会失效。
     * App 进程重启后才会新建（system_server 读端靠「长时间无新样本」自愈重映射）。
     *
     * 失败（VarHandle 不可用 / ashmem 创建失败 / 映射失败）返回 false，
     * [pushLocationToInjection] 自动回退控制文件通道，功能不回归。
     */
    private fun prepareLocationShm(): Boolean {
        if (!LocationShm.isAvailable()) {
            KailLog.w(this, TAG, "shm unavailable (no VarHandle); using control-file transport")
            return false
        }
        return runCatching {
            var buffer = locationShmBuffer
            if (buffer == null || LocationShmProvider.getSharedMemory() == null) {
                closeLocationShm()
                val shared = SharedMemory.create(LocationShm.ASHMEM_NAME, LocationShm.SIZE)
                runCatching {
                    shared.setProtect(OsConstants.PROT_READ or OsConstants.PROT_WRITE)
                }
                buffer = shared.mapReadWrite()
                locationShmShared = shared
                locationShmBuffer = buffer
                LocationShmProvider.setSharedMemory(shared)
            }
            LocationShm.initHeader(buffer, 1)
            KailLog.i(this, TAG, "location shm ready size=${LocationShm.SIZE}")
            true
        }.getOrElse {
            KailLog.e(this, TAG, "prepareLocationShm failed: ${it.message}")
            closeLocationShm()
            false
        }
    }

    /** 结束会话：仅停用共享内存，保留 ashmem 供下次会话复用。 */
    private fun disableLocationShm() {
        runCatching { LocationShm.setEnabled(locationShmBuffer, false) }
    }

    /** 彻底关闭并注销共享内存。可重复调用。 */
    private fun closeLocationShm() {
        runCatching { LocationShm.setEnabled(locationShmBuffer, false) }
        LocationShmProvider.setSharedMemory(null)
        locationShmBuffer = null
        runCatching { locationShmShared?.close() }
        locationShmShared = null
    }

    private fun pushLocationToInjection() {
        // Never push location / enable GNSS mock in WiFi-only or cell-only
        // mode — those modes spoof only WiFi scan results / cell towers.
        if (modeWifiOnly || modeCellOnly) return

        // Path 0: shared memory (Phase 1 fast path). App writes the latest
        // position into ashmem; the injected system_server mmaps the same
        // region and reads it, so there is no per-tick su/file IPC.
        val shm = locationShmBuffer
        if (shm != null && LocationShm.isEnabled(shm)) {
            var lat = mCurLat
            var lng = mCurLng
            if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean("setting_natural_jitter", false)
            ) {
                val sigma = 2.5e-6
                lat += (Math.random() * 2 - 1) * sigma
                lng += (Math.random() * 2 - 1) * sigma
                mCurLat = lat
                mCurLng = lng
            }
            if (LocationShm.writeSample(
                    shm,
                    SystemClock.elapsedRealtimeNanos(),
                    lat,
                    lng,
                    mCurAlt,
                    mCurBea.toDouble(),
                    mSpeed,
                    1.0,
                    0,
                    0
                )
            ) {
                return
            }
        }

        // Path 1: system_server control file. On some Android 16 ROMs the
        // injected binder is hidden from untrusted_app while SELinux is
        // enforcing; avoid setenforce 0 and let the injected system_server
        // thread consume location updates directly.
        runCatching {
            pushRootLocationControlAsync()
        }.onFailure { KailLog.e(this, TAG, "setLocation (control-file): ${it.message}") }
    }

    private fun recordCleanupState() {
        runCatching {
            val enforce = ShellUtils.executeCommand("getenforce").trim()
            val appOps = ShellUtils.executeCommand("appops get $packageName android:mock_location 2>/dev/null || true").trim()
            KailLog.i(this, TAG, "cleanup state: getenforce=$enforce mock_location=${appOps.ifBlank { "<none>" }}")
        }.onFailure { KailLog.w(this, TAG, "recordCleanupState: ${it.message}") }
    }

    // ------------------------------------------------------------------
    // Location loop / status broadcasts
    // ------------------------------------------------------------------

    private fun broadcastStatus() {
        val intent = Intent(ACTION_STATUS_CHANGED).apply {
            putExtra(EXTRA_IS_SIMULATING, locationLoopStarted && !isStop)
            putExtra(EXTRA_IS_PAUSED, isStop)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun broadcastStatusStopped() {
        val intent = Intent(ACTION_STATUS_CHANGED).apply {
            putExtra(EXTRA_IS_SIMULATING, false)
            putExtra(EXTRA_IS_PAUSED, false)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun initGoLocation() {
        // daemon：同 initRootControlWriter，避免进程退出时卡在 DestroyJavaVM。
        mLocHandlerThread = HandlerThread(SERVICE_GO_HANDLER_NAME, Process.THREAD_PRIORITY_DEFAULT).apply { isDaemon = true }
        mLocHandlerThread.start()
        mLocHandler = object : Handler(mLocHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                try {
                    if (!isRunning || !locationLoopStarted) return
                    val now = SystemClock.elapsedRealtime()
                    val elapsedMs = if (lastRouteTickElapsedMs > 0L) {
                        (now - lastRouteTickElapsedMs).coerceIn(0L, 30_000L)
                    } else {
                        currentLocationUpdateIntervalMs()
                    }
                    lastRouteTickElapsedMs = now
                    if (!isStop) {
                        if (mRouteEngine.isActive) {
                            val speedForStep = if (speedFluctuation) {
                                GeoPredict.randomInRangeWithMean(mSpeed * 0.5, mSpeed * 1.5, mSpeed)
                            } else {
                                mSpeed
                            }
                            mRouteEngine.advance(speedForStep * (elapsedMs / 1000.0))
                            mCurLng = mRouteEngine.currentLng
                            mCurLat = mRouteEngine.currentLat
                            mCurBea = mRouteEngine.currentBea
                            updateJoystickStatus()
                        }
                    }
                    // Always push the current position even when paused, so the
                    // mock location stays at the last simulated spot instead of
                    // snapping back to the real GPS position.
                    pushLocationToInjection()
                    logStepAckIfDue(now)
                    if (isRunning && locationLoopStarted) {
                        sendEmptyMessageDelayed(HANDLER_MSG_ID, currentLocationUpdateIntervalMs())
                    }
                } catch (e: InterruptedException) {
                    KailLog.e(this@ServiceGoRoot, TAG, "loop interrupted: ${e.message}")
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    KailLog.e(this@ServiceGoRoot, TAG, "loop: ${e.message}")
                    if (isRunning && locationLoopStarted) {
                        sendEmptyMessageDelayed(HANDLER_MSG_ID, currentLocationUpdateIntervalMs())
                    }
                }
            }
        }
    }

    private fun currentLocationUpdateIntervalMs(): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return (prefs.getString("setting_report_interval", DEFAULT_LOCATION_UPDATE_INTERVAL_MS.toString())
            ?.toLongOrNull() ?: DEFAULT_LOCATION_UPDATE_INTERVAL_MS).coerceAtLeast(0L)
    }

    private fun startLocationLoop() {
        if (!this::mLocHandler.isInitialized) return
        isStop = false
        if (locationLoopStarted) return
        locationLoopStarted = true
        lastRouteTickElapsedMs = SystemClock.elapsedRealtime()
        mLocHandler.sendEmptyMessage(HANDLER_MSG_ID)
        broadcastStatus()
    }

    private fun updateJoystickStatus() {
        if (this::mJoystickManager.isInitialized && mRouteEngine.isActive) {
            val status = mRouteEngine.buildStatusString()
            if (status != null) {
                val waitSuffix = if (mRouteEngine.isWaiting) " · " + getString(R.string.route_waiting) else ""
                mJoystickManager.updateRouteStatus(mRouteEngine.progressRatio, status.first + waitSuffix, status.second)
            }
        }
    }

    /**
     * Append newly planned points (WGS84, [lng,lat,...]) to the running route so
     * the simulation continues straight into the extension instead of parking.
     */
    private fun appendRouteFromControl(intent: Intent) {
        val arr = intent.getDoubleArrayExtra(EXTRA_ROUTE_APPEND_POINTS)
        if (arr == null || arr.size < 2) {
            KailLog.w(this, TAG, "append_route: no points provided")
            return
        }
        val pts = mutableListOf<Pair<Double, Double>>()
        var i = 0
        while (i + 1 < arr.size) {
            pts.add(Pair(arr[i], arr[i + 1]))
            i += 2
        }
        mRouteEngine.appendPoints(pts, intent.getDoubleArrayExtra(EXTRA_ROUTE_APPEND_WAIT_TIMES))
        mCurLng = mRouteEngine.currentLng
        mCurLat = mRouteEngine.currentLat
        mCurBea = mRouteEngine.currentBea
        updateJoystickStatus()
        isStop = false
        lastRouteTickElapsedMs = SystemClock.elapsedRealtime()
        if (locationLoopStarted && this::mLocHandler.isInitialized && !mLocHandler.hasMessages(HANDLER_MSG_ID)) {
            mLocHandler.sendEmptyMessage(HANDLER_MSG_ID)
        }
        broadcastStatus()
        KailLog.i(this, TAG, "append_route: +${pts.size} points, now at lat=$mCurLat lng=$mCurLng")
    }

    // ------------------------------------------------------------------
    // Joystick wiring
    // ------------------------------------------------------------------

    private fun initJoyStick() {
        mJoystickViewModel = JoystickViewModel(application)
        mJoystickManager = JoystickWindowManager(this, mJoystickViewModel, object : JoystickViewModel.ActionListener {
            override fun onMoveInfo(speed: Double, disLng: Double, disLat: Double, angle: Double) {
                mSpeed = speed
                val next = GeoPredict.nextByDisplacementKm(mCurLng, mCurLat, disLng, disLat)
                mCurLng = next.first
                mCurLat = next.second
                mCurBea = angle.toFloat()
            }

            override fun onPositionInfo(lng: Double, lat: Double, alt: Double) {
                mCurLng = lng
                mCurLat = lat
                mCurAlt = alt
            }

            override fun onRouteControl(action: String) {
                val intent = Intent(this@ServiceGoRoot, ServiceGoRoot::class.java)
                intent.putExtra(EXTRA_CONTROL_ACTION, action)
                startService(intent)
            }

            override fun onRouteSeek(progress: Float) {
                val intent = Intent(this@ServiceGoRoot, ServiceGoRoot::class.java)
                intent.putExtra(EXTRA_CONTROL_ACTION, CONTROL_SEEK)
                intent.putExtra(EXTRA_SEEK_RATIO, progress)
                startService(intent)
            }

            override fun onRouteSpeedChange(speed: Double) {
                mSpeed = speed / 3.6
            }
        })
    }

    // ------------------------------------------------------------------
    // Sensor offset probing (mirrors ServiceGoXposed)
    // ------------------------------------------------------------------

    private fun runSuCommand(cmd: String): String =
        runCatching {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val out = proc.inputStream.bufferedReader().readText().trim()
            proc.waitFor()
            out
        }.getOrDefault("")

    private fun getOffsetsFromSystem(): Pair<String, String> {
        val candidates = listOf("toybox readelf", "readelf", "/system/bin/toybox readelf")
        for (cmd in candidates) {
            try {
                val test = runSuCommand("$cmd 2>&1")
                if (test.contains("not found") || (test.isEmpty() && !cmd.startsWith("/"))) continue

                val sensorOut = runSuCommand("$cmd -Ws /system/lib64/libsensor.so 2>/dev/null | grep _ZN7android7BitTube11sendObjects")
                val sensorServiceOut = runSuCommand("$cmd -Ws /system/lib64/libsensorservice.so 2>/dev/null | grep '_ZN7android8hardware7sensors14implementation20convertToSensorEvent[^4V1]'")
                val sensorServiceV1Out = runSuCommand("$cmd -Ws /system/lib64/libsensorservice.so 2>/dev/null | grep '_ZN7android8hardware7sensors4V1_014implementation20convertToSensorEvent'")

                if (sensorOut.isNotEmpty()) {
                    val sensorOffset = parseReadelfOffset(sensorOut)
                    val sensorServiceOffset = when {
                        sensorServiceOut.isNotEmpty() -> parseReadelfOffset(sensorServiceOut)
                        sensorServiceV1Out.isNotEmpty() -> parseReadelfOffset(sensorServiceV1Out)
                        else -> ""
                    }
                    if (sensorOffset.isNotEmpty() && sensorServiceOffset.isNotEmpty()) {
                        return Pair(
                            if (sensorOffset.startsWith("0x")) sensorOffset else "0x$sensorOffset",
                            if (sensorServiceOffset.startsWith("0x")) sensorServiceOffset else "0x$sensorServiceOffset"
                        )
                    }
                }
            } catch (_: Exception) { /* try next candidate */ }
        }
        KailLog.w(this, TAG, "readelf unavailable; sensor offsets unknown")
        return Pair("0x0", "0x0")
    }

    private fun parseReadelfOffset(output: String): String {
        val trimmed = output.trim().lines().firstOrNull()?.trim() ?: return ""
        val parts = trimmed.split(Regex("\\s+"))
        return parts.firstOrNull { it.matches(Regex("^[0-9a-fA-F]{8,16}$")) } ?: ""
    }

    private fun parseHexOffset(s: String): Long {
        val v = s.removePrefix("0x").removePrefix("0X")
        return v.toLongOrNull(16) ?: 0L
    }

    inner class ServiceGoRootBinder : Binder() {
        fun getService(): ServiceGoRoot = this@ServiceGoRoot
    }
}
