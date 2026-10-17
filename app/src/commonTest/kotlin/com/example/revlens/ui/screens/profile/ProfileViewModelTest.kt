package com.example.revlens.ui.screens.profile

import com.example.revlens.core.platform.InMemorySecureStorage
import com.example.revlens.data.DefaultWorkspaceRepository
import com.example.revlens.data.fake.FakeAuthRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.model.Role
import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.model.UserAccount
import com.example.revlens.domain.model.Workspace
import com.example.revlens.domain.session.DefaultSessionManager
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

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
    fun testAuthenticatedUserLoadsProfile() = runTest(testDispatcher) {
        val user = UserAccount(
            id = "usr_1",
            name = "Alex Vance",
            email = "alex.vance@gmail.com",
            jobTitle = "Founder"
        )
        val authRepo = FakeAuthRepository(InMemorySecureStorage(), initialUser = user)
        val session = DefaultSessionManager(SessionState.Authenticated(user))
        val wsRepo = DefaultWorkspaceRepository()
        wsRepo.createWorkspace(Workspace("ws_1", "Test WS", currency = "USD", timeZoneId = "UTC"))
        val wsContext = DefaultWorkspaceContext("ws_1")

        val vm = ProfileViewModel(authRepo, session, wsRepo, wsContext)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals("Alex Vance", s.name)
        assertEquals("alex.vance@gmail.com", s.email)
        assertEquals("Founder", s.jobTitle)
        assertEquals("USD", s.currency)
        assertEquals("UTC", s.timezone)
        assertFalse(s.isGuest)
        assertFalse(s.hasChanges)
    }

    @Test
    fun testEditProfileTracksChangesAndSaves() = runTest(testDispatcher) {
        val user = UserAccount(
            id = "usr_1",
            name = "Alex Vance",
            email = "alex.vance@gmail.com"
        )
        val authRepo = FakeAuthRepository(InMemorySecureStorage(), initialUser = user)
        val session = DefaultSessionManager(SessionState.Authenticated(user))
        val wsRepo = DefaultWorkspaceRepository()
        wsRepo.createWorkspace(Workspace("ws_1", "Test WS", currency = "USD", timeZoneId = "UTC"))
        val wsContext = DefaultWorkspaceContext("ws_1")

        val vm = ProfileViewModel(authRepo, session, wsRepo, wsContext)
        advanceUntilIdle()

        vm.onEvent(ProfileEvent.NameChanged("Alex Mercer"))
        vm.onEvent(ProfileEvent.JobTitleChanged("Chief Architect"))
        vm.onEvent(ProfileEvent.CurrencyChanged("EUR"))

        assertTrue(vm.state.value.hasChanges)

        vm.onEvent(ProfileEvent.SaveChanges)
        advanceUntilIdle()

        assertFalse(vm.state.value.hasChanges)
        assertEquals("Alex Mercer", vm.state.value.name)
        assertEquals("Chief Architect", vm.state.value.jobTitle)
        assertEquals("EUR", vm.state.value.currency)

        // Verify persisted in session and workspace repo
        val currentSession = session.state.value
        assertTrue(currentSession is SessionState.Authenticated)
        assertEquals("Alex Mercer", currentSession.user.name)

        val updatedWs = wsRepo.getWorkspace("ws_1")
        assertNotNull(updatedWs)
        assertEquals("EUR", updatedWs.currency)
    }

    @Test
    fun testDeleteAccountWithTypedConfirmation() = runTest(testDispatcher) {
        val user = UserAccount(
            id = "usr_1",
            name = "Alex Vance",
            email = "alex.vance@gmail.com"
        )
        val authRepo = FakeAuthRepository(InMemorySecureStorage(), initialUser = user)
        val session = DefaultSessionManager(SessionState.Authenticated(user))
        val wsRepo = DefaultWorkspaceRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")

        val vm = ProfileViewModel(authRepo, session, wsRepo, wsContext)
        advanceUntilIdle()

        // 1. Wrong confirmation word fails
        vm.onEvent(ProfileEvent.ConfirmDeleteAccount("not_delete"))
        advanceUntilIdle()
        assertNotNull(vm.state.value.deleteError)

        // 2. Correct confirmation word succeeds and signs out
        vm.onEvent(ProfileEvent.ConfirmDeleteAccount("DELETE"))
        advanceUntilIdle()

        assertEquals(SessionState.SignedOut, session.state.value)
    }
}
