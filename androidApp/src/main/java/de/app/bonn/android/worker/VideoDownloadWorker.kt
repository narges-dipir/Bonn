package de.app.bonn.android.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import de.app.bonn.android.service.VideoLiveWallpaperService
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class VideoDownloadWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    override fun doWork(): Result {
        val videoUrl = inputData.getString("video_url") ?: return Result.failure()
        val file = File(applicationContext.getExternalFilesDir(null), "live_wallpaper.mp4")

        return try {
            downloadFile(videoUrl, file)
            // restartWallpaperService(applicationContext)
            notifyWallpaperService(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun downloadFile(url: String, outputFile: File) {
        Log.i("url", url)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream
        val outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        outputStream.close()
        inputStream.close()
    }
    private fun restartWallpaperService(context: Context) {
        val serviceIntent = Intent(context, VideoLiveWallpaperService::class.java)
        context.stopService(serviceIntent)
        context.startForegroundService(serviceIntent)
    }
    private fun notifyWallpaperService(context: Context) {
        val intent = Intent("UPDATE_LIVE_WALLPAPER")
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