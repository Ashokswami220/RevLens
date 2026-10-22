package com.revlens.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val workspaceRepository: WorkspaceRepository = DefaultWorkspaceRepository(emptyList()),
    private val workspaceContext: WorkspaceContext = DefaultWorkspaceContext(null),
    private val sessionManager: SessionManager = DefaultSessionManager(),
    private val authRepository: AuthRepository? = null
) : ViewModel() {

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

    val hasSeenIntro: StateFlow<Boolean?> = repository.hasSeenIntroFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _accessRevokedMessage = MutableStateFlow<String?>(null)
    val accessRevokedMessage: StateFlow<String?> = _accessRevokedMessage.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

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

    val currentUserRole: StateFlow<Role> = MutableStateFlow(Role.Owner).asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val user = authRepository?.getCurrentUser()
                if (user != null) {
                    sessionManager.onLoggedIn(user)
                    val remoteProfile = runCatching { repository.syncRemoteProfile() }.getOrNull()
                    if (remoteProfile == null) {
                        // Do not aggressively wipe the workspace. Just set onboarding to false.
                        repository.setOnboardingCompleted(false)
                    }
                } else {
                    // Do not aggressively wipe local data here. Firebase might just be slow to wake up.
                    sessionManager.signOut(wipeLocalSyncedData = false)
                }
            } catch (_: Exception) {
            } finally {
                _isReady.value = true
            }
        }
        viewModelScope.launch {
            runCatching {
                activeWorkspace.collectLatest { ws ->
                    if (ws != null && ws.id != "ws_default") {
                        if (workspaceContext.activeWorkspaceId.value != ws.id) {
                            workspaceContext.switchTo(ws.id)
                        }
                    }
                }
            }
        }
    }

    fun acknowledgeAccessRevoked(onNavigateToWorkspaceSetup: () -> Unit = {}) {
        viewModelScope.launch {
            _accessRevokedMessage.value = null
            wipeWorkspaceAndProfileLocally()
            onNavigateToWorkspaceSetup()
        }
    }

    private suspend fun wipeWorkspaceAndProfileLocally() {
        // Cache the intro state before clearing local data
        val wasIntroSeen = hasSeenIntro.value ?: false

        workspaceContext.switchTo("")
        repository.setOnboardingCompleted(false)

        if (wasIntroSeen) {
            repository.setHasSeenIntro(true)
        }
    }

    fun updateProfile(updatedProfile: BusinessProfile) {
        if (!RolePermissions.canEditBaselinesAndScenarios(currentUserRole.value)) return
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
            val currentWs = activeWorkspace.value
            if (currentWs != null) {
                if (currentWs.currency != updatedProfile.currencyCode) {
                    workspaceRepository.updateWorkspace(
                        currentWs.copy(currency = updatedProfile.currencyCode)
                    )
                }
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

    fun logout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    authRepository?.logout()
                } catch (_: Exception) {
                }
                wipeWorkspaceAndProfileLocally()
                sessionManager.signOut(wipeLocalSyncedData = true)
            }
            onComplete?.invoke()
        }
    }

    fun deleteAccount(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    authRepository?.deleteAccount()
                } catch (_: Exception) {
                }
                wipeWorkspaceAndProfileLocally()
                sessionManager.signOut(wipeLocalSyncedData = true)
            }
            onComplete?.invoke()
        }
    }

    fun saveScenario(scenario: Scenario) {
        if (!RolePermissions.canEditBaselinesAndScenarios(currentUserRole.value)) return
        viewModelScope.launch {
            scenarioRepository.saveScenario(scenario)
        }
    }

    fun deleteScenario(scenarioId: String) {
        if (!RolePermissions.canEditBaselinesAndScenarios(currentUserRole.value)) return
        viewModelScope.launch {
            scenarioRepository.deleteScenario(scenarioId)
        }
    }
}