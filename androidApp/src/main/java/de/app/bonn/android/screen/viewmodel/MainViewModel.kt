package de.app.bonn.android.screen.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.app.bonn.android.common.Result
import de.app.bonn.android.domain.info.GetUserAgreementUseCase
import de.app.bonn.android.screen.state.UserAgreementState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserAgreementUseCase: GetUserAgreementUseCase,
) : ViewModel() {
    var hasResumedOnce = mutableStateOf(false)

    private val _userAgreementState = MutableStateFlow(UserAgreementState())
    val userAgreementState = _userAgreementState.asStateFlow()

    init {
//        viewModelScope.launch {
//            getUserAgreement()
//        }
    }

    private suspend fun getUserAgreement() {
        val result = getUserAgreementUseCase()
        when (result) {
            is Result.Success -> {
                _userAgreementState.value = UserAgreementState(
                    userAgreement = result.data,
                    isLoading = false,
                    error = null
                )
            }
            is Result.Error -> {
                _userAgreementState.value = UserAgreementState(
                    isLoading = false,
                    error = result.message
                )
            }
            is Result.Loading -> {
                _userAgreementState.value = UserAgreementState(isLoading = true)
            }
        }
    }
}
