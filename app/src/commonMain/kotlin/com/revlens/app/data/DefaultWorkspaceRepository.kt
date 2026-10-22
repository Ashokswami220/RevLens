package com.revlens.app.data

import com.revlens.app.domain.model.Workspace
import com.revlens.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultWorkspaceRepository(
    initialWorkspaces: List<Workspace> = emptyList()
) : WorkspaceRepository {
    private val _workspaces = MutableStateFlow(initialWorkspaces)
    override val workspacesFlow: Flow<List<Workspace>> = _workspaces.asStateFlow()

    override suspend fun getWorkspace(id: String): Workspace? {
        return _workspaces.value.firstOrNull { it.id == id }
    }

    override suspend fun createWorkspace(workspace: Workspace) {
        val current = _workspaces.value.toMutableList()
        val index = current.indexOfFirst { it.id == workspace.id }
        if (index != -1) {
            current[index] = workspace
        } else {
            current.add(workspace)
        }
        _workspaces.value = current
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
        createWorkspace(workspace)
    }

    override suspend fun deleteWorkspace(id: String) {
        _workspaces.value = _workspaces.value.filter { it.id != id }
    }

    override suspend fun clearLocalData() {
        _workspaces.value = emptyList()
    }
}
