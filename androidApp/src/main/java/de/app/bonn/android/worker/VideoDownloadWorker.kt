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
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.app.bonn.android.common.LAST_VIDEO_NAME
import de.app.bonn.android.di.SharedPreferencesHelper
import de.app.bonn.android.domain.video.UpdateCachedLastVideoUseCase
import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.repository.getVideo.VideoBackgroundRepository
import de.app.bonn.android.source.db.BunnDatabase
import de.app.bonn.android.source.db.VideoLocalDataSource
import de.app.bonn.android.source.db.dao.VideoDao
import de.app.bonn.android.source.db.model.VideoCached
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


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

        if (!file.exists()) {
            ensureDownloadChannel()

            // Set foreground immediately and maintain it
            val initialForegroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(
                    notificationId,
                    buildDownloadNotification(0),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                ForegroundInfo(notificationId, buildDownloadNotification(0))
            }
            setForeground(initialForegroundInfo)

            println("*** Starting download for $video_name ***")
            val success = downloadVideo(videoUrl, file) { progress ->
                notify(notificationId, buildDownloadNotification(progress))
            }
            println(" *** Download completed for $video_name ${file.absolutePath} ***")

            if (success) {
                updateCachedLastVideoUseCase(VideoDecider(name = video_name!!, video = file.absolutePath, isCacheAvailable = true))
                return Result.success()
            }

            return Result.retry()
        } else {
            notifyWallpaperService(video_name!!, context = applicationContext)
            return Result.retry()
        }
    }

    fun notifyWallpaperService(videoName: String, context: Context) {
        println(" **** Sending broadcast for $videoName from worker")
        val intent = Intent("UPDATE_LIVE_WALLPAPER").apply {
            setPackage("de.app.bonn.android")
            putExtra("video_name", videoName)
        }
        SharedPreferencesHelper.putString(LAST_VIDEO_NAME, videoName)
        context.sendBroadcast(intent)
    }

    fun ensureDownloadChannel() {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_LOW // Changed from HIGH to LOW to prevent auto-dismissal
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
        Timber.d("*** Building download notification with progress: $progress")
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading video")
            .setContentText("$video_name - Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setAutoCancel(false) // Don't allow user to dismiss
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority to prevent auto-dismissal
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // Keep as foreground
            .build()
    }

    fun buildDownloadCompleteNotification(): Notification {
        Timber.d("Building download complete notification")
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Download complete")
            .setContentText("$video_name - Your live wallpaper is ready")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun notify(id: Int = notificationId, notification: Notification) {
        Timber.d("Notifying with notification: $notification")
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
            Timber.d("Downloading video from URL: $url")
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 30000
            connection.connect()

            val fileLength = connection.contentLength
            if (fileLength <= 0) {
                Timber.e("File length is invalid: $fileLength")
                return@withContext false
            }

            val input = connection.inputStream
            val output = FileOutputStream(outputFile)

            val buffer = ByteArray(8192) // Increased buffer size
            var total: Long = 0
            var count: Int
            var lastUpdateTime = System.currentTimeMillis()
            var lastProgress = -1

            while (input.read(buffer).also { count = it } != -1) {
                // Check if work is stopped
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

                // Update progress more frequently but throttle updates
                if ((now - lastUpdateTime > 1000) && progress != lastProgress) { // Update every 1 second
                    lastUpdateTime = now
                    lastProgress = progress
                    onProgress(progress)
                }
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()

            // Final progress update
            onProgress(100)

            true
        } catch (e: Exception) {
            Timber.e(e, "Download failed")
            outputFile.delete() // Clean up partial file
            false
        }
    }

    companion object {
        fun initiate(context: Context, videoUrl: String, video_name: String) {
            val workRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf("video_url" to videoUrl, "video_name" to video_name))
                .addTag("video_download") // Add tag for easier management
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "VideoDownloadWorker_$video_name", // Make unique per video
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
