package de.app.bonn.android.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.app.bonn.android.common.LAST_VIDEO_NAME
import de.app.bonn.android.di.SharedPreferencesHelper
import de.app.bonn.android.domain.video.UpdateCachedLastVideoUseCase
import de.app.bonn.android.network.data.responde.VideoDecider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class VideoDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateCachedLastVideoUseCase: UpdateCachedLastVideoUseCase
) : CoroutineWorker(context, workerParams), VideoDownloadWorkerInterface {

    @AssistedFactory
    interface Factory {
        fun create(context: Context, workerParams: WorkerParameters): VideoDownloadWorker
    }

    private val _downloadedVideo = MutableSharedFlow<VideoDecider>()
    override val downloadedVideo: Flow<VideoDecider> = _downloadedVideo

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

        val file = File(applicationContext.getExternalFilesDir(null), "$video_name")

        // Ensure channel and set initial foreground
        ensureDownloadChannel()
        setForeground(getForegroundInfo())

        val success = downloadVideo(videoUrl, file) { progress ->
            notify(notificationId, buildDownloadNotification(progress))
        }

        return if (success) {
            updateCachedLastVideoUseCase(
                VideoDecider(name = video_name!!, video = file.absolutePath, isCacheAvailable = true)
            )
            Result.success()
        } else {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        ensureDownloadChannel()
        val notification = buildDownloadNotification(0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun ensureDownloadChannel() {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of video downloads"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildDownloadNotification(progress: Int): Notification {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading video")
            .setContentText("$video_name - Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun buildDownloadCompleteNotification(): Notification {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Download complete")
            .setContentText("$video_name - Your live wallpaper is ready")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun notifyWallpaperService(videoName: String, context: Context) {
        val intent = Intent("UPDATE_LIVE_WALLPAPER").apply {
            setPackage("de.app.bonn.android")
            putExtra("video_name", videoName)
        }
        SharedPreferencesHelper.putString(LAST_VIDEO_NAME, videoName)
        context.sendBroadcast(intent)
    }

    fun notify(id: Int = notificationId, notification: Notification) {
        try {
            notificationManager.notify(id, notification)
        } catch (e: Exception) {
            Timber.e(e, "Failed to show notification")
        }
    }

    suspend fun downloadVideo(
        url: String,
        outputFile: File,
        onProgress: (progress: Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()

            val fileLength = connection.contentLength
            if (fileLength <= 0) return@withContext false

            val input = connection.inputStream
            val output = FileOutputStream(outputFile)

            val buffer = ByteArray(8192)
            var total: Long = 0
            var count: Int
            var lastUpdateTime = System.currentTimeMillis()
            var lastProgress = -1

            while (input.read(buffer).also { count = it } != -1) {
                if (isStopped) {
                    input.close()
                    output.close()
                    outputFile.delete()
                    return@withContext false
                }

                total += count
                output.write(buffer, 0, count)

                val progress = ((total * 100) / fileLength).toInt()
                val now = System.currentTimeMillis()

                if ((now - lastUpdateTime > 1000) && progress != lastProgress) {
                    lastUpdateTime = now
                    lastProgress = progress
                    onProgress(progress)
                }
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()

            onProgress(100)
            true
        } catch (e: Exception) {
            Timber.e(e, "Download failed")
            outputFile.delete()
            false
        }
    }

    companion object {
        fun initiate(context: Context, videoUrl: String, video_name: String) {
            val workRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf("video_url" to videoUrl, "video_name" to video_name))
                .addTag("video_download")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "VideoDownloadWorker_$video_name",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
