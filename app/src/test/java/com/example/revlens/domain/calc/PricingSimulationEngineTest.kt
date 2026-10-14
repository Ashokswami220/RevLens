package com.example.revlens.domain.calc

import com.example.revlens.model.Assumptions
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
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

    @Test
    fun `simulate modifies only target plan when targetPlanId is provided`() {
        val starterPlan = Plan(
            id = "starter_1", name = "Starter", price = BigDecimal("29.00"), customerCount = 100
        )
        val proPlan =
            Plan(id = "pro_2", name = "Pro", price = BigDecimal("99.00"), customerCount = 50)
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.03,
            monthlyGrowthRate = 0.08,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("2000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(starterPlan, proPlan)
        )

        // Adjust only Starter plan by +20%
        val assumptions = Assumptions(
            priceChangePercent = 0.20,
            targetPlanId = starterPlan.id
        )

        val projected = PricingSimulationEngine.simulate(baseline, assumptions)

        // Starter: 29.00 * 1.20 = 34.80
        assertEquals(BigDecimal("34.80"), projected.plans.find { it.id == starterPlan.id }?.price)
        // Pro: remains untouched at 99.00
        assertEquals(BigDecimal("99.00"), projected.plans.find { it.id == proPlan.id }?.price)
    }

    @Test
    fun `simulate rounds prices to 2 decimal places using HALF_UP`() {
        val plan = Plan(name = "Standard", price = BigDecimal("29.99"), customerCount = 10)
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.02,
            monthlyGrowthRate = 0.05,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("500.00"),
            variableCostPerCustomer = BigDecimal("5.00"),
            plans = listOf(plan)
        )

        // 29.99 * 1.15 = 34.4885 -> 34.49
        val assumptions = Assumptions(priceChangePercent = 0.15)
        val projected = PricingSimulationEngine.simulate(baseline, assumptions)

        assertEquals(BigDecimal("34.49"), projected.plans[0].price)
    }

    @Test
    fun `simulate coerses negative rates and costs to zero`() {
        val baseline = BusinessProfile(
            monthlyChurnRate = 0.02,
            monthlyGrowthRate = 0.05,
            cac = BigDecimal("50.00"),
            fixedCosts = BigDecimal("100.00"),
            variableCostPerCustomer = BigDecimal("5.00"),
            plans = listOf(Plan(name = "Basic", price = BigDecimal("20.00"), customerCount = 5))
        )

        val assumptions = Assumptions(
            priceChangePercent = -1.5, // -150% price change
            churnChangePercent = -0.10, // -10% churn
            growthChangePercent = -0.20, // -20% growth
            cacChange = BigDecimal("-200.00"),
            fixedCostChange = BigDecimal("-500.00")
        )

        val projected = PricingSimulationEngine.simulate(baseline, assumptions)

        assertEquals(BigDecimal("0.00"), projected.plans[0].price)
        assertEquals(0.0, projected.monthlyChurnRate, 0.0001)
        assertEquals(0.0, projected.monthlyGrowthRate, 0.0001)
        assertEquals(BigDecimal("0.00"), projected.cac)
        assertEquals(BigDecimal("0.00"), projected.fixedCosts)
    }
}
