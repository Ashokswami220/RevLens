package com.example.revlens.domain.calc

import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.MetricResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object WhatIfEngine {

    /**
     * Compares a baseline profile with a projected profile and returns a list of MetricResults.
     */
    fun compare(baseline: BusinessProfile, projected: BusinessProfile): List<MetricResult> {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
            .apply {
                currency = Currency.getInstance("USD")
                maximumFractionDigits = 0
            }
        val percentFormatter = NumberFormat.getPercentInstance(Locale.US)
            .apply {
                maximumFractionDigits = 1
            }

        val results = mutableListOf<MetricResult>()

        // 1. MRR
        val baseMrr = baseline.currentMrr
        val projMrr = projected.currentMrr
        val mrrDelta = calculatePercentDelta(baseMrr, projMrr)

        results.add(
            MetricResult(
                label = "MRR",
                value = projMrr,
                formattedValue = currencyFormatter.format(projMrr),
                deltaPercent = mrrDelta,
                isPositive = mrrDelta == null || mrrDelta >= 0
            )
        )

        // 2. ARPU
        val baseArpu = baseline.arpu
        val projArpu = projected.arpu
        val arpuDelta = calculatePercentDelta(baseArpu, projArpu)

        results.add(
            MetricResult(
                label = "ARPU",
                value = projArpu,
                formattedValue = currencyFormatter.format(projArpu),
                deltaPercent = arpuDelta,
                isPositive = arpuDelta == null || arpuDelta >= 0
            )
        )

        // 3. LTV (Lifetime Value)
        val baseLtv = LtvCalculator.calculateLtv(baseArpu, baseline.monthlyChurnRate, baseline.variableCostPerCustomer)
        val projLtv = LtvCalculator.calculateLtv(projArpu, projected.monthlyChurnRate, projected.variableCostPerCustomer)
        val ltvDelta = calculatePercentDelta(baseLtv, projLtv)

        results.add(
            MetricResult(
                label = "LTV",
                value = projLtv,
                formattedValue = currencyFormatter.format(projLtv),
                deltaPercent = ltvDelta,
                isPositive = ltvDelta == null || ltvDelta >= 0
            )
        )

        // 4. Payback Period
        val basePayback = BreakEvenCalculator.calculateCacPaybackPeriod(baseline)
        val projPayback = BreakEvenCalculator.calculateCacPaybackPeriod(projected)
        val paybackDelta = if (basePayback != null && projPayback != null && basePayback > 0) {
            ((projPayback - basePayback) / basePayback) * 100
        } else null

        val isPaybackPositive = when {
            projPayback == null -> false // Losing margin / Never recovering CAC is negative
            basePayback == null -> true  // Recovered from Never to a viable payback period
            paybackDelta == null -> true
            else -> paybackDelta <= 0    // Lower or equal payback duration is better
        }

        results.add(
            MetricResult(
                label = "CAC Payback",
                value = BigDecimal.valueOf(projPayback ?: 0.0),
                formattedValue = if (projPayback != null) "${
                    String.format(
                        Locale.US, "%.1f", projPayback
                    )
                } mo" else "Never",
                deltaPercent = paybackDelta,
                isPositive = isPaybackPositive
            )
        )

        // 5. 12-Month Projected MRR
        val base12Mo = ForecastEngine.forecast(baseline, 12).last().mrr
        val proj12Mo = ForecastEngine.forecast(projected, 12).last().mrr
        val forecastDelta = calculatePercentDelta(base12Mo, proj12Mo)

        results.add(
            MetricResult(
                label = "12-Mo MRR",
                value = proj12Mo,
                formattedValue = currencyFormatter.format(proj12Mo),
                deltaPercent = forecastDelta,
                isPositive = forecastDelta == null || forecastDelta >= 0
            )
        )

        return results
    }

    private fun calculatePercentDelta(baseline: BigDecimal, projected: BigDecimal): Double? {
        if (baseline.compareTo(BigDecimal.ZERO) == 0) return null
        val diff = projected.subtract(baseline)
        return diff.divide(baseline, 4, RoundingMode.HALF_UP)
            .toDouble() * 100
    }
}
