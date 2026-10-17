package com.example.revlens.domain.calc

import com.example.revlens.domain.model.Dataset
import com.example.revlens.domain.model.SubscriptionRecord
import com.example.revlens.model.BigDecimal
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import com.example.revlens.ui.components.display.DataSourceMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomerSegmentationCalculatorTest {

    @Test
    fun testModeledCalculation() {
        val profile = BusinessProfile(
            monthlyChurnRate = 2.5,
            monthlyGrowthRate = 8.0,
            cac = BigDecimal("120.00"),
            fixedCosts = BigDecimal("4000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(id = "1", name = "Starter", price = BigDecimal("29.00"), customerCount = 100),
                Plan(id = "2", name = "Pro", price = BigDecimal("99.00"), customerCount = 50)
            )
        )

        val result = CustomerSegmentationCalculator.calculate(
            profile = profile,
            dataset = null,
            period = AnalyticsPeriod.THREE_MONTHS,
            segmentBy = CustomerSegmentBy.PLAN
        )

        assertEquals(DataSourceMode.MODELED, result.sourceMode)
        assertEquals(150, result.activeCustomers)
        assertTrue(result.newCustomers > 0)
        assertTrue(result.churnedCustomers > 0)
        assertEquals(2, result.segments.size)
        assertEquals("Starter", result.segments[0].name)
        assertEquals(100, result.segments[0].customerCount)
        assertTrue(result.isChannelOrCountryAvailable)
        assertEquals(ConcentrationHealth.HEALTHY, result.concentrationBenchmark)
    }

    @Test
    fun testModeledModeChannelUnavailable() {
        val profile = BusinessProfile(
            monthlyChurnRate = 2.0,
            monthlyGrowthRate = 5.0,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("2000.00"),
            variableCostPerCustomer = BigDecimal("5.00"),
            plans = listOf(
                Plan(id = "1", name = "Standard", price = BigDecimal("50.00"), customerCount = 50)
            )
        )

        val result = CustomerSegmentationCalculator.calculate(
            profile = profile,
            dataset = null,
            segmentBy = CustomerSegmentBy.CHANNEL
        )

        assertFalse(result.isChannelOrCountryAvailable)
        assertTrue(result.segments.isEmpty())
    }

    @Test
    fun testActualDatasetCalculation() {
        val subscriptions = listOf(
            SubscriptionRecord("c1", "Pro", "2026-01-01", null, 10000L, "Google", "US"),
            SubscriptionRecord("c2", "Pro", "2026-01-05", null, 10000L, "Meta", "US"),
            SubscriptionRecord("c3", "Starter", "2026-01-10", null, 3000L, "Google", "UK"),
            SubscriptionRecord("c4", "Starter", "2026-01-12", null, 3000L, "Referral", "CA"),
            SubscriptionRecord("c5", "Enterprise", "2026-01-15", null, 50000L, "Direct", "US"),
            SubscriptionRecord("c6", "Starter", "2025-11-01", "2026-02-01", 3000L, "Google", "US") // churned
        )

        val dataset = Dataset(
            id = "ds_1",
            workspaceId = "ws_1",
            filename = "subs.csv",
            subscriptions = subscriptions
        )

        val resultPlan = CustomerSegmentationCalculator.calculate(
            profile = null,
            dataset = dataset,
            period = AnalyticsPeriod.THREE_MONTHS,
            segmentBy = CustomerSegmentBy.PLAN
        )

        assertEquals(DataSourceMode.ACTUAL, resultPlan.sourceMode)
        assertEquals(5, resultPlan.activeCustomers)
        assertEquals(1, resultPlan.churnedCustomers)
        assertTrue(resultPlan.isChannelOrCountryAvailable)
        // 3 plans: Starter (2), Pro (2), Enterprise (1)
        assertEquals(3, resultPlan.segments.size)

        // Concentration check: 1 enterprise customer has $500 out of $760 total active MRR (~65.7% -> HIGH_RISK)
        assertEquals(ConcentrationHealth.HIGH_RISK, resultPlan.concentrationBenchmark)
        assertTrue(resultPlan.concentrationTop10PercentMrrShare > 50.0)

        // Masked customer check
        assertEquals(5, resultPlan.topCustomers.size)
        assertTrue(resultPlan.topCustomers[0].displayName.startsWith("Customer ***"))
        assertEquals(1, resultPlan.recentlyDowngraded.size)

        // Segment by Country
        val resultCountry = CustomerSegmentationCalculator.calculate(
            profile = null,
            dataset = dataset,
            period = AnalyticsPeriod.THREE_MONTHS,
            segmentBy = CustomerSegmentBy.COUNTRY
        )
        // Countries among active: US (3), UK (1), CA (1)
        assertEquals(3, resultCountry.segments.size)
        assertEquals("US", resultCountry.segments[0].name)
        assertEquals(3, resultCountry.segments[0].customerCount)
    }
}
