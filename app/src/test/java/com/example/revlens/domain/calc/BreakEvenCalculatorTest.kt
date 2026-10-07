package com.example.revlens.domain.calc

import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class BreakEvenCalculatorTest {

    @Test
    fun `calculateCacPaybackPeriod returns correct months`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"), // CAC = 200
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"), // Var = 10
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 1) // ARPU = 50
            )
        )

        // Margin per user = 50 - 10 = 40
        // CAC Payback = 200 / 40 = 5 months
        val payback = BreakEvenCalculator.calculateCacPaybackPeriod(profile)
        assertEquals(5.0, payback!!, 0.01)
    }

    @Test
    fun `calculateCacPaybackPeriod returns null if margin is negative`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("60.00"), // Var = 60, ARPU = 50, margin = -10
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 1)
            )
        )

        val payback = BreakEvenCalculator.calculateCacPaybackPeriod(profile)
        assertNull(payback)
    }

    @Test
    fun `calculateBreakEvenCustomers returns correct customers`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"), // Fixed = 1000
            variableCostPerCustomer = BigDecimal("10.00"), // Var = 10
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 1) // ARPU = 50
            )
        )

        // Margin per user = 50 - 10 = 40
        // Break even customers = 1000 / 40 = 25
        val customers = BreakEvenCalculator.calculateBreakEvenCustomers(profile)
        assertEquals(25, customers)
    }
}
