package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.CohortCalculator
import com.revlens.app.model.BusinessProfile
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

class CohortAnalysisViewModel(
    private val profileRepository: ProfileRepository
) : MviViewModel<CohortAnalysisUiState, CohortAnalysisEvent, CohortAnalysisEffect>(
    CohortAnalysisUiState()
) {
    private var cachedProfile: BusinessProfile? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
                cachedProfile = profile
                val symbol = CurrencyUtils.getCurrency(profile?.currencyCode).symbol
                update { copy(hasActualData = false, currencySymbol = symbol) }
                recompute()
            }
        }
    }

    override fun onEvent(event: CohortAnalysisEvent) {
        when (event) {
            is CohortAnalysisEvent.SelectMetric -> {
                update { copy(metric = event.metric) }
                recompute()
            }

            is CohortAnalysisEvent.SelectGranularity -> {
                update { copy(granularity = event.granularity) }
                recompute()
            }

            is CohortAnalysisEvent.ToggleCohortSelection -> {
                val current = state.value.selectedCohortNames
                val updated = if (current.contains(event.cohortName)) {
                    current - event.cohortName
                } else {
                    if (current.size >= 3) current else current + event.cohortName
                }
                update { copy(selectedCohortNames = updated) }
            }

            is CohortAnalysisEvent.SelectCell -> {
                update { copy(selectedCellDetail = event.row to event.cell) }
            }

            CohortAnalysisEvent.DismissCellDetail -> {
                update { copy(selectedCellDetail = null) }
            }

            CohortAnalysisEvent.ImportDataClicked -> {
                emit(CohortAnalysisEffect.NavigateToDataImport)
            }

            CohortAnalysisEvent.Refresh -> recompute()
        }
    }

    private fun recompute() {
        val currentState = state.value
        val result = CohortCalculator.build(
            profile = cachedProfile,
            dataset = null,
            metric = currentState.metric,
            granularity = currentState.granularity,
            maxOffsets = 12
        )
        val defaultSelected =
            if (currentState.selectedCohortNames.isEmpty() && result.rows.isNotEmpty()) {
                result.rows.take(3)
                    .map { it.cohortLabel }
                    .toSet()
            } else {
                currentState.selectedCohortNames
            }
        update {
            copy(
                result = result,
                selectedCohortNames = defaultSelected,
                isLoading = false
            )
        }
    }
}
