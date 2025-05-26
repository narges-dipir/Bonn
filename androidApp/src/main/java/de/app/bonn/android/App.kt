package de.app.bonn.android

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import de.app.bonn.android.worker.AppWorkerFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: AppWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel( Log.DEBUG )
            .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        copyVideoToExternalFiles(this, "starter")
    }

    private fun copyVideoToExternalFiles(context: Context, videoName: String) {
        val destFile = File(context.getExternalFilesDir(null), "$videoName.mp4")
        if (destFile.exists()) return

        try {
            context.assets.open("$videoName.mp4").use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Timber.tag("VideoCopy").e("Failed to copy video from assets: ${e.message}")
        }
    }

}
