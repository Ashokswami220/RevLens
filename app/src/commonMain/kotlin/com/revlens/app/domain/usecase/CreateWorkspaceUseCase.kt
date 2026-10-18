package com.revlens.app.domain.usecase

import com.revlens.app.core.util.IdGenerator
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import com.revlens.app.utils.getCurrentTimeMillis

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
        val workspace = Workspace(
            id = idGenerator.generate("ws"),
            name = name.trim(),
            currency = currency,
            timeZoneId = timeZoneId,
            fiscalYearStartMonth = fiscalYearStartMonth,
            businessModel = businessModel,
            createdAtMillis = getCurrentTimeMillis()
        )
        workspaceRepository.createWorkspace(workspace)
        workspaceContext.switchTo(workspace.id)
        return workspace
    }
}
