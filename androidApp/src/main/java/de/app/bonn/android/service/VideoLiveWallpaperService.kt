package de.app.bonn.android.service

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
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
    private var videoName = "starter"

    override fun onCreateEngine(): Engine {
        videoEngine = VideoEngine()
        return videoEngine!!
    }

    private val wallpaperUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "UPDATE_LIVE_WALLPAPER") {
                videoName = intent.getStringExtra("video_name") ?: "starter"
                Timber.i("Video name updated: $videoName")
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
        private var isPrepared = false
        private var shouldBePlaying = false
        private lateinit var surfaceHolder: SurfaceHolder

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            playVideo(videoName)

            File(applicationContext.filesDir, "wallpaper_active.flag").writeText("1")
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            shouldBePlaying = visible
            if (isPrepared && mediaPlayer != null) {
                try {
                    if (visible) {
                        mediaPlayer?.start()
                    } else {
                        mediaPlayer?.pause()
                    }
                } catch (e: IllegalStateException) {
                    Timber.e(e, "MediaPlayer visibility change failed")
                }
            }
        }


        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopAndReleasePlayer()
        }

        fun updateVideo() {
            stopAndReleasePlayer()
            playVideo(videoName)
        }

        private fun playVideo(videoName: String) {
            val file = File(applicationContext.getExternalFilesDir(null), videoName)
            if (!file.exists()) {
                Timber.e("Video file not found: $videoName")
                return
            }

            if (!::surfaceHolder.isInitialized || !surfaceHolder.surface.isValid) {
                Timber.e("Surface not valid yet")
                return
            }

            val frame = surfaceHolder.surfaceFrame
            if (frame.width() <= 0 || frame.height() <= 0) {
                Timber.w("Surface size invalid, retrying in 500ms")
                Handler(Looper.getMainLooper()).postDelayed({
                    playVideo(videoName)
                }, 500)
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
                        stopAndReleasePlayer()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (::surfaceHolder.isInitialized && surfaceHolder.surface.isValid) {
                                playVideo(videoName)
                            }
                        }, 1000)
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
                mediaPlayer?.apply {
                    setOnPreparedListener(null)
                    setOnErrorListener(null)
                    try {
                        if (isPlaying) stop()
                    } catch (e: IllegalStateException) {
                        Timber.e(e, "Stop failed (illegal state)")
                    }
                    release()
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during MediaPlayer release")
            } finally {
                mediaPlayer = null
                isPrepared = false
            }
        }

        override fun onDestroy() {
            stopAndReleasePlayer()
            File(applicationContext.filesDir, "wallpaper_active.flag").delete()
            Timber.i("VideoLiveWallpaperService destroyed")
            super.onDestroy()
        }

        private fun logState(tag: String) {
            Timber.d("[$tag] isPrepared=$isPrepared, shouldBePlaying=$shouldBePlaying, mediaPlayer=${mediaPlayer != null}, surfaceValid=${surfaceHolder.surface.isValid}")
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
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(applicationContext, "wallpaper_service")
            .setContentTitle("Bunn is here!")
            .setContentText("Tap to explore with audio & description")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
}
