package de.app.bonn.android.manager

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import de.app.bonn.android.domain.video.UpdateBackGroundVideoUseCase
import de.app.bonn.android.worker.VideoDownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoManager @Inject constructor(
 @ApplicationContext private val context: Context,
    private val updateBackGroundVideoUseCase: UpdateBackGroundVideoUseCase
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        startObservingNewVideo()
    }
    fun startObservingNewVideo() {
        scope.launch {
            updateBackGroundVideoUseCase().collect { video ->
                println(" **** im in updateBackGroundVideoUseCase **** $video")
                if (video.name.isNotEmpty()) {
                    notifyWallpaperService(video.name)
                }
            }
        }
    }
    fun notifyWallpaperService(videoName: String) {
        println(" **** Sending broadcast for $videoName")
        val intent = Intent("UPDATE_LIVE_WALLPAPER").apply {
            setPackage("de.app.bonn.android")
            putExtra("video_name", videoName)
        }
        context.sendBroadcast(intent)
    }

    fun downloadVideoIfNeeded(videoUrl: String, videoName: String, isCached: Boolean) {
        if (!isCached) {
            VideoDownloadWorker.initiate(context, videoUrl, videoName)
        } else {
            notifyWallpaperService(videoName)
        }
    }
}