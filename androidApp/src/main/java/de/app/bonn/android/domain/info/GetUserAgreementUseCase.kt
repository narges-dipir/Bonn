package de.app.bonn.android.domain.info

import de.app.bonn.android.common.Result
import de.app.bonn.android.network.remote.UserAgreementDataSource
import javax.inject.Inject

class GetUserAgreementUseCase @Inject constructor(
    private val userAgreementDataSource: UserAgreementDataSource
) {
    suspend operator fun invoke(): Result<String> {
        return userAgreementDataSource.getTermsOfService()
    }
}