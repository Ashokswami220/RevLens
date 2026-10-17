package com.example.revlens.data

import com.example.revlens.domain.calc.ReportBuilder
import com.example.revlens.domain.model.ReportFormat
import com.example.revlens.domain.model.ReportSection
import com.example.revlens.domain.model.ReportSnapshot
import com.example.revlens.domain.model.ReportTemplate
import com.example.revlens.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultReportRepository : ReportRepository {

    private val _reportsFlow = MutableStateFlow<List<ReportSnapshot>>(initialSampleReports())
    override val reportsFlow: Flow<List<ReportSnapshot>> = _reportsFlow.asStateFlow()

    override suspend fun saveReport(snapshot: ReportSnapshot) {
        _reportsFlow.update { list ->
            val index = list.indexOfFirst { it.id == snapshot.id }
            if (index >= 0) {
                list.toMutableList().apply { set(index, snapshot) }
            } else {
                listOf(snapshot) + list
            }
        }
    }

    override suspend fun deleteReport(reportId: String) {
        _reportsFlow.update { list -> list.filterNot { it.id == reportId } }
    }

    override suspend fun getReportById(reportId: String): ReportSnapshot? {
        return _reportsFlow.value.firstOrNull { it.id == reportId }
    }

    companion object {
        fun initialSampleReports(): List<ReportSnapshot> {
            val sectionsAll = listOf(
                ReportSection.KPIS,
                ReportSection.MRR_BRIDGE,
                ReportSection.SCENARIOS,
                ReportSection.FORECAST
            )
            val rep1 = ReportBuilder.buildSnapshot(
                id = "rep_investor_sep",
                workspaceId = "default_ws",
                title = "Investor Update · Sep 2026",
                template = ReportTemplate.INVESTOR_UPDATE,
                format = ReportFormat.PDF,
                periodStart = "2026-09-01",
                periodEnd = "2026-09-30",
                sections = sectionsAll,
                profile = null,
                scenarios = emptyList(),
                customNotes = "Steady 11.2% net expansion. Cash runway remains strong at 18 months."
            ).copy(createdAt = 1760200000L)

            val rep2 = ReportBuilder.buildSnapshot(
                id = "rep_monthly_sep",
                workspaceId = "default_ws",
                title = "Monthly Revenue Bridge · Sep 2026",
                template = ReportTemplate.MONTHLY_REVENUE,
                format = ReportFormat.CSV,
                periodStart = "2026-09-01",
                periodEnd = "2026-09-30",
                sections = listOf(ReportSection.KPIS, ReportSection.MRR_BRIDGE),
                profile = null,
                scenarios = emptyList(),
                customNotes = "Exported for accounting reconciliation."
            ).copy(createdAt = 1759800000L)

            val rep3 = ReportBuilder.buildSnapshot(
                id = "rep_pricing_q4",
                workspaceId = "default_ws",
                title = "Pricing Strategy Forecast",
                template = ReportTemplate.SCENARIO_COMPARISON,
                format = ReportFormat.PDF,
                periodStart = "2026-10-01",
                periodEnd = "2027-09-30",
                sections = listOf(ReportSection.KPIS, ReportSection.SCENARIOS, ReportSection.FORECAST, ReportSection.NOTES),
                profile = null,
                scenarios = emptyList(),
                customNotes = "Testing $10 price expansion on Pro tier for Q4 cohort."
            ).copy(createdAt = 1759200000L)

            return listOf(rep1, rep2, rep3)
        }
    }
}
