package com.example.revlens.domain.calc

import com.example.revlens.domain.model.Assumptions
import com.example.revlens.domain.model.BusinessProfile
import java.math.BigDecimal
import java.math.RoundingMode

object PricingSimulationEngine {

    /**
     * Applies the assumptions in a Scenario to a BusinessProfile to project the new state.
     */
    fun simulate(baseline: BusinessProfile, assumptions: Assumptions): BusinessProfile {
        // 1. Calculate new prices for all plans
        val priceMultiplier = BigDecimal.ONE.add(BigDecimal(assumptions.priceChangePercent))

        val newPlans = baseline.plans.map { plan ->
            val newPrice = plan.price.multiply(priceMultiplier)
                .setScale(2, RoundingMode.HALF_UP)
            plan.copy(price = newPrice)
        }

        // 2. Adjust metrics based on assumptions
        val newChurnRate =
            (baseline.monthlyChurnRate + assumptions.churnChangePercent).coerceAtLeast(0.0)
        val newGrowthRate =
            (baseline.monthlyGrowthRate + assumptions.growthChangePercent).coerceAtLeast(0.0)
        val newCac = baseline.cac.add(assumptions.cacChange)
            .coerceAtLeast(BigDecimal.ZERO)
        val newFixedCosts = baseline.fixedCosts.add(assumptions.fixedCostChange)
            .coerceAtLeast(BigDecimal.ZERO)

        return BusinessProfile(
            monthlyChurnRate = newChurnRate,
            monthlyGrowthRate = newGrowthRate,
            cac = newCac,
            fixedCosts = newFixedCosts,
            variableCostPerCustomer = baseline.variableCostPerCustomer,
            plans = newPlans
        )
    }
}
