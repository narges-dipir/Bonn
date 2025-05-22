package de.app.bonn.android.repository.getVideo

import de.app.bonn.android.common.Result
import de.app.bonn.android.di.IoDispatcher
import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.network.data.responde.VideoResponse
import de.app.bonn.android.network.remote.VideoNetworkDataSource
import de.app.bonn.android.repository.toVideo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoBackgroundRepositoryImpl @Inject constructor(
    private val videoNetworkDataSource: VideoNetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VideoBackgroundRepository {
    override suspend fun getLastVideo(deviceId: String): Result<Video> {
        return try {
            withContext(ioDispatcher) {
                val response = videoNetworkDataSource.getLastVideo(deviceId = deviceId)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Result.Success(body.toVideo())
                } else {
                    Result.Error("Unable to fetch video")
                }
            }
        } catch (e: Exception) {
            Result.Error("Unable to fetch video")
        }
    }
}