package de.app.bonn.android.repository.getVideo

import de.app.bonn.android.common.Result
import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.network.data.responde.VideoResponse

interface VideoBackgroundRepository {
 suspend fun getLastVideo(deviceId: String): Result<Video>
}