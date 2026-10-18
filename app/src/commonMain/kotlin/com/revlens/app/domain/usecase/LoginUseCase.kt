package com.revlens.app.domain.usecase

import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.session.SessionManager

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val migrateGuestDataUseCase: MigrateGuestDataUseCase? = null
) {
    suspend fun loginWithGoogle(
        idToken: String? = null,
        email: String? = null,
        name: String? = null,
        avatarUrl: String? = null
    ): Result<UserAccount> {
        return authRepository.loginWithGoogle(idToken, email, name, avatarUrl).onSuccess { user ->
            sessionManager.onLoggedIn(user)
            migrateGuestDataUseCase?.invoke(user.id)
        }
    }

    suspend operator fun invoke(email: String, password: String): Result<UserAccount> {
        return authRepository.login(email, password).onSuccess { user ->
            sessionManager.onLoggedIn(user)
            migrateGuestDataUseCase?.invoke(user.id)
        }
    }
}
