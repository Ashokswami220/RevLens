package com.example.revlens.ui.screens.developer

import com.example.revlens.data.fake.FakeDeveloperRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.model.ApiKeyScope
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
    fun testInitialStateLoadsKeysWebhooksAndDeliveries() = runTest(testDispatcher) {
        val repo = FakeDeveloperRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = ApiWebhooksViewModel(repo, wsContext)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(2, s.apiKeys.size)
        assertEquals(1, s.webhooks.size)
        assertEquals(3, s.deliveries.size)
    }

    @Test
    fun testCreateApiKeyValidAndInvalid() = runTest(testDispatcher) {
        val repo = FakeDeveloperRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = ApiWebhooksViewModel(repo, wsContext)
        advanceUntilIdle()

        // Blank name
        vm.onEvent(ApiWebhooksEvent.NewKeyNameChanged("   "))
        vm.onEvent(ApiWebhooksEvent.SubmitCreateKey)
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        // Valid name
        vm.onEvent(ApiWebhooksEvent.NewKeyNameChanged("Stripe Sync Bot"))
        vm.onEvent(ApiWebhooksEvent.NewKeyScopeChanged(ApiKeyScope.READ_WRITE))
        vm.onEvent(ApiWebhooksEvent.SubmitCreateKey)
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertNotNull(vm.state.value.createdKeyToken)
        assertEquals(3, vm.state.value.apiKeys.size)
    }

    @Test
    fun testDeleteApiKey() = runTest(testDispatcher) {
        val repo = FakeDeveloperRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = ApiWebhooksViewModel(repo, wsContext)
        advanceUntilIdle()

        val firstKey = vm.state.value.apiKeys.first()
        vm.onEvent(ApiWebhooksEvent.PromptDeleteKey(firstKey))
        assertEquals(firstKey, vm.state.value.keyToDelete)

        vm.onEvent(ApiWebhooksEvent.ConfirmDeleteKey)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.apiKeys.size)
        assertNull(vm.state.value.keyToDelete)
    }

    @Test
    fun testCreateWebhookUrlValidation() = runTest(testDispatcher) {
        val repo = FakeDeveloperRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = ApiWebhooksViewModel(repo, wsContext)
        advanceUntilIdle()

        // Invalid http://
        vm.onEvent(ApiWebhooksEvent.NewWebhookUrlChanged("http://insecure.endpoint.com"))
        vm.onEvent(ApiWebhooksEvent.SubmitCreateWebhook)
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        // Valid https://
        vm.onEvent(ApiWebhooksEvent.NewWebhookUrlChanged("https://secure.endpoint.com/webhook"))
        vm.onEvent(ApiWebhooksEvent.NewWebhookDescChanged("Slack Dispatcher"))
        vm.onEvent(ApiWebhooksEvent.SubmitCreateWebhook)
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals(2, vm.state.value.webhooks.size)
    }

    @Test
    fun testRotateSecretAndRetryDelivery() = runTest(testDispatcher) {
        val repo = FakeDeveloperRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = ApiWebhooksViewModel(repo, wsContext)
        advanceUntilIdle()

        val hook = vm.state.value.webhooks.first()
        val oldSecret = hook.secret

        vm.onEvent(ApiWebhooksEvent.RotateWebhookSecret(hook.id))
        advanceUntilIdle()

        val updatedHook = vm.state.value.webhooks.first()
        assertTrue(updatedHook.secret != oldSecret)

        // Retry delivery
        val del = vm.state.value.deliveries.first()
        vm.onEvent(ApiWebhooksEvent.RetryDelivery(del.id))
        advanceUntilIdle()

        assertEquals(4, vm.state.value.deliveries.size)
    }
}
