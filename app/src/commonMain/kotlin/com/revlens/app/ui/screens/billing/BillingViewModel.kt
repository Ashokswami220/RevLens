package com.revlens.app.ui.screens.billing

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.PlanTier
import com.revlens.app.domain.repository.BillingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BillingViewModel(
    private val billingRepository: BillingRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<BillingUiState, BillingEvent, BillingEffect>(BillingUiState()) {

    private var currentWorkspaceId: String = "ws_default"
    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            workspaceContext.activeWorkspaceId.collectLatest { wsId ->
                val id = wsId ?: "ws_default"
                currentWorkspaceId = id
                startObservingBilling(id)
            }
        }
    }

    private fun startObservingBilling(wsId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            launch {
                billingRepository.getSubscription(wsId)
                    .collect { sub ->
                        update {
                            copy(
                                subscription = sub,
                                selectedPeriod = sub.period
                            )
                        }
                    }
            }
            launch {
                billingRepository.getInvoices(wsId)
                    .collect { invoices ->
                        update { copy(invoices = invoices) }
                    }
            }
        }
    }

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
            update { copy(isLoading = true) }
            val res =
                billingRepository.changePlan(currentWorkspaceId, tier, state.value.selectedPeriod)
            update { copy(isLoading = false) }

            res.onSuccess {
                update { copy(isChangePlanSheetVisible = false) }
                emit(BillingEffect.ShowToast("Plan successfully updated to ${tier.title}!"))
            }
                .onFailure { err ->
                    emit(BillingEffect.ShowToast(err.message ?: "Failed to update plan."))
                }
        }
    }

    private fun cancelSubscription() {
        viewModelScope.launch {
            update { copy(isLoading = true, isCancelDialogVisible = false) }
            val res = billingRepository.cancelSubscription(currentWorkspaceId)
            update { copy(isLoading = false) }

            res.onSuccess {
                emit(BillingEffect.ShowToast("Subscription will cancel at end of billing cycle."))
            }
                .onFailure { err ->
                    emit(BillingEffect.ShowToast(err.message ?: "Failed to cancel subscription."))
                }
        }
    }

    private fun reactivateSubscription() {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            val res = billingRepository.reactivateSubscription(currentWorkspaceId)
            update { copy(isLoading = false) }

            res.onSuccess {
                emit(BillingEffect.ShowToast("Subscription reactivated successfully!"))
            }
                .onFailure { err ->
                    emit(
                        BillingEffect.ShowToast(err.message ?: "Failed to reactivate subscription.")
                    )
                }
        }
    }
}
