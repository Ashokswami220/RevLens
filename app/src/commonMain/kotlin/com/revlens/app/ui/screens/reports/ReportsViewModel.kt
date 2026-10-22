package com.revlens.app.ui.screens.reports

import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.model.PeriodPreset
import com.revlens.app.domain.model.ReportFormat
import com.revlens.app.domain.model.ReportSnapshot
import kotlin.random.Random

class ReportsViewModel :
    MviViewModel<ReportsUiState, ReportsEvent, ReportsEffect>(ReportsUiState()) {

    private var allReports: List<ReportSnapshot> = emptyList()

    override fun onEvent(event: ReportsEvent) {
        when (event) {
            is ReportsEvent.SelectFilter -> {
                update { copy(filter = event.filter) }
                applyFilter()
            }

            is ReportsEvent.OpenGenerateSheet -> {
                val templ = event.template
                val defaultTitle = "${templ.displayName} · Oct 2026"
                update {
                    copy(
                        showGenerateSheet = true,
                        sheetTemplate = templ,
                        sheetTitle = defaultTitle
                    )
                }
            }

            ReportsEvent.DismissGenerateSheet -> update { copy(showGenerateSheet = false) }

            is ReportsEvent.UpdateTemplate -> {
                val defaultTitle = "${event.template.displayName} · Oct 2026"
                update { copy(sheetTemplate = event.template, sheetTitle = defaultTitle) }
            }

            is ReportsEvent.UpdateTitle -> update { copy(sheetTitle = event.title) }

            is ReportsEvent.UpdatePeriodPreset -> {
                val (start, end) = when (event.preset) {
                    PeriodPreset.LAST_30_DAYS -> "2026-09-17" to "2026-10-17"
                    PeriodPreset.THIS_QUARTER -> "2026-10-01" to "2026-12-31"
                    PeriodPreset.YEAR_TO_DATE -> "2026-01-01" to "2026-10-17"
                    PeriodPreset.CUSTOM -> state.value.sheetStart to state.value.sheetEnd
                }
                update { copy(sheetPreset = event.preset, sheetStart = start, sheetEnd = end) }
            }

            is ReportsEvent.UpdatePeriodDates -> update {
                copy(sheetStart = event.start, sheetEnd = event.end)
            }

            is ReportsEvent.ToggleSection -> {
                val current = state.value.sheetSections
                val updated = if (current.contains(event.section)) {
                    current - event.section
                } else {
                    current + event.section
                }
                update { copy(sheetSections = updated) }
            }

            is ReportsEvent.UpdateFormat -> update { copy(sheetFormat = event.format) }

            is ReportsEvent.UpdateNotes -> update { copy(sheetNotes = event.notes) }

            ReportsEvent.GenerateReport -> generateReport()

            is ReportsEvent.ViewReport -> update { copy(previewReport = event.report) }

            ReportsEvent.DismissViewReport -> update { copy(previewReport = null) }

            is ReportsEvent.ShareReport -> {
                emit(ReportsEffect.ShowToast("Sharing ${event.report.title}"))
            }

            is ReportsEvent.DeleteReport -> {
                allReports = allReports.filterNot { it.id == event.reportId }
                applyFilter()
                emit(ReportsEffect.ShowToast("Report deleted"))
            }

            ReportsEvent.ClickUpgradeScheduled -> update { copy(showUpgradePrompt = true) }

            ReportsEvent.DismissUpgradePrompt -> update { copy(showUpgradePrompt = false) }
        }
    }

    private fun generateReport() {
        val s = state.value
        update { copy(isGenerating = true) }

        val id = "rep_" + Random.nextInt(10000, 99999)
        val snapshot = ReportSnapshot(
            id = id,
            workspaceId = "local_ws",
            title = s.sheetTitle,
            template = s.sheetTemplate,
            format = s.sheetFormat,
            periodStart = s.sheetStart,
            periodEnd = s.sheetEnd,
            includedSections = s.sheetSections.toList(),
            createdAt = 1760000000L,
            csvData = "Metric,Value\nMRR,10000\nARR,120000\nChurn Rate,2.5%\nNet Revenue Retention,110%\n",
            notes = "Executive report generated for period ${s.sheetStart} to ${s.sheetEnd}.",
            summaryMetrics = mapOf(
                "MRR" to "$10,000",
                "ARR" to "$120,000",
                "Churn Rate" to "2.5%"
            )
        )
        allReports = listOf(snapshot) + allReports
        update { copy(isGenerating = false, showGenerateSheet = false) }
        applyFilter()
        emit(ReportsEffect.ShowToast("Report generated: ${snapshot.title}"))
    }

    private fun applyFilter() {
        val f = state.value.filter
        val filtered = when (f) {
            ReportFilter.ALL -> allReports
            ReportFilter.PDF -> allReports.filter { it.format == ReportFormat.PDF }
            ReportFilter.CSV -> allReports.filter { it.format == ReportFormat.CSV }
        }
        update { copy(reports = filtered, isLoading = false) }
    }
}
