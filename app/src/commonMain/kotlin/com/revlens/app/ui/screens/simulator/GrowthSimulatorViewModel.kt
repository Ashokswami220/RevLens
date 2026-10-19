package com.revlens.app.ui.screens.simulator

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.calc.GrowthEngine
import com.revlens.app.domain.model.ChannelPlan
import com.revlens.app.domain.model.GrowthAssumptions
import com.revlens.app.model.Assumptions
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import com.revlens.app.model.Scenario
import kotlinx.coroutines.launch
import kotlin.random.Random

class GrowthSimulatorViewModel(
    private val profileRepository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository
) : MviViewModel<GrowthSimulatorUiState, GrowthSimulatorIntent, GrowthSimulatorEffect>(
    GrowthSimulatorUiState()
) {
    private var cachedProfile: BusinessProfile? = null

    init {
        recompute()
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
                cachedProfile = profile
                if (profile != null) {
                    val mrrMinor = (profile.currentMrr.toDouble() * 100).toLong()
                    val customers = profile.totalCustomers.coerceAtLeast(1)
                    val churn = profile.monthlyChurnRate
                    update {
                        copy(
                            baselineMrrMinor = mrrMinor,
                            baselineCustomers = customers,
                            baselineChurnRate = churn,
                            currencySymbol = com.revlens.app.utils.CurrencyUtils.getCurrency(
                                profile.currencyCode
                            ).symbol
                        )
                    }
                }
                recompute()
            }
        }
    }

    override fun onEvent(event: GrowthSimulatorIntent) {
        when (event) {
            is GrowthSimulatorIntent.ChangeHorizon -> {
                update { copy(assumptions = assumptions.copy(timeHorizonMonths = event.months)) }
                recompute()
            }

            is GrowthSimulatorIntent.UpdateChannelSpend -> {
                val updatedChannels = state.value.assumptions.channels.map { ch ->
                    if (ch.id == event.channelId) ch.copy(
                        monthlySpendMinor = event.spendMinor
                    ) else ch
                }
                update { copy(assumptions = assumptions.copy(channels = updatedChannels)) }
                recompute()
            }

            is GrowthSimulatorIntent.UpdateChannelCac -> {
                val updatedChannels = state.value.assumptions.channels.map { ch ->
                    if (ch.id == event.channelId) ch.copy(targetCacMinor = event.cacMinor) else ch
                }
                update { copy(assumptions = assumptions.copy(channels = updatedChannels)) }
                recompute()
            }

            is GrowthSimulatorIntent.ToggleChannelEnabled -> {
                val updatedChannels = state.value.assumptions.channels.map { ch ->
                    if (ch.id == event.channelId) ch.copy(isEnabled = !ch.isEnabled) else ch
                }
                update { copy(assumptions = assumptions.copy(channels = updatedChannels)) }
                recompute()
            }

            is GrowthSimulatorIntent.AddChannel -> {
                val id = "ch_" + Random.nextInt(10000, 99999)
                val newChannel = ChannelPlan(
                    id = id,
                    name = event.name.ifBlank { "Custom Channel" },
                    monthlySpendMinor = event.spendMinor,
                    targetCacMinor = event.cacMinor,
                    isEnabled = true
                )
                val updatedChannels = state.value.assumptions.channels + newChannel
                update {
                    copy(
                        assumptions = assumptions.copy(channels = updatedChannels),
                        showAddChannelSheet = false
                    )
                }
                recompute()
            }

            is GrowthSimulatorIntent.RemoveChannel -> {
                if (state.value.assumptions.channels.size > 1) {
                    val updatedChannels =
                        state.value.assumptions.channels.filterNot { it.id == event.channelId }
                    update { copy(assumptions = assumptions.copy(channels = updatedChannels)) }
                    recompute()
                } else {
                    emit(GrowthSimulatorEffect.ShowToast("Must have at least one channel"))
                }
            }

            is GrowthSimulatorIntent.UpdateReferralRate -> {
                update { copy(assumptions = assumptions.copy(referralRatePercent = event.rate)) }
                recompute()
            }

            is GrowthSimulatorIntent.UpdateOrganicGrowth -> {
                update { copy(assumptions = assumptions.copy(organicGrowthPercent = event.rate)) }
                recompute()
            }

            is GrowthSimulatorIntent.UpdateExpansionRate -> {
                update { copy(assumptions = assumptions.copy(expansionRatePercent = event.rate)) }
                recompute()
            }

            is GrowthSimulatorIntent.UpdateStartingCash -> {
                update { copy(assumptions = assumptions.copy(startingCashMinor = event.cashMinor)) }
                recompute()
            }

            GrowthSimulatorIntent.ResetToDefaults -> {
                update { copy(assumptions = GrowthAssumptions()) }
                recompute()
            }

            GrowthSimulatorIntent.OpenAddChannelSheet -> update { copy(showAddChannelSheet = true) }
            GrowthSimulatorIntent.DismissAddChannelSheet -> update {
                copy(
                    showAddChannelSheet = false
                )
            }

            GrowthSimulatorIntent.OpenSaveScenarioDialog -> update {
                copy(
                    showSaveScenarioDialog = true
                )
            }

            GrowthSimulatorIntent.DismissSaveScenarioDialog -> update {
                copy(
                    showSaveScenarioDialog = false
                )
            }

            is GrowthSimulatorIntent.UpdateScenarioName -> update {
                copy(
                    scenarioNameInput = event.name
                )
            }

            GrowthSimulatorIntent.SaveScenario -> saveScenario()
            GrowthSimulatorIntent.OpenCashSheet -> update { copy(showCashSheet = true) }
            GrowthSimulatorIntent.DismissCashSheet -> update { copy(showCashSheet = false) }
        }
    }

    private fun recompute() {
        val currentState = state.value
        val result = GrowthEngine.simulate(
            startingCustomers = currentState.baselineCustomers,
            startingMrrMinor = currentState.baselineMrrMinor,
            monthlyChurnPercent = currentState.baselineChurnRate,
            assumptions = currentState.assumptions
        )
        update { copy(result = result, isCalculating = false) }
    }

    private fun saveScenario() {
        viewModelScope.launch {
            val currentState = state.value
            val name = currentState.scenarioNameInput.ifBlank {
                "Growth Plan (${currentState.assumptions.timeHorizonMonths}mo)"
            }

            val fallbackProfile = BusinessProfile(
                monthlyChurnRate = currentState.baselineChurnRate,
                monthlyGrowthRate = 5.0,
                cac = BigDecimal("100.00"),
                fixedCosts = BigDecimal("2000.00"),
                variableCostPerCustomer = BigDecimal("5.00"),
                plans = listOf(
                    Plan(
                        id = "plan_default",
                        name = "Standard",
                        price = BigDecimal("100.00"),
                        customerCount = currentState.baselineCustomers
                    )
                )
            )

            val scenario = Scenario(
                name = name,
                baselineProfile = cachedProfile ?: fallbackProfile,
                assumptions = Assumptions(
                    growth = currentState.assumptions
                )
            )

            scenarioRepository.saveScenario(scenario)
            update { copy(showSaveScenarioDialog = false, scenarioNameInput = "") }
            emit(GrowthSimulatorEffect.ShowToast("Scenario saved: $name"))
            emit(GrowthSimulatorEffect.ScenarioSaved(scenario.id))
        }
    }
}
