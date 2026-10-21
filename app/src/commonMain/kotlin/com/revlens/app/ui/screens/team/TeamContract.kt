package com.revlens.app.ui.screens.team

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite

enum class TeamTab {
    Members,
    Invites,
    Permissions
}

data class TeamUiState(
    val members: List<Member> = emptyList(),
    val invites: List<TeamInvite> = emptyList(),
    val activeTab: TeamTab = TeamTab.Members,
    val userRole: Role = Role.Owner,
    val currentUserId: String = "",
    val currentUserEmail: String = "",
    val workspaceJoinCode: String = "",
    val totalSeats: Int = 5,
    val isInviteSheetVisible: Boolean = false,
    val inviteEmail: String = "",
    val inviteRole: Role = Role.Editor,
    val isInviting: Boolean = false,
    val memberToRemove: Member? = null,
    val isLoading: Boolean = false,
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
