package de.app.bonn.android.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import de.app.bonn.android.MainActivity
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
        startForegroundService()
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
        private var isPrepared = false
        private var shouldBePlaying = false

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            playVideo(video_name)

            // Flag to signal this wallpaper is active
            val flagFile = File(applicationContext.filesDir, "wallpaper_active.flag")
            flagFile.writeText("1")
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            shouldBePlaying = visible
            if (isPrepared) {
                try {
                    if (visible) {
                        mediaPlayer?.start()
                    } else {
                        mediaPlayer?.pause()
                    }
                } catch (e: IllegalStateException) {
                    Timber.e(e, "MediaPlayer visibility transition error")
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopAndReleasePlayer()
        }

        fun updateVideo() {
            stopAndReleasePlayer()
            playVideo(video_name)
        }

        private fun playVideo(videoName: String) {
            val file = File(applicationContext.getExternalFilesDir(null), videoName)
            if (!file.exists()) {
                Timber.e("Video file not found: $videoName")
                return
            }

            if (!::surfaceHolder.isInitialized || !surfaceHolder.surface.isValid) {
                Timber.e("Surface is not ready")
                return
            }

            stopAndReleasePlayer()
            isPrepared = false

            try {
                mediaPlayer = MediaPlayer().apply {
                    setSurface(surfaceHolder.surface)
                    setDataSource(applicationContext, Uri.fromFile(file))
                    isLooping = true
                    setVolume(0f, 0f)

                    setOnPreparedListener {
                        isPrepared = true
                        Timber.d("MediaPlayer prepared")
                        if (shouldBePlaying) {
                            try {
                                start()
                            } catch (e: IllegalStateException) {
                                Timber.e(e, "Failed to start after prepared")
                            }
                        }
                    }

                    setOnErrorListener { _, what, extra ->
                        Timber.e("MediaPlayer error: what=$what, extra=$extra")
                        true
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error preparing MediaPlayer")
            }
        }

        private fun stopAndReleasePlayer() {
            try {
                mediaPlayer?.setOnPreparedListener(null)
                mediaPlayer?.setOnErrorListener(null)
                mediaPlayer?.let {
                    try {
                        it.stop()
                    } catch (e: IllegalStateException) {
                        Timber.e(e, "stop() failed, wrong state?")
                    }
                    it.release()
                }
            } catch (e: Exception) {
                Timber.e(e, "release() failed")
            } finally {
                mediaPlayer = null
                isPrepared = false
            }
        }

        override fun onDestroy() {
            stopAndReleasePlayer()
            Timber.i("*** VideoLiveWallpaperService destroyed ***")
            val flagFile = File(applicationContext.filesDir, "wallpaper_active.flag")
            if (flagFile.exists()) {
                flagFile.delete()
            }
            super.onDestroy()
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            "wallpaper_service", "Wallpaper Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        }

        return NotificationCompat.Builder(applicationContext, "wallpaper_service")
            .setContentTitle("Bunn is here!")
            .setContentText("Tap to see the video with audio and description")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
}
