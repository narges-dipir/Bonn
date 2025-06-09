package de.app.bonn.android.network.remote

import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionResponse
import retrofit2.Response

interface AppVersionDataSource {

    suspend fun checkVersion(versionRequest: VersionRequest) : Response<VersionResponse>
}