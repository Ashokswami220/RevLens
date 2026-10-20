package com.revlens.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.domain.usecase.ResolveGateUseCase
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import com.revlens.app.ui.navigation.Gate
import com.revlens.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.revlens.app.domain.model.Role
import com.revlens.app.domain.repository.TeamRepository

class MainViewModel(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val workspaceRepository: WorkspaceRepository = DefaultWorkspaceRepository(),
    private val workspaceContext: WorkspaceContext = DefaultWorkspaceContext(),
    private val sessionManager: SessionManager = DefaultSessionManager(),
    private val resolveGateUseCase: ResolveGateUseCase = ResolveGateUseCase(),
    private val authRepository: com.revlens.app.domain.repository.AuthRepository? = null,
    private val teamRepository: TeamRepository? = null
) : ViewModel() {

    init {
        viewModelScope.launch {
            val user = authRepository?.getCurrentUser()
            if (user != null) {
                sessionManager.onLoggedIn(user)
            }
        }
    }

    val profile: StateFlow<BusinessProfile?> = repository.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val scenarios: StateFlow<List<Scenario>> = scenarioRepository.scenariosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isOnboardingCompleted: StateFlow<Boolean?> = repository.isOnboardingCompletedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val hasSeenIntro: StateFlow<Boolean> = repository.hasSeenIntroFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun setIntroSeen() {
        viewModelScope.launch {
            repository.setHasSeenIntro(true)
        }
    }

    val activeWorkspace: StateFlow<Workspace?> = combine(
        workspaceContext.activeWorkspaceId,
        workspaceRepository.workspacesFlow
    ) { activeId, workspaces ->
        workspaces.firstOrNull { it.id == activeId } ?: workspaces.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val session: StateFlow<SessionState> = sessionManager.state

    val gate: StateFlow<Gate> = combine(
        sessionManager.state,
        activeWorkspace,
        profile
    ) { currentSession, ws, prof ->
        resolveGateUseCase(
            session = currentSession,
            workspace = ws,
            baseline = prof,
            isWeb = false,
            introSeen = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Gate.Main
    )

    val currentUserRole: StateFlow<Role> = combine(
        activeWorkspace,
        sessionManager.state
    ) { ws, sess ->
        val email = (sess as? SessionState.Authenticated)?.user?.email
            ?: authRepository?.getCurrentUser()?.email
        if (ws != null && !email.isNullOrBlank() && teamRepository != null) {
            teamRepository.getUserRoleInWorkspace(ws.id, email) ?: Role.Owner
        } else {
            Role.Owner
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Role.Owner
    )

    fun leaveBusiness(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val wsId = activeWorkspace.value?.id ?: run {
            onError("No active workspace found")
            return
        }
        viewModelScope.launch {
            val email = (sessionManager.state.value as? SessionState.Authenticated)?.user?.email
                ?: authRepository?.getCurrentUser()?.email
            if (email.isNullOrBlank() || teamRepository == null) {
                onError("Could not verify user account")
                return@launch
            }
            val result = teamRepository.leaveWorkspace(wsId, email)
            result.onSuccess {
                workspaceContext.switchTo("")
                repository.setOnboardingCompleted(false)
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to leave business")
            }
        }
    }

    fun updateProfile(updatedProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
            val currentWs = activeWorkspace.value
            if (currentWs != null && currentWs.currency != updatedProfile.currencyCode) {
                workspaceRepository.updateWorkspace(
                    currentWs.copy(currency = updatedProfile.currencyCode)
                )
            }
        }
    }

    fun completeOnboarding(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            val currentWs = activeWorkspace.value
            if (currentWs != null) {
                val updatedWs = currentWs.copy(currency = profile.currencyCode)
                workspaceRepository.updateWorkspace(updatedWs)
            }
            repository.setOnboardingCompleted(true)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(false)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository?.logout()
            } catch (_: Exception) {
            }
            sessionManager.signOut(wipeLocalSyncedData = false)
            repository.setOnboardingCompleted(false)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                authRepository?.deleteAccount()
            } catch (_: Exception) {
            }
            sessionManager.signOut(wipeLocalSyncedData = true)
            repository.setOnboardingCompleted(false)
        }
    }

    fun saveScenario(scenario: Scenario) {
        viewModelScope.launch {
            scenarioRepository.saveScenario(scenario)
        }
    }

    fun deleteScenario(scenarioId: String) {
        viewModelScope.launch {
            scenarioRepository.deleteScenario(scenarioId)
        }
    }

    fun switchWorkspace(workspaceId: String) {
        viewModelScope.launch {
            workspaceContext.switchTo(workspaceId)
        }
    }

    fun createWorkspace(
        name: String,
        currency: String = "USD",
        businessModel: BusinessModel = BusinessModel.B2bSaas,
        timeZoneId: String = "UTC",
        fiscalYearStartMonth: Int = 1
    ) {
        viewModelScope.launch {
            val newWorkspace = Workspace(
                id = "ws_${getCurrentTimeMillis()}",
                name = name,
                currency = currency,
                timeZoneId = timeZoneId,
                fiscalYearStartMonth = fiscalYearStartMonth,
                businessModel = businessModel,
                createdAtMillis = getCurrentTimeMillis()
            )
            workspaceRepository.createWorkspace(newWorkspace)
            workspaceContext.switchTo(newWorkspace.id)
        }
    }
}
