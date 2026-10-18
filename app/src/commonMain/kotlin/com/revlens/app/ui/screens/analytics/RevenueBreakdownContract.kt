package com.revlens.app.ui.screens.analytics

import com.revlens.app.domain.calc.RevenueBreakdownResult
import com.revlens.app.domain.calc.WaterfallStep
import com.revlens.app.ui.components.display.DataSourceMode

enum class RevenueSourceMode {
    MODELED,
    ACTUAL
}

enum class BridgePeriod(val label: String) {
    MONTH("Monthly"),
    QUARTER("Quarterly"),
    YEAR("Annual")
}

data class RevenueBreakdownUiState(
    val sourceMode: RevenueSourceMode = RevenueSourceMode.MODELED,
    val period: BridgePeriod = BridgePeriod.MONTH,
    val result: RevenueBreakdownResult? = null,
    val hasActualData: Boolean = false,
    val selectedStep: WaterfallStep? = null,
    val isLoading: Boolean = false
) {
    val activeDataSource: DataSourceMode
        get() = if (sourceMode == RevenueSourceMode.ACTUAL) DataSourceMode.ACTUAL else DataSourceMode.MODELED
}

sealed interface RevenueBreakdownEvent {
    data class SelectSourceMode(val mode: RevenueSourceMode) : RevenueBreakdownEvent
    data class SelectPeriod(val period: BridgePeriod) : RevenueBreakdownEvent
    data class SelectWaterfallStep(val step: WaterfallStep?) : RevenueBreakdownEvent
    data object Refresh : RevenueBreakdownEvent
}

sealed interface RevenueBreakdownEffect {
    data object NavigateToDataImport : RevenueBreakdownEffect
    data object NavigateToSimulator : RevenueBreakdownEffect
}
