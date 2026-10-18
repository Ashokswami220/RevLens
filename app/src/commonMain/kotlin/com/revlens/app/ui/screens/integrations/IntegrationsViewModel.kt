package com.revlens.app.ui.screens.integrations

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.IntegrationProvider
import com.revlens.app.domain.repository.IntegrationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IntegrationsViewModel(
    private val integrationRepository: IntegrationRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<IntegrationsUiState, IntegrationsEvent, IntegrationsEffect>(IntegrationsUiState()) {

    private var currentWorkspaceId: String = "ws_default"
    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            workspaceContext.activeWorkspaceId.collectLatest { wsId ->
                val id = wsId ?: "ws_default"
                currentWorkspaceId = id
                startObserving(id)
            }
        }
    }

    private fun startObserving(wsId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            integrationRepository.getConnections(wsId).collect { list ->
                update { copy(connections = list) }
            }
        }
    }

    override fun onEvent(event: IntegrationsEvent) {
        when (event) {
            is IntegrationsEvent.SelectCategory -> update { copy(selectedCategory = event.category) }
            is IntegrationsEvent.ConnectIntegration -> connect(event.provider)
            is IntegrationsEvent.PromptDisconnectIntegration -> update { copy(disconnectingProvider = event.provider) }
            IntegrationsEvent.DismissDisconnectIntegration -> update { copy(disconnectingProvider = null) }
            IntegrationsEvent.ConfirmDisconnectIntegration -> confirmDisconnect()
            is IntegrationsEvent.SyncIntegration -> sync(event.provider)
        }
    }

    private fun connect(provider: IntegrationProvider) {
        viewModelScope.launch {
            val res = integrationRepository.connectIntegration(currentWorkspaceId, provider)
            res.onSuccess {
                emit(IntegrationsEffect.ShowToast("${provider.displayName} connected successfully!"))
            }.onFailure { err ->
                emit(IntegrationsEffect.ShowToast(err.message ?: "Failed to connect ${provider.displayName}."))
            }
        }
    }

    private fun confirmDisconnect() {
        val provider = state.value.disconnectingProvider ?: return
        viewModelScope.launch {
            update { copy(disconnectingProvider = null) }
            val res = integrationRepository.disconnectIntegration(currentWorkspaceId, provider)
            res.onSuccess {
                emit(IntegrationsEffect.ShowToast("${provider.displayName} disconnected."))
            }.onFailure { err ->
                emit(IntegrationsEffect.ShowToast(err.message ?: "Could not disconnect ${provider.displayName}."))
            }
        }
    }

    private fun sync(provider: IntegrationProvider) {
        viewModelScope.launch {
            update { copy(syncingProviders = syncingProviders + provider) }
            val res = integrationRepository.syncIntegration(currentWorkspaceId, provider)
            update { copy(syncingProviders = syncingProviders - provider) }

            res.onSuccess {
                emit(IntegrationsEffect.ShowToast("${provider.displayName} synced successfully!"))
            }.onFailure { err ->
                emit(IntegrationsEffect.ShowToast(err.message ?: "Sync failed for ${provider.displayName}."))
            }
        }
    }
}
