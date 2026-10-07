package com.example.revlens.domain.calc

import com.example.revlens.model.Assumptions
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

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

        // LTV should increase since ARPU increased
        val ltvResult = results.find { it.label == "LTV" }
        assertEquals(20.0, ltvResult!!.deltaPercent!!, 0.01)
        assertTrue(ltvResult.isPositive)
    }
}
