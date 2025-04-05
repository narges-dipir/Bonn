package de.app.bonn.android

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

//    override val workManagerConfiguration: Configuration
//        get() = Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()

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
