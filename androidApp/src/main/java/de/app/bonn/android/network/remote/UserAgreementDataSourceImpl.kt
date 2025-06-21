package de.app.bonn.android.network.remote

import de.app.bonn.android.common.Result
import de.app.bonn.android.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class UserAgreementDataSourceImpl (
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserAgreementDataSource {
    override suspend fun  getTermsOfService(): Result<String> = withContext(ioDispatcher) {
        try {
            withContext(ioDispatcher) {
                val response = apiService.getTermsOfService()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Result.Success(body.string())
                } else {
                    Result.Error("Unable to fetch video")
                }
            }
        } catch (e: Exception) {
            Result.Error("Unable to fetch video")
        }
    }
}