package com.revlens.app.domain.calc

import com.revlens.app.domain.model.MarketingSpendRecord
import com.revlens.app.domain.model.SubscriptionRecord
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CacCalculatorTest {

    @Test
    fun testCalculateModeled() {
        val profile = BusinessProfile(
            monthlyChurnRate = 2.0,
            monthlyGrowthRate = 10.0,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("5000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(id = "1", name = "Starter", price = BigDecimal("50.00"), customerCount = 100)
            )
        )

        val result = CacCalculator.calculateModeled(profile)

        // totalCustomers = 100, monthlyGrowthRate = 10% -> 10 new customers
        assertEquals(10, result.totalNewCustomers)
        // base CAC = $150 = 15000 cents
        assertEquals(15000L, result.blendedCacMinor)
        assertEquals(4, result.channelBreakdown.size)
        assertEquals(3, result.funnelStages.size)
        assertTrue(result.paybackPeriodMonths > 0.0)
    }

    @Test
    fun testCalculateActual() {
        val spend = listOf(
            MarketingSpendRecord(
                month = "2024-01-01", channel = "Google Ads", spendMinor = 100000L,
                newCustomers = 10
            ),
            MarketingSpendRecord(
                month = "2024-01-01", channel = "Meta Ads", spendMinor = 50000L, newCustomers = 5
            )
        )
        val subs = listOf(
            SubscriptionRecord(
                customerId = "C1", plan = "Starter", startDate = "2024-01-01", mrrMinor = 5000L
            )
        )

        val result = CacCalculator.calculateActual(
            spendRecords = spend,
            subscriptions = subs,
            arpu = BigDecimal("50.00"),
            grossMarginPercent = 80.0
        )

        // Total spend = 150,000 cents = $1,500
        assertEquals(150000L, result.totalSpendMinor)
        // Total new customers from spend = 15
        assertEquals(15, result.totalNewCustomers)
        // Blended CAC = 150,000 / 15 = 10,000 cents = $100
        assertEquals(10000L, result.blendedCacMinor)
        assertEquals(2, result.channelBreakdown.size)
        // Payback period: CAC $100 / ($50 * 0.8 = $40) = 2.5 months
        assertEquals(2.5, result.paybackPeriodMonths)
    }
}
