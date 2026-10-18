package com.revlens.app.ui.screens.security

import com.revlens.app.core.platform.BiometricAuthenticator
import com.revlens.app.core.platform.BiometricStatus
import com.revlens.app.data.DefaultAppLockRepository
import com.revlens.app.domain.model.LockTimeout
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.session.DefaultSessionManager
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
class SecuritySettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeAuthenticator = object : BiometricAuthenticator {
        override fun checkStatus(): BiometricStatus = BiometricStatus.Available
        override suspend fun authenticate(promptTitle: String): Boolean = true
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAuthenticatedUserHasGoogleAccountAndSessions() = runTest(testDispatcher) {
        val user = UserAccount("usr_1", "Alex Vance", "alex.vance@gmail.com")
        val session = DefaultSessionManager(SessionState.Authenticated(user))
        val lockRepo = DefaultAppLockRepository()

        val vm = SecuritySettingsViewModel(lockRepo, fakeAuthenticator, session)
        advanceUntilIdle()

        val s = vm.state.value
        assertFalse(s.isGuest)
        assertTrue(s.isGoogleConnected)
        assertEquals("alex.vance@gmail.com", s.googleAccountEmail)
        assertTrue(s.sessions.size > 1)
    }

    @Test
    fun testBiometricToggleAndTimeout() = runTest(testDispatcher) {
        val session = DefaultSessionManager(SessionState.Guest)
        val lockRepo = DefaultAppLockRepository()

        val vm = SecuritySettingsViewModel(lockRepo, fakeAuthenticator, session)
        advanceUntilIdle()

        vm.onEvent(SecuritySettingsEvent.ToggleBiometric(true))
        advanceUntilIdle()
        assertTrue(vm.state.value.isBiometricEnabled)

        vm.onEvent(SecuritySettingsEvent.ChangeTimeout(LockTimeout.FIVE_MINUTES))
        advanceUntilIdle()
        assertEquals(LockTimeout.FIVE_MINUTES, vm.state.value.timeout)
    }

    @Test
    fun testRevokeSession() = runTest(testDispatcher) {
        val user = UserAccount("usr_1", "Alex Vance", "alex.vance@gmail.com")
        val session = DefaultSessionManager(SessionState.Authenticated(user))
        val lockRepo = DefaultAppLockRepository()

        val vm = SecuritySettingsViewModel(lockRepo, fakeAuthenticator, session)
        advanceUntilIdle()

        val initialCount = vm.state.value.sessions.size
        vm.onEvent(SecuritySettingsEvent.RevokeSession("sess_web_mac"))
        advanceUntilIdle()

        assertEquals(initialCount - 1, vm.state.value.sessions.size)
        assertFalse(vm.state.value.sessions.any { it.id == "sess_web_mac" })
    }

    @Test
    fun testRevokeAllOtherSessions() = runTest(testDispatcher) {
        val user = UserAccount("usr_1", "Alex Vance", "alex.vance@gmail.com")
        val session = DefaultSessionManager(SessionState.Authenticated(user))
        val lockRepo = DefaultAppLockRepository()

        val vm = SecuritySettingsViewModel(lockRepo, fakeAuthenticator, session)
        advanceUntilIdle()

        vm.onEvent(SecuritySettingsEvent.RevokeAllOtherSessions)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.sessions.size)
        assertTrue(vm.state.value.sessions.first().isCurrent)
    }
}
