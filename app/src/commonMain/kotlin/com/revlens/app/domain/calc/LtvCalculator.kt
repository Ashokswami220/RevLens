package com.revlens.app.domain.calc

import com.revlens.app.model.BigDecimal
import com.revlens.app.model.RoundingMode

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

    /**
     * Calculates Simple LTV = ARPU / Monthly Churn Rate.
     */
    fun calculateSimpleLtv(arpu: BigDecimal, churnRatePercent: Double): BigDecimal {
        val fraction = churnRatePercent / 100.0
        return calculateLtv(
            arpu = arpu, churnRateFraction = fraction, variableCostPerCustomer = BigDecimal.ZERO
        )
    }

    /**
     * Calculates Margin-Adjusted LTV = (ARPU * Gross Margin %) / Monthly Churn Rate.
     */
    fun calculateMarginAdjustedLtv(
        arpu: BigDecimal,
        grossMarginPercent: Double,
        churnRatePercent: Double
    ): BigDecimal {
        val grossMarginFraction = (grossMarginPercent / 100.0).coerceIn(0.0, 1.0)
        val netArpu = arpu.multiply(BigDecimal.valueOf(grossMarginFraction))
        val fraction = churnRatePercent / 100.0
        return calculateLtv(
            arpu = netArpu, churnRateFraction = fraction, variableCostPerCustomer = BigDecimal.ZERO
        )
    }

    /**
     * Generates a sensitivity curve across a range of churn rates (e.g. 0.5% to 10%).
     */
    fun calculateSensitivityPoints(
        arpu: BigDecimal,
        grossMarginPercent: Double,
        churnRatesPercent: List<Double> = listOf(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0)
    ): List<Pair<Double, BigDecimal>> {
        return churnRatesPercent.map { churn ->
            val ltv = calculateMarginAdjustedLtv(arpu, grossMarginPercent, churn)
            churn to ltv
        }
    }
}
