package de.app.bonn.android.forgroundService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.app.bonn.android.R

class WallpaperForegroundService: Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }
    private fun createNotification(): Notification {
        val channelId = "wallpaper_service"
        val channelName = "Wallpaper Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Live Wallpaper Running")
            .setSmallIcon(R.drawable.directions_run_24)
            .setOngoing(true)
            .build()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}