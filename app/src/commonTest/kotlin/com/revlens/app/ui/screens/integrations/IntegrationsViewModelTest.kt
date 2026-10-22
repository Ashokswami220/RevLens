package com.revlens.app.ui.screens.integrations

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
    fun testCategoryFilter() = runTest(testDispatcher) {
        val vm = IntegrationsViewModel()
        vm.onEvent(IntegrationsEvent.SelectCategory(IntegrationCategory.REVENUE))
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(IntegrationCategory.REVENUE, s.selectedCategory)
    }

    @Test
    fun testConnectAndDisconnect() = runTest(testDispatcher) {
        val vm = IntegrationsViewModel()
        vm.onEvent(IntegrationsEvent.ConnectIntegration(IntegrationProvider.STRIPE))
        advanceUntilIdle()

        val conn = vm.state.value.connections.find { it.provider == IntegrationProvider.STRIPE }
        assertNotNull(conn)

        vm.onEvent(IntegrationsEvent.PromptDisconnectIntegration(IntegrationProvider.STRIPE))
        assertEquals(IntegrationProvider.STRIPE, vm.state.value.disconnectingProvider)

        vm.onEvent(IntegrationsEvent.ConfirmDisconnectIntegration)
        advanceUntilIdle()

        assertNull(vm.state.value.connections.find { it.provider == IntegrationProvider.STRIPE })
    }
}
