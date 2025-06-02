package de.app.bonn.android.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import de.app.bonn.android.R
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class VideoLiveWallpaperService : WallpaperService() {

    private var videoEngine: VideoEngine? = null
    private var video_name = "starter"


    override fun onCreateEngine(): Engine {
        videoEngine = VideoEngine()
        return videoEngine!!
    }

    private val wallpaperUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "UPDATE_LIVE_WALLPAPER") {
                video_name = intent.getStringExtra("video_name") ?: "starter"
                Timber.i("Video name updated: $video_name")
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
        private var mediaPlayer: MediaPlayer? = null
        private lateinit var surfaceHolder: SurfaceHolder

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            startForegroundService()
            playVideo(video_name)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            mediaPlayer?.pause()
        }

        fun updateVideo() {
            stopAndReleasePlayer()
            playVideo(video_name)
        }

        private fun playVideo(videoName: String) {
            val file = File(applicationContext.getExternalFilesDir(null), "$videoName")
            if (!file.exists()) {
                Timber.tag("WallpaperService").e("Video file not found: $videoName")
                return
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, Uri.fromFile(file))
                setSurface(surfaceHolder.surface)
                isLooping = true
                setOnPreparedListener { it.start() }
                setVolume(0f, 0f)
                prepareAsync()
            }
        }

        private fun stopAndReleasePlayer() {
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
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
                .setContentTitle("")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(true)
                .build()
        }

        override fun onDestroy() {
            stopAndReleasePlayer()
            super.onDestroy()
        }
    }
}
