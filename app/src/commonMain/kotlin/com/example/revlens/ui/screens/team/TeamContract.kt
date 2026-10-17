package com.example.revlens.ui.screens.team

import com.example.revlens.domain.model.Member
import com.example.revlens.domain.model.Role
import com.example.revlens.domain.model.TeamInvite

enum class TeamTab(val label: String) {
    Members("Members"),
    Invites("Invites"),
    Permissions("Permissions Matrix")
}

data class TeamUiState(
    val activeTab: TeamTab = TeamTab.Members,
    val members: List<Member> = emptyList(),
    val invites: List<TeamInvite> = emptyList(),
    val userRole: Role = Role.Owner,
    val totalSeats: Int = 5,
    val usedSeats: Int = 3,
    val isInviteSheetVisible: Boolean = false,
    val inviteEmail: String = "",
    val inviteRole: Role = Role.Editor,
    val isInviting: Boolean = false,
    val memberToRemove: Member? = null,
    val error: String? = null
)

sealed interface TeamEvent {
    data class SelectTab(val tab: TeamTab) : TeamEvent
    data object OpenInviteSheet : TeamEvent
    data object DismissInviteSheet : TeamEvent
    data class InviteEmailChanged(val email: String) : TeamEvent
    data class InviteRoleChanged(val role: Role) : TeamEvent
    data object SubmitInvite : TeamEvent
    data class ChangeMemberRole(val memberId: String, val newRole: Role) : TeamEvent
    data class PromptRemoveMember(val member: Member) : TeamEvent
    data object ConfirmRemoveMember : TeamEvent
    data object DismissRemoveMember : TeamEvent
    data class RevokeInvite(val inviteId: String) : TeamEvent
    data class ResendInvite(val inviteId: String) : TeamEvent
}

sealed interface TeamEffect {
    data class ShowToast(val message: String) : TeamEffect
    data object NavigateBack : TeamEffect
}
