package com.revlens.app.data

import com.revlens.app.core.platform.SecureStorage
import com.revlens.app.core.ui.AppError
import com.revlens.app.core.ui.AppException
import com.revlens.app.domain.model.AuthTokens
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json

@JsFun("() => { if (window.revlensFirebase) { window.revlensFirebase.triggerGoogleSignIn(); } }")
private external fun jsTriggerGoogleSignIn()

@JsFun("() => window.revlensFirebase ? window.revlensFirebase.authStatus : 'error'")
private external fun jsGetAuthStatus(): String

@JsFun("() => (window.revlensFirebase && window.revlensFirebase.lastAuthResultJson) ? window.revlensFirebase.lastAuthResultJson : ''")
private external fun jsGetLastAuthResultJson(): String

@JsFun("() => (window.revlensFirebase && window.revlensFirebase.lastAuthError) ? window.revlensFirebase.lastAuthError : ''")
private external fun jsGetLastAuthError(): String

@JsFun("() => { if (window.revlensFirebase) { window.revlensFirebase.triggerSignOut(); } }")
private external fun jsTriggerSignOut()

@JsFun("() => (window.revlensFirebase && window.revlensFirebase.getCurrentUserJson()) ? window.revlensFirebase.getCurrentUserJson() : ''")
private external fun jsGetCurrentUserJson(): String

@JsFun("(name) => { if (window.revlensFirebase) { window.revlensFirebase.updateProfile(name); } }")
private external fun jsUpdateProfile(name: String)

@JsFun("() => { if (window.revlensFirebase) { window.revlensFirebase.deleteAccount(); } }")
private external fun jsDeleteAccount()

class WebFirebaseAuthRepository(
    private val secureStorage: SecureStorage? = null
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedUser: UserAccount? = null

    override suspend fun loginWithGoogle(
        idToken: String?,
        email: String?,
        name: String?,
        avatarUrl: String?
    ): Result<UserAccount> {
        try {
            jsTriggerGoogleSignIn()

            val success = withTimeoutOrNull(120_000L) {
                while (true) {
                    val status = jsGetAuthStatus()
                    if (status == "success") return@withTimeoutOrNull true
                    if (status == "error") return@withTimeoutOrNull false
                    delay(150)
                }
                false
            }

            if (success == true) {
                val jsonStr = jsGetLastAuthResultJson()
                if (jsonStr.isNotBlank()) {
                    val user = json.decodeFromString<UserAccount>(jsonStr)
                    cachedUser = user
                    saveTokens(user.id)
                    return Result.success(user)
                }
            }

            val errorMsg = jsGetLastAuthError().ifBlank { "Google sign-in was cancelled or encountered an error" }
            return Result.failure(AppException(AppError.Conflict(errorMsg)))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<UserAccount> {
        return Result.failure(AppException(AppError.InvalidCredentials))
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<UserAccount> {
        return Result.failure(AppException(AppError.Conflict("Sign up is handled via Google Sign-In")))
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun validateResetToken(token: String): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return Result.failure(AppException(AppError.Unauthorized))
    }

    override suspend fun logout(): Result<Unit> {
        try {
            jsTriggerSignOut()
            cachedUser = null
            secureStorage?.remove("auth_tokens")
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<AuthTokens> {
        val user = cachedUser ?: getCurrentUser() ?: return Result.failure(AppException(AppError.Unauthorized))
        val tokens = AuthTokens(
            accessToken = "web_fb_acc_${user.id}",
            refreshToken = "web_fb_ref_${user.id}"
        )
        return Result.success(tokens)
    }

    override suspend fun getCurrentUser(): UserAccount? {
        if (cachedUser != null) return cachedUser
        val userJson = jsGetCurrentUserJson()
        if (userJson.isNotBlank()) {
            return try {
                json.decodeFromString<UserAccount>(userJson).also { cachedUser = it }
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    override suspend fun getTokens(): AuthTokens? {
        val user = cachedUser ?: getCurrentUser() ?: return null
        return AuthTokens(
            accessToken = "web_fb_acc_${user.id}",
            refreshToken = "web_fb_ref_${user.id}"
        )
    }

    override suspend fun updateProfile(name: String, jobTitle: String?): Result<UserAccount> {
        val current = cachedUser ?: getCurrentUser() ?: return Result.failure(AppException(AppError.Unauthorized))
        try {
            jsUpdateProfile(name.trim())
            val updated = current.copy(name = name.trim(), jobTitle = jobTitle)
            cachedUser = updated
            return Result.success(updated)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        try {
            jsDeleteAccount()
            cachedUser = null
            secureStorage?.remove("auth_tokens")
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun saveTokens(userId: String) {
        val tokens = AuthTokens(
            accessToken = "web_fb_acc_$userId",
            refreshToken = "web_fb_ref_$userId"
        )
        try {
            secureStorage?.put("auth_tokens", json.encodeToString(AuthTokens.serializer(), tokens))
        } catch (_: Exception) {
        }
    }
}
