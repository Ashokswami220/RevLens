package com.example.revlens.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExperimentType(val displayName: String) {
    PRICE_TEST("Price test"),
    CHURN_CAMPAIGN("Churn campaign"),
    NEW_PLAN("New plan"),
    ACQUISITION_CHANNEL("Acquisition channel"),
    CUSTOM("Custom")
}

@Serializable
enum class ExperimentMetric(val displayName: String) {
    MRR("MRR"),
    CHURN_RATE("Churn Rate"),
    CONVERSION("Conversion"),
    ARPU("ARPU")
}

@Serializable
enum class ExperimentStatus(val displayName: String) {
    DRAFT("Draft"),
    RUNNING("Running"),
    WON("Won"),
    LOST("Lost"),
    INCONCLUSIVE("Inconclusive")
}

@Serializable
data class Observation(
    val id: String,
    val date: String, // YYYY-MM-DD
    val controlValue: Double,
    val controlSampleSize: Int,
    val treatmentValue: Double,
    val treatmentSampleSize: Int,
    val notes: String = ""
)

@Serializable
data class Experiment(
    val id: String,
    val workspaceId: String,
    val name: String,
    val type: ExperimentType,
    val metric: ExperimentMetric,
    val status: ExperimentStatus = ExperimentStatus.DRAFT,
    val scenarioId: String? = null,
    val scenarioName: String? = null,
    val predictedLiftPercent: Double,
    val successThresholdPercent: Double,
    val startDate: String, // YYYY-MM-DD
    val plannedDurationDays: Int = 30,
    val controlDescription: String = "",
    val treatmentDescription: String = "",
    val observations: List<Observation> = emptyList(),
    val notes: String = "",
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)
