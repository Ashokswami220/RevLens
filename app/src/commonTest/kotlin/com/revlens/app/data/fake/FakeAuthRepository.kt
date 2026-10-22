package com.revlens.app.data.fake

import com.revlens.app.core.platform.SecureStorage
import com.revlens.app.core.ui.AppError
import com.revlens.app.core.ui.AppException
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository

class FakeAuthRepository(
    private val secureStorage: SecureStorage? = null,
    initialUser: UserAccount? = null
) : AuthRepository {

    private var currentUser: UserAccount? = initialUser

    override suspend fun loginWithGoogle(
        idToken: String?,
        email: String?,
        name: String?,
        avatarUrl: String?
    ): Result<UserAccount> {
        val userEmail = email?.trim()?.lowercase() ?: "alex.vance@gmail.com"
        val userName = name?.trim() ?: "Alex Vance"
        val userAvatar = avatarUrl ?: "https://lh3.googleusercontent.com/a/default-user"

        val user = UserAccount(
            id = "usr_google_${userEmail.hashCode()}",
            name = userName,
            email = userEmail,
            emailVerified = true,
            avatarUrl = userAvatar,
            jobTitle = "Founder & CEO"
        )

        currentUser = user
        return Result.success(user)
    }

    override suspend fun logout(): Result<Unit> {
        currentUser = null
        secureStorage?.remove("auth_tokens")
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): UserAccount? = currentUser

    override suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount> {
        val current = currentUser ?: return Result.failure(AppException(AppError.Unauthorized))
        val updated = current.copy(name = name, jobTitle = jobTitle)
        currentUser = updated
        return Result.success(updated)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        currentUser = null
        secureStorage?.remove("auth_tokens")
        return Result.success(Unit)
    }
}
