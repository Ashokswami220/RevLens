package com.example.revlens.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChannelPlan(
    val id: String,
    val name: String,
    val monthlySpendMinor: Long, // e.g. 100000 = $1,000.00
    val targetCacMinor: Long,    // e.g. 10000 = $100.00
    val isEnabled: Boolean = true
) {
    val estimatedCustomers: Int
        get() = if (targetCacMinor > 0 && isEnabled) (monthlySpendMinor / targetCacMinor).toInt() else 0
}

@Serializable
data class GrowthAssumptions(
    val channels: List<ChannelPlan> = listOf(
        ChannelPlan("ch_google", "Google Search Ads", 150000L, 12000L),
        ChannelPlan("ch_meta", "Meta / Instagram", 100000L, 9000L),
        ChannelPlan("ch_linkedin", "LinkedIn Ads", 80000L, 18000L),
        ChannelPlan("ch_referral", "Referral Program", 30000L, 4000L)
    ),
    val referralRatePercent: Double = 2.0,
    val organicGrowthPercent: Double = 1.5,
    val expansionRatePercent: Double = 1.0,
    val startingCashMinor: Long = 10000000L, // $100k starting cash (0 for disabled)
    val timeHorizonMonths: Int = 12 // 12, 24, 36
)

@Serializable
data class MonthlyGrowthSnapshot(
    val monthIndex: Int,
    val paidCustomers: Int,
    val referralCustomers: Int,
    val organicCustomers: Int,
    val churnedCustomers: Int,
    val endingCustomers: Int,
    val endingMrrMinor: Long,
    val totalAdSpendMinor: Long,
    val netCashFlowMinor: Long,
    val remainingCashMinor: Long?,
    val channelBreakdown: Map<String, Int> = emptyMap()
)

@Serializable
data class GrowthResult(
    val monthlySnapshots: List<MonthlyGrowthSnapshot>,
    val finalCustomers: Int,
    val finalMrrMinor: Long,
    val netMrrGainMinor: Long,
    val totalSpendMinor: Long,
    val runwayMonths: Int?, // null if profitable or cash remains positive
    val blendedCacMinor: Long,
    val paybackPeriodMonths: Double? = null
)
