package de.app.bonn.android.domain.version

import de.app.bonn.android.common.Result
import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionDecider
import de.app.bonn.android.repository.version.CheckVersionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckVersionUseCase @Inject constructor(
    private val checkVersionRepository: CheckVersionRepository
) {
    suspend operator fun invoke(versionRequest: VersionRequest): Result<VersionDecider> {
        return checkVersionRepository.checkVersion(versionRequest = versionRequest)
    }
}