package com.example.revlens.domain.calc

import com.example.revlens.model.Assumptions
import com.example.revlens.model.BusinessProfile
import java.math.BigDecimal
import java.math.RoundingMode

object PricingSimulationEngine {

    /**
     * Applies the assumptions in a Scenario to a BusinessProfile to project the new state.
     * If [targetPlanId] or [assumptions.targetPlanId] is specified, only that plan's price is adjusted.
     * Otherwise, all plans are adjusted by [assumptions.priceChangePercent].
     */
    fun simulate(
        baseline: BusinessProfile,
        assumptions: Assumptions,
        targetPlanId: String? = assumptions.targetPlanId
    ): BusinessProfile {
        val effectivePlanId = targetPlanId ?: assumptions.targetPlanId
        val priceMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(assumptions.priceChangePercent))

        val newPlans = baseline.plans.map { plan ->
            if (effectivePlanId == null || plan.id == effectivePlanId) {
                val newPrice = plan.price.multiply(priceMultiplier)
                    .coerceAtLeast(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP)
                plan.copy(price = newPrice)
            } else {
                plan
            }
        }

        // 2. Adjust metrics based on assumptions
        val newChurnRate =
            (baseline.monthlyChurnRate + assumptions.churnChangePercent).coerceAtLeast(0.0)
        val newGrowthRate =
            (baseline.monthlyGrowthRate + assumptions.growthChangePercent).coerceAtLeast(0.0)
        val newCac = baseline.cac.add(assumptions.cacChange)
            .coerceAtLeast(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP)
        val newFixedCosts = baseline.fixedCosts.add(assumptions.fixedCostChange)
            .coerceAtLeast(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP)

        return BusinessProfile(
            monthlyChurnRate = newChurnRate,
            monthlyGrowthRate = newGrowthRate,
            cac = newCac,
            fixedCosts = newFixedCosts,
            variableCostPerCustomer = baseline.variableCostPerCustomer,
            plans = newPlans,
            currencyCode = baseline.currencyCode
        )
    }
}
