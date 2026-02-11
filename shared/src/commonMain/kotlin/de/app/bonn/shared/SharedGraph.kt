package de.app.bonn.shared

import de.app.bonn.shared.data.KtorBunnRemoteRepository
import de.app.bonn.shared.domain.CheckVersionUseCase
import de.app.bonn.shared.domain.GetLastVideoUseCase
import de.app.bonn.shared.domain.GetTermsOfServiceUseCase
import de.app.bonn.shared.presentation.SharedHomeViewModel

object SharedGraph {
    private val repository by lazy { KtorBunnRemoteRepository() }

    fun createHomeViewModel(): SharedHomeViewModel {
        return SharedHomeViewModel(
            checkVersionUseCase = CheckVersionUseCase(repository),
            getLastVideoUseCase = GetLastVideoUseCase(repository),
            getTermsOfServiceUseCase = GetTermsOfServiceUseCase(repository),
        )
    }
}
