package com.example.revlens.domain.usecase

import com.example.revlens.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> {
        return authRepository.resetPassword(token, newPassword)
    }
}
