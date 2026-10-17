package com.example.revlens.ui.screens.analytics

import com.example.revlens.domain.calc.CacAnalysisResult
import com.example.revlens.ui.components.display.DataSourceMode

data class CacAnalyticsUiState(
    val result: CacAnalysisResult? = null,
    val simulatedBudgetMinor: Long? = null,
    val dataSourceMode: DataSourceMode = DataSourceMode.MODELED,
    val isAddSpendSheetOpen: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface CacAnalyticsEvent {
    data class UpdateSimulatedBudget(val budgetMinor: Long?) : CacAnalyticsEvent
    data class ToggleAddSpendSheet(val isOpen: Boolean) : CacAnalyticsEvent
    data class AddManualSpend(val channel: String, val amountMinor: Long, val newCustomers: Int) : CacAnalyticsEvent
    data object Refresh : CacAnalyticsEvent
    data object OpenGrowthSimulator : CacAnalyticsEvent
}

sealed interface CacAnalyticsEffect {
    data object NavigateToGrowthSimulator : CacAnalyticsEffect
    data class ShowMessage(val message: String) : CacAnalyticsEffect
}
