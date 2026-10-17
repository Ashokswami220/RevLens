package com.example.revlens.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.core.ui.UiText
import com.example.revlens.core.ui.toUiText
import com.example.revlens.core.util.Validators
import com.example.revlens.domain.session.SessionManager
import com.example.revlens.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager
) : MviViewModel<LoginUiState, LoginEvent, LoginEffect>(LoginUiState()) {

    override fun onEvent(event: LoginEvent) = when (event) {
        is LoginEvent.EmailChanged -> update { copy(email = event.value, emailError = null) }
        is LoginEvent.PasswordChanged -> update { copy(password = event.value, passwordError = null) }
        LoginEvent.TogglePasswordVisibility -> update { copy(isPasswordVisible = !isPasswordVisible) }
        LoginEvent.ForgotPasswordClicked -> emit(LoginEffect.GoToForgotPassword)
        LoginEvent.SignUpClicked -> emit(LoginEffect.GoToSignUp)
        LoginEvent.ContinueAsGuestClicked -> continueAsGuest()
        LoginEvent.Submit -> submit()
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            sessionManager.continueAsGuest()
        }
    }

    private fun submit() {
        val s = state.value
        val emailErr = Validators.email(s.email)
        if (emailErr != null) {
            update { copy(emailError = emailErr) }
            return
        }

        if (s.password.isEmpty()) {
            update { copy(passwordError = UiText.DynamicString("Password cannot be empty")) }
            return
        }

        viewModelScope.launch {
            update { copy(isLoading = true) }
            val result = loginUseCase(s.email.trim(), s.password)
            result.onFailure {
                emit(LoginEffect.ShowMessage(it.toUiText()))
            }
            update { copy(isLoading = false) }
        }
    }
}
