package de.app.bonn.android.network.remote

import de.app.bonn.android.network.data.responde.VideoResponse
import retrofit2.Response
import javax.inject.Inject

class VideoNetworkDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : VideoNetworkDataSource {
    override suspend fun getLastVideo(deviceId: String): Response<VideoResponse> {
        return apiService.getLastVideo(deviceId)
    }
}