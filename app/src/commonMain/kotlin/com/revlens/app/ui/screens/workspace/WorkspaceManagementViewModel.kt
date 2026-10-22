package com.revlens.app.ui.screens.workspace

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WorkspaceManagementViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository
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
                update {
                    copy(
                        activeWorkspace = active,
                        allWorkspaces = all,
                        currentUserRole = Role.Owner
                    )
                }
            }
        }
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
            WorkspaceManagementEvent.HideSwitchSheet -> update { copy(isSwitchSheetVisible = false) }
            WorkspaceManagementEvent.ShowEditNameDialog -> update { copy(isEditNameDialogVisible = true) }
            WorkspaceManagementEvent.HideEditNameDialog -> update { copy(isEditNameDialogVisible = false) }
        }
    }
}
