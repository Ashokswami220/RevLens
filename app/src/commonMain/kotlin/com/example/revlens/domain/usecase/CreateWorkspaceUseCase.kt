package com.example.revlens.domain.usecase

import com.example.revlens.core.util.IdGenerator
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.model.BusinessModel
import com.example.revlens.domain.model.Workspace
import com.example.revlens.domain.repository.WorkspaceRepository
import com.example.revlens.utils.getCurrentTimeMillis

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
