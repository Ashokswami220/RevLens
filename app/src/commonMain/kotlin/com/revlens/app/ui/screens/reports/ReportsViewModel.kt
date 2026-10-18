package com.revlens.app.ui.screens.reports

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.platform.FileSharer
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.calc.ReportBuilder
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.PeriodPreset
import com.revlens.app.domain.model.ReportFormat
import com.revlens.app.domain.model.ReportSnapshot
import com.revlens.app.domain.model.ReportTemplate
import com.revlens.app.domain.repository.ReportRepository
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random

class ReportsViewModel(
    private val reportRepository: ReportRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val fileSharer: FileSharer
) : MviViewModel<ReportsUiState, ReportsEvent, ReportsEffect>(ReportsUiState()) {

    private var allReports: List<ReportSnapshot> = emptyList()
    private var currentProfile: BusinessProfile? = null
    private var currentScenarios: List<Scenario> = emptyList()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            reportRepository.reportsFlow.collect { list ->
                allReports = list
                applyFilter()
            }
        }
        viewModelScope.launch {
            profileRepository.profileFlow.collect { prof ->
                currentProfile = prof
            }
        }
        viewModelScope.launch {
            scenarioRepository.scenariosFlow.collect { scens ->
                currentScenarios = scens
            }
        }
    }

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
            is ReportsEvent.UpdatePeriodDates -> update { copy(sheetStart = event.start, sheetEnd = event.end) }
            is ReportsEvent.ToggleSection -> {
                val current = state.value.sheetSections
                val updated = if (current.contains(event.section)) current - event.section else current + event.section
                update { copy(sheetSections = updated) }
            }
            is ReportsEvent.UpdateFormat -> update { copy(sheetFormat = event.format) }
            is ReportsEvent.UpdateNotes -> update { copy(sheetNotes = event.notes) }
            ReportsEvent.GenerateReport -> generateReport()
            is ReportsEvent.ViewReport -> update { copy(previewReport = event.report) }
            ReportsEvent.DismissViewReport -> update { copy(previewReport = null) }
            is ReportsEvent.ShareReport -> shareReport(event.report)
            is ReportsEvent.DeleteReport -> {
                viewModelScope.launch {
                    reportRepository.deleteReport(event.reportId)
                    emit(ReportsEffect.ShowToast("Report deleted"))
                }
            }
            ReportsEvent.ClickUpgradeScheduled -> update { copy(showUpgradePrompt = true) }
            ReportsEvent.DismissUpgradePrompt -> update { copy(showUpgradePrompt = false) }
        }
    }

    private fun generateReport() {
        val s = state.value
        update { copy(isGenerating = true) }

        viewModelScope.launch {
            val id = "rep_" + Random.nextInt(10000, 99999)
            val wsId = workspaceContext.activeWorkspaceId.value ?: "default_ws"
            val snapshot = ReportBuilder.buildSnapshot(
                id = id,
                workspaceId = wsId,
                title = s.sheetTitle,
                template = s.sheetTemplate,
                format = s.sheetFormat,
                periodStart = s.sheetStart,
                periodEnd = s.sheetEnd,
                sections = s.sheetSections.toList(),
                profile = currentProfile,
                scenarios = currentScenarios,
                customNotes = s.sheetNotes,
                timestampEpoch = 1760000000L
            )
            reportRepository.saveReport(snapshot)
            update { copy(isGenerating = false, showGenerateSheet = false) }
            emit(ReportsEffect.ShowToast("Report generated: ${snapshot.title}"))
        }
    }

    private fun shareReport(report: ReportSnapshot) {
        viewModelScope.launch {
            val ext = if (report.format == ReportFormat.PDF) "pdf" else "csv"
            val fileName = "${report.title.replace(" ", "_")}.$ext"
            val bytes = report.csvData.encodeToByteArray()
            fileSharer.shareFile(fileName, report.format.mimeType, bytes)
            emit(ReportsEffect.ShowToast("Sharing ${report.title}"))
        }
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
