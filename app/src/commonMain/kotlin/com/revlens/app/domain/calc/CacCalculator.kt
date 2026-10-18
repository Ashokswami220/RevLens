package com.revlens.app.domain.calc

import com.revlens.app.domain.model.MarketingSpendRecord
import com.revlens.app.domain.model.SubscriptionRecord
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile

data class ChannelEfficiency(
    val channel: String,
    val totalSpendMinor: Long,
    val newCustomers: Int,
    val cacMinor: Long,
    val sharePercent: Double,
    val efficiencyRating: String // "Top Tier", "Strong", "Underperforming"
)

data class FunnelStage(
    val stageName: String,
    val count: Int,
    val conversionRateFromPrevious: Double = 100.0,
    val overallConversionRate: Double = 100.0
)

data class CacAnalysisResult(
    val blendedCacMinor: Long,
    val totalSpendMinor: Long,
    val totalNewCustomers: Int,
    val paybackPeriodMonths: Double,
    val channelBreakdown: List<ChannelEfficiency>,
    val funnelStages: List<FunnelStage>
)

object CacCalculator {

    fun calculateModeled(
        profile: BusinessProfile,
        simulatedBudgetMinor: Long? = null
    ): CacAnalysisResult {
        val baseCacMinor = (profile.cac.toDouble() * 100).toLong().coerceAtLeast(100L)
        val defaultMonthlyNewCustomers = (profile.totalCustomers * (profile.monthlyGrowthRate / 100.0)).toInt().coerceAtLeast(1)
        val defaultSpendMinor = simulatedBudgetMinor ?: (defaultMonthlyNewCustomers * baseCacMinor)

        val totalCustomers = defaultMonthlyNewCustomers
        val blendedCac = if (totalCustomers > 0) defaultSpendMinor / totalCustomers else baseCacMinor

        val arpuMinor = (profile.arpu.toDouble() * 100).toLong().coerceAtLeast(100L)
        val varCostMinor = (profile.variableCostPerCustomer.toDouble() * 100).toLong()
        val marginFraction = if (arpuMinor > 0) {
            ((arpuMinor - varCostMinor).toDouble() / arpuMinor.toDouble()).coerceIn(0.1, 1.0)
        } else 0.8
        val monthlyMarginMinor = (arpuMinor * marginFraction).toLong().coerceAtLeast(10L)
        val paybackMonths = if (monthlyMarginMinor > 0) {
            blendedCac.toDouble() / monthlyMarginMinor.toDouble()
        } else 0.0

        // Simulated distribution across standard acquisition channels
        val channels = listOf(
            Triple("Direct / Organic", 0.35, 0.40),
            Triple("Google Search Ads", 0.30, 0.25),
            Triple("Meta / Instagram", 0.25, 0.20),
            Triple("Referral & Viral", 0.10, 0.15)
        )

        val channelBreakdown = channels.map { (name, spendShare, custShare) ->
            val spend = (defaultSpendMinor * spendShare).toLong()
            val custs = (totalCustomers * custShare).toInt().coerceAtLeast(1)
            val cac = if (custs > 0) spend / custs else 0L
            val rating = when {
                cac <= (blendedCac * 0.75) -> "Top Tier"
                cac <= (blendedCac * 1.25) -> "Strong"
                else -> "Underperforming"
            }
            ChannelEfficiency(
                channel = name,
                totalSpendMinor = spend,
                newCustomers = custs,
                cacMinor = cac,
                sharePercent = spendShare * 100.0,
                efficiencyRating = rating
            )
        }

        // Standard SaaS acquisition funnel benchmark (e.g. 10,000 visitors -> 600 trials (6%) -> 150 paid (25%))
        val paidCount = totalCustomers
        val trialCount = (paidCount * 4.5).toInt().coerceAtLeast(paidCount)
        val visitorCount = (trialCount * 14.0).toInt().coerceAtLeast(trialCount)

        val funnelStages = listOf(
            FunnelStage(
                stageName = "Website Visitors",
                count = visitorCount,
                conversionRateFromPrevious = 100.0,
                overallConversionRate = 100.0
            ),
            FunnelStage(
                stageName = "Signups & Trials",
                count = trialCount,
                conversionRateFromPrevious = if (visitorCount > 0) (trialCount.toDouble() / visitorCount.toDouble()) * 100 else 0.0,
                overallConversionRate = if (visitorCount > 0) (trialCount.toDouble() / visitorCount.toDouble()) * 100 else 0.0
            ),
            FunnelStage(
                stageName = "Paid Customers",
                count = paidCount,
                conversionRateFromPrevious = if (trialCount > 0) (paidCount.toDouble() / trialCount.toDouble()) * 100 else 0.0,
                overallConversionRate = if (visitorCount > 0) (paidCount.toDouble() / visitorCount.toDouble()) * 100 else 0.0
            )
        )

        return CacAnalysisResult(
            blendedCacMinor = blendedCac,
            totalSpendMinor = defaultSpendMinor,
            totalNewCustomers = totalCustomers,
            paybackPeriodMonths = (paybackMonths * 10).toInt() / 10.0,
            channelBreakdown = channelBreakdown,
            funnelStages = funnelStages
        )
    }

