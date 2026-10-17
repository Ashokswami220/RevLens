package com.example.revlens.domain.usecase

import com.example.revlens.domain.repository.AuthRepository

class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.requestPasswordReset(email)
    }
}
