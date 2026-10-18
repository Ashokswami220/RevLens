package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.util.IdGenerator
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.CacCalculator
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.DataSource
import com.revlens.app.domain.model.Dataset
import com.revlens.app.domain.model.MarketingSpendRecord
import com.revlens.app.domain.repository.AnalyticsRepository
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.ui.components.display.DataSourceMode
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CacAnalyticsViewModel(
    private val profileRepository: ProfileRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<CacAnalyticsUiState, CacAnalyticsEvent, CacAnalyticsEffect>(
    CacAnalyticsUiState()
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
                recalculate()
            }.collect {}
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
                addManualSpend(event.channel, event.amountMinor, event.newCustomers)
            }
            CacAnalyticsEvent.Refresh -> recalculate()
            CacAnalyticsEvent.OpenGrowthSimulator -> {
                emit(CacAnalyticsEffect.NavigateToGrowthSimulator)
            }
        }
    }

    private fun recalculate() {
        val profile = cachedProfile ?: return
        val dataset = cachedDataset

        val hasActualSpend = dataset != null && dataset.spend.isNotEmpty()
        val mode = if (hasActualSpend) DataSourceMode.ACTUAL else DataSourceMode.MODELED

        val result = if (hasActualSpend) {
            val arpu = profile.arpu
            val varCost = profile.variableCostPerCustomer
            val margin = if (arpu > BigDecimal.ZERO && arpu > varCost) {
                ((arpu.toDouble() - varCost.toDouble()) / arpu.toDouble()) * 100.0
            } else 80.0
            CacCalculator.calculateActual(
                spendRecords = dataset!!.spend,
                subscriptions = dataset.subscriptions,
                arpu = arpu,
                grossMarginPercent = margin
            )
        } else {
            CacCalculator.calculateModeled(
                profile = profile,
                simulatedBudgetMinor = state.value.simulatedBudgetMinor
            )
        }

        update {
            copy(
                result = result,
                dataSourceMode = mode
            )
        }
    }

    private fun addManualSpend(channel: String, amountMinor: Long, newCustomers: Int) {
        viewModelScope.launch {
            try {
                val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
                val existing = analyticsRepository.getDataset(wsId)
                val newRecord = MarketingSpendRecord(
                    month = "2024-03-01",
                    channel = channel,
                    spendMinor = amountMinor,
                    newCustomers = newCustomers
                )
                val updatedSpend = (existing?.spend ?: emptyList()) + newRecord
                val updatedDataset = existing?.copy(
                    spend = updatedSpend,
                    source = DataSource.Manual
                ) ?: Dataset(
                    id = IdGenerator.newId("ds"),
                    workspaceId = wsId,
                    source = DataSource.Manual,
                    filename = "manual_spend.csv",
                    spend = updatedSpend
                )

                analyticsRepository.saveDataset(updatedDataset)
                update { copy(isAddSpendSheetOpen = false) }
                emit(CacAnalyticsEffect.ShowMessage("Added spend record for $channel"))
            } catch (e: Exception) {
                emit(CacAnalyticsEffect.ShowMessage(e.message ?: "Failed to save spend"))
            }
        }
    }
}
