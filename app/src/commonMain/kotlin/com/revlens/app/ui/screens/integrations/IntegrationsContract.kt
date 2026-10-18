package com.revlens.app.ui.screens.integrations

import com.revlens.app.domain.model.IntegrationCategory
import com.revlens.app.domain.model.IntegrationConnection
import com.revlens.app.domain.model.IntegrationProvider

data class IntegrationsUiState(
    val connections: List<IntegrationConnection> = emptyList(),
    val selectedCategory: IntegrationCategory? = null,
    val syncingProviders: Set<IntegrationProvider> = emptySet(),
    val disconnectingProvider: IntegrationProvider? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface IntegrationsEvent {
    data class SelectCategory(val category: IntegrationCategory?) : IntegrationsEvent
    data class ConnectIntegration(val provider: IntegrationProvider) : IntegrationsEvent
    data class PromptDisconnectIntegration(val provider: IntegrationProvider) : IntegrationsEvent
    data object ConfirmDisconnectIntegration : IntegrationsEvent
    data object DismissDisconnectIntegration : IntegrationsEvent
    data class SyncIntegration(val provider: IntegrationProvider) : IntegrationsEvent
}

sealed interface IntegrationsEffect {
    data class ShowToast(val message: String) : IntegrationsEffect
    data object NavigateBack : IntegrationsEffect
}
