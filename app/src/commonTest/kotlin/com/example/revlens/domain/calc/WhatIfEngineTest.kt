package com.example.revlens.domain.calc

import com.example.revlens.model.Assumptions
import com.example.revlens.model.BigDecimal
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WhatIfEngineTest {

    @Test
    fun `compare generates correct metric results`() {
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 100)
            )
        )

        // Scenario: price increases by 20%
        val assumptions = Assumptions(priceChangePercent = 0.20)
        val projected = PricingSimulationEngine.simulate(baseline, assumptions)

        val results = WhatIfEngine.compare(baseline, projected)

        // MRR should be +20%
        val mrrResult = results.find { it.label == "MRR" }
        assertEquals(20.0, mrrResult!!.deltaPercent!!, 0.01)
        assertTrue(mrrResult.isPositive)

        // LTV should increase: margin increases from $40 to $50 (+25%)
        val ltvResult = results.find { it.label == "LTV" }
        assertEquals(25.0, ltvResult!!.deltaPercent!!, 0.01)
        assertTrue(ltvResult.isPositive)

        // CAC payback should drop from 5.0 mo to 4.0 mo (-20%) and be marked positive (shorter payback is good)
        val paybackResult = results.find { it.label == "CAC Payback" }
        assertEquals(-20.0, paybackResult!!.deltaPercent!!, 0.01)
        assertTrue(paybackResult.isPositive)

        // 12-Mo MRR should also be present and positive
        val forecastResult = results.find { it.label == "12-Mo MRR" }
        assertTrue(forecastResult!!.deltaPercent!! > 0)
        assertTrue(forecastResult.isPositive)
    }

    @Test
    fun `compare flags payback as negative when margin becomes non-viable`() {
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 100)
            )
        )

        // Variable cost exceeds ARPU: margin is negative, payback is Never
        val projected = baseline.copy(
            variableCostPerCustomer = BigDecimal("60.00")
        )

        val results = WhatIfEngine.compare(baseline, projected)
        val paybackResult = results.find { it.label == "CAC Payback" }
        assertEquals("Never", paybackResult!!.formattedValue)
        assertFalse(
            paybackResult.isPositive, "Payback of Never must be marked negative"
        )
    }

    @Test
    fun `compare reflects growth acceleration in 12-Mo MRR`() {
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.03,
            monthlyGrowthRate = 0.05,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("5.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 100)
            )
        )

        // Growth rate boosted by +10% (from 5% to 15%)
        val assumptions = Assumptions(growthChangePercent = 0.10)
        val projected = PricingSimulationEngine.simulate(baseline, assumptions)

        val results = WhatIfEngine.compare(baseline, projected)
        val forecastResult = results.find { it.label == "12-Mo MRR" }

        assertTrue(
            forecastResult!!.deltaPercent!! > 50.0,
            "Projected 12-Mo MRR should expand significantly"
        )
        assertTrue(forecastResult.isPositive)
    }

    @Test
    fun `compare handles zero baseline gracefully without arithmetic exceptions`() {
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("500.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 0)
            )
        )

        val projected = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("500.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 50)
            )
        )

        val results = WhatIfEngine.compare(baseline, projected)
        assertEquals(5, results.size)
        // With 0 baseline MRR, deltaPercent should be null without crashing
        val mrrResult = results.find { it.label == "MRR" }
        assertNull(mrrResult!!.deltaPercent)
        assertTrue(mrrResult.isPositive)
    }
}
