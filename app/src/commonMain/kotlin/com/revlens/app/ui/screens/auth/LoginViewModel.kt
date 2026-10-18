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
        LoginEvent.GoogleSignInClicked -> signInWithGoogle()
        LoginEvent.ContinueAsGuestClicked -> continueAsGuest()
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }
            val result = loginUseCase.loginWithGoogle()
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
        }
    }
}
