package com.revlens.app.ui.screens.experiments

import com.revlens.app.domain.calc.ExperimentAnalysis
import com.revlens.app.domain.model.Experiment
import com.revlens.app.domain.model.ExperimentStatus

data class ExperimentDetailsUiState(
    val experiment: Experiment? = null,
    val analysis: ExperimentAnalysis? = null,
    val showLogObservationSheet: Boolean = false,
    val showCompleteDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ExperimentDetailsEvent {
    data class LoadExperiment(val experimentId: String) : ExperimentDetailsEvent
    data object OpenLogObservationSheet : ExperimentDetailsEvent
    data object DismissLogObservationSheet : ExperimentDetailsEvent
    data class LogObservation(
        val date: String,
        val controlVal: Double,
        val controlN: Int,
        val treatmentVal: Double,
        val treatmentN: Int,
        val notes: String
    ) : ExperimentDetailsEvent

    data object OpenCompleteDialog : ExperimentDetailsEvent
    data object DismissCompleteDialog : ExperimentDetailsEvent
    data class CompleteExperiment(val verdict: ExperimentStatus) : ExperimentDetailsEvent
    data class UpdateNotes(val notes: String) : ExperimentDetailsEvent
}

sealed interface ExperimentDetailsEffect {
    data class ShowToast(val message: String) : ExperimentDetailsEffect
    data object NavigateBack : ExperimentDetailsEffect
}
