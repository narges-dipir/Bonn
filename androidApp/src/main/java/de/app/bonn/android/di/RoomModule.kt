package de.app.bonn.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.app.bonn.android.source.db.BunnDatabase
import de.app.bonn.android.source.db.dao.VideoDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideVideoDao(database: BunnDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    @Singleton
    fun provideBunnDatabase(
        @ApplicationContext context: Context
    ): BunnDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            BunnDatabase::class.java,
            "bunn_database"
        ).build()
    }
}