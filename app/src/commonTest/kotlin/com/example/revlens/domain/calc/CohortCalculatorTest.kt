package com.example.revlens.domain.calc

import com.example.revlens.domain.model.Dataset
import com.example.revlens.domain.model.SubscriptionRecord
import com.example.revlens.model.BigDecimal
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import com.example.revlens.ui.components.display.DataSourceMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CohortCalculatorTest {

    @Test
    fun testModeledRetentionCurve() {
        val profile = BusinessProfile(
            monthlyChurnRate = 5.0,
            monthlyGrowthRate = 10.0,
            cac = BigDecimal("100.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("5.00"),
            plans = listOf(Plan(id = "1", name = "Default", price = BigDecimal("50.00"), customerCount = 100))
        )

        val result = CohortCalculator.build(
            profile = profile,
            dataset = null,
            maxOffsets = 6
        )

        assertEquals(DataSourceMode.MODELED, result.sourceMode)
        assertTrue(result.rows.isEmpty())
        assertEquals(6, result.averageCurve.size)
        // M0 is 100%
        assertEquals(100.0, result.averageCurve[0])
        // M1 is 95% (1 - 0.05)
        assertEquals(95.0, result.averageCurve[1])
        // Monotonically decreasing
        assertTrue(result.averageCurve[2] < result.averageCurve[1])
        assertTrue(result.bestCohortInsight.contains("Modeled Month-3 retention"))
    }

    @Test
    fun testActualLogoRetentionMatrix() {
        val subscriptions = listOf(
            // Jan 2026 cohort (3 customers)
            SubscriptionRecord("c1", "Pro", "2026-01-01", null, 10000L),
            SubscriptionRecord("c2", "Pro", "2026-01-10", "2026-02-15", 10000L), // churned at M1
            SubscriptionRecord("c3", "Starter", "2026-01-15", null, 5000L),
            // Feb 2026 cohort (2 customers)
            SubscriptionRecord("c4", "Pro", "2026-02-01", null, 10000L),
            SubscriptionRecord("c5", "Starter", "2026-02-10", null, 5000L)
        )

        val dataset = Dataset(
            id = "ds_cohort",
            workspaceId = "ws_cohort",
            filename = "subs.csv",
            subscriptions = subscriptions
        )

        val result = CohortCalculator.build(
            profile = null,
            dataset = dataset,
            metric = CohortMetric.LOGO_RETENTION,
            maxOffsets = 4
        )

        assertEquals(DataSourceMode.ACTUAL, result.sourceMode)
        assertEquals(2, result.rows.size)

        val janCohort = result.rows[0]
        assertEquals("Jan '26", janCohort.cohortLabel)
        assertEquals(3, janCohort.initialSize)
        assertEquals(100.0, janCohort.cells[0].retentionPercent)

        val febCohort = result.rows[1]
        assertEquals("Feb '26", febCohort.cohortLabel)
        assertEquals(2, febCohort.initialSize)

        // For Feb cohort, M2 and M3 are younger (beyond available months)
        assertTrue(febCohort.cells[1].isYounger || febCohort.cells[2].isYounger)
    }
}