    fun calculateActual(
        spendRecords: List<MarketingSpendRecord>,
        subscriptions: List<SubscriptionRecord>,
        arpu: BigDecimal,
        grossMarginPercent: Double
    ): CacAnalysisResult {
        if (spendRecords.isEmpty() && subscriptions.isEmpty()) {
            return CacAnalysisResult(
                blendedCacMinor = 0L,
                totalSpendMinor = 0L,
                totalNewCustomers = 0,
                paybackPeriodMonths = 0.0,
                channelBreakdown = emptyList(),
                funnelStages = emptyList()
            )
        }

        val totalSpend = spendRecords.sumOf { it.spendMinor }
        val groupedSpend = spendRecords.groupBy { it.channel.ifBlank { "Direct" } }
        val newFromSpend = spendRecords.sumOf { it.newCustomers ?: 0 }
        val newFromSubs = subscriptions.count { it.endDate == null }
        val totalNew = if (newFromSpend > 0) newFromSpend else newFromSubs.coerceAtLeast(1)

        val blendedCac = if (totalNew > 0) totalSpend / totalNew else totalSpend

        val monthlyMargin = arpu.toDouble() * (grossMarginPercent / 100.0)
        val paybackMonths = if (monthlyMargin > 0) (blendedCac / 100.0) / monthlyMargin else 0.0

        val channelList = mutableListOf<ChannelEfficiency>()
        groupedSpend.forEach { (channel, records) ->
            val spend = records.sumOf { it.spendMinor }
            val custs = records.sumOf { it.newCustomers ?: 0 }.coerceAtLeast(1)
            val cac = spend / custs
            val share = if (totalSpend > 0) (spend.toDouble() / totalSpend.toDouble()) * 100.0 else 0.0
            val rating = when {
                cac <= (blendedCac * 0.75) -> "Top Tier"
                cac <= (blendedCac * 1.25) -> "Strong"
                else -> "Underperforming"
            }
            channelList.add(
                ChannelEfficiency(
                    channel = channel,
                    totalSpendMinor = spend,
                    newCustomers = custs,
                    cacMinor = cac,
                    sharePercent = share,
                    efficiencyRating = rating
                )
            )
        }

        // Funnel estimation
        val paidCount = totalNew
        val trialCount = (paidCount * 4).coerceAtLeast(paidCount)
        val visitorCount = (trialCount * 12).coerceAtLeast(trialCount)

        val funnelStages = listOf(
            FunnelStage("Website Visitors", visitorCount, 100.0, 100.0),
            FunnelStage(
                "Signups & Trials",
                trialCount,
                if (visitorCount > 0) (trialCount.toDouble() / visitorCount.toDouble()) * 100 else 0.0,
                if (visitorCount > 0) (trialCount.toDouble() / visitorCount.toDouble()) * 100 else 0.0
            ),
            FunnelStage(
                "Paid Customers",
                paidCount,
                if (trialCount > 0) (paidCount.toDouble() / trialCount.toDouble()) * 100 else 0.0,
                if (visitorCount > 0) (paidCount.toDouble() / visitorCount.toDouble()) * 100 else 0.0
            )
        )

        return CacAnalysisResult(
            blendedCacMinor = blendedCac,
            totalSpendMinor = totalSpend,
            totalNewCustomers = totalNew,
            paybackPeriodMonths = (paybackMonths * 10).toInt() / 10.0,
            channelBreakdown = channelList.sortedByDescending { it.totalSpendMinor },
            funnelStages = funnelStages
        )
    }
}
