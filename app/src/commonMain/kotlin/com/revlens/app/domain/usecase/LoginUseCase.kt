package com.revlens.app.domain.usecase

import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.session.SessionManager

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) {
    suspend fun loginWithGoogle(
        idToken: String? = null,
        email: String? = null,
        name: String? = null,
        avatarUrl: String? = null
    ): Result<UserAccount> {
        return authRepository.loginWithGoogle(idToken, email, name, avatarUrl)
            .onSuccess { user ->
                sessionManager.onLoggedIn(user)
            }
    }
}
