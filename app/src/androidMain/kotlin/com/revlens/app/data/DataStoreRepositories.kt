package com.revlens.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreProfileRepository(
    private val dataStore: DataStore<Preferences>
) : ProfileRepository {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val PROFILE_JSON_KEY = stringPreferencesKey("business_profile_json")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val HAS_SEEN_INTRO_KEY = booleanPreferencesKey("has_seen_intro")

        private fun userProfileBackupKey(userKey: String) =
            stringPreferencesKey(
                "user_profile_backup_${
                    userKey.trim()
                        .lowercase()
                }"
            )
    }

    private val _profileOverride = MutableStateFlow<BusinessProfile?>(null)
    private val _onboardingOverride = MutableStateFlow<Boolean?>(null)
    private val _introOverride = MutableStateFlow<Boolean?>(null)

    override val profileFlow: Flow<BusinessProfile?> = combine(
        dataStore.data,
        _profileOverride
    ) { prefs, overrideProfile ->
        if (overrideProfile != null) {
            overrideProfile
        } else {
            val raw = prefs[PROFILE_JSON_KEY]
            if (!raw.isNullOrBlank()) {
                runCatching { json.decodeFromString<BusinessProfile>(raw) }.getOrNull()
            } else {
                null
            }
        }
    }.distinctUntilChanged()

    override val isOnboardingCompletedFlow: Flow<Boolean> = combine(
        dataStore.data,
        _onboardingOverride
    ) { prefs, overrideCompleted ->
        overrideCompleted ?: (prefs[ONBOARDING_COMPLETED_KEY] ?: false)
    }.distinctUntilChanged()

    override val hasSeenIntroFlow: Flow<Boolean> = combine(
        dataStore.data,
        _introOverride
    ) { prefs, overrideIntro ->
        overrideIntro ?: (prefs[HAS_SEEN_INTRO_KEY] ?: false)
    }.distinctUntilChanged()

    override suspend fun saveProfile(profile: BusinessProfile) {
        _profileOverride.value = profile
        val encoded = json.encodeToString(BusinessProfile.serializer(), profile)
        dataStore.edit { prefs ->
            prefs[PROFILE_JSON_KEY] = encoded
        }
    }

    suspend fun saveUserProfileBackup(userKey: String, profile: BusinessProfile) {
        if (userKey.isBlank()) return
        val encoded = json.encodeToString(BusinessProfile.serializer(), profile)
        dataStore.edit { prefs ->
            prefs[userProfileBackupKey(userKey)] = encoded
        }
    }

    suspend fun getUserProfileBackup(userKey: String): BusinessProfile? {
        if (userKey.isBlank()) return null
        return try {
            val prefs = dataStore.data.first()
            val raw = prefs[userProfileBackupKey(userKey)]
            if (!raw.isNullOrBlank()) {
                json.decodeFromString<BusinessProfile>(raw)
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingOverride.value = completed
        if (!completed) {
            _profileOverride.value = null
        }
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = completed
            if (!completed) {
                prefs.remove(PROFILE_JSON_KEY)
            }
        }
    }

    override suspend fun setHasSeenIntro(seen: Boolean) {
        _introOverride.value = seen
        dataStore.edit { prefs ->
            prefs[HAS_SEEN_INTRO_KEY] = seen
        }
    }

    override suspend fun clearLocalData() {
        _profileOverride.value = null
        _onboardingOverride.value = false
        dataStore.edit { prefs ->
            prefs.remove(PROFILE_JSON_KEY)
            prefs[ONBOARDING_COMPLETED_KEY] = false
        }
    }
}

class DataStoreScenarioRepository(
    private val dataStore: DataStore<Preferences>
) : ScenarioRepository {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val SCENARIOS_JSON_KEY = stringPreferencesKey("scenarios_json")
    }

    override val scenariosFlow: Flow<List<Scenario>> = dataStore.data
        .map { prefs ->
            val raw = prefs[SCENARIOS_JSON_KEY]
            if (!raw.isNullOrBlank()) {
                runCatching { json.decodeFromString<List<Scenario>>(raw) }.getOrDefault(emptyList())
            } else {
                emptyList()
            }
        }
        .distinctUntilChanged()

    override suspend fun saveScenario(scenario: Scenario) {
        dataStore.edit { prefs ->
            val currentRaw = prefs[SCENARIOS_JSON_KEY]
            val currentList = if (!currentRaw.isNullOrBlank()) {
                runCatching { json.decodeFromString<List<Scenario>>(currentRaw) }.getOrDefault(
                    emptyList()
                )
            } else {
                emptyList()
            }
            val mutable = currentList.toMutableList()
            val index = mutable.indexOfFirst { it.id == scenario.id }
            if (index != -1) {
                mutable[index] = scenario
            } else {
                mutable.add(scenario)
            }
            prefs[SCENARIOS_JSON_KEY] = json.encodeToString(mutable)
        }
    }

    override suspend fun deleteScenario(scenarioId: String) {
        dataStore.edit { prefs ->
            val currentRaw = prefs[SCENARIOS_JSON_KEY]
            val currentList = if (!currentRaw.isNullOrBlank()) {
                runCatching { json.decodeFromString<List<Scenario>>(currentRaw) }.getOrDefault(
                    emptyList()
                )
            } else {
                emptyList()
            }
            val updated = currentList.filter { it.id != scenarioId }
            prefs[SCENARIOS_JSON_KEY] = json.encodeToString(updated)
        }
    }

    override suspend fun clearLocalData() {
        dataStore.edit { prefs ->
            prefs.remove(SCENARIOS_JSON_KEY)
        }
    }
}
