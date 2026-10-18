package com.revlens.app.ui.screens.auth

import com.revlens.app.core.platform.InMemorySecureStorage
import com.revlens.app.data.fake.FakeAuthRepository
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager()
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        val s = vm.state.value
        assertFalse(s.isLoading)
        assertEquals(null, s.error)
    }

    @Test
    fun testGoogleSignInSuccess() = runTest(testDispatcher) {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager()
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        vm.onEvent(LoginEvent.GoogleSignInClicked)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        val sessionState = session.state.value
        assertTrue(sessionState is SessionState.Authenticated)
        assertEquals("Alex Vance", sessionState.user.name)
    }

    @Test
    fun testContinueAsGuest() = runTest(testDispatcher) {
        val repo = FakeAuthRepository(InMemorySecureStorage())
        val session = DefaultSessionManager(SessionState.SignedOut)
        val vm = LoginViewModel(LoginUseCase(repo, session), session)

        vm.onEvent(LoginEvent.ContinueAsGuestClicked)
        advanceUntilIdle()

        assertEquals(SessionState.Guest, session.state.value)
    }
}
