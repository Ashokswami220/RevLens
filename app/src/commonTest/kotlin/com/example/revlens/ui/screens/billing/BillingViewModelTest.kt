package com.example.revlens.ui.screens.billing

import com.example.revlens.data.fake.FakeBillingRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.model.BillingPeriod
import com.example.revlens.domain.model.PlanTier
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
class BillingViewModelTest {

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
    fun testInitialStateLoadsSubscriptionAndInvoices() = runTest(testDispatcher) {
        val repo = FakeBillingRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = BillingViewModel(repo, wsContext)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(PlanTier.PRO, s.subscription.planTier)
        assertEquals(3, s.invoices.size)
        assertFalse(s.subscription.cancelAtPeriodEnd)
    }

    @Test
    fun testChangePlanUpdatesSubscriptionAndAddsInvoice() = runTest(testDispatcher) {
        val repo = FakeBillingRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = BillingViewModel(repo, wsContext)
        advanceUntilIdle()

        vm.onEvent(BillingEvent.SelectBillingPeriod(BillingPeriod.MONTHLY))
        vm.onEvent(BillingEvent.SelectPlan(PlanTier.ENTERPRISE))
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(PlanTier.ENTERPRISE, s.subscription.planTier)
        assertEquals(BillingPeriod.MONTHLY, s.subscription.period)
        assertEquals(4, s.invoices.size)
    }

    @Test
    fun testCancelSubscriptionAndReactivate() = runTest(testDispatcher) {
        val repo = FakeBillingRepository()
        val wsContext = DefaultWorkspaceContext("ws_1")
        val vm = BillingViewModel(repo, wsContext)
        advanceUntilIdle()

        // Cancel
        vm.onEvent(BillingEvent.ConfirmCancelSubscription)
        advanceUntilIdle()
        assertTrue(vm.state.value.subscription.cancelAtPeriodEnd)

        // Reactivate
        vm.onEvent(BillingEvent.ReactivateSubscription)
        advanceUntilIdle()
        assertFalse(vm.state.value.subscription.cancelAtPeriodEnd)
    }
}
