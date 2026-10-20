package com.revlens.app.ui.screens.auth

import com.revlens.app.core.ui.UiText
import com.revlens.app.domain.model.TeamInviteDetail

data class InviteAcceptanceUiState(
    val isLoading: Boolean = false,
    val invites: List<TeamInviteDetail> = emptyList(),
    val userEmail: String = "",
    val error: UiText? = null
)

sealed interface InviteAcceptanceEvent {
    data class AcceptInvite(val inviteId: String) : InviteAcceptanceEvent
    data class DeclineInvite(val inviteId: String) : InviteAcceptanceEvent
}

sealed interface InviteAcceptanceEffect {
    data class ShowMessage(val text: UiText) : InviteAcceptanceEffect
    data object NavigateToDashboard : InviteAcceptanceEffect
    data object NavigateToCreateWorkspace : InviteAcceptanceEffect
}
