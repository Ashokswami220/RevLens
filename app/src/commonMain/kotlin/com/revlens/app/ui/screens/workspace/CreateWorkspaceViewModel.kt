package com.revlens.app.ui.screens.workspace

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.util.Validators
import com.revlens.app.domain.usecase.CreateWorkspaceUseCase
import kotlinx.coroutines.launch

class CreateWorkspaceViewModel(
    private val createWorkspaceUseCase: CreateWorkspaceUseCase
) : MviViewModel<CreateWorkspaceUiState, CreateWorkspaceEvent, CreateWorkspaceEffect>(
    CreateWorkspaceUiState()
) {
    override fun onEvent(event: CreateWorkspaceEvent) {
        when (event) {
            is CreateWorkspaceEvent.NameChanged -> update {
                copy(name = event.value, nameError = null)
            }

            is CreateWorkspaceEvent.CurrencyChanged -> update {
                copy(currency = event.value)
            }

            is CreateWorkspaceEvent.BusinessModelChanged -> update {
                copy(businessModel = event.model)
            }

            is CreateWorkspaceEvent.TimeZoneChanged -> update {
                copy(timeZoneId = event.value)
            }

            is CreateWorkspaceEvent.FiscalMonthChanged -> update {
                copy(fiscalYearStartMonth = event.month)
            }

            CreateWorkspaceEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = state.value
        val nameErr = Validators.workspaceName(s.name)
        if (nameErr != null) {
            update { copy(nameError = nameErr) }
            return
        }
        if (!s.canSubmit) return

        viewModelScope.launch {
            update { copy(isLoading = true) }
            createWorkspaceUseCase(
                name = s.name,
                currency = s.currency,
                businessModel = s.businessModel,
                timeZoneId = s.timeZoneId,
                fiscalYearStartMonth = s.fiscalYearStartMonth
            )
            update { copy(isLoading = false) }
            emit(CreateWorkspaceEffect.NavigateToOnboarding)
        }
    }
}
