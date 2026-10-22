package com.revlens.app.ui.screens.team

import com.revlens.app.data.fake.FakeTeamRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
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
        val repo = FakeTeamRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = TeamViewModel(repo, wsContext)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(0, s.members.size)
    }
}
