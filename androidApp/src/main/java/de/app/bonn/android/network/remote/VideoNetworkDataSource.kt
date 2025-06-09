package de.app.bonn.android.network.remote

import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VideoResponse
import retrofit2.Response

interface VideoNetworkDataSource {
    suspend fun getLastVideo(deviceId: String): Response<VideoResponse>
}