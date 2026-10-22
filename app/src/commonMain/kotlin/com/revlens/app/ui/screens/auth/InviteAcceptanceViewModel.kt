package com.revlens.app.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInviteDetail
import kotlinx.coroutines.launch

class InviteAcceptanceViewModel :
    MviViewModel<InviteAcceptanceUiState, InviteAcceptanceEvent, InviteAcceptanceEffect>(
        InviteAcceptanceUiState(
            invites = listOf(
                TeamInviteDetail(
                    inviteId = "inv_sample",
                    workspaceId = "ws_sample",
                    workspaceName = "Sample Business",
                    invitedEmail = "alex@revlens.io",
                    role = Role.Editor,
                    invitedBy = "Alex Rivera",
                    sentAt = "2 days ago"
                )
            )
        )
    ) {

    override fun onEvent(event: InviteAcceptanceEvent) {
        when (event) {
            is InviteAcceptanceEvent.AcceptInvite -> accept(event.inviteId)
            is InviteAcceptanceEvent.DeclineInvite -> decline(event.inviteId)
        }
    }

    private fun accept(inviteId: String) {
        viewModelScope.launch {
            emit(InviteAcceptanceEffect.ShowMessage(UiText.DynamicString("Joined workspace!")))
            emit(InviteAcceptanceEffect.NavigateToDashboard)
        }
    }

    private fun decline(inviteId: String) {
        viewModelScope.launch {
            val remaining = state.value.invites.filterNot { it.inviteId == inviteId }
            update { copy(invites = remaining) }
            emit(InviteAcceptanceEffect.ShowMessage(UiText.DynamicString("Invitation declined")))
            if (remaining.isEmpty()) {
                emit(InviteAcceptanceEffect.NavigateToCreateWorkspace)
            }
        }
    }
}
