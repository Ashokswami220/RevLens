package com.revlens.app.domain.auth

import com.revlens.app.core.platform.InMemorySecureStorage
import com.revlens.app.core.ui.AppError
import com.revlens.app.core.ui.toAppError
import com.revlens.app.data.fake.FakeAuthRepository
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.usecase.LoginUseCase
import com.revlens.app.domain.usecase.SignUpUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    @Test
    fun testLoginSuccess() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val sessionManager = DefaultSessionManager()
        val login = LoginUseCase(repo, sessionManager)

        val result = login("alex@revlens.com", "password123")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("alex@revlens.com", user.email)
        assertEquals(SessionState.Authenticated(user), sessionManager.state.value)
    }

    @Test
    fun testLoginWithGoogleSuccess() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val sessionManager = DefaultSessionManager()
        val login = LoginUseCase(repo, sessionManager)

        val result = login.loginWithGoogle(
            idToken = "mock_google_id_token",
            email = "alex.google@example.com",
            name = "Alex Google",
            avatarUrl = "https://example.com/avatar.png"
        )
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("alex.google@example.com", user.email)
        assertEquals("Alex Google", user.name)
        assertEquals("https://example.com/avatar.png", user.avatarUrl)
        assertEquals(SessionState.Authenticated(user), sessionManager.state.value)
    }

    @Test
    fun testLoginFailureInvalidCredentials() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val sessionManager = DefaultSessionManager()
        val login = LoginUseCase(repo, sessionManager)

        val result = login("alex@revlens.com", "wrongpassword")
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
            ?.toAppError()
        assertEquals(AppError.InvalidCredentials, error)
    }

    @Test
    fun testSignUpSuccessAndConflict() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val sessionManager = DefaultSessionManager()
        val signUp = SignUpUseCase(repo, sessionManager)

        // 1. New user signs up successfully
        val res1 = signUp("Sarah Connor", "sarah@skynet.ai", "Password123!")
        assertTrue(res1.isSuccess)
        val user = res1.getOrNull()
        assertNotNull(user)
        assertEquals("sarah@skynet.ai", user.email)

        // 2. Duplicate email causes Conflict
        val res2 = signUp("Sarah Duplicate", "sarah@skynet.ai", "Password123!")
        assertTrue(res2.isFailure)
        val error = res2.exceptionOrNull()
            ?.toAppError()
        assertTrue(error is AppError.Conflict)
    }

    @Test
    fun testPasswordResetFlow() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())

        // Request reset
        val reqRes = repo.requestPasswordReset("alex@revlens.com")
        assertTrue(reqRes.isSuccess)

        // Validate token
        val valRes = repo.validateResetToken("mock-reset-token-valid")
        assertTrue(valRes.isSuccess && valRes.getOrNull() == true)

        // Reset password
        val resetRes = repo.resetPassword("mock-reset-token-valid", "newBrandNewPassword123!")
        assertTrue(resetRes.isSuccess)

        // Old password fails
        val oldLogin = repo.login("alex@revlens.com", "password123")
        assertTrue(oldLogin.isFailure)

        // New password succeeds
        val newLogin = repo.login("alex@revlens.com", "newBrandNewPassword123!")
        assertTrue(newLogin.isSuccess)
    }
}
