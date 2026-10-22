package com.revlens.app.ui.screens.developer

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.model.ApiKey
import com.revlens.app.domain.model.WebhookEndpoint
import kotlinx.coroutines.launch

class ApiWebhooksViewModel :
    MviViewModel<ApiWebhooksUiState, ApiWebhooksEvent, ApiWebhooksEffect>(ApiWebhooksUiState()) {

    override fun onEvent(event: ApiWebhooksEvent) {
        when (event) {
            is ApiWebhooksEvent.SelectTab -> update { copy(activeTab = event.tab) }

            // API Keys
            ApiWebhooksEvent.OpenCreateKeySheet -> update {
                copy(isCreateKeySheetVisible = true, newKeyName = "", error = null)
            }

            ApiWebhooksEvent.DismissCreateKeySheet -> update {
                copy(isCreateKeySheetVisible = false)
            }

            is ApiWebhooksEvent.NewKeyNameChanged -> update {
                copy(newKeyName = event.name, error = null)
            }

            is ApiWebhooksEvent.NewKeyScopeChanged -> update { copy(newKeyScope = event.scope) }
            ApiWebhooksEvent.SubmitCreateKey -> submitCreateKey()
            ApiWebhooksEvent.DismissKeyRevealDialog -> update { copy(createdKeyToken = null) }
            is ApiWebhooksEvent.PromptDeleteKey -> update { copy(keyToDelete = event.key) }
            ApiWebhooksEvent.DismissDeleteKey -> update { copy(keyToDelete = null) }
            ApiWebhooksEvent.ConfirmDeleteKey -> confirmDeleteKey()

            // Webhooks
            ApiWebhooksEvent.OpenCreateWebhookSheet -> update {
                copy(
                    isCreateWebhookSheetVisible = true,
                    newWebhookUrl = "",
                    newWebhookDesc = "",
                    selectedEvents = setOf("scenario.created"),
                    error = null
                )
            }

            ApiWebhooksEvent.DismissCreateWebhookSheet -> update {
                copy(isCreateWebhookSheetVisible = false)
            }

            is ApiWebhooksEvent.NewWebhookUrlChanged -> update {
                copy(newWebhookUrl = event.url, error = null)
            }

            is ApiWebhooksEvent.NewWebhookDescChanged -> update {
                copy(newWebhookDesc = event.desc)
            }

            is ApiWebhooksEvent.ToggleWebhookEvent -> toggleEvent(event.event)
            ApiWebhooksEvent.SubmitCreateWebhook -> submitCreateWebhook()
            is ApiWebhooksEvent.PromptDeleteWebhook -> update {
                copy(webhookToDelete = event.endpoint)
            }

            ApiWebhooksEvent.DismissDeleteWebhook -> update { copy(webhookToDelete = null) }
            ApiWebhooksEvent.ConfirmDeleteWebhook -> confirmDeleteWebhook()
            is ApiWebhooksEvent.RotateWebhookSecret -> rotateSecret(event.endpointId)

            // Deliveries
            is ApiWebhooksEvent.SelectDelivery -> update { copy(selectedDelivery = event.delivery) }
            is ApiWebhooksEvent.RetryDelivery -> retryDelivery(event.deliveryId)
            is ApiWebhooksEvent.CopyText -> {
                viewModelScope.launch {
                    emit(ApiWebhooksEffect.ShowToast("${event.label} copied!"))
                }
            }
        }
    }

    private fun submitCreateKey() {
        val name = state.value.newKeyName.trim()
        if (name.isBlank()) {
            update { copy(error = "Please enter an API key name.") }
            return
        }

        val secretToken = "rev_live_${
            name.lowercase()
                .replace(" ", "_")
        }_xyz123"
        val newKey = ApiKey(
            id = "key_${name.lowercase()}",
            name = name,
            prefix = "rev_live_***",
            scope = state.value.newKeyScope,
            createdAt = "Nov 15, 2026"
        )

        update {
            copy(
                apiKeys = apiKeys + newKey,
                isCreateKeySheetVisible = false,
                createdKeyToken = secretToken,
                newKeyName = ""
            )
        }
    }

    private fun confirmDeleteKey() {
        val key = state.value.keyToDelete ?: return
        viewModelScope.launch {
            update {
                copy(
                    apiKeys = apiKeys.filter { it.id != key.id },
                    keyToDelete = null
                )
            }
            emit(ApiWebhooksEffect.ShowToast("API key ${key.name} revoked."))
        }
    }

    private fun toggleEvent(event: String) {
        val current = state.value.selectedEvents
        val updated = if (current.contains(event)) {
            if (current.size > 1) current - event else current
        } else {
            current + event
        }
        update { copy(selectedEvents = updated) }
    }

    private fun submitCreateWebhook() {
        val url = state.value.newWebhookUrl.trim()
        if (!url.startsWith("https://")) {
            update { copy(error = "Endpoint URL must begin with https://") }
            return
        }

        val newEndpoint = WebhookEndpoint(
            id = "wh_${url.hashCode()}",
            url = url,
            description = state.value.newWebhookDesc,
            events = state.value.selectedEvents.toList(),
            secret = "whsec_sample123",
            createdAt = "Nov 15, 2026"
        )

        viewModelScope.launch {
            update {
                copy(
                    webhooks = webhooks + newEndpoint,
                    isCreateWebhookSheetVisible = false
                )
            }
            emit(ApiWebhooksEffect.ShowToast("Webhook endpoint registered successfully!"))
        }
    }

    private fun confirmDeleteWebhook() {
        val endpoint = state.value.webhookToDelete ?: return
        viewModelScope.launch {
            update {
                copy(
                    webhooks = webhooks.filter { it.id != endpoint.id },
                    webhookToDelete = null
                )
            }
            emit(ApiWebhooksEffect.ShowToast("Webhook endpoint removed."))
        }
    }

    private fun rotateSecret(endpointId: String) {
        viewModelScope.launch {
            emit(ApiWebhooksEffect.ShowToast("Webhook signing secret rotated!"))
        }
    }

    private fun retryDelivery(deliveryId: String) {
        viewModelScope.launch {
            emit(
                ApiWebhooksEffect.ShowToast(
                    "Webhook delivery redelivered successfully (Status: 200)"
                )
            )
        }
    }
}
