package de.app.bonn.android.source.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import de.app.bonn.android.source.db.model.VideoCached

@Dao
interface VideoDao {
    @Query("SELECT * FROM VideoCached WHERE name = :name")
    fun getLastVideo(name: String): VideoCached?

    @Transaction
    fun updateVideo(video: VideoCached) {
        insertVideo(video)
    }

    @Insert (onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun insertVideo(video: VideoCached)
}