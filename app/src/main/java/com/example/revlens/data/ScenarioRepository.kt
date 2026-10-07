package com.example.revlens.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.revlens.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ScenarioRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val SCENARIOS_KEY = stringPreferencesKey("saved_scenarios")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val scenariosFlow: Flow<List<Scenario>> = dataStore.data.map { preferences ->
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

    suspend fun saveScenario(scenario: Scenario) {
        dataStore.edit { preferences ->
            val currentJson = preferences[SCENARIOS_KEY]
            val currentList = if (currentJson != null) {
                try {
                    json.decodeFromString<List<Scenario>>(currentJson).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }
            
            // Overwrite if exists, else add
            val index = currentList.indexOfFirst { it.id == scenario.id }
            if (index != -1) {
                currentList[index] = scenario
            } else {
                currentList.add(scenario)
            }
            
            preferences[SCENARIOS_KEY] = json.encodeToString(currentList)
        }
    }

    suspend fun deleteScenario(scenarioId: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[SCENARIOS_KEY]
            if (currentJson != null) {
                try {
                    val currentList = json.decodeFromString<List<Scenario>>(currentJson).toMutableList()
                    currentList.removeAll { it.id == scenarioId }
                    preferences[SCENARIOS_KEY] = json.encodeToString(currentList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
