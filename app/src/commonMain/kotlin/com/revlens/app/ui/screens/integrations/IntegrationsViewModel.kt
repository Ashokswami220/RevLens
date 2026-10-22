package com.revlens.app.ui.screens.integrations

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.model.ConnectionStatus
import com.revlens.app.domain.model.IntegrationConnection
import com.revlens.app.domain.model.IntegrationProvider
import kotlinx.coroutines.launch

class IntegrationsViewModel :
    MviViewModel<IntegrationsUiState, IntegrationsEvent, IntegrationsEffect>(IntegrationsUiState()) {

    override fun onEvent(event: IntegrationsEvent) {
        when (event) {
            is IntegrationsEvent.SelectCategory -> update {
                copy(selectedCategory = event.category)
            }

            is IntegrationsEvent.ConnectIntegration -> connect(event.provider)
            is IntegrationsEvent.PromptDisconnectIntegration -> update {
                copy(disconnectingProvider = event.provider)
            }

            IntegrationsEvent.DismissDisconnectIntegration -> update {
                copy(disconnectingProvider = null)
            }

            IntegrationsEvent.ConfirmDisconnectIntegration -> confirmDisconnect()
            is IntegrationsEvent.SyncIntegration -> sync(event.provider)
        }
    }

    private fun connect(provider: IntegrationProvider) {
        viewModelScope.launch {
            val updated = state.value.connections.filter { it.provider != provider } +
                    IntegrationConnection(
                        id = "conn_${provider.name.lowercase()}",
                        provider = provider,
                        status = ConnectionStatus.CONNECTED,
                        lastSyncAt = "Just now"
                    )
            update { copy(connections = updated) }
            emit(IntegrationsEffect.ShowToast("${provider.displayName} connected successfully!"))
        }
    }

    private fun confirmDisconnect() {
        val provider = state.value.disconnectingProvider ?: return
        viewModelScope.launch {
            val updated = state.value.connections.filter { it.provider != provider }
            update { copy(connections = updated, disconnectingProvider = null) }
            emit(IntegrationsEffect.ShowToast("${provider.displayName} disconnected."))
        }
    }

    private fun sync(provider: IntegrationProvider) {
        viewModelScope.launch {
            emit(IntegrationsEffect.ShowToast("${provider.displayName} synced successfully!"))
        }
    }
}
