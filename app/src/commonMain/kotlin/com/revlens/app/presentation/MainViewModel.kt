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
import com.revlens.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class MainViewModel(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val workspaceRepository: WorkspaceRepository = DefaultWorkspaceRepository(emptyList()),
    private val workspaceContext: WorkspaceContext = DefaultWorkspaceContext(null),
    private val sessionManager: SessionManager = DefaultSessionManager(),
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _immediateProfile = MutableStateFlow<BusinessProfile?>(null)
    private val _immediateOnboardingCompleted = MutableStateFlow<Boolean?>(null)

    val profile: StateFlow<BusinessProfile?> = combine(
        repository.profileFlow,
        _immediateProfile
    ) { repoProfile, immediate ->
        immediate ?: repoProfile
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val scenarios: StateFlow<List<Scenario>> = scenarioRepository.scenariosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isOnboardingCompleted: StateFlow<Boolean?> = combine(
        repository.isOnboardingCompletedFlow,
        _immediateOnboardingCompleted
    ) { repoCompleted, immediate ->
        immediate ?: repoCompleted
    }.stateIn(
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

    private val _currentUserRole = MutableStateFlow(Role.Owner)
    val currentUserRole: StateFlow<Role> = _currentUserRole.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                withTimeoutOrNull(1500L.milliseconds) {
                    repository.hasSeenIntroFlow.firstOrNull()
                }

                // 1. Initial check on app launch
                val initialUser = runCatching { authRepository?.getCurrentUser() }.getOrNull()
                if (initialUser != null) {
                    sessionManager.onLoggedIn(initialUser)
                } else {
                    sessionManager.signOut(wipeLocalSyncedData = false)
                    wipeWorkspaceAndProfileLocally()
                }

                // 2. Observe session state continuously for logins & logouts
                sessionManager.state.collect { state ->
                    when (state) {
                        is SessionState.Authenticated -> {
                            val localCompleted = withTimeoutOrNull(1000L.milliseconds) {
                                repository.isOnboardingCompletedFlow.firstOrNull()
                            } == true
                            val localProf = withTimeoutOrNull(1000L.milliseconds) {
                                repository.profileFlow.firstOrNull()
                            }

                            if (localCompleted && localProf != null) {
                                _immediateProfile.value = localProf
                                _immediateOnboardingCompleted.value = true
                                ensureActiveWorkspaceExists(localProf.currencyCode)
                                repository.setOnboardingCompleted(true)
                                viewModelScope.launch {
                                    runCatching { repository.syncRemoteProfile() }
                                }
                            } else {
                                // Set to null so the UI shows a loading state instead of bouncing to Onboarding
                                _immediateOnboardingCompleted.value = null

                                val remoteProfile = runCatching {
                                    withTimeoutOrNull(5000L.milliseconds) {
                                        repository.syncRemoteProfile()
                                    }
                                }.getOrNull()

                                if (remoteProfile != null) {
                                    _immediateProfile.value = remoteProfile
                                    _immediateOnboardingCompleted.value = true
                                    ensureActiveWorkspaceExists(remoteProfile.currencyCode)
                                    repository.setOnboardingCompleted(true)
                                } else {
                                    wipeWorkspaceAndProfileLocally()
                                }
                            }
                        }
                        is SessionState.SignedOut -> {
                            wipeWorkspaceAndProfileLocally()
                        }
                        is SessionState.Unknown -> {}
                    }
                    _isReady.value = true // Ensure UI unlocks after state processing
                }
            } catch (_: Exception) {
                _isReady.value = true
            }
        }
    }

    private suspend fun ensureActiveWorkspaceExists(currencyCode: String) {
        val existingList = workspaceRepository.workspacesFlow.firstOrNull().orEmpty()
        val currentId = workspaceContext.activeWorkspaceId.value
        if (existingList.isEmpty()) {
            val wsId = if (!currentId.isNullOrBlank() && currentId != "ws_default") {
                currentId
            } else {
                val uid = authRepository?.getCurrentUser()?.id
                if (!uid.isNullOrBlank()) "ws_$uid" else "ws_${getCurrentTimeMillis()}"
            }
            val ws = Workspace(
                id = wsId,
                name = "My Business",
                currency = currencyCode,
                createdAtMillis = getCurrentTimeMillis()
            )
            workspaceRepository.createWorkspace(ws)
            workspaceContext.switchTo(ws.id)
        } else if (currentId.isNullOrBlank() || currentId == "ws_default") {
            workspaceContext.switchTo(existingList.first().id)
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
        _immediateProfile.value = null
        _immediateOnboardingCompleted.value = false
        workspaceRepository.clearLocalData()
        workspaceContext.switchTo("")
        repository.clearLocalData()
        scenarioRepository.clearLocalData()
        repository.setOnboardingCompleted(false)
    }

    fun updateProfile(updatedProfile: BusinessProfile) {
        if (!RolePermissions.canEditBaselinesAndScenarios(currentUserRole.value)) return
        _immediateProfile.value = updatedProfile
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

    fun completeOnboarding(profile: BusinessProfile, onComplete: (() -> Unit)? = null) {
        // Set synchronous state immediately so NavGraph never sees an incomplete state on DashboardRoute
        _immediateProfile.value = profile
        _immediateOnboardingCompleted.value = true
        onComplete?.invoke()

        viewModelScope.launch {
            withContext(NonCancellable) {
                ensureActiveWorkspaceExists(profile.currencyCode)
                repository.saveProfile(profile)
                repository.setOnboardingCompleted(true)
                val currentWs = activeWorkspace.value
                if (currentWs != null && currentWs.currency != profile.currencyCode) {
                    val updatedWs = currentWs.copy(currency = profile.currencyCode)
                    workspaceRepository.updateWorkspace(updatedWs)
                }
            }
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