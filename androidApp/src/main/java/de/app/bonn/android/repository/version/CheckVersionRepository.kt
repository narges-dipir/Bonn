package de.app.bonn.android.repository.version

import de.app.bonn.android.common.Result
import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionDecider

interface CheckVersionRepository {
    suspend fun checkVersion(versionRequest: VersionRequest): Result<VersionDecider>
}