package com.mandarinpirate.pinetimegear

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainActivity


@AndroidEntryPoint
class BleForegroundService: Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.startForeground(
            NOTIFICATION_ID,
            getNotification(
                title = getString(R.string.app_name),
                message = "Device connected battery 0%"
            )
        )
        isStarted = true

        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(title: String, message: String): Notification {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_ID.toString(),
            NOTIFICATION_CHANNEL,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        mNotificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_ID.toString())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(true)// true mean non-dismissible
        return mBuilder.build()
    }


    companion object {
        var isStarted = false
            private set

        const val NOTIFICATION_CHANNEL: String = "Device state"
        const val NOTIFICATION_CHANNEL_DESCRIPTION: String =
            "Shows connection state and battery state"
        const val NOTIFICATION_ID: Int = 5910
    }
}