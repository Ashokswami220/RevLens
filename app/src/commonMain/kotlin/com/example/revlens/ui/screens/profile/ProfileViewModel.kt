package com.example.revlens.ui.screens.profile

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.repository.AuthRepository
import com.example.revlens.domain.repository.WorkspaceRepository
import com.example.revlens.domain.session.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<ProfileUiState, ProfileEvent, ProfileEffect>(ProfileUiState()) {

    private var initialName: String = ""
    private var initialJobTitle: String = ""
    private var initialTimezone: String = "UTC"
    private var initialCurrency: String = "USD"

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val session = sessionManager.state.value
        val isGuest = session !is SessionState.Authenticated

        val (name, email, avatarUrl, jobTitle) = when (session) {
            is SessionState.Authenticated -> {
                val u = session.user
                Quad(u.name, u.email, u.avatarUrl, u.jobTitle ?: "")
            }
            else -> Quad("Guest User", "local-guest@revlens.internal", null, "")
        }

        initialName = name
        initialJobTitle = jobTitle

        update {
            copy(
                name = name,
                email = email,
                avatarUrl = avatarUrl,
                jobTitle = jobTitle,
                isGuest = isGuest,
                hasChanges = false
            )
        }

        viewModelScope.launch {
            val wsId = workspaceContext.activeWorkspaceId.value
            if (wsId != null) {
                val ws = workspaceRepository.getWorkspace(wsId)
                if (ws != null) {
                    initialTimezone = ws.timeZoneId
                    initialCurrency = ws.currency
                    update {
                        copy(
                            timezone = ws.timeZoneId,
                            currency = ws.currency,
                            hasChanges = calculateHasChanges(name, jobTitle, ws.timeZoneId, ws.currency)
                        )
                    }
                }
            }
        }
    }

    private fun calculateHasChanges(
        name: String,
        jobTitle: String,
        timezone: String,
        currency: String
    ): Boolean {
        return name.trim() != initialName.trim() ||
            jobTitle.trim() != initialJobTitle.trim() ||
            timezone != initialTimezone ||
            currency != initialCurrency
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NameChanged -> {
                update {
                    copy(
                        name = event.name,
                        hasChanges = calculateHasChanges(event.name, jobTitle, timezone, currency)
                    )
                }
            }
            is ProfileEvent.JobTitleChanged -> {
                update {
                    copy(
                        jobTitle = event.jobTitle,
                        hasChanges = calculateHasChanges(name, event.jobTitle, timezone, currency)
                    )
                }
            }
            is ProfileEvent.TimezoneChanged -> {
                update {
                    copy(
                        timezone = event.timezone,
                        hasChanges = calculateHasChanges(name, jobTitle, event.timezone, currency)
                    )
                }
            }
            is ProfileEvent.CurrencyChanged -> {
                update {
                    copy(
                        currency = event.currency,
                        hasChanges = calculateHasChanges(name, jobTitle, timezone, event.currency)
                    )
                }
            }
            ProfileEvent.SaveChanges -> saveChanges()
            ProfileEvent.OpenDeleteDialog -> update { copy(showDeleteDialog = true, deleteError = null) }
            ProfileEvent.DismissDeleteDialog -> update { copy(showDeleteDialog = false, deleteError = null) }
            is ProfileEvent.ConfirmDeleteAccount -> confirmDelete(event.confirmationWord)
        }
    }

    private fun saveChanges() {
        val s = state.value
        if (s.name.isBlank()) {
            emit(ProfileEffect.ShowToast("Name cannot be empty"))
            return
        }

        viewModelScope.launch {
            update { copy(isSaving = true) }

            if (!s.isGuest) {
                val authRes = authRepository.updateProfile(s.name.trim(), s.jobTitle.trim().ifEmpty { null })
                authRes.onSuccess { updatedUser ->
                    sessionManager.onLoggedIn(updatedUser)
                }
            }

            val wsId = workspaceContext.activeWorkspaceId.value
            if (wsId != null) {
                val ws = workspaceRepository.getWorkspace(wsId)
                if (ws != null) {
                    val updatedWs = ws.copy(
                        currency = s.currency,
                        timeZoneId = s.timezone
                    )
                    workspaceRepository.updateWorkspace(updatedWs)
                }
            }

            initialName = s.name.trim()
            initialJobTitle = s.jobTitle.trim()
            initialTimezone = s.timezone
            initialCurrency = s.currency

            update { copy(isSaving = false, hasChanges = false) }
            emit(ProfileEffect.ShowToast("Profile changes saved"))
        }
    }

    private fun confirmDelete(confirmationWord: String) {
        if (confirmationWord.trim().uppercase() != "DELETE") {
            update { copy(deleteError = "Please type DELETE to confirm.") }
            return
        }

        viewModelScope.launch {
            update { copy(isDeleting = true, showDeleteDialog = false) }
            authRepository.deleteAccount()
            sessionManager.signOut(wipeLocalSyncedData = true)
            update { copy(isDeleting = false) }
            emit(ProfileEffect.AccountDeleted)
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
