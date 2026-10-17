package com.example.revlens.domain.usecase

import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.model.Workspace
import com.example.revlens.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.first

class MigrateGuestDataUseCase(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext,
    private val profileRepository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository
) {
    suspend operator fun invoke(userId: String) {
        val activeWsId = workspaceContext.activeWorkspaceId.value
        val workspaces = workspaceRepository.workspacesFlow.first()

        // If no workspace exists yet for this user, create or ensure active workspace
        if (workspaces.isEmpty()) {
            val defaultWs = Workspace(
                id = activeWsId ?: "ws_${userId.takeLast(6)}",
                name = "My SaaS Workspace"
            )
            workspaceRepository.createWorkspace(defaultWs)
            workspaceContext.switchTo(defaultWs.id)
        }
    }
}
