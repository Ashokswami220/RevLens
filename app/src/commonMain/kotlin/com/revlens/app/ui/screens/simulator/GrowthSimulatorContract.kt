package com.revlens.app.ui.screens.simulator

import com.revlens.app.domain.calc.GrowthEngine
import com.revlens.app.domain.model.GrowthAssumptions
import com.revlens.app.domain.model.GrowthResult

data class GrowthSimulatorUiState(
    val assumptions: GrowthAssumptions = GrowthAssumptions(),
    val result: GrowthResult? = GrowthEngine.simulate(
        startingCustomers = 100,
        startingMrrMinor = 1000000L,
        monthlyChurnPercent = 3.0,
        assumptions = GrowthAssumptions()
    ),
    val isCalculating: Boolean = false,
    val baselineMrrMinor: Long = 1000000L, // $10,000.00
    val baselineCustomers: Int = 100,
    val baselineChurnRate: Double = 3.0,
    val showAddChannelSheet: Boolean = false,
    val showSaveScenarioDialog: Boolean = false,
    val showCashSheet: Boolean = false,
    val scenarioNameInput: String = "",
    val currencySymbol: String = "$",
    val errorMessage: String? = null
)

sealed interface GrowthSimulatorIntent {
    data class ChangeHorizon(val months: Int) : GrowthSimulatorIntent
    data class UpdateChannelSpend(val channelId: String, val spendMinor: Long) :
        GrowthSimulatorIntent

    data class UpdateChannelCac(val channelId: String, val cacMinor: Long) : GrowthSimulatorIntent
    data class ToggleChannelEnabled(val channelId: String) : GrowthSimulatorIntent
    data class AddChannel(val name: String, val spendMinor: Long, val cacMinor: Long) :
        GrowthSimulatorIntent

    data class RemoveChannel(val channelId: String) : GrowthSimulatorIntent
    data class UpdateReferralRate(val rate: Double) : GrowthSimulatorIntent
    data class UpdateOrganicGrowth(val rate: Double) : GrowthSimulatorIntent
    data class UpdateExpansionRate(val rate: Double) : GrowthSimulatorIntent
    data class UpdateStartingCash(val cashMinor: Long) : GrowthSimulatorIntent
    data object ResetToDefaults : GrowthSimulatorIntent

    data object OpenAddChannelSheet : GrowthSimulatorIntent
    data object DismissAddChannelSheet : GrowthSimulatorIntent
    data object OpenSaveScenarioDialog : GrowthSimulatorIntent
    data object DismissSaveScenarioDialog : GrowthSimulatorIntent
    data class UpdateScenarioName(val name: String) : GrowthSimulatorIntent
    data object SaveScenario : GrowthSimulatorIntent
    data object OpenCashSheet : GrowthSimulatorIntent
    data object DismissCashSheet : GrowthSimulatorIntent
    data class LoadScenarioById(val scenarioId: String) : GrowthSimulatorIntent
}

sealed interface GrowthSimulatorEffect {
    data class ShowToast(val message: String) : GrowthSimulatorEffect
    data class ScenarioSaved(val scenarioId: String) : GrowthSimulatorEffect
}
