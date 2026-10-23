package com.revlens.app.domain.usecase

import com.revlens.app.core.util.IdGenerator
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.firstOrNull

class CreateWorkspaceUseCase(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceContext: WorkspaceContext,
    private val idGenerator: IdGenerator
) {
    suspend operator fun invoke(
        name: String,
        currency: String = "USD",
        businessModel: BusinessModel = BusinessModel.B2bSaas,
        timeZoneId: String = "UTC",
        fiscalYearStartMonth: Int = 1
    ): Workspace {
        // Reuse existing workspace ID if the user already created one and navigated back
        val activeId = workspaceContext.activeWorkspaceId.value
            ?.takeIf { it.isNotBlank() && it != "ws_default" }
        val existingWs = workspaceRepository.workspacesFlow.firstOrNull()
            ?.firstOrNull { it.id.isNotBlank() && it.id != "ws_default" }

        val targetId = activeId ?: existingWs?.id ?: idGenerator.generate("ws")
        val createdAt = existingWs?.createdAtMillis?.takeIf { it > 0L } ?: getCurrentTimeMillis()

        val workspace = Workspace(
            id = targetId,
            name = name.trim(),
            currency = currency,
            timeZoneId = timeZoneId,
            fiscalYearStartMonth = fiscalYearStartMonth,
            businessModel = businessModel,
            createdAtMillis = createdAt
        )
        workspaceRepository.createWorkspace(workspace)
        workspaceContext.switchTo(workspace.id)
        return workspace
    }
}
