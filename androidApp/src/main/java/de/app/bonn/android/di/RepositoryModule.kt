package de.app.bonn.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.app.bonn.android.network.remote.AppVersionDataSource
import de.app.bonn.android.network.remote.VideoNetworkDataSource
import de.app.bonn.android.repository.getVideo.VideoBackgroundRepository
import de.app.bonn.android.repository.getVideo.VideoBackgroundRepositoryImpl
import de.app.bonn.android.repository.version.CheckVersionRepository
import de.app.bonn.android.repository.version.CheckVersionRepositoryImpl
import de.app.bonn.android.source.db.VideoLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCheckVersionRepository(
        appVersionDataSource: AppVersionDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : CheckVersionRepository {
        return CheckVersionRepositoryImpl(
            appVersionDataSource = appVersionDataSource,
            ioDispatcher = ioDispatcher
        )
    }

    @Provides
    @Singleton
    fun provideVideoBackgroundRepository(
        videoLocalDataSource: VideoLocalDataSource,
        videoNetworkDataSource: VideoNetworkDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : VideoBackgroundRepository {
        return VideoBackgroundRepositoryImpl(
            videoLocalDataSource = videoLocalDataSource,
            videoNetworkDataSource = videoNetworkDataSource,
            ioDispatcher = ioDispatcher
        )

    }
}