package com.example.revlens.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DataSource {
    Csv,
    Integration,
    Manual
}

@Serializable
data class SubscriptionRecord(
    val customerId: String,
    val plan: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String? = null, // null if active
    val mrrMinor: Long, // in cents/minor units (e.g. 2900 for $29.00)
    val channel: String? = null,
    val country: String? = null
)

@Serializable
data class MarketingSpendRecord(
    val month: String, // YYYY-MM-01
    val channel: String,
    val spendMinor: Long,
    val newCustomers: Int? = null
)

@Serializable
data class Dataset(
    val id: String,
    val workspaceId: String,
    val source: DataSource = DataSource.Csv,
    val filename: String,
    val subscriptions: List<SubscriptionRecord> = emptyList(),
    val spend: List<MarketingSpendRecord> = emptyList(),
    val importedAtMillis: Long = 0L
)
