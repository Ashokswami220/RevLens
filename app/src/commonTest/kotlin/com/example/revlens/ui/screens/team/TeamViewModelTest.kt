package com.example.revlens.ui.screens.team

import com.example.revlens.data.fake.FakeTeamRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.model.Role
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
class TeamViewModelTest {

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
    fun testInitialStateLoadsMembersAndInvites() = runTest(testDispatcher) {
        val repo = FakeTeamRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = TeamViewModel(repo, wsContext)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(3, s.members.size)
        assertEquals(1, s.invites.size)
        assertEquals(3, s.usedSeats)
        assertEquals(Role.Owner, s.members.first().role)
    }

    @Test
    fun testSubmitInviteValidAndInvalid() = runTest(testDispatcher) {
        val repo = FakeTeamRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = TeamViewModel(repo, wsContext)
        advanceUntilIdle()

        // 1. Invalid email
        vm.onEvent(TeamEvent.InviteEmailChanged("invalid-email"))
        vm.onEvent(TeamEvent.SubmitInvite)
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        // 2. Valid email
        vm.onEvent(TeamEvent.InviteEmailChanged("newmember@acme.ai"))
        vm.onEvent(TeamEvent.InviteRoleChanged(Role.Editor))
        vm.onEvent(TeamEvent.SubmitInvite)
        advanceUntilIdle()

        assertEquals(null, vm.state.value.error)
        assertEquals(2, vm.state.value.invites.size)
        assertTrue(vm.state.value.invites.any { it.email == "newmember@acme.ai" })
    }

    @Test
    fun testChangeRoleAndRemoveMember() = runTest(testDispatcher) {
        val repo = FakeTeamRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = TeamViewModel(repo, wsContext)
        advanceUntilIdle()

        // Change role of editor to Admin
        val editor = vm.state.value.members.find { it.role == Role.Editor }!!
        vm.onEvent(TeamEvent.ChangeMemberRole(editor.id, Role.Admin))
        advanceUntilIdle()

        val updated = vm.state.value.members.find { it.id == editor.id }!!
        assertEquals(Role.Admin, updated.role)

        // Remove member
        vm.onEvent(TeamEvent.PromptRemoveMember(updated))
        vm.onEvent(TeamEvent.ConfirmRemoveMember)
        advanceUntilIdle()

        assertEquals(2, vm.state.value.members.size)
        assertFalse(vm.state.value.members.any { it.id == editor.id })
    }
}
