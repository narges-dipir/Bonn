package de.app.bonn.android.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("device/register")
    suspend fun registerToken(@Body request: TokenRequest): Response<Unit>
}