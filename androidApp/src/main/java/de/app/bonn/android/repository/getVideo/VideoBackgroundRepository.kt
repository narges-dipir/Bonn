package de.app.bonn.android.repository.getVideo

import de.app.bonn.android.common.Result
import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.source.db.model.VideoCached
import kotlinx.coroutines.flow.Flow

interface VideoBackgroundRepository {
 suspend fun getRemoteLastVideo(deviceId: String): Result<Video>
 suspend fun getCachedLastVideo(name: String) : Result<VideoCached>
 suspend fun updateCachedLastVideo(video: Video)
}