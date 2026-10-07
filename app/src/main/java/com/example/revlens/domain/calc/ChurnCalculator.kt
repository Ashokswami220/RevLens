package com.example.revlens.domain.calc

import java.math.BigDecimal
import java.math.RoundingMode

object ChurnCalculator {
    /**
     * Calculates the Monthly Churn Rate as a percentage (0-100).
     */
    fun calculateChurnRatePercentage(
        churnedCustomers: Int, totalCustomersAtStart: Int
    ): BigDecimal {
        if (totalCustomersAtStart == 0) return BigDecimal.ZERO
        val fraction = BigDecimal(churnedCustomers).divide(
            BigDecimal(totalCustomersAtStart), 4, RoundingMode.HALF_UP
        )
        return fraction.multiply(BigDecimal("100.00"))
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Calculates the estimated number of customers lost next month based on churn rate.
     */
    fun estimateChurnedCustomers(totalCustomers: Int, churnRatePercentage: BigDecimal): Int {
        val fraction = churnRatePercentage.divide(BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
        return fraction.multiply(BigDecimal(totalCustomers))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()
    }
}
