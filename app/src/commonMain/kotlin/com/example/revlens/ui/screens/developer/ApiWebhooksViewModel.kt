package com.example.revlens.ui.screens.developer

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.repository.DeveloperRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ApiWebhooksViewModel(
    private val developerRepository: DeveloperRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<ApiWebhooksUiState, ApiWebhooksEvent, ApiWebhooksEffect>(ApiWebhooksUiState()) {

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
            launch {
                developerRepository.getApiKeys(wsId).collect { keys ->
                    update { copy(apiKeys = keys) }
                }
            }
            launch {
                developerRepository.getWebhooks(wsId).collect { hooks ->
                    update { copy(webhooks = hooks) }
                }
            }
            launch {
                developerRepository.getDeliveries(wsId).collect { dels ->
                    update { copy(deliveries = dels) }
                }
            }
        }
    }

    override fun onEvent(event: ApiWebhooksEvent) {
        when (event) {
            is ApiWebhooksEvent.SelectTab -> update { copy(activeTab = event.tab) }

            // API Keys
            ApiWebhooksEvent.OpenCreateKeySheet -> update {
                copy(isCreateKeySheetVisible = true, newKeyName = "", error = null)
            }
            ApiWebhooksEvent.DismissCreateKeySheet -> update { copy(isCreateKeySheetVisible = false) }
            is ApiWebhooksEvent.NewKeyNameChanged -> update { copy(newKeyName = event.name, error = null) }
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
            ApiWebhooksEvent.DismissCreateWebhookSheet -> update { copy(isCreateWebhookSheetVisible = false) }
            is ApiWebhooksEvent.NewWebhookUrlChanged -> update { copy(newWebhookUrl = event.url, error = null) }
            is ApiWebhooksEvent.NewWebhookDescChanged -> update { copy(newWebhookDesc = event.desc) }
            is ApiWebhooksEvent.ToggleWebhookEvent -> toggleEvent(event.event)
            ApiWebhooksEvent.SubmitCreateWebhook -> submitCreateWebhook()
            is ApiWebhooksEvent.PromptDeleteWebhook -> update { copy(webhookToDelete = event.endpoint) }
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

        viewModelScope.launch {
            update { copy(isLoading = true) }
            val res = developerRepository.createApiKey(currentWorkspaceId, name, state.value.newKeyScope)
            update { copy(isLoading = false) }

            res.onSuccess { (_, secretToken) ->
                update {
                    copy(
                        isCreateKeySheetVisible = false,
                        createdKeyToken = secretToken,
                        newKeyName = ""
                    )
                }
            }.onFailure { err ->
                update { copy(error = err.message ?: "Failed to generate API key.") }
            }
        }
    }

    private fun confirmDeleteKey() {
        val key = state.value.keyToDelete ?: return
        viewModelScope.launch {
            update { copy(keyToDelete = null) }
            val res = developerRepository.deleteApiKey(currentWorkspaceId, key.id)
            res.onSuccess {
                emit(ApiWebhooksEffect.ShowToast("API key ${key.name} revoked."))
            }.onFailure { err ->
                emit(ApiWebhooksEffect.ShowToast(err.message ?: "Failed to revoke key."))
            }
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

        viewModelScope.launch {
            update { copy(isLoading = true) }
            val res = developerRepository.createWebhook(
                currentWorkspaceId,
                url,
                state.value.newWebhookDesc,
                state.value.selectedEvents.toList()
            )
            update { copy(isLoading = false) }

            res.onSuccess {
                update { copy(isCreateWebhookSheetVisible = false) }
                emit(ApiWebhooksEffect.ShowToast("Webhook endpoint registered successfully!"))
            }.onFailure { err ->
                update { copy(error = err.message ?: "Failed to create webhook.") }
            }
        }
    }

    private fun confirmDeleteWebhook() {
        val endpoint = state.value.webhookToDelete ?: return
        viewModelScope.launch {
            update { copy(webhookToDelete = null) }
            val res = developerRepository.deleteWebhook(currentWorkspaceId, endpoint.id)
            res.onSuccess {
                emit(ApiWebhooksEffect.ShowToast("Webhook endpoint removed."))
            }.onFailure { err ->
                emit(ApiWebhooksEffect.ShowToast(err.message ?: "Could not remove endpoint."))
            }
        }
    }

    private fun rotateSecret(endpointId: String) {
        viewModelScope.launch {
            val res = developerRepository.rotateWebhookSecret(currentWorkspaceId, endpointId)
            res.onSuccess {
                emit(ApiWebhooksEffect.ShowToast("Webhook signing secret rotated!"))
            }.onFailure { err ->
                emit(ApiWebhooksEffect.ShowToast(err.message ?: "Failed to rotate secret."))
            }
        }
    }

    private fun retryDelivery(deliveryId: String) {
        viewModelScope.launch {
            val res = developerRepository.retryDelivery(currentWorkspaceId, deliveryId)
            res.onSuccess {
                emit(ApiWebhooksEffect.ShowToast("Webhook delivery redelivered successfully (Status: 200)"))
            }.onFailure { err ->
                emit(ApiWebhooksEffect.ShowToast(err.message ?: "Redelivery failed."))
            }
        }
    }
}
