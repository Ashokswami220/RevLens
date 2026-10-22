package com.revlens.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revlens.app.data.DefaultWorkspaceRepository
import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.DefaultWorkspaceContext
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.MembershipCheckResult
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.domain.session.DefaultSessionManager
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import com.revlens.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class MainViewModel(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val workspaceRepository: WorkspaceRepository = DefaultWorkspaceRepository(emptyList()),
    private val workspaceContext: WorkspaceContext = DefaultWorkspaceContext(null),
    private val sessionManager: SessionManager = DefaultSessionManager(),
    private val authRepository: AuthRepository? = null,
    private val teamRepository: TeamRepository? = null
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserRole: StateFlow<Role> = combine(
        activeWorkspace,
        sessionManager.state
    ) { ws, sess ->
        Pair(ws, sess)
    }.flatMapLatest { (ws, sess) ->
        val teamRepo = teamRepository
        if (ws == null || teamRepo == null) {
            flowOf(Role.Owner)
        } else {
            teamRepo.getMembers(ws.id).map { members ->
                val user = (sess as? SessionState.Authenticated)?.user
                    ?: authRepository?.getCurrentUser()
                val email = user?.email?.trim()?.lowercase()
                val uid = user?.id
                if (email.isNullOrBlank() && uid.isNullOrBlank()) {
                    Role.Owner
                } else {
                    val matched = members.firstOrNull { m ->
                        (!uid.isNullOrBlank() && m.id == uid) ||
                            (!email.isNullOrBlank() && m.email.equals(email, ignoreCase = true))
                    }
                    matched?.role ?: (teamRepo.getUserRoleInWorkspace(ws.id, email.orEmpty()) ?: Role.Owner)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Role.Owner
    )

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
                        val sharedProfile = runCatching {
                            teamRepository?.getWorkspaceProfile(ws.id)
                        }.getOrNull()
                        if (sharedProfile != null) {
                            repository.saveProfile(sharedProfile)
                            repository.setOnboardingCompleted(true)
                        }
                    }
                }
            }
        }
        observeRealtimeMembershipRemoval()
        startMembershipPolling()
    }

    private fun observeRealtimeMembershipRemoval() {
        val teamRepo = teamRepository ?: return
        viewModelScope.launch {
            activeWorkspace.collectLatest { ws ->
                if (ws == null || ws.id == "ws_default") return@collectLatest
                var wasConfirmedMember = false
                teamRepo.getMembers(ws.id).collect { members ->
                    val user = (sessionManager.state.value as? SessionState.Authenticated)?.user
                        ?: authRepository?.getCurrentUser()
                    if (user != null && members.isNotEmpty()) {
                        val isMember = members.any { m ->
                            (user.id.isNotBlank() && m.id == user.id) ||
                                (user.email.isNotBlank() && m.email.equals(user.email, ignoreCase = true))
                        }
                        if (isMember) {
                            wasConfirmedMember = true
                        } else if (wasConfirmedMember) {
                            triggerAccessRevoked()
                        }
                    }
                }
            }
        }
    }

    private fun startMembershipPolling() {
        val teamRepo = teamRepository ?: return
        viewModelScope.launch {
            while (isActive) {
                delay(6_000L.milliseconds)
                val ws = activeWorkspace.value
                val user = (sessionManager.state.value as? SessionState.Authenticated)?.user
                    ?: authRepository?.getCurrentUser()
                if (ws != null && ws.id != "ws_default" && user != null && _accessRevokedMessage.value == null) {
                    val status = teamRepo.checkMembershipStatus(ws.id, user.id, user.email)
                    if (status == MembershipCheckResult.REVOKED) {
                        triggerAccessRevoked()
                    }
                }
            }
        }
    }

    private fun triggerAccessRevoked() {
        if (_accessRevokedMessage.value == null) {
            _accessRevokedMessage.value =
                "The workspace owner has removed your access to this workspace. Tap OK to go back to workspace setup, where you can create your own business or join another team."
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
                wipeWorkspaceAndProfileLocally()
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to leave business")
            }
        }
    }

    fun updateProfile(updatedProfile: BusinessProfile) {
        if (!RolePermissions.canEditBaselinesAndScenarios(currentUserRole.value)) return
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
            val currentWs = activeWorkspace.value
            if (currentWs != null) {
                teamRepository?.saveWorkspaceProfile(currentWs.id, updatedProfile)
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
                teamRepository?.saveWorkspaceProfile(currentWs.id, profile)
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

    fun createWorkspace(
        name: String,
        currency: String = "USD",
        businessModel: BusinessModel = BusinessModel.B2bSaas,
        timeZoneId: String = "UTC",
        fiscalYearStartMonth: Int = 1
    ) {
        viewModelScope.launch {
            val allWs = workspaceRepository.workspacesFlow.firstOrNull().orEmpty()
            allWs.forEach { ws -> workspaceRepository.deleteWorkspace(ws.id) }

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