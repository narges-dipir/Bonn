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
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import de.app.bonn.android.R
import timber.log.Timber
import java.io.File

class VideoLiveWallpaperService: WallpaperService() {
    private var videoEngine: VideoEngine? = null
    private lateinit var exoPlayer: ExoPlayer
    private var videoName = "starter"
    override fun onCreateEngine(): Engine {
        exoPlayer = ExoPlayer.Builder(applicationContext).build()
        videoEngine = VideoEngine()
        return videoEngine!!
    }
    private val wallpaperUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "UPDATE_LIVE_WALLPAPER") {
                videoName = intent.getStringExtra("videoName") ?: ""
                Timber.tag("WallpaperService").d("Updating live wallpaper video...")
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
        private lateinit var surfaceHolder: SurfaceHolder
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            startForegroundService()
            playVideo(videoName)
        }
        fun updateVideo() {
            playVideo(videoName)
        }

        private fun playVideo(videoName: String) {
            val file = File(applicationContext.getExternalFilesDir(null), "$videoName.mp4")
            if (!file.exists()) return

            exoPlayer = exoPlayer.apply {
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