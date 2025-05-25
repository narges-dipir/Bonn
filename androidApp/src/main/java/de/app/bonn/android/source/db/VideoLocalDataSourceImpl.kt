package de.app.bonn.android.source.db

import de.app.bonn.android.source.db.dao.VideoDao
import de.app.bonn.android.source.db.model.VideoCached

class VideoLocalDataSourceImpl(
    private val videoDao: VideoDao
) : VideoLocalDataSource {
    override fun getLastCachedVideo(name: String): VideoCached {
        return videoDao.getLastVideo(name) ?: VideoCached(name = "", storagePath = "")
    }

    override fun updateVideo(video: VideoCached) {
        videoDao.updateVideo(video)
    }
}