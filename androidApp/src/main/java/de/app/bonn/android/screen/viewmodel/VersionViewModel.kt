package de.app.bonn.android.screen.viewmodel


import androidx.lifecycle.ViewModel
import de.app.bonn.android.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import de.app.bonn.android.BuildConfig
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.domain.version.CheckVersionUseCase
import de.app.bonn.android.network.data.VersionRequest
import de.app.bonn.android.network.data.responde.VersionDecider
import de.app.bonn.android.screen.state.VersionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject



@HiltViewModel
class VersionViewModel @Inject constructor(
    private val checkVersionUseCase: CheckVersionUseCase,
    private val deviceIdProvider: DeviceIdProvider
) : ViewModel() {

    private val _versionState = MutableStateFlow(VersionState())
    val versionState = _versionState.asStateFlow()


    suspend fun getLatestVersion() {
        val deviceId = deviceIdProvider.getDeviceId()
        val version = BuildConfig.VERSION_CODE
        when(val result = checkVersionUseCase(VersionRequest(deviceId, version))) {
            is Result.Success -> {
                _versionState.value = VersionState(
                    version = result.data,
                    isLoading = false,
                    error = null
                )
            }
            is Result.Error -> {
                _versionState.value = VersionState(
                    isLoading = false,
                    error = result.message
                )
            }
            is Result.Loading -> {
                _versionState.value = VersionState(isLoading = true)
            }
        }
    }
}