package de.app.bonn.android.service

import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

        override fun onDestroy() {
            exoPlayer?.release()
            super.onDestroy()
        }
    }
}