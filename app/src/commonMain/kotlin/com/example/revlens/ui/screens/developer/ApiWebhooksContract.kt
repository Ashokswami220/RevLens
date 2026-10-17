package com.example.revlens.ui.screens.developer

import com.example.revlens.domain.model.ApiKey
import com.example.revlens.domain.model.ApiKeyScope
import com.example.revlens.domain.model.WebhookDelivery
import com.example.revlens.domain.model.WebhookEndpoint

enum class DeveloperTab(val label: String) {
    API_KEYS("API Keys"),
    WEBHOOKS("Webhooks"),
    DELIVERIES("Delivery Logs")
}

data class ApiWebhooksUiState(
    val activeTab: DeveloperTab = DeveloperTab.API_KEYS,
    val apiKeys: List<ApiKey> = emptyList(),
    val webhooks: List<WebhookEndpoint> = emptyList(),
    val deliveries: List<WebhookDelivery> = emptyList(),
    val isCreateKeySheetVisible: Boolean = false,
    val newKeyName: String = "",
    val newKeyScope: ApiKeyScope = ApiKeyScope.READ_WRITE,
    val createdKeyToken: String? = null,
    val isCreateWebhookSheetVisible: Boolean = false,
    val newWebhookUrl: String = "",
    val newWebhookDesc: String = "",
    val selectedEvents: Set<String> = setOf("scenario.created"),
    val selectedDelivery: WebhookDelivery? = null,
    val keyToDelete: ApiKey? = null,
    val webhookToDelete: WebhookEndpoint? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ApiWebhooksEvent {
    data class SelectTab(val tab: DeveloperTab) : ApiWebhooksEvent
    data object OpenCreateKeySheet : ApiWebhooksEvent
    data object DismissCreateKeySheet : ApiWebhooksEvent
    data class NewKeyNameChanged(val name: String) : ApiWebhooksEvent
    data class NewKeyScopeChanged(val scope: ApiKeyScope) : ApiWebhooksEvent
    data object SubmitCreateKey : ApiWebhooksEvent
    data object DismissKeyRevealDialog : ApiWebhooksEvent
    data class PromptDeleteKey(val key: ApiKey) : ApiWebhooksEvent
    data object ConfirmDeleteKey : ApiWebhooksEvent
    data object DismissDeleteKey : ApiWebhooksEvent

    data object OpenCreateWebhookSheet : ApiWebhooksEvent
    data object DismissCreateWebhookSheet : ApiWebhooksEvent
    data class NewWebhookUrlChanged(val url: String) : ApiWebhooksEvent
    data class NewWebhookDescChanged(val desc: String) : ApiWebhooksEvent
    data class ToggleWebhookEvent(val event: String) : ApiWebhooksEvent
    data object SubmitCreateWebhook : ApiWebhooksEvent
    data class PromptDeleteWebhook(val endpoint: WebhookEndpoint) : ApiWebhooksEvent
    data object ConfirmDeleteWebhook : ApiWebhooksEvent
    data object DismissDeleteWebhook : ApiWebhooksEvent
    data class RotateWebhookSecret(val endpointId: String) : ApiWebhooksEvent

    data class SelectDelivery(val delivery: WebhookDelivery?) : ApiWebhooksEvent
    data class RetryDelivery(val deliveryId: String) : ApiWebhooksEvent
    data class CopyText(val text: String, val label: String) : ApiWebhooksEvent
}

sealed interface ApiWebhooksEffect {
    data class ShowToast(val message: String) : ApiWebhooksEffect
    data object NavigateBack : ApiWebhooksEffect
}
