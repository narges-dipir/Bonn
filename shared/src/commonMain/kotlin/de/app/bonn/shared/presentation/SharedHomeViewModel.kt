package de.app.bonn.shared.presentation

import de.app.bonn.shared.AppResult
import de.app.bonn.shared.domain.CheckVersionUseCase
import de.app.bonn.shared.domain.GetLastVideoUseCase
import de.app.bonn.shared.domain.GetTermsOfServiceUseCase
import de.app.bonn.shared.model.VersionDecider
import de.app.bonn.shared.model.VideoDecider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SharedHomeState(
    val isLoading: Boolean = false,
    val version: VersionDecider? = null,
    val video: VideoDecider? = null,
    val termsPreview: String? = null,
    val error: String? = null,
)

class SharedHomeViewModel(
    private val checkVersionUseCase: CheckVersionUseCase,
    private val getLastVideoUseCase: GetLastVideoUseCase,
    private val getTermsOfServiceUseCase: GetTermsOfServiceUseCase,
) {
    private val job: Job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Default)

    private val _state = MutableStateFlow(SharedHomeState())
    val state: StateFlow<SharedHomeState> = _state.asStateFlow()

    fun currentState(): SharedHomeState = _state.value

    suspend fun refresh(deviceId: String, versionCode: Int) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        val versionResult = scope.async { checkVersionUseCase(deviceId, versionCode) }
        val videoResult = scope.async { getLastVideoUseCase(deviceId) }
        val termsResult = scope.async { getTermsOfServiceUseCase() }

        val version = when (val result = versionResult.await()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> {
                _state.value = _state.value.copy(error = result.message)
                null
            }
            AppResult.Loading -> null
        }

        val video = when (val result = videoResult.await()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> {
                _state.value = _state.value.copy(error = result.message)
                null
            }
            AppResult.Loading -> null
        }

        val termsPreview = when (val result = termsResult.await()) {
            is AppResult.Success -> result.data.take(180)
            is AppResult.Error -> {
                _state.value = _state.value.copy(error = result.message)
                null
            }
            AppResult.Loading -> null
        }

        _state.value = _state.value.copy(
            isLoading = false,
            version = version,
            video = video,
            termsPreview = termsPreview,
        )
    }

    fun refreshAsync(deviceId: String, versionCode: Int) {
        scope.launch {
            refresh(deviceId, versionCode)
        }
    }

    fun clear() {
        scope.cancel()
    }
}
