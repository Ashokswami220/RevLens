package com.revlens.app.ui.screens.workspace

import com.revlens.app.core.ui.UiText
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Workspace

data class WorkspaceManagementUiState(
    val activeWorkspace: Workspace? = null,
    val allWorkspaces: List<Workspace> = emptyList(),
    val isSwitchSheetVisible: Boolean = false,
    val isEditNameDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isTransferOwnershipDialogVisible: Boolean = false,
    val eligibleTransferMembers: List<String> = listOf(
        "Sarah Connor (sarah@skynet.ai)",
        "Dave Bowman (dave@discovery.org)",
        "Elena Rostova (elena@orbit.io)"
    ),
    val selectedTransferMember: String = "Sarah Connor (sarah@skynet.ai)",
    val error: UiText? = null
)

sealed interface WorkspaceManagementEvent {
    data class SwitchWorkspace(val id: String) : WorkspaceManagementEvent
    data class UpdateName(val newName: String) : WorkspaceManagementEvent
    data class UpdateBusinessModel(val model: BusinessModel) : WorkspaceManagementEvent
    data object ShowSwitchSheet : WorkspaceManagementEvent
    data object HideSwitchSheet : WorkspaceManagementEvent
    data object ShowEditNameDialog : WorkspaceManagementEvent
    data object HideEditNameDialog : WorkspaceManagementEvent
    data object ShowDeleteDialog : WorkspaceManagementEvent
    data object HideDeleteDialog : WorkspaceManagementEvent
    data object ConfirmDeleteWorkspace : WorkspaceManagementEvent
    data object ShowTransferOwnershipDialog : WorkspaceManagementEvent
    data object HideTransferOwnershipDialog : WorkspaceManagementEvent
    data class SelectTransferMember(val member: String) : WorkspaceManagementEvent
    data object ConfirmTransferOwnership : WorkspaceManagementEvent
}

sealed interface WorkspaceManagementEffect {
    data class ShowMessage(val message: UiText) : WorkspaceManagementEffect
    data object NavigateToCreateWorkspace : WorkspaceManagementEffect
    data object NavigateToBaseline : WorkspaceManagementEffect
}
