package com.kail.location.utils.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kail.location.R
import com.kail.location.utils.KailLog
import com.kail.location.views.locationpicker.LocationPickerActivity

/**
 * 封装前台服务通知的创建、Channel 注册、BroadcastReceiver 管理与 startForeground。
 */
class ServiceNotificationHelper(
    private val service: Service,
    private val channelId: String,
    private val channelName: String,
    private val noteId: Int,
    private val onShowJoystick: () -> Unit,
    private val onHideJoystick: () -> Unit
) {
    companion object {
        private const val TAG = "ServiceNotification"
        const val ACTION_JOYSTICK_SHOW = "ShowJoyStick"
        const val ACTION_JOYSTICK_HIDE = "HideJoyStick"
    }

    private var receiver: BroadcastReceiver? = null
    var notification: Notification? = null
        private set

    fun initAndStartForeground() {
        if (receiver == null) {
            receiver = NoteActionReceiver()
            val filter = IntentFilter().apply {
                addAction(ACTION_JOYSTICK_SHOW)
                addAction(ACTION_JOYSTICK_HIDE)
            }
            ContextCompat.registerReceiver(
                service,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.createNotificationChannel(channel)

        val clickIntent = Intent(service, LocationPickerActivity::class.java)
        val clickPI = PendingIntent.getActivity(service, 1, clickIntent, PendingIntent.FLAG_IMMUTABLE)
        val showIntent = Intent(ACTION_JOYSTICK_SHOW)
        val showPendingPI = PendingIntent.getBroadcast(service, 0, showIntent, PendingIntent.FLAG_IMMUTABLE)
        val hideIntent = Intent(ACTION_JOYSTICK_HIDE)
        val hidePendingPI = PendingIntent.getBroadcast(service, 0, hideIntent, PendingIntent.FLAG_IMMUTABLE)

        val built = NotificationCompat.Builder(service, channelId)
            .setChannelId(channelId)
            .setContentTitle(service.resources.getString(R.string.app_name))
            .setContentText(service.resources.getString(R.string.app_service_tips))
            .setContentIntent(clickPI)
            .addAction(NotificationCompat.Action(null, service.resources.getString(R.string.note_show), showPendingPI))
            .addAction(NotificationCompat.Action(null, service.resources.getString(R.string.note_hide), hidePendingPI))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        notification = built

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                service.startForeground(noteId, built, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                service.startForeground(noteId, built)
            }
            KailLog.i(service, TAG, "initAndStartForeground: foreground started (noteId=$noteId)")
        } catch (e: Exception) {
            KailLog.e(service, TAG, "initAndStartForeground: startForeground failed", e)
        }
    }

    fun startForegroundIfReady() {
        val n = notification ?: run {
            KailLog.w(service, TAG, "startForegroundIfReady: notification not built yet")
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                service.startForeground(noteId, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                service.startForeground(noteId, n)
            }
        } catch (e: Exception) {
            KailLog.e(service, TAG, "startForegroundIfReady: startForeground failed", e)
        }
    }

    fun stopForeground() {
        receiver?.let {
            try {
                service.unregisterReceiver(it)
            } catch (_: Exception) {}
            receiver = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            service.stopForeground(true)
        }
    }

    private inner class NoteActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_JOYSTICK_SHOW -> onShowJoystick()
                ACTION_JOYSTICK_HIDE -> onHideJoystick()
            }
        }
    }
}
