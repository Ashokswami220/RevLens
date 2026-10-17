package com.example.revlens.ui.screens.reports

import com.example.revlens.domain.model.PeriodPreset
import com.example.revlens.domain.model.ReportFormat
import com.example.revlens.domain.model.ReportSection
import com.example.revlens.domain.model.ReportSnapshot
import com.example.revlens.domain.model.ReportTemplate

enum class ReportFilter(val label: String) {
    ALL("All Reports"),
    PDF("PDFs"),
    CSV("CSVs")
}

data class ReportsUiState(
    val reports: List<ReportSnapshot> = emptyList(),
    val filter: ReportFilter = ReportFilter.ALL,
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val showGenerateSheet: Boolean = false,
    val previewReport: ReportSnapshot? = null,
    // Sheet inputs
    val sheetTemplate: ReportTemplate = ReportTemplate.INVESTOR_UPDATE,
    val sheetTitle: String = "Investor Update · Sep 2026",
    val sheetPreset: PeriodPreset = PeriodPreset.LAST_30_DAYS,
    val sheetStart: String = "2026-09-01",
    val sheetEnd: String = "2026-09-30",
    val sheetSections: Set<ReportSection> = setOf(
        ReportSection.KPIS,
        ReportSection.MRR_BRIDGE,
        ReportSection.SCENARIOS,
        ReportSection.FORECAST
    ),
    val sheetFormat: ReportFormat = ReportFormat.PDF,
    val sheetNotes: String = "",
    val showUpgradePrompt: Boolean = false
)

sealed interface ReportsEvent {
    data class SelectFilter(val filter: ReportFilter) : ReportsEvent
    data class OpenGenerateSheet(val template: ReportTemplate = ReportTemplate.INVESTOR_UPDATE) : ReportsEvent
    data object DismissGenerateSheet : ReportsEvent
    data class UpdateTemplate(val template: ReportTemplate) : ReportsEvent
    data class UpdateTitle(val title: String) : ReportsEvent
    data class UpdatePeriodPreset(val preset: PeriodPreset) : ReportsEvent
    data class UpdatePeriodDates(val start: String, val end: String) : ReportsEvent
    data class ToggleSection(val section: ReportSection) : ReportsEvent
    data class UpdateFormat(val format: ReportFormat) : ReportsEvent
    data class UpdateNotes(val notes: String) : ReportsEvent
    data object GenerateReport : ReportsEvent
    data class ViewReport(val report: ReportSnapshot) : ReportsEvent
    data object DismissViewReport : ReportsEvent
    data class ShareReport(val report: ReportSnapshot) : ReportsEvent
    data class DeleteReport(val reportId: String) : ReportsEvent
    data object ClickUpgradeScheduled : ReportsEvent
    data object DismissUpgradePrompt : ReportsEvent
}

sealed interface ReportsEffect {
    data class ShowToast(val message: String) : ReportsEffect
}
