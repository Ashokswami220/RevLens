package com.example.revlens.ui.screens.auth

import com.example.revlens.core.ui.UiText

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSessionExpiredNotice: Boolean = false
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotEmpty() && !isLoading
}

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object TogglePasswordVisibility : LoginEvent
    data object Submit : LoginEvent
    data object ForgotPasswordClicked : LoginEvent
    data object SignUpClicked : LoginEvent
    data object ContinueAsGuestClicked : LoginEvent
}

sealed interface LoginEffect {
    data class ShowMessage(val text: UiText) : LoginEffect
    data object GoToForgotPassword : LoginEffect
    data object GoToSignUp : LoginEffect
}
