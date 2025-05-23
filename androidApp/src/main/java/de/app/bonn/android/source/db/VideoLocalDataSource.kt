package de.app.bonn.android.source.db

import de.app.bonn.android.source.db.model.VideoCached

interface VideoLocalDataSource {
    fun getLastCachedVideo(name: String): VideoCached
}