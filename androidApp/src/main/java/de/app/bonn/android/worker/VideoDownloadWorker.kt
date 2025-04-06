package de.app.bonn.android.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
) : CoroutineWorker(context, workerParams) {

    private var video_name: String? = ""
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "video_download_channel"
    private val notificationId = 1002

    override suspend fun doWork(): Result {
        val videoUrl = inputData.getString("video_url") ?: return Result.failure()
        video_name = inputData.getString("video_name") ?: return Result.failure()
        Timber.i("Video URL: $videoUrl")
        Timber.i("Video Name: $video_name")

        val file = File(applicationContext.getExternalFilesDir(null), "$video_name.mp4")

        if (!file.exists()) {
            ensureDownloadChannel()

            val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(
                    notificationId,
                    buildDownloadNotification(0),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                ForegroundInfo(notificationId, buildDownloadNotification(0))
            }
            setForeground(foregroundInfo)

            val success = downloadVideo(videoUrl, file) { progress ->
                notify(notificationId, buildDownloadNotification(progress))
            }

            if (success) {
                notifyWallpaperService(applicationContext)
                notify(notificationId, buildDownloadCompleteNotification())
                return Result.success()
            }

            return Result.retry()
        } else {
            Timber.i("**** file exist ${file.exists()}")
            notifyWallpaperService(applicationContext)
            return Result.retry()
        }
    }

    private fun notifyWallpaperService(context: Context) {
        val intent = Intent("UPDATE_LIVE_WALLPAPER").apply {
            setPackage("de.app.bonn.android")
            putExtra("video_name", video_name)
        }
        context.sendBroadcast(intent)
    }

    fun ensureDownloadChannel() {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows progress of video downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildDownloadNotification(progress: Int): Notification {
        Timber.d("*** Building download notification with progress: $progress")
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading video")
            .setContentText("Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    fun buildDownloadCompleteNotification(): Notification {
        Timber.d("Building download complete notification")
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Download complete")
            .setContentText("Your live wallpaper is ready")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
    }

    fun notify(id: Int = notificationId, notification: Notification) {
        Timber.d("Notifying with notification: $notification")
        notificationManager.notify(id, notification)
    }

    suspend fun downloadVideo(
        url: String,
        outputFile: File,
        onProgress: (progress: Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Downloading video from URL: $url")
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val fileLength = connection.contentLength
            if (fileLength <= 0) {
                Timber.e("File length is invalid: $fileLength")
                return@withContext false
            }

            val input = connection.inputStream
            val output = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var total: Long = 0
            var count: Int
            var lastUpdateTime = System.currentTimeMillis()
            var lastProgress = -1

            while (input.read(buffer).also { count = it } != -1) {
                total += count
                output.write(buffer, 0, count)

                val progress = ((total * 100) / fileLength).toInt()
                val now = System.currentTimeMillis()

                if ((now - lastUpdateTime > 500) && progress != lastProgress) {
                    lastUpdateTime = now
                    lastProgress = progress
                    onProgress(progress)
                }
            }

            output.flush()
            output.close()
            input.close()

            true
        } catch (e: Exception) {
            Timber.e(e, "Download failed")
            false
        }
    }

    companion object {
        fun initiate(context: Context, videoUrl: String, video_name: String) {
            val workRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf("video_url" to videoUrl, "video_name" to video_name))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "VideoDownloadWorker",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
