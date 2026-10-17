package com.example.revlens.ui.screens.analytics

import com.example.revlens.domain.calc.AnalyticsPeriod
import com.example.revlens.domain.calc.CustomerAnalyticsResult
import com.example.revlens.domain.calc.CustomerRecordItem
import com.example.revlens.domain.calc.CustomerSegmentBy

data class CustomerAnalyticsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.THREE_MONTHS,
    val segmentBy: CustomerSegmentBy = CustomerSegmentBy.PLAN,
    val result: CustomerAnalyticsResult? = null,
    val selectedCustomer: CustomerRecordItem? = null,
    val hasActualData: Boolean = false,
    val currencySymbol: String = "$",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CustomerAnalyticsEvent {
    data class SelectPeriod(val period: AnalyticsPeriod) : CustomerAnalyticsEvent
    data class SelectSegmentBy(val segment: CustomerSegmentBy) : CustomerAnalyticsEvent
    data class SelectCustomer(val customer: CustomerRecordItem?) : CustomerAnalyticsEvent
    data object DismissCustomerSheet : CustomerAnalyticsEvent
    data object ImportDataClicked : CustomerAnalyticsEvent
    data object Refresh : CustomerAnalyticsEvent
}

sealed interface CustomerAnalyticsEffect {
    data object NavigateToDataImport : CustomerAnalyticsEffect
    data class ShowToast(val message: String) : CustomerAnalyticsEffect
}
