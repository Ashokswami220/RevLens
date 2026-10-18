package com.revlens.app.domain.usecase

import com.revlens.app.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> {
        return authRepository.resetPassword(token, newPassword)
    }
}
