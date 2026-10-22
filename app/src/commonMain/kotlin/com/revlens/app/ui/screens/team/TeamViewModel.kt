package com.revlens.app.ui.screens.team

import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import kotlin.random.Random

class TeamViewModel : MviViewModel<TeamUiState, TeamEvent, TeamEffect>(
    TeamUiState(
        userRole = Role.Owner,
        currentUserId = "mem_1",
        currentUserEmail = "alex@revlens.io",
        workspaceJoinCode = "REV-7749",
        totalSeats = 5,
        members = listOf(
            Member(
                id = "mem_1",
                name = "Alex Rivera (You)",
                email = "alex@revlens.io",
                role = Role.Owner,
                joinedAt = "Workspace Creator"
            ),
            Member(
                id = "mem_2",
                name = "Sarah Chen",
                email = "sarah.chen@company.com",
                role = Role.Admin,
                joinedAt = "Joined 2 months ago"
            ),
            Member(
                id = "mem_3",
                name = "Marcus Vance",
                email = "marcus.vance@company.com",
                role = Role.Editor,
                joinedAt = "Joined 3 weeks ago"
            )
        ),
        invites = listOf(
            TeamInvite(
                id = "inv_1",
                email = "elena.rostova@contractor.io",
                role = Role.Viewer,
                sentAt = "2 days ago",
                invitedBy = "Alex Rivera"
            )
        )
    )
) {

    override fun onEvent(event: TeamEvent) {
        when (event) {
            is TeamEvent.SelectTab -> update { copy(activeTab = event.tab) }

            TeamEvent.OpenInviteSheet -> update {
                copy(
                    isInviteSheetVisible = true,
                    inviteEmail = "",
                    inviteRole = Role.Editor,
                    error = null
                )
            }

            TeamEvent.DismissInviteSheet -> update {
                copy(isInviteSheetVisible = false, error = null)
            }

            is TeamEvent.InviteEmailChanged -> update {
                copy(inviteEmail = event.email, error = null)
            }

            is TeamEvent.InviteRoleChanged -> {
                if (event.role != Role.Owner) {
                    update { copy(inviteRole = event.role, error = null) }
                }
            }

            TeamEvent.SubmitInvite -> submitInvite()

            is TeamEvent.ChangeMemberRole -> changeMemberRole(event.memberId, event.newRole)

            is TeamEvent.PromptRemoveMember -> update { copy(memberToRemove = event.member) }

            TeamEvent.DismissRemoveMember -> update { copy(memberToRemove = null) }

            TeamEvent.ConfirmRemoveMember -> confirmRemoveMember()

            is TeamEvent.RevokeInvite -> {
                update { copy(invites = invites.filterNot { it.id == event.inviteId }) }
                emit(TeamEffect.ShowToast("Invitation revoked"))
            }

            is TeamEvent.ResendInvite -> {
                emit(TeamEffect.ShowToast("Invitation resent"))
            }
        }
    }

    private fun submitInvite() {
        val current = state.value
        val email = current.inviteEmail.trim()
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            update { copy(error = "Please enter a valid email address") }
            return
        }
        if (current.currentUserEmail.isNotBlank() && email.equals(current.currentUserEmail, ignoreCase = true)) {
            update { copy(error = "You are already a member of this workspace") }
            return
        }
        if (current.members.size >= current.totalSeats) {
            update { copy(error = "Seat limit reached for your current plan") }
            return
        }

        val newInvite = TeamInvite(
            id = "inv_${Random.nextInt(1000, 9999)}",
            email = email,
            role = current.inviteRole,
            sentAt = "Just now",
            invitedBy = "You"
        )
        update {
            copy(
                invites = invites + newInvite,
                isInviteSheetVisible = false,
                inviteEmail = "",
                error = null
            )
        }
        emit(TeamEffect.ShowToast("Invitation sent to $email"))
    }

    private fun changeMemberRole(memberId: String, newRole: Role) {
        val current = state.value
        val target = current.members.firstOrNull { it.id == memberId } ?: return
        if (target.id == current.currentUserId) {
            emit(TeamEffect.ShowToast("You cannot change your own role."))
            return
        }
        if (target.role == Role.Owner || newRole == Role.Owner) {
            emit(TeamEffect.ShowToast("Workspace Owner role cannot be modified."))
            return
        }

        update {
            copy(
                members = members.map {
                    if (it.id == memberId) it.copy(role = newRole) else it
                }
            )
        }
        emit(TeamEffect.ShowToast("${target.name}'s role updated to ${newRole.name}"))
    }

    private fun confirmRemoveMember() {
        val current = state.value
        val target = current.memberToRemove ?: return
        if (target.id == current.currentUserId) {
            update { copy(memberToRemove = null) }
            emit(TeamEffect.ShowToast("You cannot remove yourself from the team."))
            return
        }
        if (target.role == Role.Owner) {
            update { copy(memberToRemove = null) }
            emit(TeamEffect.ShowToast("The Workspace Owner cannot be removed."))
            return
        }

        update {
            copy(
                members = members.filterNot { it.id == target.id },
                memberToRemove = null
            )
        }
        emit(TeamEffect.ShowToast("${target.name} removed from workspace"))
    }
}
