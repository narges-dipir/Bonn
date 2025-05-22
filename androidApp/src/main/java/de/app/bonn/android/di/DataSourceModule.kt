package de.app.bonn.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.app.bonn.android.network.remote.ApiService
import de.app.bonn.android.network.remote.VideoNetworkDataSource
import de.app.bonn.android.network.remote.VideoNetworkDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideVideoNetworkDataSource(apiService: ApiService): VideoNetworkDataSource {
        return VideoNetworkDataSourceImpl(apiService = apiService)
    }
}