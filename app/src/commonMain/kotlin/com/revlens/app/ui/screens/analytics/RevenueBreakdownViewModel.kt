package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.RevenueBreakdownCalculator
import com.revlens.app.domain.calc.RevenueBreakdownResult
import com.revlens.app.model.BusinessProfile
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

class RevenueBreakdownViewModel(
    private val profileRepository: ProfileRepository
) : MviViewModel<RevenueBreakdownUiState, RevenueBreakdownEvent, RevenueBreakdownEffect>(
    RevenueBreakdownUiState()
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

        val result: RevenueBreakdownResult? =
            if (cachedProfile != null) {
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
