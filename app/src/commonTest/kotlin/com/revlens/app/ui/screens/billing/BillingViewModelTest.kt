package com.revlens.app.ui.screens.billing

import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.PlanTier
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
    fun testInitialState() = runTest(testDispatcher) {
        val vm = BillingViewModel()
        val s = vm.state.value
        assertEquals(PlanTier.PRO, s.subscription.planTier)
        assertFalse(s.isChangePlanSheetVisible)
    }

    @Test
    fun testChangePlan() = runTest(testDispatcher) {
        val vm = BillingViewModel()

        vm.onEvent(BillingEvent.OpenChangePlanSheet)
        assertTrue(vm.state.value.isChangePlanSheetVisible)

        vm.onEvent(BillingEvent.SelectBillingPeriod(BillingPeriod.MONTHLY))
        vm.onEvent(BillingEvent.SelectPlan(PlanTier.ENTERPRISE))
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(PlanTier.ENTERPRISE, s.subscription.planTier)
        assertFalse(s.isChangePlanSheetVisible)
    }
}
