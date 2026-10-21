package com.revlens.app.ui.screens.workspace

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.core.util.Validators
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.CreateWorkspaceUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CreateWorkspaceViewModel(
    private val createWorkspaceUseCase: CreateWorkspaceUseCase,
    openedFromSettings: Boolean = false,
    private val teamRepository: TeamRepository? = null,
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
        val teamRepo = teamRepository
        if (teamRepo == null) {
            update {
                copy(joinCodeError = UiText.DynamicString("Team service is not available."))
            }
            return
        }

        viewModelScope.launch {
            update { copy(isLoading = true, joinCodeError = null) }
            val sessionUser = (sessionManager?.state?.value as? SessionState.Authenticated)?.user
            val user = sessionUser
                ?: authRepository?.getCurrentUser()
                ?: UserAccount(
                    id = "usr_member",
                    name = "Team Member",
                    email = "member@revlens.com"
                )

            val result = teamRepo.joinWorkspaceByCode(code, user)
            result.onSuccess { targetWsId ->
                val existing = workspaceRepository?.workspacesFlow?.firstOrNull()
                    .orEmpty()
                existing.forEach { ws ->
                    workspaceRepository?.deleteWorkspace(ws.id)
                }

                val remoteWs = teamRepo.getRemoteWorkspace(targetWsId)
                    ?: Workspace(
                        id = targetWsId,
                        name = "Joined Workspace",
                        currency = "USD"
                    )
                workspaceRepository?.createWorkspace(remoteWs)
                workspaceContext?.switchTo(targetWsId)

                val sharedProfile = try {
                    teamRepo.getWorkspaceProfile(targetWsId)
                } catch (_: Throwable) {
                    null
                }
                if (sharedProfile != null) {
                    profileRepository?.saveProfile(sharedProfile)
                } else {
                    profileRepository?.syncRemoteProfile()
                }
                profileRepository?.setOnboardingCompleted(true)

                update { copy(isLoading = false) }
                emit(
                    CreateWorkspaceEffect.ShowMessage(
                        UiText.DynamicString("Successfully joined ${remoteWs.name}!")
                    )
                )
                emit(CreateWorkspaceEffect.NavigateToDashboard)
            }
                .onFailure { err ->
                    update {
                        copy(
                            isLoading = false,
                            joinCodeError = UiText.DynamicString(
                                err.message
                                    ?: "Invalid team code. Please check with your workspace owner."
                            )
                        )
                    }
                }
        }
    }
}
