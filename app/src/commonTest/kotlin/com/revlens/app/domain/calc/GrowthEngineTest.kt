package com.revlens.app.domain.calc

import com.revlens.app.domain.model.ChannelPlan
import com.revlens.app.domain.model.GrowthAssumptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GrowthEngineTest {

    @Test
    fun testStandard12MonthSimulation() {
        val assumptions = GrowthAssumptions(
            channels = listOf(
                ChannelPlan("c1", "Google Ads", monthlySpendMinor = 100000L, targetCacMinor = 10000L), // 10 customers/mo, $1000 spend
                ChannelPlan("c2", "Meta Ads", monthlySpendMinor = 50000L, targetCacMinor = 5000L)       // 10 customers/mo, $500 spend
            ),
            referralRatePercent = 2.0,
            organicGrowthPercent = 1.0,
            expansionRatePercent = 0.5,
            startingCashMinor = 50000000L, // $500k
            timeHorizonMonths = 12
        )

        val result = GrowthEngine.simulate(
            startingCustomers = 200,
            startingMrrMinor = 2000000L, // $20,000 MRR (ARPU = $100.00 = 10000 minor)
            monthlyChurnPercent = 2.0,
            assumptions = assumptions
        )

        assertEquals(12, result.monthlySnapshots.size)
        // With 20 paid customers/mo + referral + organic > churn (4), ending customers should increase
        assertTrue(result.finalCustomers > 200)
        assertTrue(result.finalMrrMinor > 2000000L)
        assertTrue(result.netMrrGainMinor > 0L)
        assertEquals(150000L * 12, result.totalSpendMinor)
        assertTrue(result.blendedCacMinor > 0L)
        assertNotNull(result.paybackPeriodMonths)
        // With ample cash, runwayMonths should be null (runway not exhausted)
        assertNull(result.runwayMonths)
    }

    @Test
    fun testZeroSpendSimulation() {
        val assumptions = GrowthAssumptions(
            channels = listOf(
                ChannelPlan("c1", "Google Ads", monthlySpendMinor = 0L, targetCacMinor = 10000L)
            ),
            referralRatePercent = 0.0,
            organicGrowthPercent = 5.0,
            expansionRatePercent = 0.0,
            startingCashMinor = 0L,
            timeHorizonMonths = 6
        )

        val result = GrowthEngine.simulate(
            startingCustomers = 100,
            startingMrrMinor = 1000000L,
            monthlyChurnPercent = 2.0,
            assumptions = assumptions
        )

        assertEquals(6, result.monthlySnapshots.size)
        assertEquals(0L, result.totalSpendMinor)
        assertEquals(0L, result.blendedCacMinor)
        assertNull(result.paybackPeriodMonths)
        // 5% organic - 2% churn = net positive growth
        assertTrue(result.finalCustomers > 100)
    }

    @Test
    fun testCashRunwayExhaustion() {
        val assumptions = GrowthAssumptions(
            channels = listOf(
                ChannelPlan("c1", "Heavy Ads", monthlySpendMinor = 10000000L, targetCacMinor = 50000L) // $100k spend/mo
            ),
            referralRatePercent = 0.0,
            organicGrowthPercent = 0.0,
            expansionRatePercent = 0.0,
            startingCashMinor = 15000000L, // $150k starting cash
            timeHorizonMonths = 12
        )

        // Starting with $1,000 MRR and spending $100k/mo -> cash will run out in month 2
        val result = GrowthEngine.simulate(
            startingCustomers = 10,
            startingMrrMinor = 100000L,
            monthlyChurnPercent = 5.0,
            assumptions = assumptions
        )

        assertNotNull(result.runwayMonths)
        assertTrue(result.runwayMonths!! <= 3)
    }

    @Test
    fun testZeroOrNegativeInputsHandledGracefully() {
        val assumptions = GrowthAssumptions(
            channels = emptyList(),
            referralRatePercent = -5.0,
            organicGrowthPercent = -2.0,
            expansionRatePercent = 0.0,
            startingCashMinor = -1000L,
            timeHorizonMonths = -5
        )

        val result = GrowthEngine.simulate(
            startingCustomers = 0,
            startingMrrMinor = 0L,
            monthlyChurnPercent = 150.0,
            assumptions = assumptions
        )

        // Min horizon is 1
        assertEquals(1, result.monthlySnapshots.size)
        assertTrue(result.finalCustomers >= 0)
        assertTrue(result.finalMrrMinor >= 0L)
    }
}
