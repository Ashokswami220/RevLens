package com.revlens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BusinessModel {
    B2bSaas,
    B2cSaas,
    Other
}

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    val currency: String = "USD",
    val timeZoneId: String = "UTC",
    val fiscalYearStartMonth: Int = 1,
    val businessModel: BusinessModel = BusinessModel.B2bSaas,
    val createdAtMillis: Long = 0L
)
