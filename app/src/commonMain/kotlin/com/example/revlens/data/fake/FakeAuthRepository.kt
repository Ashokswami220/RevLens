package com.example.revlens.data.fake

import com.example.revlens.core.platform.SecureStorage
import com.example.revlens.core.ui.AppError
import com.example.revlens.core.ui.AppException
import com.example.revlens.domain.model.AuthTokens
import com.example.revlens.domain.model.UserAccount
import com.example.revlens.domain.repository.AuthRepository
import kotlinx.serialization.json.Json

class FakeAuthRepository(
    private val secureStorage: SecureStorage? = null
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val usersByEmail = mutableMapOf<String, Pair<UserAccount, String>>()
    private val resetTokens = mutableMapOf<String, String>() // token -> email
    private var currentUser: UserAccount? = null
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

    private suspend fun saveTokens(tokens: AuthTokens) {
        try {
            secureStorage?.put("auth_tokens", json.encodeToString(AuthTokens.serializer(), tokens))
        } catch (_: Exception) {
        }
    }
}
