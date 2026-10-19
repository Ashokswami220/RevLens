package com.revlens.app.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.toUiText
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.LoginUseCase
import com.revlens.app.model.createDefaultGuestProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository? = null
) : MviViewModel<LoginUiState, LoginEvent, LoginEffect>(LoginUiState()) {

    override fun onEvent(event: LoginEvent) = when (event) {
        is LoginEvent.GoogleSignInClicked -> signInWithGoogle(
            event.idToken, event.email, event.displayName
        )
    }

    private fun signInWithGoogle(
        idToken: String? = null, email: String? = null, displayName: String? = null
    ) {
        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }
            val result = loginUseCase.loginWithGoogle(idToken, email, displayName)
            result.onSuccess {
                val remoteProf = profileRepository?.syncRemoteProfile()
                val localProf = profileRepository?.profileFlow?.first()
                if (remoteProf != null || localProf != null) {
                    profileRepository?.setOnboardingCompleted(true)
                    emit(LoginEffect.NavigateToDashboard)
                } else {
                    emit(LoginEffect.NavigateToCreateWorkspace)
                }
            }
            result.onFailure {
                emit(LoginEffect.ShowMessage(it.toUiText()))
                update { copy(error = it.toUiText()) }
            }
            update { copy(isLoading = false) }
        }
    }
}
