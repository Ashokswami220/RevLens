package com.revlens.app.ui.screens.analytics

import com.revlens.app.domain.calc.AnalyticsPeriod
import com.revlens.app.domain.calc.CustomerAnalyticsResult
import com.revlens.app.domain.calc.CustomerRecordItem
import com.revlens.app.domain.calc.CustomerSegmentBy

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
