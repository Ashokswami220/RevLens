package com.revlens.app.data.fake

import com.revlens.app.core.platform.SecureStorage
import com.revlens.app.core.ui.AppError
import com.revlens.app.core.ui.AppException
import com.revlens.app.domain.model.AuthTokens
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import kotlinx.serialization.json.Json

class FakeAuthRepository(
    private val secureStorage: SecureStorage? = null,
    initialUser: UserAccount? = null
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val usersByEmail = mutableMapOf<String, Pair<UserAccount, String>>()
    private val resetTokens = mutableMapOf<String, String>() // token -> email
    private var currentUser: UserAccount? = initialUser
    private var currentTokens: AuthTokens? = null

    init {
        // Pre-fill demo user
        val demoUser = UserAccount(
            id = "usr_demo_1",
            name = "Alex Vance",
            email = "alex@revlens.com",
            emailVerified = true,
            jobTitle = "Founder & CEO"
        )
        usersByEmail["alex@revlens.com"] = Pair(demoUser, "password123")
        resetTokens["mock-reset-token-valid"] = "alex@revlens.com"

        if (initialUser != null) {
            usersByEmail[initialUser.email] = Pair(initialUser, "password123")
        }
    }

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

        val tokens = AuthTokens(
            accessToken = "google_acc_${user.id}",
            refreshToken = "google_ref_${user.id}"
        )

        currentUser = user
        currentTokens = tokens
        saveTokens(tokens)

        return Result.success(user)
    }

    override suspend fun login(email: String, password: String): Result<UserAccount> {
        val normalized = email.trim().lowercase()
        val entry = usersByEmail[normalized]

        if (entry == null || entry.second != password) {
            return Result.failure(AppException(AppError.InvalidCredentials))
        }

        val user = entry.first
        val tokens = AuthTokens(
            accessToken = "mock_acc_${user.id}_${usersByEmail.size}",
            refreshToken = "mock_ref_${user.id}_${usersByEmail.size}"
        )

        currentUser = user
        currentTokens = tokens
        saveTokens(tokens)

        return Result.success(user)
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<UserAccount> {
        val normalized = email.trim().lowercase()
        if (usersByEmail.containsKey(normalized)) {
            return Result.failure(
                AppException(AppError.Conflict("An account with this email address already exists."))
            )
        }

        val newId = "usr_${usersByEmail.size + 1}"
        val user = UserAccount(
            id = newId,
            name = name.trim(),
            email = normalized,
            emailVerified = true
        )

        usersByEmail[normalized] = Pair(user, password)
        val tokens = AuthTokens(
            accessToken = "mock_acc_${user.id}",
            refreshToken = "mock_ref_${user.id}"
        )

        currentUser = user
        currentTokens = tokens
        saveTokens(tokens)

        return Result.success(user)
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        val normalized = email.trim().lowercase()
        if (usersByEmail.containsKey(normalized)) {
            val token = "tok_reset_${usersByEmail.size}"
            resetTokens[token] = normalized
        }
        // Always succeed to prevent email enumeration
        return Result.success(Unit)
    }

    override suspend fun validateResetToken(token: String): Result<Boolean> {
        val isValid = token == "mock-reset-token-valid" || resetTokens.containsKey(token)
        return Result.success(isValid)
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        val email = resetTokens[token] ?: if (token == "mock-reset-token-valid") "alex@revlens.com" else null
        if (email == null) {
            return Result.failure(AppException(AppError.Unauthorized))
        }

        val existing = usersByEmail[email]
        if (existing != null) {
            usersByEmail[email] = Pair(existing.first, newPassword)
            resetTokens.remove(token)
            return Result.success(Unit)
        }

        return Result.failure(AppException(AppError.NotFound))
    }

    override suspend fun logout(): Result<Unit> {
        currentUser = null
        currentTokens = null
        secureStorage?.remove("auth_tokens")
        return Result.success(Unit)
    }

    override suspend fun refreshToken(): Result<AuthTokens> {
        val user = currentUser
            ?: return Result.failure(AppException(AppError.Unauthorized))

        val refreshed = AuthTokens(
            accessToken = "mock_acc_refreshed_${user.id}",
            refreshToken = "mock_ref_refreshed_${user.id}"
        )
        currentTokens = refreshed
        saveTokens(refreshed)
        return Result.success(refreshed)
    }

    override suspend fun getCurrentUser(): UserAccount? = currentUser

    override suspend fun getTokens(): AuthTokens? {
        if (currentTokens != null) return currentTokens
        val raw = secureStorage?.get("auth_tokens") ?: return null
        return try {
            json.decodeFromString<AuthTokens>(raw).also { currentTokens = it }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount> {
        val current = currentUser ?: return Result.failure(AppException(AppError.Unauthorized))
        val updated = current.copy(name = name, jobTitle = jobTitle)
        currentUser = updated
        val existingPair = usersByEmail[current.email]
        if (existingPair != null) {
            usersByEmail[current.email] = Pair(updated, existingPair.second)
        }
        return Result.success(updated)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val current = currentUser
        if (current != null) {
            usersByEmail.remove(current.email)
        }
        currentUser = null
        currentTokens = null
        secureStorage?.remove("auth_tokens")
        return Result.success(Unit)
    }

    private suspend fun saveTokens(tokens: AuthTokens) {
        try {
            secureStorage?.put("auth_tokens", json.encodeToString(AuthTokens.serializer(), tokens))
        } catch (_: Exception) {
        }
    }
}
