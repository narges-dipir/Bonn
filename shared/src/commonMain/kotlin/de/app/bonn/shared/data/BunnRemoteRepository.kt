package de.app.bonn.shared.data

import de.app.bonn.shared.AppResult
import de.app.bonn.shared.model.TokenRequest
import de.app.bonn.shared.model.VersionDecider
import de.app.bonn.shared.model.VersionRequest
import de.app.bonn.shared.model.VideoDecider

interface BunnRemoteRepository {
    suspend fun registerToken(request: TokenRequest): AppResult<Unit>
    suspend fun getLastVideo(deviceId: String): AppResult<VideoDecider>
    suspend fun checkVersion(request: VersionRequest): AppResult<VersionDecider>
    suspend fun getTermsOfService(): AppResult<String>
}
