package com.example.revlens.domain.calc

import com.example.revlens.domain.model.GrowthAssumptions
import com.example.revlens.domain.model.GrowthResult
import com.example.revlens.domain.model.MonthlyGrowthSnapshot
import kotlin.math.max
import kotlin.math.roundToInt

object GrowthEngine {

    /**
     * Simulates growth month-by-month over the configured time horizon.
     *
     * @param startingCustomers Baseline customer count (e.g. 100)
     * @param startingMrrMinor Baseline MRR in minor units (e.g. 1000000L = $10,000.00)
     * @param monthlyChurnPercent Monthly churn rate in percent (e.g. 3.0 for 3%)
     * @param assumptions Growth configuration including channels, referral, organic, cash, and horizon
     */
    fun simulate(
        startingCustomers: Int,
        startingMrrMinor: Long,
        monthlyChurnPercent: Double,
        assumptions: GrowthAssumptions
    ): GrowthResult {
        val safeStartingCustomers = max(1, startingCustomers)
        val safeStartingMrr = max(0L, startingMrrMinor)
        val safeChurnPercent = monthlyChurnPercent.coerceIn(0.0, 100.0)
        val horizon = assumptions.timeHorizonMonths.coerceIn(1, 60)

        // ARPU in minor units (default to $50.00 if zero)
        val arpuMinor = if (safeStartingCustomers > 0 && safeStartingMrr > 0) {
            safeStartingMrr / safeStartingCustomers
        } else {
            5000L // $50.00 fallback
        }.coerceAtLeast(100L)

        val snapshots = ArrayList<MonthlyGrowthSnapshot>(horizon)
        var currentCustomers = safeStartingCustomers
        var currentMrr = safeStartingMrr

        var cashMinor: Long? = if (assumptions.startingCashMinor > 0) assumptions.startingCashMinor else null
        var runwayMonths: Int? = null

        var totalSpendAccumulator = 0L
        var totalAcquiredCustomers = 0

        for (m in 1..horizon) {
            // 1. Channel paid customers
            var monthlyAdSpend = 0L
            val channelBreakdown = mutableMapOf<String, Int>()
            var paidCustomers = 0

            for (channel in assumptions.channels) {
                if (channel.isEnabled && channel.monthlySpendMinor > 0 && channel.targetCacMinor > 0) {
                    val acquired = (channel.monthlySpendMinor / channel.targetCacMinor).toInt()
                    channelBreakdown[channel.id] = acquired
                    paidCustomers += acquired
                    monthlyAdSpend += channel.monthlySpendMinor
                } else {
                    channelBreakdown[channel.id] = 0
                }
            }

            // 2. Referral & Organic
            val referralCustomers = max(0, (currentCustomers * (assumptions.referralRatePercent.coerceIn(0.0, 100.0) / 100.0)).roundToInt())
            val organicCustomers = max(0, (currentCustomers * (assumptions.organicGrowthPercent.coerceIn(0.0, 100.0) / 100.0)).roundToInt())

            val totalNewCustomers = paidCustomers + referralCustomers + organicCustomers
            totalAcquiredCustomers += totalNewCustomers
            totalSpendAccumulator += monthlyAdSpend

            // 3. Churn
            val churnedCustomers = (currentCustomers * (safeChurnPercent / 100.0)).roundToInt().coerceAtMost(currentCustomers)

            // 4. Ending customers
            val endingCustomers = max(0, currentCustomers + totalNewCustomers - churnedCustomers)

            // 5. MRR Compounding
            val churnedMrr = (currentMrr * (safeChurnPercent / 100.0)).toLong().coerceAtMost(currentMrr)
            val expansionMrr = (currentMrr * (assumptions.expansionRatePercent.coerceIn(0.0, 100.0) / 100.0)).toLong()
            val newMrr = (totalNewCustomers.toLong() * arpuMinor)
            val endingMrrMinor = max(0L, currentMrr - churnedMrr + expansionMrr + newMrr)

            // 6. Cash flow and Runway
            val netCashFlowMinor = endingMrrMinor - monthlyAdSpend
            if (cashMinor != null) {
                val newCash = cashMinor + netCashFlowMinor
                cashMinor = newCash
                if (newCash <= 0 && runwayMonths == null) {
                    runwayMonths = m
                }
            }

            snapshots.add(
                MonthlyGrowthSnapshot(
                    monthIndex = m,
                    paidCustomers = paidCustomers,
                    referralCustomers = referralCustomers,
                    organicCustomers = organicCustomers,
                    churnedCustomers = churnedCustomers,
                    endingCustomers = endingCustomers,
                    endingMrrMinor = endingMrrMinor,
                    totalAdSpendMinor = monthlyAdSpend,
                    netCashFlowMinor = netCashFlowMinor,
                    remainingCashMinor = cashMinor,
                    channelBreakdown = channelBreakdown
                )
            )

            currentCustomers = endingCustomers
            currentMrr = endingMrrMinor
        }

        val finalCustomers = currentCustomers
        val finalMrr = currentMrr
        val netGain = finalMrr - safeStartingMrr

        val blendedCacMinor = if (totalAcquiredCustomers > 0) {
            totalSpendAccumulator / totalAcquiredCustomers
        } else {
            0L
        }

        val paybackPeriodMonths = if (blendedCacMinor > 0 && arpuMinor > 0) {
            blendedCacMinor.toDouble() / arpuMinor.toDouble()
        } else {
            null
        }

        return GrowthResult(
            monthlySnapshots = snapshots,
            finalCustomers = finalCustomers,
            finalMrrMinor = finalMrr,
            netMrrGainMinor = netGain,
            totalSpendMinor = totalSpendAccumulator,
            runwayMonths = runwayMonths,
            blendedCacMinor = blendedCacMinor,
            paybackPeriodMonths = paybackPeriodMonths
        )
    }
}
