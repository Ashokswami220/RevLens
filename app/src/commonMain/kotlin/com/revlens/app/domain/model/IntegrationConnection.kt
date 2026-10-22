package com.revlens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class IntegrationCategory(val label: String) {
    REVENUE("Revenue Data"),
    SPREADSHEET("Spreadsheets"),
    ALERTS("Alerts & Comms")
}

@Serializable
enum class IntegrationProvider(
    val id: String,
    val displayName: String,
    val category: IntegrationCategory,
    val description: String,
    val defaultScopes: List<String>
) {
    STRIPE(
        id = "stripe",
        displayName = "Stripe",
        category = IntegrationCategory.REVENUE,
        description = "Sync recurring MRR, churned subscriptions, and customer transactions automatically.",
        defaultScopes = listOf("read_charges", "read_subscriptions", "read_customers")
    ),
    PADDLE(
        id = "paddle",
        displayName = "Paddle",
        category = IntegrationCategory.REVENUE,
        description = "Import international SaaS checkouts, tax compliance, and recurring subscription data.",
        defaultScopes = listOf("read_transactions", "read_subscription_plans")
    ),
    GOOGLE_SHEETS(
        id = "google_sheets",
        displayName = "Google Sheets",
        category = IntegrationCategory.SPREADSHEET,
        description = "Live two-way sync of monthly customer churn, acquisition cohorts, and marketing spend.",
        defaultScopes = listOf("spreadsheets.readonly", "drive.file")
    ),
    SLACK(
        id = "slack",
        displayName = "Slack",
        category = IntegrationCategory.ALERTS,
        description = "Instant alerts for revenue milestones, abnormal churn rate spikes, and forecast targets.",
        defaultScopes = listOf("chat:write", "incoming-webhook")
    )
}

@Serializable
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
}

@Serializable
data class IntegrationConnection(
    val id: String,
    val provider: IntegrationProvider,
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val accountName: String? = null,
    val lastSyncAt: String? = null,
    val grantedScopes: List<String> = emptyList(),
    val errorMessage: String? = null
)
