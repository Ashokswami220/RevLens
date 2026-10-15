package com.example.revlens.domain.calc

import com.example.revlens.model.BigDecimal
import com.example.revlens.model.RoundingMode

object LtvCalculator {
    /**
     * Calculates Customer Lifetime Value (LTV).
     *
     * In standard SaaS financial modeling:
     * LTV = (ARPU - VariableCostPerCustomer) / MonthlyChurnRate
     *
     * @param arpu Average Monthly Revenue Per User.
     * @param churnRateFraction Monthly Churn Rate as a decimal fraction (e.g., 0.02 for 2%).
     * @param variableCostPerCustomer Monthly cost directly incurred to service a user (COGS).
     * @param maxMonthsHorizon Maximum lifetime horizon cap if churn is 0% (defaults to 60 months / 5 years).
     * @return Calculated LTV in exact currency precision.
     */
    fun calculateLtv(
        arpu: BigDecimal,
        churnRateFraction: Double,
        variableCostPerCustomer: BigDecimal = BigDecimal.ZERO,
        maxMonthsHorizon: Int = 60
    ): BigDecimal {
        val netMarginPerCustomer = arpu.subtract(variableCostPerCustomer)
            .coerceAtLeast(BigDecimal.ZERO)
        if (netMarginPerCustomer <= BigDecimal.ZERO) return BigDecimal.ZERO

        // If churn is 0 or negative (100% retention), cap to a realistic 5-year maximum horizon
        if (churnRateFraction <= 0.0) {
            return netMarginPerCustomer.multiply(BigDecimal(maxMonthsHorizon))
                .setScale(2, RoundingMode.HALF_UP)
        }

        val churnBd = BigDecimal.valueOf(churnRateFraction)
        if (churnBd <= BigDecimal.ZERO) {
            return netMarginPerCustomer.multiply(BigDecimal(maxMonthsHorizon))
                .setScale(2, RoundingMode.HALF_UP)
        }

        return netMarginPerCustomer.divide(churnBd, 2, RoundingMode.HALF_UP)
    }
}
