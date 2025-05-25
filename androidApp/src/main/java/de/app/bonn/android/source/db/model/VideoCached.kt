package de.app.bonn.android.source.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoCached(
    @PrimaryKey
    val name: String,
    val storagePath: String,
)
