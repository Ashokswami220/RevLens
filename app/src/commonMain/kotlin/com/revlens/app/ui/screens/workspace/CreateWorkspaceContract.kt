package com.revlens.app.ui.screens.workspace

import com.revlens.app.core.ui.UiText
import com.revlens.app.domain.model.BusinessModel

data class CreateWorkspaceUiState(
    val name: String = "",
    val currency: String = "USD",
    val businessModel: BusinessModel = BusinessModel.B2bSaas,
    val timeZoneId: String = "UTC",
    val fiscalYearStartMonth: Int = 1,
    val nameError: UiText? = null,
    val isLoading: Boolean = false,
    val openedFromSettings: Boolean = false
) {
    val canSubmit: Boolean get() = name.trim().length in 2..40 && !isLoading
}

sealed interface CreateWorkspaceEvent {
    data class NameChanged(val value: String) : CreateWorkspaceEvent
    data class CurrencyChanged(val value: String) : CreateWorkspaceEvent
    data class BusinessModelChanged(val model: BusinessModel) : CreateWorkspaceEvent
    data class TimeZoneChanged(val value: String) : CreateWorkspaceEvent
    data class FiscalMonthChanged(val month: Int) : CreateWorkspaceEvent
    data object Submit : CreateWorkspaceEvent
}

sealed interface CreateWorkspaceEffect {
    data object NavigateToOnboarding : CreateWorkspaceEffect
    data object NavigateBack : CreateWorkspaceEffect
    data class ShowMessage(val text: UiText) : CreateWorkspaceEffect
}
