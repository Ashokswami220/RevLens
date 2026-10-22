package com.revlens.app.data

import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProfileRepository {
    val profileFlow: Flow<BusinessProfile?>
    suspend fun saveProfile(profile: BusinessProfile)
    val isOnboardingCompletedFlow: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    val hasSeenIntroFlow: Flow<Boolean>
        get() = flowOf(false)

    suspend fun setHasSeenIntro(seen: Boolean) {}
    suspend fun syncRemoteProfile(): BusinessProfile? = null
    suspend fun clearLocalData() {}
}

interface ScenarioRepository {
    val scenariosFlow: Flow<List<Scenario>>
    suspend fun saveScenario(scenario: Scenario)
    suspend fun deleteScenario(scenarioId: String)
    suspend fun clearLocalData() {}
}
