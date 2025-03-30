package de.app.bonn.android.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.app.bonn.android.R
import de.app.bonn.android.download.VideoDownloader
import de.app.bonn.android.firebase.NotificationHelper
import de.app.bonn.android.service.VideoLiveWallpaperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class VideoDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val videoDownloader: VideoDownloader,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {
        val videoUrl = inputData.getString("video_url") ?: return Result.failure()
        val file = File(applicationContext.getExternalFilesDir(null), "live_wallpaper.mp4")

        notificationHelper.ensureDownloadChannel()
        setForeground(
            ForegroundInfo(
                notificationHelper.notificationId,
                notificationHelper.buildDownloadNotification(0),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )

        val success = videoDownloader.downloadVideo(videoUrl, file) { progress ->
            notificationHelper.notify(
                notificationHelper.notificationId,
                notificationHelper.buildDownloadNotification(progress)
            )
        }

        if (success) {
            notifyWallpaperService(applicationContext)
            notificationHelper.notify(
                notificationHelper.notificationId,
                notificationHelper.buildDownloadCompleteNotification()
            )
            return Result.success()
        }

        return Result.retry()
    }

    private fun notifyWallpaperService(context: Context) {
        val intent = Intent("UPDATE_LIVE_WALLPAPER").setPackage(
            "de.app.bonn.android"
        )
        context.sendBroadcast(intent)
    }

    companion object {
        fun initiate(context: Context, videoUrl: String) {
            val workRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
                .setInputData(workDataOf("video_url" to videoUrl))
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}