package com.example.revlens.domain.usecase

import com.example.revlens.domain.model.UserAccount
import com.example.revlens.domain.repository.AuthRepository
import com.example.revlens.domain.session.SessionManager

class SignUpUseCase(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val migrateGuestDataUseCase: MigrateGuestDataUseCase? = null
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<UserAccount> {
        return authRepository.signUp(name, email, password).onSuccess { user ->
            sessionManager.onLoggedIn(user)
            migrateGuestDataUseCase?.invoke(user.id)
        }
    }
}
