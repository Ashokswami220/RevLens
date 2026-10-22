package com.revlens.app.ui.screens.developer

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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ApiWebhooksViewModelTest {

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
    fun testTabSwitching() = runTest(testDispatcher) {
        val vm = ApiWebhooksViewModel()
        vm.onEvent(ApiWebhooksEvent.SelectTab(DeveloperTab.WEBHOOKS))
        assertEquals(DeveloperTab.WEBHOOKS, vm.state.value.activeTab)
    }

    @Test
    fun testCreateAndRevokeApiKey() = runTest(testDispatcher) {
        val vm = ApiWebhooksViewModel()

        // Blank name gives error
        vm.onEvent(ApiWebhooksEvent.NewKeyNameChanged("   "))
        vm.onEvent(ApiWebhooksEvent.SubmitCreateKey)
        assertNotNull(vm.state.value.error)

        // Valid name creates key
        vm.onEvent(ApiWebhooksEvent.NewKeyNameChanged("Production Key"))
        vm.onEvent(ApiWebhooksEvent.SubmitCreateKey)
        advanceUntilIdle()

        assertTrue(vm.state.value.apiKeys.any { it.name == "Production Key" })
        assertNotNull(vm.state.value.createdKeyToken)

        // Revoke key
        val created = vm.state.value.apiKeys.first { it.name == "Production Key" }
        vm.onEvent(ApiWebhooksEvent.PromptDeleteKey(created))
        assertEquals(created, vm.state.value.keyToDelete)

        vm.onEvent(ApiWebhooksEvent.ConfirmDeleteKey)
        advanceUntilIdle()
        assertNull(vm.state.value.apiKeys.find { it.name == "Production Key" })
    }
}
