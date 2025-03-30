package de.app.bonn.android.download

import android.content.Context
import androidx.work.impl.utils.forAll
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun downloadVideo(
        url: String,
        outputFile: File,
        onProgress: (progress: Int) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val fileLength = connection.contentLength
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
            e.printStackTrace()
            false
        }
    }
}