package de.app.bonn.android.repository.getVideo

import de.app.bonn.android.common.Result
import de.app.bonn.android.di.IoDispatcher
import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.network.remote.VideoNetworkDataSource
import de.app.bonn.android.repository.toVideo
import de.app.bonn.android.repository.toVideoCached
import de.app.bonn.android.repository.toVideoDecider
import de.app.bonn.android.source.db.VideoLocalDataSource
import de.app.bonn.android.source.db.model.VideoCached
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class VideoBackgroundRepositoryImpl @Inject constructor(
    private val videoNetworkDataSource: VideoNetworkDataSource,
    private val videoLocalDataSource: VideoLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VideoBackgroundRepository {
    private val _newVideo = MutableSharedFlow<VideoDecider>()
    override val newVideo: Flow<VideoDecider> = _newVideo
    override fun getLastVideo(deviceId: String): Flow<Result<VideoDecider>> = flow {
        val remoteVideo = getRemoteLastVideo(deviceId)
        if (remoteVideo is Result.Success) {
            val video = remoteVideo.data
            val cachedVideo = getCachedLastVideo(video.name)
            if (cachedVideo is Result.Success) {
                emit(Result.Success( VideoDecider(
                    isCacheAvailable = true,
                    video = video.name,
                    name = video.name
                )
                ))
            } else {
                emit( Result.Success( VideoDecider(
                    isCacheAvailable = false,
                    video = video.url,
                    name = video.name
                )
                )
                )
            }
        } else {
            emit(Result.Error("Unable to fetch video"))
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

    override suspend fun getCachedLastVideo(name: String): Result<VideoCached> = withContext(ioDispatcher) {
        val cachedVideo = videoLocalDataSource.getLastCachedVideo(name)
        return@withContext if (cachedVideo.storagePath != "" && doesVideoExist(cachedVideo.storagePath, videoName = name)) {
            Result.Success(cachedVideo)
        } else {
            Result.Error("Cached video not found")
        }
    }

    override fun updateCachedLastVideo(videoDecider: VideoDecider) {
        videoLocalDataSource.updateVideo(videoDecider.toVideoCached())
        val video = videoLocalDataSource.getLastCachedVideo(videoDecider.name)
        _newVideo.tryEmit(video.toVideoDecider())
    }

    override fun updateBackGroundVideo(videoName: String): Flow<Result<VideoDecider>> = flow {
        val video = videoLocalDataSource.getLastCachedVideo(videoName)
        emit(Result.Success(video.toVideoDecider()))
    }

    private fun doesVideoExist(storagePath: String, videoName: String): Boolean {
        val file = File(storagePath, "$videoName.mp4")
        return file.exists()
    }

}