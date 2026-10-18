package com.revlens.app.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.toUiText
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager
) : MviViewModel<LoginUiState, LoginEvent, LoginEffect>(LoginUiState()) {

    override fun onEvent(event: LoginEvent) = when (event) {
        is LoginEvent.GoogleSignInClicked -> signInWithGoogle(event.idToken, event.email, event.displayName)
        LoginEvent.ContinueAsGuestClicked -> continueAsGuest()
    }

    private fun signInWithGoogle(idToken: String? = null, email: String? = null, displayName: String? = null) {
        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }
            val result = loginUseCase.loginWithGoogle(idToken, email, displayName)
            result.onSuccess {
                emit(LoginEffect.NavigateToDashboard)
            }
            result.onFailure {
                emit(LoginEffect.ShowMessage(it.toUiText()))
                update { copy(error = it.toUiText()) }
            }
            update { copy(isLoading = false) }
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            sessionManager.continueAsGuest()
            emit(LoginEffect.NavigateToDashboard)
        }
    }
}
