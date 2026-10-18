package com.revlens.app.ui.screens.marketing

import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.PlanTier

data class PublicPricingUiState(
    val billingPeriod: BillingPeriod = BillingPeriod.ANNUAL,
    val expandedFaqIndices: Set<Int> = setOf(0)
)

sealed interface PublicPricingEvent {
    data class SelectBillingPeriod(val period: BillingPeriod) : PublicPricingEvent
    data class ToggleFaq(val index: Int) : PublicPricingEvent
    data class SelectPlan(val tier: PlanTier) : PublicPricingEvent
    data object ClickLogin : PublicPricingEvent
    data object ClickHome : PublicPricingEvent
}

sealed interface PublicPricingEffect {
    data class NavigateToAuth(val selectedTier: PlanTier? = null) : PublicPricingEffect
    data object NavigateToHome : PublicPricingEffect
    data class ShowToast(val message: String) : PublicPricingEffect
}
