package com.revlens.app.ui.screens.marketing

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import kotlinx.coroutines.launch

class PublicPricingViewModel :
    MviViewModel<PublicPricingUiState, PublicPricingEvent, PublicPricingEffect>(
        PublicPricingUiState()
    ) {
    override fun onEvent(event: PublicPricingEvent) {
        when (event) {
            is PublicPricingEvent.SelectBillingPeriod -> update {
                copy(
                    billingPeriod = event.period
                )
            }

            is PublicPricingEvent.ToggleFaq -> {
                val current = state.value.expandedFaqIndices
                val updated = if (current.contains(
                        event.index
                    )
                ) current - event.index else current + event.index
                update { copy(expandedFaqIndices = updated) }
            }

            is PublicPricingEvent.SelectPlan -> {
                viewModelScope.launch {
                    emit(PublicPricingEffect.NavigateToAuth(event.tier))
                }
            }

            PublicPricingEvent.ClickLogin -> {
                viewModelScope.launch {
                    emit(PublicPricingEffect.NavigateToAuth(null))
                }
            }

            PublicPricingEvent.ClickHome -> {
                viewModelScope.launch {
                    emit(PublicPricingEffect.NavigateToHome)
                }
            }
        }
    }
}
