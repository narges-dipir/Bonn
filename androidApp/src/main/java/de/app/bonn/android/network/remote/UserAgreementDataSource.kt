package de.app.bonn.android.network.remote

import de.app.bonn.android.common.Result

interface UserAgreementDataSource {
    suspend fun getTermsOfService(): Result<String>
}