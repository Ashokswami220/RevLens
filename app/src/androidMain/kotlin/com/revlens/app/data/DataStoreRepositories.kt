package com.revlens.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreProfileRepository(
    private val dataStore: DataStore<Preferences>
) : ProfileRepository {
    companion object {
        val PROFILE_KEY = stringPreferencesKey("business_profile")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        val INTRO_SEEN_KEY = booleanPreferencesKey("intro_seen")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override val profileFlow: Flow<BusinessProfile?> = dataStore.data.map { preferences ->
        val profileJson = preferences[PROFILE_KEY]
        if (profileJson != null) {
            try {
                json.decodeFromString(profileJson)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    override suspend fun saveProfile(profile: BusinessProfile) {
        dataStore.edit { preferences ->
            val profileJson = json.encodeToString(profile)
            preferences[PROFILE_KEY] = profileJson
        }
    }

    override val isOnboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] == true
    }

    override suspend fun clearLocalData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            if (completed) {
                preferences[ONBOARDING_COMPLETED_KEY] = true
            } else {
                preferences.remove(ONBOARDING_COMPLETED_KEY)
                preferences.remove(PROFILE_KEY)
            }
        }
    }

    override val hasSeenIntroFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INTRO_SEEN_KEY] == true
    }

    override suspend fun setHasSeenIntro(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[INTRO_SEEN_KEY] = seen
        }
    }
}

class DataStoreScenarioRepository(
    private val dataStore: DataStore<Preferences>
) : ScenarioRepository {
    companion object {
        val SCENARIOS_KEY = stringPreferencesKey("saved_scenarios")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override val scenariosFlow: Flow<List<Scenario>> = dataStore.data.map { preferences ->
        val jsonStr = preferences[SCENARIOS_KEY]
        if (jsonStr != null) {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun saveScenario(scenario: Scenario) {
        dataStore.edit { preferences ->
            val currentJson = preferences[SCENARIOS_KEY]
            val currentList = if (currentJson != null) {
                try {
                    json.decodeFromString<List<Scenario>>(currentJson)
                        .toMutableList()
                } catch (_: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            val index = currentList.indexOfFirst { it.id == scenario.id }
            if (index != -1) {
                currentList[index] = scenario
            } else {
                currentList.add(scenario)
            }

            preferences[SCENARIOS_KEY] = json.encodeToString(currentList)
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[SCENARIOS_KEY]
            if (currentJson != null) {
                try {
                    val currentList = json.decodeFromString<List<Scenario>>(currentJson)
                        .toMutableList()
                    currentList.removeAll { it.id == scenarioId }
                    preferences[SCENARIOS_KEY] = json.encodeToString(currentList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
