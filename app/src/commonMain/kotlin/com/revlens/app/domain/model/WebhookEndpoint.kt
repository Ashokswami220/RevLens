package com.revlens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WebhookEndpoint(
    val id: String,
    val url: String,
    val description: String,
    val events: List<String>,
    val secret: String,
    val isActive: Boolean = true,
    val createdAt: String
)

@Serializable
data class WebhookDelivery(
    val id: String,
    val endpointId: String,
    val event: String,
    val statusCode: Int,
    val durationMs: Long,
    val timestamp: String,
    val payloadJson: String,
    val responseBody: String
)

object WebhookSupportedEvents {
    val ALL = listOf(
        "scenario.created",
        "scenario.updated",
        "goal.reached",
        "alert.triggered",
        "invoice.payment_succeeded"
    )
}
