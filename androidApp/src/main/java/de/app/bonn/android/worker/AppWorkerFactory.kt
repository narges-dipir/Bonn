package de.app.bonn.android.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import timber.log.Timber
import javax.inject.Inject

class AppWorkerFactory @Inject constructor(
    private val videoDownloadWorkerFactory: VideoDownloadWorker.Factory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Timber.d("WorkerFactory", "Creating worker for: $workerClassName")
        return when (workerClassName) {
            VideoDownloadWorker::class.java.name ->
                videoDownloadWorkerFactory.create(appContext, workerParameters)
            else -> null
        }
    }
}
