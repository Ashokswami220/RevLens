package com.example.revlens.data

import com.example.revlens.domain.model.Experiment
import com.example.revlens.domain.model.ExperimentMetric
import com.example.revlens.domain.model.ExperimentStatus
import com.example.revlens.domain.model.ExperimentType
import com.example.revlens.domain.model.Observation
import com.example.revlens.domain.repository.ExperimentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultExperimentRepository : ExperimentRepository {

    private val _experimentsFlow = MutableStateFlow<List<Experiment>>(defaultSampleExperiments())
    override val experimentsFlow: Flow<List<Experiment>> = _experimentsFlow.asStateFlow()

    override suspend fun saveExperiment(experiment: Experiment) {
        _experimentsFlow.update { list ->
            val existingIndex = list.indexOfFirst { it.id == experiment.id }
            if (existingIndex >= 0) {
                list.toMutableList().apply { set(existingIndex, experiment) }
            } else {
                list + experiment
            }
        }
    }

    override suspend fun deleteExperiment(experimentId: String) {
        _experimentsFlow.update { list -> list.filterNot { it.id == experimentId } }
    }

    override suspend fun logObservation(experimentId: String, observation: Observation) {
        _experimentsFlow.update { list ->
            list.map { exp ->
                if (exp.id == experimentId) {
                    exp.copy(observations = exp.observations + observation)
                } else exp
            }
        }
    }

    override suspend fun getExperimentById(experimentId: String): Experiment? {
        return _experimentsFlow.value.firstOrNull { it.id == experimentId }
    }

    companion object {
        fun defaultSampleExperiments(): List<Experiment> {
            return listOf(
                Experiment(
                    id = "exp_pro_price",
                    workspaceId = "default_ws",
                    name = "Pro Price to $39",
                    type = ExperimentType.PRICE_TEST,
                    metric = ExperimentMetric.MRR,
                    status = ExperimentStatus.RUNNING,
                    predictedLiftPercent = 14.0,
                    successThresholdPercent = 5.0,
                    startDate = "2026-10-01",
                    plannedDurationDays = 30,
                    controlDescription = "Standard $29.00/mo pricing",
                    treatmentDescription = "Increased to $39.00/mo for new cohorts",
                    observations = listOf(
                        Observation("o1", "2026-10-05", controlValue = 41200.0, controlSampleSize = 150, treatmentValue = 44500.0, treatmentSampleSize = 150),
                        Observation("o2", "2026-10-12", controlValue = 41500.0, controlSampleSize = 160, treatmentValue = 45200.0, treatmentSampleSize = 160)
                    ),
                    notes = "Early lift is strong with minimal sign-up drop off."
                ),
                Experiment(
                    id = "exp_annual_banner",
                    workspaceId = "default_ws",
                    name = "Annual Discount In-App Banner",
                    type = ExperimentType.CHURN_CAMPAIGN,
                    metric = ExperimentMetric.CONVERSION,
                    status = ExperimentStatus.WON,
                    predictedLiftPercent = 20.0,
                    successThresholdPercent = 8.0,
                    startDate = "2026-09-01",
                    plannedDurationDays = 30,
                    controlDescription = "No prompt on dashboard",
                    treatmentDescription = "Sticky bottom banner offering 2 months free",
                    observations = listOf(
                        Observation("o3", "2026-09-10", controlValue = 12.0, controlSampleSize = 300, treatmentValue = 18.0, treatmentSampleSize = 300),
                        Observation("o4", "2026-09-20", controlValue = 13.0, controlSampleSize = 350, treatmentValue = 21.0, treatmentSampleSize = 350)
                    ),
                    notes = "Won with high significance. Applied discount to regular billing."
                )
            )
        }
    }
}
