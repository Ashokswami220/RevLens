package com.revlens.app.domain.calc

import com.revlens.app.domain.model.SubscriptionRecord
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RevenueBreakdownCalculatorTest {

    @Test
    fun testCalculateModeled() {
        val profile = BusinessProfile(
            monthlyChurnRate = 2.0,
            monthlyGrowthRate = 5.0,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("5000.00"),
            variableCostPerCustomer = BigDecimal("5.00"),
            plans = listOf(
                Plan(id = "1", name = "Starter", price = BigDecimal("29.00"), customerCount = 100),
                Plan(id = "2", name = "Pro", price = BigDecimal("79.00"), customerCount = 50)
            )
        )

        val result = RevenueBreakdownCalculator.calculateModeled(
            profile = profile,
            newGrowthRatePercent = 10.0,
            expansionRatePercent = 2.0,
            contractionRatePercent = 1.0
        )

        // Starting MRR = 100*29 + 50*79 = 2900 + 3950 = $6,850.00 = 685,000 cents
        assertEquals(685000L, result.startingMrrMinor)
        assertEquals(2, result.planShares.size)
        // New MRR = 10% of 685,000 = 68,500
        assertEquals(68500L, result.newMrrMinor)
        // Churn MRR = 2% of 685,000 = 13,700
        assertEquals(13700L, result.churnMrrMinor)
        // Ending MRR > Starting MRR
        assertTrue(result.endingMrrMinor > result.startingMrrMinor)
        assertTrue(result.quickRatio > 1.0)
        assertEquals(6, result.waterfallSteps.size)
    }

    @Test
    fun testCalculateActual() {
        val subscriptions = listOf(
            SubscriptionRecord(customerId = "C1", plan = "Pro", startDate = "2024-01-01", mrrMinor = 7900L),
            SubscriptionRecord(customerId = "C2", plan = "Starter", startDate = "2024-01-10", mrrMinor = 2900L),
            SubscriptionRecord(customerId = "C3", plan = "Starter", startDate = "2024-01-01", endDate = "2024-02-01", mrrMinor = 2900L)
        )

        val result = RevenueBreakdownCalculator.calculateActual(subscriptions)

        // Active: C1 (7900) + C2 (2900) = 10800
        assertEquals(10800L, result.endingMrrMinor)
        // Churned: C3 (2900)
        assertEquals(2900L, result.churnMrrMinor)
        assertEquals(2, result.planShares.size)
        assertTrue(result.quickRatio > 0.0)
    }
}
