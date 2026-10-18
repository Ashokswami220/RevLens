package com.revlens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ApiKeyScope(val label: String, val description: String) {
    READ_ONLY("Read Only", "Read scenarios, baselines, and revenue analytics"),
    READ_WRITE("Read & Write", "Full access to create, update, and delete scenarios and models")
}

@Serializable
data class ApiKey(
    val id: String,
    val name: String,
    val prefix: String,
    val scope: ApiKeyScope = ApiKeyScope.READ_ONLY,
    val createdAt: String,
    val lastUsedAt: String? = null
)
