package com.example.revlens.data.fake

import com.example.revlens.domain.model.ApiKey
import com.example.revlens.domain.model.ApiKeyScope
import com.example.revlens.domain.model.WebhookDelivery
import com.example.revlens.domain.model.WebhookEndpoint
import com.example.revlens.domain.repository.DeveloperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeDeveloperRepository : DeveloperRepository {

    private val keysByWs = mutableMapOf<String, MutableStateFlow<List<ApiKey>>>()
    private val webhooksByWs = mutableMapOf<String, MutableStateFlow<List<WebhookEndpoint>>>()
    private val deliveriesByWs = mutableMapOf<String, MutableStateFlow<List<WebhookDelivery>>>()

    private fun getKeysFlow(workspaceId: String): MutableStateFlow<List<ApiKey>> {
        return keysByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    ApiKey(
                        id = "key_1",
                        name = "Production CI Sync",
                        prefix = "rl_live_8f3a",
                        scope = ApiKeyScope.READ_WRITE,
                        createdAt = "Created 2 weeks ago",
                        lastUsedAt = "Today at 08:14 AM"
                    ),
                    ApiKey(
                        id = "key_2",
                        name = "Dashboard Widget Embed",
                        prefix = "rl_live_4b2c",
                        scope = ApiKeyScope.READ_ONLY,
                        createdAt = "Created 1 month ago",
                        lastUsedAt = "Yesterday"
                    )
                )
            )
        }
    }

    private fun getWebhooksFlow(workspaceId: String): MutableStateFlow<List<WebhookEndpoint>> {
        return webhooksByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    WebhookEndpoint(
                        id = "wh_1",
                        url = "https://api.acme.com/v1/revlens-events",
                        description = "Acme Core Revenue Pipeline",
                        events = listOf("scenario.created", "goal.reached"),
                        secret = "whsec_90fe81a7b4c2",
                        isActive = true,
                        createdAt = "Created 3 weeks ago"
                    )
                )
            )
        }
    }

    private fun getDeliveriesFlow(workspaceId: String): MutableStateFlow<List<WebhookDelivery>> {
        return deliveriesByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    WebhookDelivery(
                        id = "del_1",
                        endpointId = "wh_1",
                        event = "scenario.created",
                        statusCode = 200,
                        durationMs = 142L,
                        timestamp = "5 mins ago",
                        payloadJson = "{\n  \"event\": \"scenario.created\",\n  \"scenario_id\": \"sc_889\",\n  \"name\": \"Aggressive Growth 2027\"\n}",
                        responseBody = "{\"status\": \"received\", \"acknowledged\": true}"
                    ),
                    WebhookDelivery(
                        id = "del_2",
                        endpointId = "wh_1",
                        event = "goal.reached",
                        statusCode = 200,
                        durationMs = 198L,
                        timestamp = "2 hours ago",
                        payloadJson = "{\n  \"event\": \"goal.reached\",\n  \"goal\": \"$100k MRR\"\n}",
                        responseBody = "{\"status\": \"ok\"}"
                    ),
                    WebhookDelivery(
                        id = "del_3",
                        endpointId = "wh_1",
                        event = "alert.triggered",
                        statusCode = 500,
                        durationMs = 520L,
                        timestamp = "Yesterday",
                        payloadJson = "{\n  \"event\": \"alert.triggered\",\n  \"metric\": \"churn_rate\"\n}",
                        responseBody = "{\"error\": \"Internal Server Error\"}"
                    )
                )
            )
        }
    }

    override fun getApiKeys(workspaceId: String): Flow<List<ApiKey>> =
        getKeysFlow(workspaceId).asStateFlow()

    override suspend fun createApiKey(
        workspaceId: String,
        name: String,
        scope: ApiKeyScope
    ): Result<Pair<ApiKey, String>> {
        val flow = getKeysFlow(workspaceId)
        val id = "key_${flow.value.size + 1}"
        val rawSecret = "rl_live_sec_" + (100000..999999).random().toString() + "_x89"
        val prefix = rawSecret.take(12)

        val apiKey = ApiKey(
            id = id,
            name = name.trim().ifBlank { "Untitled Key" },
            prefix = prefix,
            scope = scope,
            createdAt = "Just now",
            lastUsedAt = "Never"
        )

        flow.update { listOf(apiKey) + it }
        return Result.success(apiKey to rawSecret)
    }

    override suspend fun deleteApiKey(workspaceId: String, keyId: String): Result<Unit> {
        getKeysFlow(workspaceId).update { list -> list.filterNot { it.id == keyId } }
        return Result.success(Unit)
    }

    override fun getWebhooks(workspaceId: String): Flow<List<WebhookEndpoint>> =
        getWebhooksFlow(workspaceId).asStateFlow()

    override suspend fun createWebhook(
        workspaceId: String,
        url: String,
        description: String,
        events: List<String>
    ): Result<WebhookEndpoint> {
        val flow = getWebhooksFlow(workspaceId)
        val id = "wh_${flow.value.size + 1}"
        val secret = "whsec_" + (100000..999999).random().toString()

        val endpoint = WebhookEndpoint(
            id = id,
            url = url.trim(),
            description = description.trim().ifBlank { "Endpoint $id" },
            events = if (events.isEmpty()) listOf("scenario.created") else events,
            secret = secret,
            isActive = true,
            createdAt = "Just now"
        )

        flow.update { listOf(endpoint) + it }
        return Result.success(endpoint)
    }

    override suspend fun deleteWebhook(workspaceId: String, endpointId: String): Result<Unit> {
        getWebhooksFlow(workspaceId).update { list -> list.filterNot { it.id == endpointId } }
        return Result.success(Unit)
    }

    override suspend fun rotateWebhookSecret(workspaceId: String, endpointId: String): Result<String> {
        val flow = getWebhooksFlow(workspaceId)
        val newSecret = "whsec_" + (100000..999999).random().toString() + "_rotated"
        flow.update { list ->
            list.map { if (it.id == endpointId) it.copy(secret = newSecret) else it }
        }
        return Result.success(newSecret)
    }

    override fun getDeliveries(workspaceId: String): Flow<List<WebhookDelivery>> =
        getDeliveriesFlow(workspaceId).asStateFlow()

    override suspend fun retryDelivery(workspaceId: String, deliveryId: String): Result<WebhookDelivery> {
        val flow = getDeliveriesFlow(workspaceId)
        val existing = flow.value.find { it.id == deliveryId }
            ?: return Result.failure(NoSuchElementException("Delivery not found"))

        val retried = existing.copy(
            id = "del_${flow.value.size + 1}",
            statusCode = 200,
            durationMs = 135L,
            timestamp = "Just now",
            responseBody = "{\"status\": \"retried_ok\"}"
        )

        flow.update { listOf(retried) + it }
        return Result.success(retried)
    }
}
