package com.revlens.app.domain.auth

import com.revlens.app.core.platform.InMemorySecureStorage
import com.revlens.app.data.fake.FakeAuthRepository
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.usecase.LoginUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

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
    fun testLogoutAndProfile() = runTest {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val loginRes = repo.loginWithGoogle(email = "alex@test.com", name = "Alex")
        assertTrue(loginRes.isSuccess)

        val updateRes = repo.updateProfile("Alexander", "CTO")
        assertTrue(updateRes.isSuccess)
        assertEquals("Alexander", updateRes.getOrNull()?.name)
        assertEquals("CTO", updateRes.getOrNull()?.jobTitle)

        val logoutRes = repo.logout()
        assertTrue(logoutRes.isSuccess)
        assertEquals(null, repo.getCurrentUser())
    }
}
