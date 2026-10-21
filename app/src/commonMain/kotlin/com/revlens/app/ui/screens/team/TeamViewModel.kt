package com.revlens.app.ui.screens.team

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.session.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: TeamRepository,
    private val workspaceContext: WorkspaceContext,
    initialRole: Role = Role.Owner,
    private val sessionManager: SessionManager? = null,
    private val authRepository: AuthRepository? = null
) : MviViewModel<TeamUiState, TeamEvent, TeamEffect>(
    TeamUiState(userRole = initialRole)
) {

    init {
        observeWorkspaceTeam()
    }

    private fun observeWorkspaceTeam() {
        viewModelScope.launch {
            workspaceContext.activeWorkspaceId.collectLatest { rawWsId ->
                val wsId = rawWsId?.ifBlank { "ws_default" } ?: "ws_default"
                val code = try {
                    teamRepository.getWorkspaceJoinCode(wsId)
                } catch (_: Throwable) {
                    ""
                }
                update { copy(workspaceJoinCode = code) }

                combine(
                    teamRepository.getMembers(wsId),
                    teamRepository.getInvites(wsId)
                ) { members, invites ->
                    Pair(members, invites)
                }.collect { (members, invites) ->
                    val sessionUser =
                        (sessionManager?.state?.value as? SessionState.Authenticated)?.user
                    val authUser = sessionUser ?: try {
                        authRepository?.getCurrentUser()
                    } catch (_: Throwable) {
                        null
                    }

                    val uid = authUser?.id.orEmpty()
                    val email = authUser?.email?.trim()
                        ?.lowercase()
                        .orEmpty()

                    val matchedMember = if (uid.isNotBlank() || email.isNotBlank()) {
                        members.firstOrNull { member ->
                            (uid.isNotBlank() && member.id == uid) ||
                                    (email.isNotBlank() && member.email.equals(
                                        email, ignoreCase = true
                                    ))
                        }
                    } else {
                        null
                    }

                    val fallbackRole = if (email.isNotBlank()) {
                        teamRepository.getUserRoleInWorkspace(wsId, email)
                    } else {
                        null
                    }
                    val effectiveRole = matchedMember?.role ?: fallbackRole ?: state.value.userRole

                    update {
                        copy(
                            members = members,
                            invites = invites,
                            userRole = effectiveRole,
                            currentUserId = uid.ifBlank { matchedMember?.id.orEmpty() },
                            currentUserEmail = email.ifBlank { matchedMember?.email.orEmpty() }
                        )
                    }
                }
            }
        }
    }

    private fun isMemberSelf(member: Member): Boolean {
        val current = state.value
        val sameId = current.currentUserId.isNotBlank() && member.id == current.currentUserId
        val sameEmail = current.currentUserEmail.isNotBlank() &&
                member.email.equals(current.currentUserEmail, ignoreCase = true)
        return sameId || sameEmail
    }

    override fun onEvent(event: TeamEvent) {
        when (event) {
            is TeamEvent.SelectTab -> update { copy(activeTab = event.tab) }

            TeamEvent.OpenInviteSheet -> {
                if (!RolePermissions.canManageTeamAndInvites(state.value.userRole)) {
                    emit(TeamEffect.ShowToast("Only Owners and Admins can invite team members."))
                    return
                }
                update {
                    copy(
                        isInviteSheetVisible = true,
                        inviteEmail = "",
                        inviteRole = Role.Editor,
                        error = null
                    )
                }
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

            is TeamEvent.PromptRemoveMember -> {
                val current = state.value
                val isSelf = isMemberSelf(event.member)
                if (!RolePermissions.canRemoveMember(current.userRole, event.member.role, isSelf)) {
                    val msg = when {
                        isSelf -> "You cannot remove yourself from the team."
                        current.userRole != Role.Owner -> "Only the Workspace Owner can remove team members."
                        else -> "The Workspace Owner cannot be removed."
                    }
                    emit(TeamEffect.ShowToast(msg))
                    return
                }
                update { copy(memberToRemove = event.member) }
            }

            TeamEvent.DismissRemoveMember -> update { copy(memberToRemove = null) }

            TeamEvent.ConfirmRemoveMember -> confirmRemoveMember()

            is TeamEvent.RevokeInvite -> {
                if (!RolePermissions.canManageTeamAndInvites(state.value.userRole)) {
                    emit(TeamEffect.ShowToast("Permission denied"))
                    return
                }
                viewModelScope.launch {
                    val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
                    teamRepository.revokeInvite(wsId, event.inviteId)
                    emit(TeamEffect.ShowToast("Invitation revoked"))
                }
            }

            is TeamEvent.ResendInvite -> {
                if (!RolePermissions.canManageTeamAndInvites(state.value.userRole)) {
                    emit(TeamEffect.ShowToast("Permission denied"))
                    return
                }
                viewModelScope.launch {
                    val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
                    teamRepository.resendInvite(wsId, event.inviteId)
                    emit(TeamEffect.ShowToast("Invitation resent"))
                }
            }
        }
    }

    private fun submitInvite() {
        val current = state.value
        if (!RolePermissions.canManageTeamAndInvites(current.userRole)) {
            update { copy(error = "You do not have permission to invite members") }
            return
        }
        val email = current.inviteEmail.trim()
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            update { copy(error = "Please enter a valid email address") }
            return
        }
        if (current.currentUserEmail.isNotBlank() && email.equals(
                current.currentUserEmail, ignoreCase = true
            )
        ) {
            update { copy(error = "You are already a member of this workspace") }
            return
        }
        if (current.members.size >= current.totalSeats) {
            update { copy(error = "Seat limit reached for your current plan") }
            return
        }

        viewModelScope.launch {
            update { copy(isInviting = true, error = null) }
            val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
            val inviterName = current.members.firstOrNull { isMemberSelf(it) }?.name
                ?: current.members.firstOrNull { it.role == Role.Owner }?.name
                ?: "Workspace Owner"

            val res = teamRepository.inviteMember(wsId, email, current.inviteRole, inviterName)
            res.onSuccess {
                update {
                    copy(
                        isInviting = false,
                        isInviteSheetVisible = false,
                        inviteEmail = "",
                        error = null
                    )
                }
                emit(TeamEffect.ShowToast("Invitation sent to $email"))
            }
                .onFailure { err ->
                    update {
                        copy(
                            isInviting = false,
                            error = err.message ?: "Failed to send invitation"
                        )
                    }
                }
        }
    }

    private fun changeMemberRole(memberId: String, newRole: Role) {
        val current = state.value
        val target = current.members.firstOrNull { it.id == memberId } ?: return
        val isSelf = isMemberSelf(target)
        if (!RolePermissions.canChangeMemberRole(
                current.userRole, target.role, isSelf
            ) || newRole == Role.Owner
        ) {
            val reason = when {
                isSelf -> "You cannot change your own role."
                target.role == Role.Owner -> "The Workspace Owner's role cannot be modified."
                else -> "You do not have permission to change this member's role."
            }
            emit(TeamEffect.ShowToast(reason))
            return
        }
        viewModelScope.launch {
            val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
            teamRepository.updateMemberRole(wsId, memberId, newRole)
                .onSuccess {
                    emit(TeamEffect.ShowToast("${target.name}'s role updated to ${newRole.name}"))
                }
                .onFailure { err ->
                    emit(TeamEffect.ShowToast(err.message ?: "Failed to update role"))
                }
        }
    }

    private fun confirmRemoveMember() {
        val current = state.value
        val target = current.memberToRemove ?: return
        val isSelf = isMemberSelf(target)
        if (!RolePermissions.canRemoveMember(current.userRole, target.role, isSelf)) {
            update { copy(memberToRemove = null) }
            emit(TeamEffect.ShowToast("Only the Workspace Owner can remove team members."))
            return
        }
        viewModelScope.launch {
            val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
            teamRepository.removeMember(wsId, target.id)
                .onSuccess {
                    update { copy(memberToRemove = null) }
                    emit(TeamEffect.ShowToast("${target.name} removed from workspace"))
                }
                .onFailure { err ->
                    update { copy(memberToRemove = null) }
                    emit(TeamEffect.ShowToast(err.message ?: "Failed to remove member"))
                }
        }
    }
}
