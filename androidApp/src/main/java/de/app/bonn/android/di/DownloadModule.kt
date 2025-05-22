package de.app.bonn.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.app.bonn.android.firebase.NotificationHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {


    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}