package com.revlens.app.domain.repository

import com.revlens.app.domain.model.UserAccount

interface AuthRepository {
    suspend fun loginWithGoogle(
        idToken: String? = null,
        email: String? = null,
        name: String? = null,
        avatarUrl: String? = null
    ): Result<UserAccount>

    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): UserAccount?
    suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount>
    suspend fun deleteAccount(): Result<Unit>
}
