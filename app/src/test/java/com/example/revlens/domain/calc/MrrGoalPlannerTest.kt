package com.example.revlens.domain.calc

import com.example.revlens.domain.model.BusinessProfile
import com.example.revlens.domain.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class MrrGoalPlannerTest {

    @Test
    fun `calculateRequiredMetrics returns correct targets`() {
        val currentProfile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 100) // MRR = 5000
            )
        )

        // Target 10,000 MRR in 12 months. Current is 5,000
        val plan =
            MrrGoalPlanner.calculateRequiredMetrics(currentProfile, BigDecimal("10000.00"), 12)

        // target Customers = 10,000 / 50 = 200
        // new needed = 100. Over 12 months = 100 / 12 = 8 per month
        assertEquals(8, plan.requiredNewCustomersPerMonth)

        // Target ARPU if keeping same customers = 10000 / 100 = 100
        assertEquals(BigDecimal("100.00"), plan.requiredArpu)

        // Required CMGR: (10000 / 5000)^(1/12) - 1 = 2^(1/12) - 1 = ~0.05946
        assertEquals(0.05946, plan.requiredMonthlyGrowthRate, 0.0001)
    }
}
