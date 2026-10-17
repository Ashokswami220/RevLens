package com.example.revlens.ui.screens.experiments

import com.example.revlens.domain.model.Experiment
import com.example.revlens.domain.model.ExperimentMetric
import com.example.revlens.domain.model.ExperimentType

enum class ExperimentFilter(val label: String) {
    ALL("All"),
    RUNNING("Running"),
    DONE("Done"),
    DRAFT("Draft")
}

data class ExperimentsUiState(
    val filter: ExperimentFilter = ExperimentFilter.ALL,
    val experiments: List<Experiment> = emptyList(),
    val showCreateSheet: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ExperimentsEvent {
    data class SelectFilter(val filter: ExperimentFilter) : ExperimentsEvent
    data object OpenCreateSheet : ExperimentsEvent
    data object DismissCreateSheet : ExperimentsEvent
    data class CreateExperiment(
        val name: String,
        val type: ExperimentType,
        val metric: ExperimentMetric,
        val predictedLift: Double,
        val threshold: Double,
        val durationDays: Int,
        val controlDesc: String,
        val treatmentDesc: String
    ) : ExperimentsEvent
    data class DeleteExperiment(val experimentId: String) : ExperimentsEvent
    data class DuplicateExperiment(val experimentId: String) : ExperimentsEvent
    data class ClickExperiment(val experimentId: String) : ExperimentsEvent
}

sealed interface ExperimentsEffect {
    data class NavigateToDetails(val experimentId: String) : ExperimentsEffect
    data class ShowToast(val message: String) : ExperimentsEffect
}
