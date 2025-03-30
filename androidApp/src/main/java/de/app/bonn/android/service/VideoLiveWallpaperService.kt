package de.app.bonn.android.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.FirebaseApp
import de.app.bonn.android.R
import de.app.bonn.android.forgroundService.WallpaperForegroundService
import java.io.File

class VideoLiveWallpaperService: WallpaperService() {
    private var videoEngine: VideoEngine? = null

    override fun onCreateEngine(): Engine {
        videoEngine = VideoEngine()
        return videoEngine!!
    }
    private val wallpaperUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "UPDATE_LIVE_WALLPAPER") {
                Log.d("WallpaperService", "Updating live wallpaper video...")
                videoEngine?.updateVideo()
            }
        }

    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("UPDATE_LIVE_WALLPAPER")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(wallpaperUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(wallpaperUpdateReceiver, filter)
        }
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
        fun updateVideo() {
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

            val channel = NotificationChannel(
                "wallpaper_service", "Wallpaper Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)

            startForeground(1, notification)
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
            super.onDestroy()
        }
    }
}