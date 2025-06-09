package de.app.bonn.android.network.remote

import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionResponse
import retrofit2.Response
import javax.inject.Inject

class AppVersionDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : AppVersionDataSource {
    override suspend fun checkVersion(versionRequest: VersionRequest): Response<VersionResponse> {
        return apiService.checkVersion(versionRequest)
    }
}