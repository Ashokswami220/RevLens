package com.example.revlens.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.data.ProfileRepository
import com.example.revlens.domain.calc.RevenueBreakdownCalculator
import com.example.revlens.domain.calc.RevenueBreakdownResult
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.model.Dataset
import com.example.revlens.domain.repository.AnalyticsRepository
import com.example.revlens.model.BusinessProfile
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RevenueBreakdownViewModel(
    private val profileRepository: ProfileRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<RevenueBreakdownUiState, RevenueBreakdownEvent, RevenueBreakdownEffect>(
    RevenueBreakdownUiState()
) {
    private var cachedProfile: BusinessProfile? = null
    private var cachedDataset: Dataset? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                profileRepository.profileFlow,
                analyticsRepository.datasetFlow
            ) { profile, dataset ->
                cachedProfile = profile
                cachedDataset = dataset
                val hasActual = dataset != null && dataset.subscriptions.isNotEmpty()
                update { copy(hasActualData = hasActual) }
                recompute()
            }.collect {}
        }
    }

    override fun onEvent(event: RevenueBreakdownEvent) {
        when (event) {
            is RevenueBreakdownEvent.SelectSourceMode -> {
                update { copy(sourceMode = event.mode) }
                recompute()
            }
            is RevenueBreakdownEvent.SelectPeriod -> {
                update { copy(period = event.period) }
                recompute()
            }
            is RevenueBreakdownEvent.SelectWaterfallStep -> {
                update { copy(selectedStep = event.step) }
            }
            RevenueBreakdownEvent.Refresh -> recompute()
        }
    }

    private fun recompute() {
        val s = state.value
        val multiplier = when (s.period) {
            BridgePeriod.MONTH -> 1.0
            BridgePeriod.QUARTER -> 3.0
            BridgePeriod.YEAR -> 12.0
        }

        val result: RevenueBreakdownResult? = if (s.sourceMode == RevenueSourceMode.ACTUAL && cachedDataset != null && cachedDataset!!.subscriptions.isNotEmpty()) {
            RevenueBreakdownCalculator.calculateActual(cachedDataset!!.subscriptions)
        } else if (cachedProfile != null) {
            val baseProfile = cachedProfile!!
            RevenueBreakdownCalculator.calculateModeled(
                profile = baseProfile,
                newGrowthRatePercent = 6.5 * multiplier,
                expansionRatePercent = 2.0 * multiplier,
                contractionRatePercent = 1.0 * multiplier
            )
        } else {
            null
        }

        update { copy(result = result) }
    }
}
