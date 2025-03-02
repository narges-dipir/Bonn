package de.app.bonn.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import de.app.bonn.android.R
import de.app.bonn.android.forgroundService.WallpaperForegroundService
import java.io.File

class VideoLiveWallpaperService: WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }
    private inner class VideoEngine : Engine() {
        private var exoPlayer: ExoPlayer? = null
        private lateinit var surfaceHolder: SurfaceHolder

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            startForegroundService()
            playVideo()
        }

        private fun playVideo() {
            val file = File(applicationContext.getExternalFilesDir(null), "live_wallpaper.mp4")
            if (!file.exists()) return

            exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = true
                setVideoSurfaceHolder(surfaceHolder)
                prepare()
            }
        }

        private fun startForegroundService() {
            val notification = createNotification()
            val notificationManager = getSystemService(NotificationManager::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "wallpaper_service", "Wallpaper Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            startForeground(1, notification)  // Start foreground service when wallpaper starts
        }

        private fun createNotification(): Notification {
            return NotificationCompat.Builder(applicationContext, "wallpaper_service")
                .setContentTitle("Live Video Wallpaper Running")
                .setSmallIcon(R.drawable.directions_run_24)
                .setOngoing(true)
                .build()
        }

        override fun onDestroy() {
            exoPlayer?.release()
            stopForeground(true)
            super.onDestroy()
        }
    }
}