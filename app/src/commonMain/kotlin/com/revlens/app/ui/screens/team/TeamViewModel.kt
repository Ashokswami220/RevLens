package com.revlens.app.ui.screens.team

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.repository.TeamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: TeamRepository,
    private val workspaceContext: WorkspaceContext,
    private val authRepository: com.revlens.app.domain.repository.AuthRepository? = null,
    private val sessionManager: com.revlens.app.domain.session.SessionManager? = null
) : MviViewModel<TeamUiState, TeamEvent, TeamEffect>(TeamUiState()) {

    private var currentWorkspaceId: String = "ws_default"
    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            workspaceContext.activeWorkspaceId.collectLatest { wsId ->
                val id = wsId ?: "ws_default"
                currentWorkspaceId = id
                startObservingWorkspace(id)
            }
        }
    }

    private fun startObservingWorkspace(wsId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            launch {
                teamRepository.getMembers(wsId)
                    .collect { list ->
                        update {
                            copy(
                                members = list,
                                usedSeats = list.size
                            )
                        }
                    }
            }
            launch {
                teamRepository.getInvites(wsId)
                    .collect { invites ->
                        update { copy(invites = invites) }
                    }
            }
            val userEmail = (sessionManager?.state?.value as? com.revlens.app.domain.model.SessionState.Authenticated)?.user?.email
                ?: authRepository?.getCurrentUser()?.email
            if (userEmail != null) {
                val role = teamRepository.getUserRoleInWorkspace(wsId, userEmail) ?: com.revlens.app.domain.model.Role.Owner
                update { copy(userRole = role) }
            }
        }
    }

    override fun onEvent(event: TeamEvent) {
        when (event) {
            is TeamEvent.SelectTab -> update { copy(activeTab = event.tab) }
            TeamEvent.OpenInviteSheet -> update {
                copy(
                    isInviteSheetVisible = true,
                    inviteEmail = "",
                    error = null
                )
            }

            TeamEvent.DismissInviteSheet -> update {
                copy(
                    isInviteSheetVisible = false,
                    error = null
                )
            }

            is TeamEvent.InviteEmailChanged -> update {
                copy(
                    inviteEmail = event.email, error = null
                )
            }

            is TeamEvent.InviteRoleChanged -> update { copy(inviteRole = event.role) }
            TeamEvent.SubmitInvite -> submitInvite()
            is TeamEvent.ChangeMemberRole -> changeRole(event.memberId, event.newRole)
            is TeamEvent.PromptRemoveMember -> update { copy(memberToRemove = event.member) }
            TeamEvent.DismissRemoveMember -> update { copy(memberToRemove = null) }
            TeamEvent.ConfirmRemoveMember -> confirmRemoveMember()
            is TeamEvent.RevokeInvite -> revokeInvite(event.inviteId)
            is TeamEvent.ResendInvite -> resendInvite(event.inviteId)
        }
    }

    private fun submitInvite() {
        val email = state.value.inviteEmail.trim()
        if (email.isBlank() || !email.contains("@")) {
            update { copy(error = "Please enter a valid email address.") }
            return
        }

        val totalUsed = state.value.members.size + state.value.invites.size
        if (totalUsed >= state.value.totalSeats) {
            update {
                copy(
                    error = "Seat limit reached (5 of 5). Upgrade plan to invite more members."
                )
            }
            return
        }

        viewModelScope.launch {
            update { copy(isInviting = true) }
            val inviter = (sessionManager?.state?.value as? com.revlens.app.domain.model.SessionState.Authenticated)?.user?.name
                ?: authRepository?.getCurrentUser()?.name
                ?: "Owner"
            val res = teamRepository.inviteMember(currentWorkspaceId, email, state.value.inviteRole, inviterName = inviter)
            update { copy(isInviting = false) }

            res.onSuccess {
                update { copy(isInviteSheetVisible = false, inviteEmail = "") }
                emit(TeamEffect.ShowToast("Invitation sent to $email"))
            }
                .onFailure { err ->
                    update { copy(error = err.message ?: "Failed to send invitation.") }
                }
        }
    }

    private fun changeRole(memberId: String, newRole: com.revlens.app.domain.model.Role) {
        viewModelScope.launch {
            val res = teamRepository.updateMemberRole(currentWorkspaceId, memberId, newRole)
            res.onSuccess {
                emit(TeamEffect.ShowToast("Member role updated to ${newRole.name}"))
            }
                .onFailure { err ->
                    emit(TeamEffect.ShowToast(err.message ?: "Could not update role"))
                }
        }
    }

    private fun confirmRemoveMember() {
        val member = state.value.memberToRemove ?: return
        viewModelScope.launch {
            val res = teamRepository.removeMember(currentWorkspaceId, member.id)
            update { copy(memberToRemove = null) }
            res.onSuccess {
                emit(TeamEffect.ShowToast("${member.name} removed from workspace"))
            }
                .onFailure { err ->
                    emit(TeamEffect.ShowToast(err.message ?: "Could not remove member"))
                }
        }
    }

    private fun revokeInvite(inviteId: String) {
        viewModelScope.launch {
            teamRepository.revokeInvite(currentWorkspaceId, inviteId)
            emit(TeamEffect.ShowToast("Invitation revoked"))
        }
    }

    private fun resendInvite(inviteId: String) {
        viewModelScope.launch {
            teamRepository.resendInvite(currentWorkspaceId, inviteId)
            emit(TeamEffect.ShowToast("Invitation email resent"))
        }
    }
}
