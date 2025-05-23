package de.app.bonn.android.source.db

import androidx.room.Database
import androidx.room.RoomDatabase
import de.app.bonn.android.source.db.dao.VideoDao
import de.app.bonn.android.source.db.model.VideoCached

@Database(version = 1, entities = [VideoCached::class])
abstract class BunnDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}