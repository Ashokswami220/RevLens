package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.CacCalculator
import com.revlens.app.model.BusinessProfile
import com.revlens.app.ui.components.display.DataSourceMode
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

class CacAnalyticsViewModel(
    private val profileRepository: ProfileRepository
) : MviViewModel<CacAnalyticsUiState, CacAnalyticsEvent, CacAnalyticsEffect>(
    CacAnalyticsUiState()
) {
    private var cachedProfile: BusinessProfile? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
                cachedProfile = profile
                recalculate()
            }
        }
    }

    override fun onEvent(event: CacAnalyticsEvent) {
        when (event) {
            is CacAnalyticsEvent.UpdateSimulatedBudget -> {
                update { copy(simulatedBudgetMinor = event.budgetMinor) }
                recalculate()
            }

            is CacAnalyticsEvent.ToggleAddSpendSheet -> {
                update { copy(isAddSpendSheetOpen = event.isOpen) }
            }

            is CacAnalyticsEvent.AddManualSpend -> {
                update { copy(isAddSpendSheetOpen = false) }
                emit(CacAnalyticsEffect.ShowMessage("Added spend record for ${event.channel}"))
            }

            CacAnalyticsEvent.Refresh -> recalculate()
            CacAnalyticsEvent.OpenGrowthSimulator -> {
                emit(CacAnalyticsEffect.NavigateToGrowthSimulator)
            }
        }
    }

    private fun recalculate() {
        val profile = cachedProfile ?: return
        val result = CacCalculator.calculateModeled(
            profile = profile,
            simulatedBudgetMinor = state.value.simulatedBudgetMinor
        )

        val symbol = CurrencyUtils.getCurrency(profile.currencyCode).symbol
        update {
            copy(
                result = result,
                dataSourceMode = DataSourceMode.MODELED,
                currencySymbol = symbol
            )
        }
    }
}
