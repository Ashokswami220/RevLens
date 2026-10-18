package com.revlens.app.domain.calc

import com.revlens.app.domain.model.ReportFormat
import com.revlens.app.domain.model.ReportSection
import com.revlens.app.domain.model.ReportTemplate
import com.revlens.app.model.Assumptions
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReportBuilderTest {

    @Test
    fun testBuildSnapshotBasic() {
        val testProfile = BusinessProfile(
            monthlyChurnRate = 5.0,
            monthlyGrowthRate = 10.0,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("5000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(id = "1", name = "Starter", price = BigDecimal("29.00"), customerCount = 100),
                Plan(id = "2", name = "Pro", price = BigDecimal("99.00"), customerCount = 50)
            ),
            currencyCode = "USD"
        )

        val snapshot = ReportBuilder.buildSnapshot(
            id = "test_rep_1",
            workspaceId = "ws_test",
            title = "Executive Q3 Review",
            template = ReportTemplate.INVESTOR_UPDATE,
            format = ReportFormat.PDF,
            periodStart = "2026-07-01",
            periodEnd = "2026-09-30",
            sections = listOf(ReportSection.KPIS, ReportSection.MRR_BRIDGE, ReportSection.FORECAST, ReportSection.NOTES),
            profile = testProfile,
            scenarios = emptyList(),
            customNotes = "High capital efficiency maintained."
        )

        assertEquals("test_rep_1", snapshot.id)
        assertEquals("Executive Q3 Review", snapshot.title)
        assertEquals(ReportFormat.PDF, snapshot.format)
        assertTrue(snapshot.summaryMetrics.isNotEmpty())
        assertTrue(snapshot.summaryMetrics.containsKey("Monthly Recurring Revenue (MRR)"))
        assertTrue(snapshot.csvData.contains("CloudMetrics Inc") || snapshot.csvData.contains("Executive Q3 Review"))
        assertTrue(snapshot.csvData.contains("--- EXECUTIVE METRICS ---"))
        assertTrue(snapshot.csvData.contains("--- REVENUE BREAKDOWN (MRR BRIDGE) ---"))
        assertTrue(snapshot.csvData.contains("--- 12-MONTH PROJECTIONS ---"))
        assertTrue(snapshot.csvData.contains("High capital efficiency maintained."))
    }

    @Test
    fun testCsvFormattingWithoutOptionalSections() {
        val snapshot = ReportBuilder.buildSnapshot(
            id = "test_rep_csv",
            workspaceId = "ws_test",
            title = "Monthly Revenue CSV",
            template = ReportTemplate.MONTHLY_REVENUE,
            format = ReportFormat.CSV,
            periodStart = "2026-09-01",
            periodEnd = "2026-09-30",
            sections = listOf(ReportSection.KPIS),
            profile = null,
            scenarios = emptyList(),
            customNotes = ""
        )

        assertEquals(ReportFormat.CSV, snapshot.format)
        assertTrue(snapshot.csvData.contains("Monthly Revenue CSV"))
        assertTrue(snapshot.csvData.contains("--- EXECUTIVE METRICS ---"))
        assertTrue(!snapshot.csvData.contains("--- REVENUE BREAKDOWN (MRR BRIDGE) ---"))
        assertTrue(!snapshot.csvData.contains("--- 12-MONTH PROJECTIONS ---"))
    }
}
