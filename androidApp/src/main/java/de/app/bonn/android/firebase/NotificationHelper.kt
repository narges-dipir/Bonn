package de.app.bonn.android.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelId = "video_download_channel"
    val notificationId = 1001

    fun ensureDownloadChannel() {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of video downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildDownloadNotification(progress: Int): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Downloading video")
            .setContentText("Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    fun buildDownloadCompleteNotification(): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Download complete")
            .setContentText("Your live wallpaper is ready")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
    }

    fun cancelNotification(id: Int = notificationId) {
        notificationManager.cancel(id)
    }

    fun notify(id: Int = notificationId, notification: Notification) {
        notificationManager.notify(id, notification)
    }
}
