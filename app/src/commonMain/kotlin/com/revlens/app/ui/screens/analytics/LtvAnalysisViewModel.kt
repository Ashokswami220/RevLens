package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.LtvCalculator
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.ui.components.display.DataSourceMode
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

class LtvAnalysisViewModel(
    private val profileRepository: ProfileRepository
) : MviViewModel<LtvAnalysisUiState, LtvAnalysisEvent, LtvAnalysisEffect>(
    LtvAnalysisUiState()
) {
    private var currentProfile: BusinessProfile? = null

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
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

                    val symbol = CurrencyUtils.getCurrency(profile.currencyCode).symbol
                    update {
                        copy(
                            baseArpu = arpu,
                            baseChurnPercent = churn,
                            simulatedChurnPercent = churn,
                            grossMarginPercent = margin,
                            cac = profile.cac,
                            currencySymbol = symbol,
                            dataSourceMode = DataSourceMode.MODELED
                        )
                    }
                    recalculate()
                }
            }
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

        val payback = if (arpu > BigDecimal.ZERO && margin > 0.0) {
            val netMonthly = arpu.toDouble() * (margin / 100.0)
            if (netMonthly > 0.0) s.cac.toDouble() / netMonthly else 0.0
        } else 0.0

        val ratio = if (s.cac > BigDecimal.ZERO) {
            simLtv.toDouble() / s.cac.toDouble()
        } else 0.0

        val curve = LtvCalculator.calculateSensitivityPoints(arpu, margin)

        update {
            copy(
                baseLtv = baseLtv,
                simulatedLtv = simLtv,
                paybackPeriodMonths = payback,
                ltvToCacRatio = ratio,
                sensitivityCurve = curve
            )
        }
    }
}
