package com.example.revlens.domain.repository

import com.example.revlens.domain.model.IntegrationConnection
import com.example.revlens.domain.model.IntegrationProvider
import kotlinx.coroutines.flow.Flow

interface IntegrationRepository {
    fun getConnections(workspaceId: String): Flow<List<IntegrationConnection>>
    suspend fun connectIntegration(workspaceId: String, provider: IntegrationProvider): Result<IntegrationConnection>
    suspend fun disconnectIntegration(workspaceId: String, provider: IntegrationProvider): Result<Unit>
    suspend fun syncIntegration(workspaceId: String, provider: IntegrationProvider): Result<IntegrationConnection>
}
