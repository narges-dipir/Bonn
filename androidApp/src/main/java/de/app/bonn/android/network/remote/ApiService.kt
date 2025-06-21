package de.app.bonn.android.network.remote

import de.app.bonn.android.network.data.TokenRequest
import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionResponse
import de.app.bonn.android.network.data.responde.VideoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("device/register")
    suspend fun registerToken(@Body request: TokenRequest): Response<Unit>

    @GET("device/last_video")
    suspend fun getLastVideo(@Query("userId") deviceId: String): Response<VideoResponse>

    @POST("api/version/check")
    suspend fun checkVersion(@Body request: VersionRequest) : Response<VersionResponse>

    @GET("api/info/terms")
    suspend fun getTermsOfService(): Response<ResponseBody>

}