package com.limor.app.service.recording

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import com.limor.app.R
import com.limor.app.scenes.main_new.MainActivityNew
import androidx.core.app.NotificationCompat
import android.app.NotificationManager

import android.app.NotificationChannel
import android.graphics.BitmapFactory
import androidx.annotation.RequiresApi
import com.facebook.FacebookSdk
import javax.inject.Inject

class RecordService : Service() {

    private var started = false

    companion object {
        fun start(context: Context, startAction: String) {
            context.startService(
                Intent(
                    context.applicationContext,
                    RecordService::class.java
                ).apply {
                    action = startAction
                })
        }

        const val ACTION_START_RECORDING_SERVICE = "LIMOR_ACTION_START_RECORDING_SERVICE"
        const val ACTION_STOP_RECORDING_SERVICE = "LIMOR_ACTION_STOP_RECORDING_SERVICE"

        private const val RECORDING_CHANNEL_ID = "com.limor.app.recording_channel"
        private const val RECORDING_NOTIFICATION_ID = 111
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Service onStartCommand with action -> ${intent?.action}")
        when (intent?.action) {
            ACTION_START_RECORDING_SERVICE -> if (!started) {
                startInForeground()
            }
            ACTION_STOP_RECORDING_SERVICE -> stopForegroundService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException()
    }

    private fun startInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(RECORDING_CHANNEL_ID, "Recording");
        }

        val pendingIntent = Intent(applicationContext, MainActivityNew::class.java).let { ni ->
            PendingIntent.getActivity(applicationContext, 0, ni, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(this, RECORDING_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.recording_notification_title))
            setContentText(getString(R.string.recording_notification_text))
            setTicker(getString(R.string.recording_notification_text))
            setContentIntent(pendingIntent)
            setSmallIcon(R.drawable.limor_circle)
            setLargeIcon(
                BitmapFactory.decodeResource(resources, R.drawable.notification)
            )
            setOngoing(true)
            priority = NotificationManager.IMPORTANCE_MAX
        }

        startForeground(RECORDING_NOTIFICATION_ID, notification.build())

        started = true
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
        started = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.getNotificationChannel(channelId)?.let {
            return
        }

        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            lightColor = getColor(R.color.colorAccent)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }.also {
            nm.createNotificationChannel(it)
        }
    }
}
