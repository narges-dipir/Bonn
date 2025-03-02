package de.app.bonn.android.worker

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class VideoDownloadWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    override fun doWork(): Result {
        val videoUrl = "https://videos.pexels.com/video-files/9669111/9669111-hd_1080_1920_25fps.mp4"
        val file = File(applicationContext.getExternalFilesDir(null), "live_wallpaper.mp4")

        return try {
            downloadFile(videoUrl, file)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun downloadFile(url: String, outputFile: File) {
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
    companion object {
       fun initiate(context: Context) {
           val workRequest = OneTimeWorkRequest.Builder(VideoDownloadWorker::class.java).build()
           WorkManager.getInstance(context).enqueue(workRequest)
       }
    }
}