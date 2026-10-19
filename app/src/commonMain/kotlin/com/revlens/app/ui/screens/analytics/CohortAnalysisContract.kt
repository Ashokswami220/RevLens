package com.revlens.app.ui.screens.analytics

import com.revlens.app.domain.calc.CohortCell
import com.revlens.app.domain.calc.CohortGranularity
import com.revlens.app.domain.calc.CohortMatrixResult
import com.revlens.app.domain.calc.CohortMetric
import com.revlens.app.domain.calc.CohortRow

data class CohortAnalysisUiState(
    val metric: CohortMetric = CohortMetric.LOGO_RETENTION,
    val granularity: CohortGranularity = CohortGranularity.MONTHLY,
    val result: CohortMatrixResult? = null,
    val selectedCohortNames: Set<String> = emptySet(),
    val selectedCellDetail: Pair<CohortRow, CohortCell>? = null,
    val currencySymbol: String = "$",
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
