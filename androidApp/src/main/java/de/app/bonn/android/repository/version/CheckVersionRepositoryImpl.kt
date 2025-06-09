package de.app.bonn.android.repository.version

import de.app.bonn.android.common.Result
import de.app.bonn.android.di.IoDispatcher
import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionDecider
import de.app.bonn.android.network.data.responde.VersionResponse
import de.app.bonn.android.network.remote.AppVersionDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class CheckVersionRepositoryImpl @Inject constructor(
    private val appVersionDataSource: AppVersionDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CheckVersionRepository {
    override suspend fun checkVersion(versionRequest: VersionRequest): Result<VersionDecider> = withContext(ioDispatcher) {
        val response = appVersionDataSource.checkVersion(versionRequest)
         if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.Success(body.toVersionDecider())
            } else {
                Result.Error("Response body is null")
            }
        } else {
            Result.Error("Error checking version: ${response.message()}")
        }
    }


    private fun VersionResponse.toVersionDecider(): VersionDecider {
        return VersionDecider(
            mustUpdate = this.mustUpdate,
            shouldUpdate = this.shouldUpdate,
            minVersion = this.minVersion,
            latestVersion = this.latestVersion,
            message = this.message,
            playStoreUrl = this.playStoreUrl
        )
    }
}