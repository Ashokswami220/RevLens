package com.revlens.app.ui.screens.workspace

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WorkspaceManagementViewModel(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository
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
                update { copy(activeWorkspace = active, allWorkspaces = all) }
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
                viewModelScope.launch {
                    workspaceRepository.updateWorkspace(current.copy(name = event.newName))
                    update { copy(isEditNameDialogVisible = false) }
                    emit(
                        WorkspaceManagementEffect.ShowMessage(
                            UiText.DynamicString("Workspace renamed")
                        )
                    )
                }
            }

            is WorkspaceManagementEvent.UpdateBusinessModel -> {
                val current = state.value.activeWorkspace ?: return
                update { copy(activeWorkspace = current.copy(businessModel = event.model)) }
                viewModelScope.launch {
                    workspaceRepository.updateWorkspace(current.copy(businessModel = event.model))
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

            WorkspaceManagementEvent.ShowTransferOwnershipDialog -> update {
                copy(
                    isTransferOwnershipDialogVisible = true
                )
            }

            WorkspaceManagementEvent.HideTransferOwnershipDialog -> update {
                copy(
                    isTransferOwnershipDialogVisible = false
                )
            }

            is WorkspaceManagementEvent.SelectTransferMember -> update {
                copy(
                    selectedTransferMember = event.member
                )
            }

            WorkspaceManagementEvent.ConfirmTransferOwnership -> {
                val member = state.value.selectedTransferMember
                update { copy(isTransferOwnershipDialogVisible = false) }
                emit(
                    WorkspaceManagementEffect.ShowMessage(
                        UiText.DynamicString("Ownership transferred to $member")
                    )
                )
            }
        }
    }
}
