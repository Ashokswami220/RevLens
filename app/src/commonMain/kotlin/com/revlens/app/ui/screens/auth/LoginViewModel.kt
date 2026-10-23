package com.revlens.app.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.toUiText
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
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
            try {
                val result = loginUseCase.loginWithGoogle(idToken, email, displayName)
                result.onSuccess {
                    val remoteProf = runCatching {
                        withTimeoutOrNull(6000L.milliseconds) {
                            profileRepository?.syncRemoteProfile()
                        }
                    }.getOrNull()

                    if (remoteProf != null) {
                        profileRepository?.setOnboardingCompleted(true)
                        emit(LoginEffect.NavigateToDashboard)
                    } else {
                        profileRepository?.clearLocalData()
                        profileRepository?.setOnboardingCompleted(false)
                        emit(LoginEffect.NavigateToCreateWorkspace)
                    }
                }
                result.onFailure {
                    emit(LoginEffect.ShowMessage(it.toUiText()))
                    update { copy(error = it.toUiText()) }
                }
            } catch (e: Exception) {
                val err = e.toUiText()
                emit(LoginEffect.ShowMessage(err))
                update { copy(error = err) }
            } finally {
                update { copy(isLoading = false) }
            }
        }
    }
}
