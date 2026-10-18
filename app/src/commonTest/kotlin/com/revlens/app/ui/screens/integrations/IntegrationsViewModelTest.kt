package com.revlens.app.ui.screens.integrations

import com.revlens.app.data.fake.FakeIntegrationRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.model.ConnectionStatus
import com.revlens.app.domain.model.IntegrationCategory
import com.revlens.app.domain.model.IntegrationProvider
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class IntegrationsViewModelTest {

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
    fun testInitialStateLoadsConnections() = runTest(testDispatcher) {
        val repo = FakeIntegrationRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = IntegrationsViewModel(repo, wsContext)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(4, s.connections.size)
        val stripe = s.connections.find { it.provider == IntegrationProvider.STRIPE }
        assertNotNull(stripe)
        assertEquals(ConnectionStatus.CONNECTED, stripe.status)
    }

    @Test
    fun testCategoryFilter() = runTest(testDispatcher) {
        val repo = FakeIntegrationRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = IntegrationsViewModel(repo, wsContext)
        advanceUntilIdle()

        vm.onEvent(IntegrationsEvent.SelectCategory(IntegrationCategory.REVENUE))
        advanceUntilIdle()

        assertEquals(IntegrationCategory.REVENUE, vm.state.value.selectedCategory)
    }

    @Test
    fun testConnectAndDisconnect() = runTest(testDispatcher) {
        val repo = FakeIntegrationRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = IntegrationsViewModel(repo, wsContext)
        advanceUntilIdle()

        // Connect Paddle
        vm.onEvent(IntegrationsEvent.ConnectIntegration(IntegrationProvider.PADDLE))
        advanceUntilIdle()

        var paddle = vm.state.value.connections.find { it.provider == IntegrationProvider.PADDLE }!!
        assertEquals(ConnectionStatus.CONNECTED, paddle.status)
        assertNotNull(paddle.accountName)

        // Disconnect Paddle
        vm.onEvent(IntegrationsEvent.PromptDisconnectIntegration(IntegrationProvider.PADDLE))
        assertEquals(IntegrationProvider.PADDLE, vm.state.value.disconnectingProvider)

        vm.onEvent(IntegrationsEvent.ConfirmDisconnectIntegration)
        advanceUntilIdle()

        paddle = vm.state.value.connections.find { it.provider == IntegrationProvider.PADDLE }!!
        assertEquals(ConnectionStatus.DISCONNECTED, paddle.status)
        assertNull(vm.state.value.disconnectingProvider)
    }

    @Test
    fun testSyncIntegration() = runTest(testDispatcher) {
        val repo = FakeIntegrationRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = IntegrationsViewModel(repo, wsContext)
        advanceUntilIdle()

        vm.onEvent(IntegrationsEvent.SyncIntegration(IntegrationProvider.STRIPE))
        advanceUntilIdle()

        val stripe = vm.state.value.connections.find { it.provider == IntegrationProvider.STRIPE }!!
        assertEquals("Just now", stripe.lastSyncAt)
    }
}
