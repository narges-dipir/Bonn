package de.app.bonn.android.source.db.dao

import androidx.room.Query
import de.app.bonn.android.source.db.model.VideoCached


interface VideoDao {
    @Query("SELECT * FROM VideoCached WHERE name = :name ORDER BY id DESC LIMIT 1")
    fun getLastVideo(name: String): VideoCached
}