package com.example.revlens.domain.calc

import java.math.BigDecimal
import java.math.RoundingMode

object LtvCalculator {
    /**
     * Calculates Customer Lifetime Value (LTV).
     * @param arpu Average Revenue Per User (Monthly)
     * @param churnRatePercentage Monthly Churn Rate as a percentage (e.g., 5.0 for 5%)
     * @return LTV = ARPU / (ChurnRate / 100), or 0 if churn rate is 0.
     */
    fun calculateLtv(arpu: BigDecimal, churnRateFraction: Double): BigDecimal {
        if (churnRateFraction <= 0.0) return BigDecimal.ZERO
        return arpu.divide(BigDecimal(churnRateFraction), 2, RoundingMode.HALF_UP)
    }
}
