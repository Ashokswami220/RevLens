package com.example.revlens.domain.calc

import com.example.revlens.domain.model.BusinessProfile
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

data class GoalPlan(
    val targetMrr: BigDecimal,
    val months: Int,
    val requiredMonthlyGrowthRate: Double,
    val requiredNewCustomersPerMonth: Int,
    val requiredArpu: BigDecimal
)

object MrrGoalPlanner {

    /**
     * Calculates the required metrics to hit a target MRR in a given number of months.
     */
    fun calculateRequiredMetrics(
        currentProfile: BusinessProfile,
        targetMrr: BigDecimal,
        months: Int
    ): GoalPlan {
        val currentMrr = currentProfile.currentMrr

        if (currentMrr <= BigDecimal.ZERO || months <= 0) {
            return GoalPlan(
                targetMrr = targetMrr,
                months = months,
                requiredMonthlyGrowthRate = 0.0,
                requiredNewCustomersPerMonth = 0,
                requiredArpu = currentProfile.arpu
            )
        }

        // Required compound monthly growth rate (CMGR)
        val cmgr = (targetMrr.toDouble() / currentMrr.toDouble()).pow(1.0 / months) - 1.0

        // Target customers if ARPU remains the same
        val targetCustomers = targetMrr.divide(currentProfile.arpu, 0, RoundingMode.CEILING)
            .toInt()
        val totalNewCustomersNeeded = targetCustomers - currentProfile.totalCustomers
        val requiredNewCustomersPerMonth =
            if (totalNewCustomersNeeded > 0) totalNewCustomersNeeded / months else 0

        // Target ARPU if customers remain the same
        val requiredArpu = if (currentProfile.totalCustomers > 0) {
            targetMrr.divide(BigDecimal(currentProfile.totalCustomers), 2, RoundingMode.HALF_UP)
        } else {
            targetMrr
        }

        return GoalPlan(
            targetMrr = targetMrr,
            months = months,
            requiredMonthlyGrowthRate = cmgr,
            requiredNewCustomersPerMonth = requiredNewCustomersPerMonth,
            requiredArpu = requiredArpu
        )
    }
}
