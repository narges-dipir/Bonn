package de.app.bonn.shared.domain

import de.app.bonn.shared.AppResult
import de.app.bonn.shared.data.BunnRemoteRepository
import de.app.bonn.shared.model.VersionDecider
import de.app.bonn.shared.model.VersionRequest
import de.app.bonn.shared.model.VideoDecider

class CheckVersionUseCase(
    private val repository: BunnRemoteRepository,
) {
    suspend operator fun invoke(deviceId: String, versionCode: Int): AppResult<VersionDecider> {
        return repository.checkVersion(VersionRequest(deviceId = deviceId, version = versionCode))
    }
}

class GetLastVideoUseCase(
    private val repository: BunnRemoteRepository,
) {
    suspend operator fun invoke(deviceId: String): AppResult<VideoDecider> {
        return repository.getLastVideo(deviceId)
    }
}

class GetTermsOfServiceUseCase(
    private val repository: BunnRemoteRepository,
) {
    suspend operator fun invoke(): AppResult<String> {
        return repository.getTermsOfService()
    }
}
