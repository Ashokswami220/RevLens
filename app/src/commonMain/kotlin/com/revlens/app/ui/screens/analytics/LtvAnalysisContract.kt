package com.revlens.app.ui.screens.analytics

import com.revlens.app.model.BigDecimal
import com.revlens.app.ui.components.display.DataSourceMode

enum class LtvMethod(val title: String, val subtitle: String) {
    MARGIN_ADJUSTED("Margin-Adjusted LTV", "(ARPU × Gross Margin) ÷ Churn"),
    SIMPLE("Simple Revenue LTV", "ARPU ÷ Churn")
}

data class LtvAnalysisUiState(
    val method: LtvMethod = LtvMethod.MARGIN_ADJUSTED,
    val baseArpu: BigDecimal = BigDecimal.ZERO,
    val baseChurnPercent: Double = 3.0,
    val simulatedChurnPercent: Double = 3.0,
    val grossMarginPercent: Double = 80.0,
    val baseLtv: BigDecimal = BigDecimal.ZERO,
    val simulatedLtv: BigDecimal = BigDecimal.ZERO,
    val cac: BigDecimal = BigDecimal.ZERO,
    val paybackPeriodMonths: Double = 0.0,
    val ltvToCacRatio: Double = 0.0,
    val sensitivityCurve: List<Pair<Double, BigDecimal>> = emptyList(),
    val dataSourceMode: DataSourceMode = DataSourceMode.MODELED,
    val currencySymbol: String = "$",
    val isLoading: Boolean = false
) {
    val deltaPercent: Double
        get() {
            if (baseLtv <= BigDecimal.ZERO) return 0.0
            val diff = simulatedLtv.toDouble() - baseLtv.toDouble()
            return (diff / baseLtv.toDouble()) * 100
        }
}

sealed interface LtvAnalysisEvent {
    data class SelectMethod(val method: LtvMethod) : LtvAnalysisEvent
    data class UpdateSimulatedChurn(val churnPercent: Double) : LtvAnalysisEvent
    data class UpdateGrossMargin(val marginPercent: Double) : LtvAnalysisEvent
    data object ResetSimulation : LtvAnalysisEvent
    data object OpenInWhatIf : LtvAnalysisEvent
}

sealed interface LtvAnalysisEffect {
    data class NavigateToWhatIf(val simulatedChurn: Double) : LtvAnalysisEffect
}
