package com.example.revlens.domain.context

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WorkspaceContext {
    val activeWorkspaceId: StateFlow<String?>
    suspend fun switchTo(workspaceId: String)
}

class DefaultWorkspaceContext(initialWorkspaceId: String? = "ws_default") : WorkspaceContext {
    private val _activeWorkspaceId = MutableStateFlow(initialWorkspaceId)
    override val activeWorkspaceId: StateFlow<String?> = _activeWorkspaceId.asStateFlow()

    override suspend fun switchTo(workspaceId: String) {
        _activeWorkspaceId.value = workspaceId
    }
}
