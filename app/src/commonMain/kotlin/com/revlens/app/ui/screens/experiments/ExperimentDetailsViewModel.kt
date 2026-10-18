package com.revlens.app.ui.screens.experiments

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.calc.ExperimentAnalyzer
import com.revlens.app.domain.model.ExperimentStatus
import com.revlens.app.domain.model.Observation
import com.revlens.app.domain.repository.ExperimentRepository
import kotlinx.coroutines.launch
import kotlin.random.Random

class ExperimentDetailsViewModel(
    private val experimentRepository: ExperimentRepository
) : MviViewModel<ExperimentDetailsUiState, ExperimentDetailsEvent, ExperimentDetailsEffect>(
    ExperimentDetailsUiState()
) {
    private var currentExperimentId: String? = null

    override fun onEvent(event: ExperimentDetailsEvent) {
        when (event) {
            is ExperimentDetailsEvent.LoadExperiment -> {
                currentExperimentId = event.experimentId
                loadDetails(event.experimentId)
            }
            ExperimentDetailsEvent.OpenLogObservationSheet -> update { copy(showLogObservationSheet = true) }
            ExperimentDetailsEvent.DismissLogObservationSheet -> update { copy(showLogObservationSheet = false) }
            is ExperimentDetailsEvent.LogObservation -> {
                val expId = currentExperimentId ?: return
                viewModelScope.launch {
                    val newObs = Observation(
                        id = "obs_" + Random.nextInt(10000, 99999),
                        date = event.date,
                        controlValue = event.controlVal,
                        controlSampleSize = event.controlN,
                        treatmentValue = event.treatmentVal,
                        treatmentSampleSize = event.treatmentN,
                        notes = event.notes
                    )
                    experimentRepository.logObservation(expId, newObs)
                    update { copy(showLogObservationSheet = false) }
                    loadDetails(expId)
                    emit(ExperimentDetailsEffect.ShowToast("Observation logged"))
                }
            }
            ExperimentDetailsEvent.OpenCompleteDialog -> update { copy(showCompleteDialog = true) }
            ExperimentDetailsEvent.DismissCompleteDialog -> update { copy(showCompleteDialog = false) }
            is ExperimentDetailsEvent.CompleteExperiment -> {
                val current = state.value.experiment ?: return
                viewModelScope.launch {
                    val completed = current.copy(
                        status = event.verdict,
                        completedAt = 1760000000L
                    )
                    experimentRepository.saveExperiment(completed)
                    update { copy(showCompleteDialog = false) }
                    loadDetails(completed.id)
                    emit(ExperimentDetailsEffect.ShowToast("Experiment completed as ${event.verdict.displayName}"))
                }
            }
            is ExperimentDetailsEvent.UpdateNotes -> {
                val current = state.value.experiment ?: return
                viewModelScope.launch {
                    val updated = current.copy(notes = event.notes)
                    experimentRepository.saveExperiment(updated)
                    loadDetails(updated.id)
                }
            }
        }
    }

    private fun loadDetails(experimentId: String) {
        viewModelScope.launch {
            val exp = experimentRepository.getExperimentById(experimentId)
            if (exp != null) {
                val analysis = ExperimentAnalyzer.analyze(exp)
                update {
                    copy(
                        experiment = exp,
                        analysis = analysis,
                        isLoading = false
                    )
                }
            }
        }
    }
}
