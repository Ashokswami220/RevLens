package com.revlens.app.ui.screens.analytics

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.calc.CustomerSegmentationCalculator
import com.revlens.app.model.BusinessProfile
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

class CustomerAnalyticsViewModel(
    private val profileRepository: ProfileRepository
) : MviViewModel<CustomerAnalyticsUiState, CustomerAnalyticsEvent, CustomerAnalyticsEffect>(
    CustomerAnalyticsUiState()
) {
    private var cachedProfile: BusinessProfile? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
                cachedProfile = profile
                val symbol = CurrencyUtils.getCurrency(profile?.currencyCode).symbol
                update {
                    copy(
                        hasActualData = false,
                        currencySymbol = symbol
                    )
                }
                recompute()
            }
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
            dataset = null,
            period = currentState.period,
            segmentBy = currentState.segmentBy
        )
        update { copy(result = result, isLoading = false) }
    }
}
