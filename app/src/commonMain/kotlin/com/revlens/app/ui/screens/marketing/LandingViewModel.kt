package com.revlens.app.ui.screens.marketing

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import kotlinx.coroutines.launch

class LandingViewModel : MviViewModel<LandingUiState, LandingEvent, LandingEffect>(LandingUiState()) {
    override fun onEvent(event: LandingEvent) {
        when (event) {
            LandingEvent.ClickGetStarted -> viewModelScope.launch { emit(LandingEffect.NavigateToAuth) }
            LandingEvent.ClickExploreDemo -> viewModelScope.launch { emit(LandingEffect.NavigateToDemo) }
            LandingEvent.ClickPricing -> viewModelScope.launch { emit(LandingEffect.NavigateToPricing) }
        }
    }
}
