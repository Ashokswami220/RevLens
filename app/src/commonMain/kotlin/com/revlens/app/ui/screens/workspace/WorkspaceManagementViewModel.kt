package com.revlens.app.ui.screens.workspace

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.session.SessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WorkspaceManagementViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val teamRepository: TeamRepository? = null,
    private val authRepository: AuthRepository? = null,
    private val sessionManager: SessionManager? = null
) : MviViewModel<WorkspaceManagementUiState, WorkspaceManagementEvent, WorkspaceManagementEffect>(
    WorkspaceManagementUiState()
) {

    init {
        viewModelScope.launch {
            combine(
                workspaceContext.activeWorkspaceId,
                workspaceRepository.workspacesFlow
            ) { activeId, workspaces ->
                val active =
                    workspaces.firstOrNull { it.id == activeId } ?: workspaces.firstOrNull()
                active to workspaces
            }.collect { (active, all) ->
                if (active != null && workspaceContext.activeWorkspaceId.value != active.id) {
                    workspaceContext.switchTo(active.id)
                }
                val role = resolveRole(active?.id)
                update { copy(activeWorkspace = active, allWorkspaces = all, currentUserRole = role) }
            }
        }
    }

    private suspend fun resolveRole(workspaceId: String?): Role {
        val repo = teamRepository
        if (workspaceId.isNullOrBlank() || repo == null) return Role.Owner
        val email = getCurrentUserEmail() ?: return Role.Owner
        return runCatching { repo.getUserRoleInWorkspace(workspaceId, email) }.getOrNull() ?: Role.Owner
    }

    private suspend fun getCurrentUserEmail(): String? {
        val sessionUser = (sessionManager?.state?.value as? SessionState.Authenticated)?.user
        if (sessionUser != null && sessionUser.email.isNotBlank()) return sessionUser.email
        return authRepository?.getCurrentUser()?.email
    }

    override fun onEvent(event: WorkspaceManagementEvent) {
        when (event) {
            is WorkspaceManagementEvent.SwitchWorkspace -> {
                viewModelScope.launch {
                    workspaceContext.switchTo(event.id)
                    update { copy(isSwitchSheetVisible = false) }
                    emit(
                        WorkspaceManagementEffect.ShowMessage(
                            UiText.DynamicString("Switched workspace")
                        )
                    )
                }
            }

            is WorkspaceManagementEvent.UpdateName -> {
                val current = state.value.activeWorkspace ?: return
                val trimmed = event.newName.trim()
                if (trimmed.isBlank() || trimmed == current.name) {
                    update { copy(isEditNameDialogVisible = false) }
                    return
                }
                val updated = current.copy(name = trimmed)
                update {
                    copy(
                        activeWorkspace = updated,
                        allWorkspaces = allWorkspaces.map { if (it.id == updated.id) updated else it },
                        isEditNameDialogVisible = false
                    )
                }
                viewModelScope.launch {
                    workspaceRepository.updateWorkspace(updated)
                    emit(
                        WorkspaceManagementEffect.ShowMessage(
                            UiText.DynamicString("Workspace renamed")
                        )
                    )
                }
            }

            is WorkspaceManagementEvent.UpdateBusinessModel -> {
                val current = state.value.activeWorkspace ?: return
                if (current.businessModel == event.model) return
                val updated = current.copy(businessModel = event.model)
                update {
                    copy(
                        activeWorkspace = updated,
                        allWorkspaces = allWorkspaces.map { if (it.id == updated.id) updated else it }
                    )
                }
                viewModelScope.launch {
                    workspaceRepository.updateWorkspace(updated)
                }
            }

            WorkspaceManagementEvent.ShowSwitchSheet -> update { copy(isSwitchSheetVisible = true) }
            WorkspaceManagementEvent.HideSwitchSheet -> update {
                copy(
                    isSwitchSheetVisible = false
                )
            }

            WorkspaceManagementEvent.ShowEditNameDialog -> update {
                copy(
                    isEditNameDialogVisible = true
                )
            }

            WorkspaceManagementEvent.HideEditNameDialog -> update {
                copy(
                    isEditNameDialogVisible = false
                )
            }

            WorkspaceManagementEvent.ShowDeleteDialog -> update {
                copy(
                    isDeleteDialogVisible = true
                )
            }

            WorkspaceManagementEvent.HideDeleteDialog -> update {
                copy(
                    isDeleteDialogVisible = false
                )
            }

            WorkspaceManagementEvent.ConfirmDeleteWorkspace -> {
                val current = state.value.activeWorkspace ?: return
                viewModelScope.launch {
                    workspaceRepository.deleteWorkspace(current.id)
                    val remaining = state.value.allWorkspaces.filter { it.id != current.id }
                    if (remaining.isNotEmpty()) {
                        workspaceContext.switchTo(remaining.first().id)
                    }
                    update { copy(isDeleteDialogVisible = false) }
                    emit(
                        WorkspaceManagementEffect.ShowMessage(
                            UiText.DynamicString("Workspace deleted")
                        )
                    )
                }
            }

            WorkspaceManagementEvent.ShowLeaveDialog -> update {
                copy(isLeaveDialogVisible = true)
            }

            WorkspaceManagementEvent.HideLeaveDialog -> update {
                copy(isLeaveDialogVisible = false)
            }

            WorkspaceManagementEvent.ConfirmLeaveWorkspace -> {
                val current = state.value.activeWorkspace ?: return
                viewModelScope.launch {
                    val email = getCurrentUserEmail()
                    if (email == null || teamRepository == null) {
                        update { copy(isLeaveDialogVisible = false) }
                        emit(WorkspaceManagementEffect.ShowMessage(UiText.DynamicString("Could not identify current user.")))
                        return@launch
                    }
                    val result = teamRepository.leaveWorkspace(current.id, email)
                    result.onSuccess {
                        update { copy(isLeaveDialogVisible = false) }
                        workspaceContext.switchTo("")
                        profileRepository.setOnboardingCompleted(false)
                        emit(WorkspaceManagementEffect.ShowMessage(UiText.DynamicString("You left ${current.name}.")))
                        emit(WorkspaceManagementEffect.NavigateToCreateWorkspace)
                    }.onFailure { err ->
                        update { copy(isLeaveDialogVisible = false) }
                        emit(WorkspaceManagementEffect.ShowMessage(UiText.DynamicString(err.message ?: "Could not leave workspace.")))
                    }
                }
            }
        }
    }
}
