package com.example.revlens.domain.repository

import com.example.revlens.domain.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    val workspacesFlow: Flow<List<Workspace>>
    suspend fun getWorkspace(id: String): Workspace?
    suspend fun createWorkspace(workspace: Workspace)
    suspend fun updateWorkspace(workspace: Workspace)
    suspend fun deleteWorkspace(id: String)
}
