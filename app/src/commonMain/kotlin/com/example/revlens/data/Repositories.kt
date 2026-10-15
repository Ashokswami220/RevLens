package com.example.revlens.data

import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Scenario
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profileFlow: Flow<BusinessProfile?>
    suspend fun saveProfile(profile: BusinessProfile)
}

interface ScenarioRepository {
    val scenariosFlow: Flow<List<Scenario>>
    suspend fun saveScenario(scenario: Scenario)
    suspend fun deleteScenario(scenarioId: String)
}
