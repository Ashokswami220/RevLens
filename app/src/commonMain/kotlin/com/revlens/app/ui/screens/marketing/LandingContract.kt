package com.revlens.app.ui.screens.marketing

data class LandingUiState(
    val simulatedMrr: String = "$58,400",
    val simulatedLift: String = "+32.5%",
    val simulatedLtv: String = "$1,850",
    val simulatedPayback: String = "5.8 months"
)

sealed interface LandingEvent {
    data object ClickGetStarted : LandingEvent
    data object ClickExploreDemo : LandingEvent
    data object ClickPricing : LandingEvent
}

sealed interface LandingEffect {
    data object NavigateToAuth : LandingEffect
    data object NavigateToDemo : LandingEffect
    data object NavigateToPricing : LandingEffect
}
