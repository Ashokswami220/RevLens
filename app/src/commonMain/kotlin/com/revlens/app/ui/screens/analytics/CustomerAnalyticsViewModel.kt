package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.CustomerSegmentationCalculator
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Dataset
import com.revlens.app.domain.repository.AnalyticsRepository
import com.revlens.app.model.BusinessProfile
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CustomerAnalyticsViewModel(
    private val profileRepository: ProfileRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<CustomerAnalyticsUiState, CustomerAnalyticsEvent, CustomerAnalyticsEffect>(
    CustomerAnalyticsUiState()
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
                val hasActual = dataset != null && dataset.subscriptions.isNotEmpty()
                val symbol =
                    if (profile?.currencyCode == "EUR") "€" else if (profile?.currencyCode == "GBP") "£" else "$"
                update {
                    copy(
                        hasActualData = hasActual,
                        currencySymbol = symbol
                    )
                }
                recompute()
            }.collect {}
        }
    }

    override fun onEvent(event: CustomerAnalyticsEvent) {
        when (event) {
            is CustomerAnalyticsEvent.SelectPeriod -> {
                update { copy(period = event.period) }
                recompute()
            }

            is CustomerAnalyticsEvent.SelectSegmentBy -> {
                update { copy(segmentBy = event.segment) }
                recompute()
            }

            is CustomerAnalyticsEvent.SelectCustomer -> {
                update { copy(selectedCustomer = event.customer) }
            }

            CustomerAnalyticsEvent.DismissCustomerSheet -> {
                update { copy(selectedCustomer = null) }
            }

            CustomerAnalyticsEvent.ImportDataClicked -> {
                emit(CustomerAnalyticsEffect.NavigateToDataImport)
            }

            CustomerAnalyticsEvent.Refresh -> recompute()
        }
    }

    private fun recompute() {
        val currentState = state.value
        val result = CustomerSegmentationCalculator.calculate(
            profile = cachedProfile,
            dataset = cachedDataset,
            period = currentState.period,
            segmentBy = currentState.segmentBy
        )
        update { copy(result = result, isLoading = false) }
    }
}
