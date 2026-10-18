package com.revlens.app.domain.repository

import com.revlens.app.domain.model.AuthTokens
import com.revlens.app.domain.model.UserAccount
interface AuthRepository {
    suspend fun loginWithGoogle(
        idToken: String? = null,
        email: String? = null,
        name: String? = null,
        avatarUrl: String? = null
    ): Result<UserAccount>
    suspend fun login(email: String, password: String): Result<UserAccount>
    suspend fun signUp(name: String, email: String, password: String): Result<UserAccount>
    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun validateResetToken(token: String): Result<Boolean>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<AuthTokens>
    suspend fun getCurrentUser(): UserAccount?
    suspend fun getTokens(): AuthTokens?
    suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount>
    suspend fun deleteAccount(): Result<Unit>
}
