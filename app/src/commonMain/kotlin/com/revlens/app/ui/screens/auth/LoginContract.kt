package com.revlens.app.ui.screens.auth

import com.revlens.app.core.ui.UiText

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSessionExpiredNotice: Boolean = false,
    val error: UiText? = null
)

sealed interface LoginEvent {
    data object GoogleSignInClicked : LoginEvent
    data object ContinueAsGuestClicked : LoginEvent
}

sealed interface LoginEffect {
    data class ShowMessage(val text: UiText) : LoginEffect
}
