package com.revlens.app.ui.screens.workspace

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.core.util.Validators
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.CreateWorkspaceUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CreateWorkspaceViewModel(
    private val createWorkspaceUseCase: CreateWorkspaceUseCase,
    openedFromSettings: Boolean = false,
    private val workspaceRepository: WorkspaceRepository? = null,
    private val workspaceContext: WorkspaceContext? = null,
    private val profileRepository: ProfileRepository? = null,
    private val sessionManager: SessionManager? = null,
    private val authRepository: AuthRepository? = null
) : MviViewModel<CreateWorkspaceUiState, CreateWorkspaceEvent, CreateWorkspaceEffect>(
    CreateWorkspaceUiState(openedFromSettings = openedFromSettings)
) {

    override fun onEvent(event: CreateWorkspaceEvent) {
        when (event) {
            is CreateWorkspaceEvent.ModeChanged -> update {
                copy(mode = event.mode, nameError = null, joinCodeError = null)
            }

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

            is CreateWorkspaceEvent.JoinCodeChanged -> update {
                copy(joinCode = event.code, joinCodeError = null)
            }

            CreateWorkspaceEvent.Submit -> submitCreate()
            CreateWorkspaceEvent.SubmitJoinCode -> submitJoinByCode()
        }
    }

    private fun submitCreate() {
        val current = state.value
        val nameErr = Validators.workspaceName(current.name)
        if (nameErr != null) {
            update { copy(nameError = nameErr) }
            return
        }

        viewModelScope.launch {
            update { copy(isLoading = true, nameError = null) }
            try {
                val existing = workspaceRepository?.workspacesFlow?.firstOrNull()
                    .orEmpty()
                existing.forEach { ws ->
                    workspaceRepository?.deleteWorkspace(ws.id)
                }

                createWorkspaceUseCase(
                    name = current.name,
                    currency = current.currency,
                    businessModel = current.businessModel,
                    timeZoneId = current.timeZoneId,
                    fiscalYearStartMonth = current.fiscalYearStartMonth
                )
                update { copy(isLoading = false) }
                if (current.openedFromSettings) {
                    emit(CreateWorkspaceEffect.NavigateBack)
                } else {
                    emit(CreateWorkspaceEffect.NavigateToOnboarding)
                }
            } catch (e: Throwable) {
                update { copy(isLoading = false) }
                emit(
                    CreateWorkspaceEffect.ShowMessage(
                        UiText.DynamicString(e.message ?: "Failed to create workspace")
                    )
                )
            }
        }
    }

    private fun submitJoinByCode() {
        val code = state.value.joinCode.trim()
        if (code.length < 3) {
            update {
                copy(joinCodeError = UiText.DynamicString("Please enter a valid team code."))
            }
            return
        }

        viewModelScope.launch {
            update { copy(isLoading = true, joinCodeError = null) }
            val targetWsId = "ws_${
                code.lowercase()
                    .replace(" ", "_")
            }"
            val remoteWs = Workspace(
                id = targetWsId,
                name = "Joined Workspace ($code)",
                currency = "USD"
            )
            workspaceRepository?.createWorkspace(remoteWs)
            workspaceContext?.switchTo(targetWsId)
            profileRepository?.setOnboardingCompleted(true)
            update { copy(isLoading = false) }
            emit(
                CreateWorkspaceEffect.ShowMessage(
                    UiText.DynamicString("Successfully joined ${remoteWs.name}!")
                )
            )
            emit(CreateWorkspaceEffect.NavigateToDashboard)
        }
    }
}
