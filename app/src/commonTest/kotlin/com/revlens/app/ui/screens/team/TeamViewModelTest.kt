package com.revlens.app.ui.screens.team

import com.revlens.app.domain.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun testInitialStateLoadsMembers() = runTest(testDispatcher) {
        val vm = TeamViewModel()
        val s = vm.state.value
        assertEquals(3, s.members.size)
        assertEquals(1, s.invites.size)
        assertEquals(Role.Owner, s.userRole)
    }

    @Test
    fun testInviteMemberSuccess() = runTest(testDispatcher) {
        val vm = TeamViewModel()
        vm.onEvent(TeamEvent.OpenInviteSheet)
        assertTrue(vm.state.value.isInviteSheetVisible)

        vm.onEvent(TeamEvent.InviteEmailChanged("newdev@company.com"))
        vm.onEvent(TeamEvent.InviteRoleChanged(Role.Admin))
        vm.onEvent(TeamEvent.SubmitInvite)

        assertEquals(2, vm.state.value.invites.size)
        assertEquals(false, vm.state.value.isInviteSheetVisible)
    }

    @Test
    fun testRevokeInvite() = runTest(testDispatcher) {
        val vm = TeamViewModel()
        assertEquals(1, vm.state.value.invites.size)
        vm.onEvent(TeamEvent.RevokeInvite("inv_1"))
        assertEquals(0, vm.state.value.invites.size)
    }
}
