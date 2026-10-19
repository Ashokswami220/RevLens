package com.revlens.app.data

import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profileFlow: Flow<BusinessProfile?>
    suspend fun saveProfile(profile: BusinessProfile)
    val isOnboardingCompletedFlow: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    val hasSeenIntroFlow: Flow<Boolean> get() = kotlinx.coroutines.flow.flowOf(false)
    suspend fun setHasSeenIntro(seen: Boolean) {}
    suspend fun syncRemoteProfile(): BusinessProfile? = null
}

interface ScenarioRepository {
    val scenariosFlow: Flow<List<Scenario>>
    suspend fun saveScenario(scenario: Scenario)
    suspend fun deleteScenario(scenarioId: String)
}
