package de.app.bonn.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.app.bonn.android.network.remote.ApiService
import de.app.bonn.android.network.remote.AppVersionDataSource
import de.app.bonn.android.network.remote.AppVersionDataSourceImpl
import de.app.bonn.android.network.remote.UserAgreementDataSource
import de.app.bonn.android.network.remote.UserAgreementDataSourceImpl
import de.app.bonn.android.network.remote.VideoNetworkDataSource
import de.app.bonn.android.network.remote.VideoNetworkDataSourceImpl
import de.app.bonn.android.source.db.VideoLocalDataSource
import de.app.bonn.android.source.db.VideoLocalDataSourceImpl
import de.app.bonn.android.source.db.dao.VideoDao
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideUserAgreementDataSource(apiService: ApiService,
                                       @IoDispatcher ioDispatcher: CoroutineDispatcher): UserAgreementDataSource {
        return UserAgreementDataSourceImpl(apiService = apiService, ioDispatcher = ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideAppVersionDataSource(apiService: ApiService): AppVersionDataSource {
        return AppVersionDataSourceImpl(apiService = apiService)
    }

    @Provides
    @Singleton
    fun provideVideoNetworkDataSource(apiService: ApiService): VideoNetworkDataSource {
        return VideoNetworkDataSourceImpl(apiService = apiService)
    }

    @Provides
    @Singleton
    fun provideVideoLocalDataSource(
        videoDao: VideoDao
    ): VideoLocalDataSource {
        return VideoLocalDataSourceImpl(videoDao = videoDao)
    }
}