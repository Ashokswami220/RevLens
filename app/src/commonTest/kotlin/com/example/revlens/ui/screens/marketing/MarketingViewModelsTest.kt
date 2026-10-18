package com.example.revlens.ui.screens.marketing

import com.example.revlens.domain.model.BillingPeriod
import com.example.revlens.domain.model.PlanTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
class MarketingViewModelsTest {

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
    fun testPublicPricingBillingPeriodSwitch() = runTest(testDispatcher) {
        val vm = PublicPricingViewModel()
        assertEquals(BillingPeriod.ANNUAL, vm.state.value.billingPeriod)

        vm.onEvent(PublicPricingEvent.SelectBillingPeriod(BillingPeriod.MONTHLY))
        advanceUntilIdle()

        assertEquals(BillingPeriod.MONTHLY, vm.state.value.billingPeriod)
    }

    @Test
    fun testPublicPricingFaqToggle() = runTest(testDispatcher) {
        val vm = PublicPricingViewModel()
        assertTrue(vm.state.value.expandedFaqIndices.contains(0))

        // Toggle index 0 off
        vm.onEvent(PublicPricingEvent.ToggleFaq(0))
        advanceUntilIdle()
        assertFalse(vm.state.value.expandedFaqIndices.contains(0))

        // Toggle index 1 on
        vm.onEvent(PublicPricingEvent.ToggleFaq(1))
        advanceUntilIdle()
        assertTrue(vm.state.value.expandedFaqIndices.contains(1))
    }

    @Test
    fun testLandingNavigationEvents() = runTest(testDispatcher) {
        val vm = LandingViewModel()
        var effectReceived: LandingEffect? = null

        val job = launch {
            vm.effects.collect { effectReceived = it }
        }

        vm.onEvent(LandingEvent.ClickGetStarted)
        advanceUntilIdle()
        assertEquals(LandingEffect.NavigateToAuth, effectReceived)

        vm.onEvent(LandingEvent.ClickExploreDemo)
        advanceUntilIdle()
        assertEquals(LandingEffect.NavigateToDemo, effectReceived)

        vm.onEvent(LandingEvent.ClickPricing)
        advanceUntilIdle()
        assertEquals(LandingEffect.NavigateToPricing, effectReceived)

        job.cancel()
    }
}
