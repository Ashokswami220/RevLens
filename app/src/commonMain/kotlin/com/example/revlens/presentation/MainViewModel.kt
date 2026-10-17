package com.example.revlens.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revlens.data.DefaultWorkspaceRepository
import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.domain.context.DefaultWorkspaceContext
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.model.BusinessModel
import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.model.Workspace
import com.example.revlens.domain.repository.WorkspaceRepository
import com.example.revlens.domain.session.DefaultSessionManager
import com.example.revlens.domain.session.SessionManager
import com.example.revlens.domain.usecase.ResolveGateUseCase
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Scenario
import com.example.revlens.ui.navigation.Gate
import com.example.revlens.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository,
    private val workspaceRepository: WorkspaceRepository = DefaultWorkspaceRepository(),
    private val workspaceContext: WorkspaceContext = DefaultWorkspaceContext(),
    private val sessionManager: SessionManager = DefaultSessionManager(),
    private val resolveGateUseCase: ResolveGateUseCase = ResolveGateUseCase()
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

    fun updateProfile(updatedProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
        }
    }

    fun completeOnboarding(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            repository.setOnboardingCompleted(true)
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
