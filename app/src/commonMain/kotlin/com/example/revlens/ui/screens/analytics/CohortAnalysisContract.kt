package com.example.revlens.ui.screens.analytics

import com.example.revlens.domain.calc.CohortCell
import com.example.revlens.domain.calc.CohortGranularity
import com.example.revlens.domain.calc.CohortMatrixResult
import com.example.revlens.domain.calc.CohortMetric
import com.example.revlens.domain.calc.CohortRow

data class CohortAnalysisUiState(
    val metric: CohortMetric = CohortMetric.LOGO_RETENTION,
    val granularity: CohortGranularity = CohortGranularity.MONTHLY,
    val result: CohortMatrixResult? = null,
    val selectedCohortNames: Set<String> = emptySet(),
    val selectedCellDetail: Pair<CohortRow, CohortCell>? = null,
    val isLoading: Boolean = false,
    val hasActualData: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CohortAnalysisEvent {
    data class SelectMetric(val metric: CohortMetric) : CohortAnalysisEvent
    data class SelectGranularity(val granularity: CohortGranularity) : CohortAnalysisEvent
    data class ToggleCohortSelection(val cohortName: String) : CohortAnalysisEvent
    data class SelectCell(val row: CohortRow, val cell: CohortCell) : CohortAnalysisEvent
    data object DismissCellDetail : CohortAnalysisEvent
    data object ImportDataClicked : CohortAnalysisEvent
    data object Refresh : CohortAnalysisEvent
}

sealed interface CohortAnalysisEffect {
    data object NavigateToDataImport : CohortAnalysisEffect
}
