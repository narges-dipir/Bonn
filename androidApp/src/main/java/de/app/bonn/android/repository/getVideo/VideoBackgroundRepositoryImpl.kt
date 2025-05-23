package de.app.bonn.android.repository.getVideo

import de.app.bonn.android.common.Result
import de.app.bonn.android.di.IoDispatcher
import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.network.remote.VideoNetworkDataSource
import de.app.bonn.android.repository.toVideo
import de.app.bonn.android.source.db.VideoLocalDataSource
import de.app.bonn.android.source.db.model.VideoCached
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class VideoBackgroundRepositoryImpl @Inject constructor(
    private val videoNetworkDataSource: VideoNetworkDataSource,
    private val videoLocalDataSource: VideoLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VideoBackgroundRepository {

    suspend fun getLastVideo(): VideoDecider {
        val remoteVideo = getRemoteLastVideo("")
        if (remoteVideo is Result.Success) {
            val video = remoteVideo.data
            val cachedVideo = getCachedLastVideo(video.name)
            if (cachedVideo is Result.Success) {
                return VideoDecider(
                    isCacheAvailable = true,
                    video = video.name,
                    name = video.name
                )
            } else {
                return VideoDecider(
                    isCacheAvailable = false,
                    video = video.url,
                    name = video.name
                )
            }
        } else {
            return VideoDecider(name = "", video = "", isCacheAvailable = false)
        }
    }
    override suspend fun getRemoteLastVideo(deviceId: String): Result<Video> = withContext(ioDispatcher) {
        try {
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

    override suspend fun getCachedLastVideo(name: String): Result<VideoCached> {
        val cachedVideo = videoLocalDataSource.getLastCachedVideo(name)
        if (cachedVideo.storagePath.isNotEmpty() && doesVideoExist( cachedVideo.storagePath, videoName = name)) {
            return Result.Success(cachedVideo)
        } else {
            return Result.Error("Cached video not found")
        }

    }

    override suspend fun updateCachedLastVideo(video: Video) {
        TODO("Not yet implemented")
    }

    fun doesVideoExist(storagePath: String, videoName: String): Boolean {
        val file = File(storagePath, "$videoName.mp4")
        return file.exists()
    }

}