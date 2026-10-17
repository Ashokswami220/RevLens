package com.example.revlens.domain.usecase

import com.example.revlens.domain.repository.AuthRepository

class ValidateResetTokenUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String): Result<Boolean> {
        return authRepository.validateResetToken(token)
    }
}
