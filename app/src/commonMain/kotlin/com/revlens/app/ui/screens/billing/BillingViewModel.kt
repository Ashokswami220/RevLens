package com.revlens.app.ui.screens.billing

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.model.PlanTier
import kotlinx.coroutines.launch

class BillingViewModel :
    MviViewModel<BillingUiState, BillingEvent, BillingEffect>(BillingUiState()) {

    override fun onEvent(event: BillingEvent) {
        when (event) {
            BillingEvent.OpenChangePlanSheet -> update {
                copy(isChangePlanSheetVisible = true, selectedPeriod = subscription.period)
            }

            BillingEvent.DismissChangePlanSheet -> update { copy(isChangePlanSheetVisible = false) }
            is BillingEvent.SelectBillingPeriod -> update { copy(selectedPeriod = event.period) }
            is BillingEvent.SelectPlan -> changePlan(event.tier)
            BillingEvent.PromptCancelSubscription -> update { copy(isCancelDialogVisible = true) }
            BillingEvent.DismissCancelSubscription -> update { copy(isCancelDialogVisible = false) }
            BillingEvent.ConfirmCancelSubscription -> cancelSubscription()
            BillingEvent.ReactivateSubscription -> reactivateSubscription()
            is BillingEvent.DownloadInvoice -> {
                viewModelScope.launch {
                    emit(BillingEffect.ShowToast("Downloading invoice ${event.invoiceId}..."))
                }
            }
        }
    }

    private fun changePlan(tier: PlanTier) {
        viewModelScope.launch {
            val updatedSub = state.value.subscription.copy(
                planTier = tier,
                period = state.value.selectedPeriod
            )
            update {
                copy(
                    subscription = updatedSub,
                    isChangePlanSheetVisible = false
                )
            }
            emit(BillingEffect.ShowToast("Plan successfully updated to ${tier.title}!"))
        }
    }

    private fun cancelSubscription() {
        viewModelScope.launch {
            update { copy(isCancelDialogVisible = false) }
            emit(BillingEffect.ShowToast("Subscription will cancel at end of billing cycle."))
        }
    }

    private fun reactivateSubscription() {
        viewModelScope.launch {
            emit(BillingEffect.ShowToast("Subscription reactivated successfully!"))
        }
    }
}
