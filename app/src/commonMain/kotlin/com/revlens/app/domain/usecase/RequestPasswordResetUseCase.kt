package com.revlens.app.domain.usecase

import com.revlens.app.domain.repository.AuthRepository

class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.requestPasswordReset(email)
    }
}
