package com.revlens.app.domain.session

import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.UserAccount
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionManagerTest {

    @Test
    fun testDefaultStateIsGuest() {
        val sessionManager = DefaultSessionManager()
        assertEquals(SessionState.Guest, sessionManager.state.value)
    }

    @Test
    fun testCustomInitialState() {
        val sessionManager = DefaultSessionManager(SessionState.SignedOut)
        assertEquals(SessionState.SignedOut, sessionManager.state.value)
    }

    @Test
    fun testLoginTransitionsToAuthenticated() = runTest {
        val sessionManager = DefaultSessionManager(SessionState.SignedOut)
        val user = UserAccount(
            id = "user_123",
            name = "Test User",
            email = "user@revlens.com",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        sessionManager.onLoggedIn(user)

        val state = sessionManager.state.value
        assertTrue(state is SessionState.Authenticated)
        assertEquals(user, state.user)
        assertEquals("user@revlens.com", state.user.email)
    }

    @Test
    fun testContinueAsGuestTransitionsToGuest() = runTest {
        val sessionManager = DefaultSessionManager(SessionState.SignedOut)
        sessionManager.continueAsGuest()
        assertEquals(SessionState.Guest, sessionManager.state.value)
    }

    @Test
    fun testSignOutTransitionsToSignedOut() = runTest {
        val user = UserAccount(id = "user_1", name = "Test", email = "test@example.com")
        val sessionManager = DefaultSessionManager(SessionState.Authenticated(user))

        sessionManager.signOut(wipeLocalSyncedData = false)
        assertEquals(SessionState.SignedOut, sessionManager.state.value)
    }
}
