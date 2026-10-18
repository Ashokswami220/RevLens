package com.revlens.app.ui.screens.experiments

import androidx.lifecycle.viewModelScope
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.context.WorkspaceContext
import com.revlens.app.domain.model.Experiment
import com.revlens.app.domain.model.ExperimentStatus
import com.revlens.app.domain.repository.ExperimentRepository
import kotlinx.coroutines.launch
import kotlin.random.Random

class ExperimentsViewModel(
    private val experimentRepository: ExperimentRepository,
    private val workspaceContext: WorkspaceContext
) : MviViewModel<ExperimentsUiState, ExperimentsEvent, ExperimentsEffect>(
    ExperimentsUiState()
) {
    private var allExperiments: List<Experiment> = emptyList()

    init {
        observeExperiments()
    }

    private fun observeExperiments() {
        viewModelScope.launch {
            experimentRepository.experimentsFlow.collect { list ->
                allExperiments = list
                applyFilter()
            }
        }
    }

    override fun onEvent(event: ExperimentsEvent) {
        when (event) {
            is ExperimentsEvent.SelectFilter -> {
                update { copy(filter = event.filter) }
                applyFilter()
            }

            ExperimentsEvent.OpenCreateSheet -> update { copy(showCreateSheet = true) }
            ExperimentsEvent.DismissCreateSheet -> update { copy(showCreateSheet = false) }
            is ExperimentsEvent.CreateExperiment -> {
                viewModelScope.launch {
                    val id = "exp_" + Random.nextInt(10000, 99999)
                    val newExperiment = Experiment(
                        id = id,
                        workspaceId = workspaceContext.activeWorkspaceId.value ?: "default_ws",
                        name = event.name.ifBlank { "New Experiment" },
                        type = event.type,
                        metric = event.metric,
                        status = ExperimentStatus.RUNNING,
                        predictedLiftPercent = event.predictedLift,
                        successThresholdPercent = event.threshold,
                        startDate = "2026-10-15",
                        plannedDurationDays = event.durationDays,
                        controlDescription = event.controlDesc,
                        treatmentDescription = event.treatmentDesc,
                        observations = emptyList(),
                        createdAt = 1760000000L
                    )
                    experimentRepository.saveExperiment(newExperiment)
                    update { copy(showCreateSheet = false) }
                    emit(ExperimentsEffect.ShowToast("Created: ${newExperiment.name}"))
                    emit(ExperimentsEffect.NavigateToDetails(newExperiment.id))
                }
            }

            is ExperimentsEvent.DeleteExperiment -> {
                viewModelScope.launch {
                    experimentRepository.deleteExperiment(event.experimentId)
                    emit(ExperimentsEffect.ShowToast("Experiment deleted"))
                }
            }

            is ExperimentsEvent.DuplicateExperiment -> {
                viewModelScope.launch {
                    val existing = experimentRepository.getExperimentById(event.experimentId)
                    if (existing != null) {
                        val duplicate = existing.copy(
                            id = "exp_" + Random.nextInt(10000, 99999),
                            name = "${existing.name} (Copy)",
                            status = ExperimentStatus.DRAFT,
                            observations = emptyList(),
                            completedAt = null
                        )
                        experimentRepository.saveExperiment(duplicate)
                        emit(ExperimentsEffect.ShowToast("Duplicated experiment"))
                    }
                }
            }

            is ExperimentsEvent.ClickExperiment -> {
                emit(ExperimentsEffect.NavigateToDetails(event.experimentId))
            }
        }
    }

    private fun applyFilter() {
        val f = state.value.filter
        val filtered = when (f) {
            ExperimentFilter.ALL -> allExperiments
            ExperimentFilter.RUNNING -> allExperiments.filter { it.status == ExperimentStatus.RUNNING }
            ExperimentFilter.DONE -> allExperiments.filter {
                it.status == ExperimentStatus.WON || it.status == ExperimentStatus.LOST || it.status == ExperimentStatus.INCONCLUSIVE
            }

            ExperimentFilter.DRAFT -> allExperiments.filter { it.status == ExperimentStatus.DRAFT }
        }
        update { copy(experiments = filtered, isLoading = false) }
    }
}
