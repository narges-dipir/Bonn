package de.app.bonn.android.screen.state

data class UserAgreementState (
    val userAgreement: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)