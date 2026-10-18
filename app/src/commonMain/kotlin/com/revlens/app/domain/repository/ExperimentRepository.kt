package com.revlens.app.domain.repository

import com.revlens.app.domain.model.Experiment
import com.revlens.app.domain.model.Observation
import kotlinx.coroutines.flow.Flow

interface ExperimentRepository {
    val experimentsFlow: Flow<List<Experiment>>
    suspend fun saveExperiment(experiment: Experiment)
    suspend fun deleteExperiment(experimentId: String)
    suspend fun logObservation(experimentId: String, observation: Observation)
    suspend fun getExperimentById(experimentId: String): Experiment?
}
