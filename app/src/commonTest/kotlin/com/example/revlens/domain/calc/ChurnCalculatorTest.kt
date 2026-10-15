package com.example.revlens.domain.calc

import com.example.revlens.model.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ChurnCalculatorTest {

    @Test
    fun `calculateChurnRatePercentage calculates exact percentage`() {
        // 5 churned out of 100 = 5.00%
        val rate = ChurnCalculator.calculateChurnRatePercentage(5, 100)
        assertEquals(BigDecimal("5.00"), rate)

        // 3 churned out of 150 = 2.00%
        val rate2 = ChurnCalculator.calculateChurnRatePercentage(3, 150)
        assertEquals(BigDecimal("2.00"), rate2)
    }

    @Test
    fun `calculateChurnRatePercentage returns zero if start count is zero`() {
        val rate = ChurnCalculator.calculateChurnRatePercentage(5, 0)
        assertEquals(BigDecimal.ZERO, rate)
    }

    @Test
    fun `estimateChurnedCustomers rounds to correct count`() {
        // 150 customers * 2.00% churn = 3 customers lost
        val lost = ChurnCalculator.estimateChurnedCustomers(150, BigDecimal("2.00"))
        assertEquals(3, lost)

        // 100 customers * 5.5% churn = 5.5 -> rounds to 6 customers lost
        val lost2 = ChurnCalculator.estimateChurnedCustomers(100, BigDecimal("5.50"))
        assertEquals(6, lost2)
    }
}
