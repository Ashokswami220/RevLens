package com.example.revlens.domain.repository

import com.example.revlens.domain.model.ApiKey
import com.example.revlens.domain.model.ApiKeyScope
import com.example.revlens.domain.model.WebhookDelivery
import com.example.revlens.domain.model.WebhookEndpoint
import kotlinx.coroutines.flow.Flow

interface DeveloperRepository {
    fun getApiKeys(workspaceId: String): Flow<List<ApiKey>>
    suspend fun createApiKey(workspaceId: String, name: String, scope: ApiKeyScope): Result<Pair<ApiKey, String>>
    suspend fun deleteApiKey(workspaceId: String, keyId: String): Result<Unit>

    fun getWebhooks(workspaceId: String): Flow<List<WebhookEndpoint>>
    suspend fun createWebhook(workspaceId: String, url: String, description: String, events: List<String>): Result<WebhookEndpoint>
    suspend fun deleteWebhook(workspaceId: String, endpointId: String): Result<Unit>
    suspend fun rotateWebhookSecret(workspaceId: String, endpointId: String): Result<String>

    fun getDeliveries(workspaceId: String): Flow<List<WebhookDelivery>>
    suspend fun retryDelivery(workspaceId: String, deliveryId: String): Result<WebhookDelivery>
}
