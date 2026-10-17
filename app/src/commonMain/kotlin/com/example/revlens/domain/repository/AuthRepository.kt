package com.example.revlens.domain.repository

import com.example.revlens.domain.model.AuthTokens
import com.example.revlens.domain.model.UserAccount

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserAccount>
    suspend fun signUp(name: String, email: String, password: String): Result<UserAccount>
    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun validateResetToken(token: String): Result<Boolean>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<AuthTokens>
    suspend fun getCurrentUser(): UserAccount?
    suspend fun getTokens(): AuthTokens?
}
