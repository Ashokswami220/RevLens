package com.example.revlens.domain.calc

import com.example.revlens.model.BigDecimal
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun `calculateBreakEvenMrr returns exact required monthly revenue`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 1)
            )
        )

        // Break even customers = 25
        // Break even MRR = 25 * 50 = 1250.00
        val breakEvenMrr = BreakEvenCalculator.calculateBreakEvenMrr(profile)
        assertEquals(BigDecimal("1250.00"), breakEvenMrr)
    }

    @Test
    fun `calculateBreakEvenCustomers rounds up using CEILING for fractional customers`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("1000.00"), // Fixed = 1000
            variableCostPerCustomer = BigDecimal("10.00"), // Var = 10
            plans = listOf(
                Plan(
                    name = "Basic", price = BigDecimal("40.00"), customerCount = 1
                ) // ARPU = 40, margin = 30
            )
        )

        // 1000 / 30 = 33.333... -> CEILING must be 34
        val customers = BreakEvenCalculator.calculateBreakEvenCustomers(profile)
        assertEquals(34, customers)
    }

    @Test
    fun `calculateCacPaybackPeriod returns 0 when CAC is zero`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal.ZERO,
            fixedCosts = BigDecimal("500.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 10)
            )
        )

        val payback = BreakEvenCalculator.calculateCacPaybackPeriod(profile)
        assertEquals(0.0, payback!!, 0.001)
    }

    @Test
    fun `calculateBreakEvenCustomers returns null when margin is zero or negative`() {
        val zeroMarginProfile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("50.00"), // ARPU = 50, Var = 50, Margin = 0
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 10)
            )
        )

        assertNull(BreakEvenCalculator.calculateBreakEvenCustomers(zeroMarginProfile))
        assertNull(BreakEvenCalculator.calculateBreakEvenMrr(zeroMarginProfile))
        assertNull(BreakEvenCalculator.calculateCacPaybackPeriod(zeroMarginProfile))
    }

    @Test
    fun `break even returns null when profile has zero customers`() {
        val emptyProfile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 0)
            )
        )

        // ARPU is 0, margin is 0 - 10 = -10 <= 0 -> returns null
        assertNull(BreakEvenCalculator.calculateBreakEvenCustomers(emptyProfile))
        assertNull(BreakEvenCalculator.calculateBreakEvenMrr(emptyProfile))
        assertNull(BreakEvenCalculator.calculateCacPaybackPeriod(emptyProfile))
    }
}
