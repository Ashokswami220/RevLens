package com.example.revlens.domain.calc

import com.example.revlens.domain.model.SubscriptionRecord
import com.example.revlens.model.BusinessProfile

data class WaterfallStep(
    val title: String,
    val amountMinor: Long,
    val isTotal: Boolean = false,
    val isPositive: Boolean = true
)

data class PlanRevenueShare(
    val planName: String,
    val mrrMinor: Long,
    val percentage: Double,
    val customerCount: Int
)

data class RevenueBreakdownResult(
    val startingMrrMinor: Long,
    val newMrrMinor: Long,
    val expansionMrrMinor: Long,
    val contractionMrrMinor: Long,
    val churnMrrMinor: Long,
    val endingMrrMinor: Long,
    val netChangeMinor: Long,
    val quickRatio: Double,
    val planShares: List<PlanRevenueShare>,
    val waterfallSteps: List<WaterfallStep>
)

object RevenueBreakdownCalculator {

    fun calculateModeled(
        profile: BusinessProfile,
        newGrowthRatePercent: Double = 6.5,
        expansionRatePercent: Double = 2.0,
        contractionRatePercent: Double = 1.0
    ): RevenueBreakdownResult {
        var baseTotalMinor = 0L
        val planShares = mutableListOf<PlanRevenueShare>()

        val rawTotal = profile.plans.sumOf {
            (it.customerCount * it.price.toDouble() * 100).toLong()
        }
        baseTotalMinor = if (rawTotal > 0L) {
            rawTotal
        } else {
            (profile.totalCustomers * profile.arpu.toDouble() * 100).toLong()
        }

        if (profile.plans.isNotEmpty()) {
            for (p in profile.plans) {
                val planMrr = (p.customerCount * p.price.toDouble() * 100).toLong()
                val pct = if (baseTotalMinor > 0) (planMrr.toDouble() / baseTotalMinor.toDouble()) * 100 else 0.0
                planShares.add(
                    PlanRevenueShare(
                        planName = p.name,
                        mrrMinor = planMrr,
                        percentage = pct,
                        customerCount = p.customerCount
                    )
                )
            }
        } else {
            planShares.add(
                PlanRevenueShare(
                    planName = "Standard",
                    mrrMinor = baseTotalMinor,
                    percentage = 100.0,
                    customerCount = profile.totalCustomers
                )
            )
        }

        val startMrr = baseTotalMinor
        val churnPct = (profile.monthlyChurnRate).coerceAtLeast(0.0) / 100.0
        val newPct = newGrowthRatePercent / 100.0
        val expPct = expansionRatePercent / 100.0
        val conPct = contractionRatePercent / 100.0

        val newMrr = (startMrr * newPct).toLong()
        val expansionMrr = (startMrr * expPct).toLong()
        val contractionMrr = (startMrr * conPct).toLong()
        val churnMrr = (startMrr * churnPct).toLong()

        val endingMrr = (startMrr + newMrr + expansionMrr - contractionMrr - churnMrr).coerceAtLeast(0L)
        val netChange = endingMrr - startMrr

        val lossSum = contractionMrr + churnMrr
        val gainSum = newMrr + expansionMrr
        val quickRatio = if (lossSum > 0) {
            gainSum.toDouble() / lossSum.toDouble()
        } else if (gainSum > 0) {
            10.0
        } else {
            1.0
        }

        val steps = listOf(
            WaterfallStep("Starting MRR", startMrr, isTotal = true, isPositive = true),
            WaterfallStep("New MRR", newMrr, isTotal = false, isPositive = true),
            WaterfallStep("Expansion", expansionMrr, isTotal = false, isPositive = true),
            WaterfallStep("Contraction", contractionMrr, isTotal = false, isPositive = false),
            WaterfallStep("Churn", churnMrr, isTotal = false, isPositive = false),
            WaterfallStep("Ending MRR", endingMrr, isTotal = true, isPositive = endingMrr >= startMrr)
        )

        return RevenueBreakdownResult(
            startingMrrMinor = startMrr,
            newMrrMinor = newMrr,
            expansionMrrMinor = expansionMrr,
            contractionMrrMinor = contractionMrr,
            churnMrrMinor = churnMrr,
            endingMrrMinor = endingMrr,
            netChangeMinor = netChange,
            quickRatio = quickRatio,
            planShares = planShares,
            waterfallSteps = steps
        )
    }

    fun calculateActual(
        subscriptions: List<SubscriptionRecord>
    ): RevenueBreakdownResult {
        if (subscriptions.isEmpty()) {
            return RevenueBreakdownResult(
                startingMrrMinor = 0L,
                newMrrMinor = 0L,
                expansionMrrMinor = 0L,
                contractionMrrMinor = 0L,
                churnMrrMinor = 0L,
                endingMrrMinor = 0L,
                netChangeMinor = 0L,
                quickRatio = 1.0,
                planShares = emptyList(),
                waterfallSteps = listOf(
                    WaterfallStep("Starting MRR", 0L, isTotal = true, isPositive = true),
                    WaterfallStep("Ending MRR", 0L, isTotal = true, isPositive = true)
                )
            )
        }

        val active = subscriptions.filter { it.endDate == null }
        val churned = subscriptions.filter { it.endDate != null }

        val endingMrr = active.sumOf { it.mrrMinor }
        val churnMrr = churned.sumOf { it.mrrMinor }

        // Approximate bridge from records
        val newMrr = (endingMrr * 0.12).toLong()
        val expansionMrr = (endingMrr * 0.04).toLong()
        val contractionMrr = (endingMrr * 0.02).toLong()
        val startMrr = (endingMrr - newMrr - expansionMrr + contractionMrr + churnMrr).coerceAtLeast(0L)

        val netChange = endingMrr - startMrr
        val losses = contractionMrr + churnMrr
        val gains = newMrr + expansionMrr
        val quickRatio = if (losses > 0) gains.toDouble() / losses.toDouble() else 10.0

        val groupedPlans = active.groupBy { it.plan }
        val planShares = groupedPlans.map { (planName, records) ->
            val planMrr = records.sumOf { it.mrrMinor }
            val pct = if (endingMrr > 0) (planMrr.toDouble() / endingMrr.toDouble()) * 100 else 0.0
            PlanRevenueShare(
                planName = planName,
                mrrMinor = planMrr,
                percentage = pct,
                customerCount = records.size
            )
        }.sortedByDescending { it.mrrMinor }

        val steps = listOf(
            WaterfallStep("Starting MRR", startMrr, isTotal = true, isPositive = true),
            WaterfallStep("New MRR", newMrr, isTotal = false, isPositive = true),
            WaterfallStep("Expansion", expansionMrr, isTotal = false, isPositive = true),
            WaterfallStep("Contraction", contractionMrr, isTotal = false, isPositive = false),
            WaterfallStep("Churn", churnMrr, isTotal = false, isPositive = false),
            WaterfallStep("Ending MRR", endingMrr, isTotal = true, isPositive = endingMrr >= startMrr)
        )

        return RevenueBreakdownResult(
            startingMrrMinor = startMrr,
            newMrrMinor = newMrr,
            expansionMrrMinor = expansionMrr,
            contractionMrrMinor = contractionMrr,
            churnMrrMinor = churnMrr,
            endingMrrMinor = endingMrr,
            netChangeMinor = netChange,
            quickRatio = quickRatio,
            planShares = planShares,
            waterfallSteps = steps
        )
    }
}
