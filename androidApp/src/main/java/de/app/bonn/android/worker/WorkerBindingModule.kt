package de.app.bonn.android.worker

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WorkerBindingModule {

//    @Binds
//    fun bindVideoDownloadWorkerFactory(
//        factory: VideoDownloadWorker.Factory
//    ): VideoDownloadWorker.Factory
}
