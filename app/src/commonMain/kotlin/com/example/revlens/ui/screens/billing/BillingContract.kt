package com.example.revlens.ui.screens.billing

import com.example.revlens.domain.model.BillingPeriod
import com.example.revlens.domain.model.Invoice
import com.example.revlens.domain.model.PlanTier
import com.example.revlens.domain.model.Subscription

data class BillingUiState(
    val subscription: Subscription = Subscription(),
    val invoices: List<Invoice> = emptyList(),
    val selectedPeriod: BillingPeriod = BillingPeriod.ANNUAL,
    val isChangePlanSheetVisible: Boolean = false,
    val isCancelDialogVisible: Boolean = false,
    val isLoading: Boolean = false,
    val usedSeats: Int = 3,
    val usedScenarios: Int = 8,
    val error: String? = null
)

sealed interface BillingEvent {
    data object OpenChangePlanSheet : BillingEvent
    data object DismissChangePlanSheet : BillingEvent
    data class SelectBillingPeriod(val period: BillingPeriod) : BillingEvent
    data class SelectPlan(val tier: PlanTier) : BillingEvent
    data object PromptCancelSubscription : BillingEvent
    data object DismissCancelSubscription : BillingEvent
    data object ConfirmCancelSubscription : BillingEvent
    data object ReactivateSubscription : BillingEvent
    data class DownloadInvoice(val invoiceId: String) : BillingEvent
}

sealed interface BillingEffect {
    data class ShowToast(val message: String) : BillingEffect
    data object NavigateBack : BillingEffect
}
