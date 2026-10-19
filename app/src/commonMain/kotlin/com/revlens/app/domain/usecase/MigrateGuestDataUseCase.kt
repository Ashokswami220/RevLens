package com.revlens.app.domain.usecase

import com.revlens.app.data.ProfileRepository
import com.revlens.app.data.ScenarioRepository
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class MigrateGuestDataUseCase(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository
) {
    suspend operator fun invoke(userId: String) {
        val activeWsId = workspaceContext.activeWorkspaceId.value
        val workspaces = withTimeoutOrNull(2000L) {
            workspaceRepository.workspacesFlow.first()
        } ?: emptyList()

        // If no workspace exists yet for this user, create or ensure active workspace
        if (workspaces.isEmpty()) {
            val defaultWs = Workspace(
                id = activeWsId ?: "ws_${userId.takeLast(6)}",
                name = "My SaaS Workspace"
            )
            workspaceRepository.createWorkspace(defaultWs)
            workspaceContext.switchTo(defaultWs.id)
        }

        // Migrate any profile or scenarios created during guest session
        val guestProfile = withTimeoutOrNull(1500L) {
            profileRepository.profileFlow.first()
        }
        if (guestProfile != null) {
            profileRepository.saveProfile(guestProfile)
        }

        val guestScenarios = withTimeoutOrNull(1500L) {
            scenarioRepository.scenariosFlow.first()
        } ?: emptyList()
        for (scenario in guestScenarios) {
            scenarioRepository.saveScenario(scenario)
        }
    }
}
