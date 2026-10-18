package com.revlens.app.domain.usecase

import com.revlens.app.domain.repository.AuthRepository

class ValidateResetTokenUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String): Result<Boolean> {
        return authRepository.validateResetToken(token)
    }
}
