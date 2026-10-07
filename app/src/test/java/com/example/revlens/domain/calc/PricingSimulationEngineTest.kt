package com.example.revlens.domain.calc

import com.example.revlens.domain.model.Assumptions
import com.example.revlens.domain.model.BusinessProfile
import com.example.revlens.domain.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class PricingSimulationEngineTest {

    @Test
    fun `simulate applies assumptions correctly`() {
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.02,
            monthlyGrowthRate = 0.05,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 100)
            )
        )

        val assumptions = Assumptions(
            priceChangePercent = 0.10, // 10% increase
            churnChangePercent = 0.01, // 1% increase
            cacChange = BigDecimal("20.00"), // CAC increases by 20
            growthChangePercent = -0.01, // Growth drops by 1%
            fixedCostChange = BigDecimal("500.00") // Fixed cost increases by 500
        )

        val projected = PricingSimulationEngine.simulate(baseline, assumptions)

        // Prices should be 50 * 1.1 = 55
        assertEquals(BigDecimal("55.00"), projected.plans[0].price)
        // Churn should be 0.02 + 0.01 = 0.03
        assertEquals(0.03, projected.monthlyChurnRate, 0.0001)
        // Growth should be 0.05 - 0.01 = 0.04
        assertEquals(0.04, projected.monthlyGrowthRate, 0.0001)
        // CAC should be 120
        assertEquals(BigDecimal("120.00"), projected.cac)
        // Fixed costs should be 1500
        assertEquals(BigDecimal("1500.00"), projected.fixedCosts)
    }
}
