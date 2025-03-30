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
    private val videoDownloader: VideoDownloader
) : CoroutineWorker(context, workerParams) {

    private val notificationId = 1001
    private val channelId = "video_download_channel"

    override suspend fun doWork(): Result {
        val videoUrl = inputData.getString("video_url") ?: return Result.failure()
        val file = File(applicationContext.getExternalFilesDir(null), "live_wallpaper.mp4")

        createNotificationChannel()

        setForeground(createForegroundInfo(0))

        val success = videoDownloader.downloadVideo(videoUrl, file) { progress ->
            updateProgressNotification(progress)
        }
        if (success) {
            notifyWallpaperService(applicationContext)
            return Result.success()
        } else {
            Result.retry()
        }
        return Result.retry()
    }

    private fun notifyWallpaperService(context: Context) {
        val intent = Intent("UPDATE_LIVE_WALLPAPER").setPackage(
            "de.app.bonn.android"
        )
        context.sendBroadcast(intent)
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading video")
            .setContentText("Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun createNotificationChannel() {
        val name = "Video Download"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, name, importance)
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun updateProgressNotification(progress: Int) {
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading video")
            .setContentText("Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        if (progress >= 100) {
            notificationManager.cancel(notificationId)
        }
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