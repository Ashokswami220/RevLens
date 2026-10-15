package com.example.revlens.domain.calc

import com.example.revlens.model.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class LtvCalculatorTest {

    @Test
    fun `calculateLtv calculates pure revenue LTV correctly`() {
        val arpu = BigDecimal("50.00")
        val churn = 0.05 // 5% monthly churn -> 20 months lifespan
        // 50 / 0.05 = 1000.00
        val ltv = LtvCalculator.calculateLtv(arpu, churn)
        assertEquals(BigDecimal("1000.00"), ltv)
    }

    @Test
    fun `calculateLtv subtracts variable costs for gross margin LTV`() {
        val arpu = BigDecimal("65.67")
        val varCost = BigDecimal("15.00") // Net margin = 50.67
        val churn = 0.02 // 2% churn -> 50 months lifespan
        // 50.67 / 0.02 = 2533.50
        val ltv = LtvCalculator.calculateLtv(arpu, churn, varCost)
        assertEquals(BigDecimal("2533.50"), ltv)
    }

    @Test
    fun `calculateLtv handles 0 churn by capping at realistic 5 year horizon`() {
        val arpu = BigDecimal("100.00")
        val varCost = BigDecimal("20.00") // Net margin = 80.00
        val churn = 0.0 // 0% churn
        // 80.00 * 60 months = 4800.00
        val ltv = LtvCalculator.calculateLtv(arpu, churn, varCost)
        assertEquals(BigDecimal("4800.00"), ltv)
    }

    @Test
    fun `calculateLtv returns zero if variable costs exceed ARPU`() {
        val arpu = BigDecimal("50.00")
        val varCost = BigDecimal("60.00") // Negative margin
        val churn = 0.05
        val ltv = LtvCalculator.calculateLtv(arpu, churn, varCost)
        assertEquals(BigDecimal.ZERO, ltv)
    }

    @Test
    fun `calculateLtv returns zero if ARPU is zero`() {
        val ltv = LtvCalculator.calculateLtv(BigDecimal.ZERO, 0.05)
        assertEquals(BigDecimal.ZERO, ltv)
    }
}
