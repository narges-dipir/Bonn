package de.app.bonn.shared.data

import de.app.bonn.shared.AppResult
import de.app.bonn.shared.model.TokenRequest
import de.app.bonn.shared.model.VersionDecider
import de.app.bonn.shared.model.VersionRequest
import de.app.bonn.shared.model.VersionResponse
import de.app.bonn.shared.model.VideoDecider
import de.app.bonn.shared.model.VideoResponse
import de.app.bonn.shared.network.platformHttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorBunnRemoteRepository(
    private val baseUrl: String = "https://api.bunn.live/",
) : BunnRemoteRepository {

    private val client = platformHttpClient().config {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
    }

    override suspend fun registerToken(request: TokenRequest): AppResult<Unit> {
        return safeCall {
            val response = client.post("${baseUrl}device/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error("Register token failed: ${response.status}")
            }
        }
    }

    override suspend fun getLastVideo(deviceId: String): AppResult<VideoDecider> {
        return safeCall {
            val body = client.get("${baseUrl}device/last_video") {
                parameter("userId", deviceId)
            }.body<VideoResponse>()
            AppResult.Success(
                VideoDecider(
                    isCacheAvailable = false,
                    video = body.url,
                    silentUrl = body.silentUrl,
                    name = body.name,
                ),
            )
        }
    }

    override suspend fun checkVersion(request: VersionRequest): AppResult<VersionDecider> {
        return safeCall {
            val body = client.post("${baseUrl}api/version/check") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<VersionResponse>()

            AppResult.Success(
                VersionDecider(
                    mustUpdate = body.mustUpdate,
                    shouldUpdate = body.shouldUpdate,
                    minVersion = body.minVersion,
                    latestVersion = body.latestVersion,
                    message = body.message,
                    playStoreUrl = body.playStoreUrl,
                ),
            )
        }
    }

    override suspend fun getTermsOfService(): AppResult<String> {
        return safeCall {
            val body = client.get("${baseUrl}api/info/terms").body<String>()
            AppResult.Success(body)
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> AppResult<T>): AppResult<T> {
        return try {
            block()
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unknown network error")
        }
    }
}
