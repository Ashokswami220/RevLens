package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.LtvCalculator
import com.revlens.app.domain.repository.AnalyticsRepository
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.ui.components.display.DataSourceMode
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LtvAnalysisViewModel(
    private val profileRepository: ProfileRepository,
    private val analyticsRepository: AnalyticsRepository
) : MviViewModel<LtvAnalysisUiState, LtvAnalysisEvent, LtvAnalysisEffect>(
    LtvAnalysisUiState()
) {
    private var currentProfile: BusinessProfile? = null

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                profileRepository.profileFlow,
                analyticsRepository.datasetFlow
            ) { profile, dataset ->
                currentProfile = profile
                if (profile != null) {
                    val arpu = profile.arpu
                    val churn = profile.monthlyChurnRate
                    val varCost = profile.variableCostPerCustomer
                    val margin = if (arpu > BigDecimal.ZERO && arpu > varCost) {
                        ((arpu.toDouble() - varCost.toDouble()) / arpu.toDouble()) * 100.0
                    } else {
                        80.0
                    }

                    val symbol =
                        CurrencyUtils.getCurrency(profile.currencyCode).symbol
                    update {
                        copy(
                            baseArpu = arpu,
                            baseChurnPercent = churn,
                            simulatedChurnPercent = churn,
                            grossMarginPercent = margin,
                            cac = profile.cac,
                            currencySymbol = symbol,
                            dataSourceMode = if (dataset != null && dataset.subscriptions.isNotEmpty()) {
                                DataSourceMode.ACTUAL
                            } else {
                                DataSourceMode.MODELED
                            }
                        )
                    }
                    recalculate()
                }
            }.collect {}
        }
    }

    override fun onEvent(event: LtvAnalysisEvent) {
        when (event) {
            is LtvAnalysisEvent.SelectMethod -> {
                update { copy(method = event.method) }
                recalculate()
            }

            is LtvAnalysisEvent.UpdateSimulatedChurn -> {
                update { copy(simulatedChurnPercent = event.churnPercent) }
                recalculate()
            }

            is LtvAnalysisEvent.UpdateGrossMargin -> {
                update { copy(grossMarginPercent = event.marginPercent) }
                recalculate()
            }

            LtvAnalysisEvent.ResetSimulation -> {
                update { copy(simulatedChurnPercent = baseChurnPercent) }
                recalculate()
            }

            LtvAnalysisEvent.OpenInWhatIf -> {
                emit(LtvAnalysisEffect.NavigateToWhatIf(state.value.simulatedChurnPercent))
            }
        }
    }

    private fun recalculate() {
        val s = state.value
        val arpu = s.baseArpu
        val margin = s.grossMarginPercent

        val baseLtv = when (s.method) {
            LtvMethod.MARGIN_ADJUSTED -> LtvCalculator.calculateMarginAdjustedLtv(
                arpu, margin, s.baseChurnPercent
            )

            LtvMethod.SIMPLE -> LtvCalculator.calculateSimpleLtv(arpu, s.baseChurnPercent)
        }

        val simLtv = when (s.method) {
            LtvMethod.MARGIN_ADJUSTED -> LtvCalculator.calculateMarginAdjustedLtv(
                arpu, margin, s.simulatedChurnPercent
            )

            LtvMethod.SIMPLE -> LtvCalculator.calculateSimpleLtv(arpu, s.simulatedChurnPercent)
        }

        val curve = LtvCalculator.calculateSensitivityPoints(arpu, margin)

        val monthlyProfitPerCust = arpu.toDouble() * (margin / 100.0)
        val payback = if (monthlyProfitPerCust > 0) s.cac.toDouble() / monthlyProfitPerCust else 0.0
        val ltvToCac = if (s.cac.toDouble() > 0) simLtv.toDouble() / s.cac.toDouble() else 0.0

        update {
            copy(
                baseLtv = baseLtv,
                simulatedLtv = simLtv,
                paybackPeriodMonths = (payback * 10).toInt() / 10.0,
                ltvToCacRatio = (ltvToCac * 10).toInt() / 10.0,
                sensitivityCurve = curve
            )
        }
    }
}
