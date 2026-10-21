package com.revlens.app.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.data.ProfileRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.session.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class InviteAcceptanceViewModel(
    private val teamRepository: TeamRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository? = null
) : MviViewModel<InviteAcceptanceUiState, InviteAcceptanceEvent, InviteAcceptanceEffect>(
    InviteAcceptanceUiState()
) {

    init {
        loadInvites()
    }

    private fun loadInvites() {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            val currentUser = getCurrentUser()
            val email = currentUser?.email.orEmpty()
            update { copy(userEmail = email) }
            if (email.isNotBlank()) {
                val list = teamRepository.getPendingInvitesForEmail(email)
                update { copy(invites = list, isLoading = false) }
                if (list.isEmpty()) {
                    emit(InviteAcceptanceEffect.NavigateToCreateWorkspace)
                }
            } else {
                update { copy(isLoading = false) }
            }
        }
    }

    private suspend fun getCurrentUser(): UserAccount? {
        val sessionUser = (sessionManager.state.value as? SessionState.Authenticated)?.user
        if (sessionUser != null) return sessionUser
        return authRepository?.getCurrentUser()
    }

    override fun onEvent(event: InviteAcceptanceEvent) {
        when (event) {
            is InviteAcceptanceEvent.AcceptInvite -> accept(event.inviteId)
            is InviteAcceptanceEvent.DeclineInvite -> decline(event.inviteId)
        }
    }

    private fun accept(inviteId: String) {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            val user = getCurrentUser() ?: run {
                update { copy(isLoading = false) }
                emit(InviteAcceptanceEffect.ShowMessage(UiText.DynamicString("User session not found")))
                return@launch
            }

            val result = teamRepository.acceptInvite(inviteId, user)
            result.onSuccess { targetWorkspaceId ->
                workspaceContext.switchTo(targetWorkspaceId)
                // Synchronize existing business profile from workspace so the member accesses the SAME profile
                try {
                    val wsProfile = teamRepository.getWorkspaceProfile(targetWorkspaceId)
                    if (wsProfile != null) {
                        profileRepository.saveProfile(wsProfile)
                    } else {
                        val remote = profileRepository.syncRemoteProfile()
                        if (remote == null) {
                            val current = profileRepository.profileFlow.firstOrNull()
                            if (current != null) {
                                teamRepository.saveWorkspaceProfile(targetWorkspaceId, current)
                            }
                        }
                    }
                } catch (_: Exception) {
                }
                profileRepository.setOnboardingCompleted(true)
                update { copy(isLoading = false) }
                emit(InviteAcceptanceEffect.ShowMessage(UiText.DynamicString("Welcome to the workspace!")))
                emit(InviteAcceptanceEffect.NavigateToDashboard)
            }.onFailure { err ->
                update { copy(isLoading = false, error = UiText.DynamicString(err.message ?: "Failed to accept invite")) }
                emit(InviteAcceptanceEffect.ShowMessage(UiText.DynamicString(err.message ?: "Failed to accept invite")))
            }
        }
    }

    private fun decline(inviteId: String) {
        viewModelScope.launch {
            update { copy(isLoading = true) }
            val email = state.value.userEmail
            teamRepository.declineInvite(inviteId, email)
            val remaining = teamRepository.getPendingInvitesForEmail(email)
            update { copy(invites = remaining, isLoading = false) }
            emit(InviteAcceptanceEffect.ShowMessage(UiText.DynamicString("Invitation declined")))
            if (remaining.isEmpty()) {
                emit(InviteAcceptanceEffect.NavigateToCreateWorkspace)
            }
        }
    }
}
