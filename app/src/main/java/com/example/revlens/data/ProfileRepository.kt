package com.example.revlens.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.revlens.domain.model.BusinessProfile
import com.example.revlens.domain.model.Plan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val PROFILE_KEY = stringPreferencesKey("business_profile")
    }

    private val json = Json { ignoreUnknownKeys = true }

    // Fallback default profile if nothing is saved
    private val defaultProfile = BusinessProfile(
        monthlyChurnRate = 0.02,
        monthlyGrowthRate = 0.05,
        cac = BigDecimal("150.00"),
        fixedCosts = BigDecimal("5000.00"),
        variableCostPerCustomer = BigDecimal("15.00"),
        plans = listOf(
            Plan(name = "Starter", price = BigDecimal("49.00"), customerCount = 100),
            Plan(name = "Pro", price = BigDecimal("99.00"), customerCount = 50)
        )
    )

    val profileFlow: Flow<BusinessProfile> = dataStore.data.map { preferences ->
        val profileJson = preferences[PROFILE_KEY]
        if (profileJson != null) {
            try {
                json.decodeFromString(profileJson)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultProfile
            }
        } else {
            defaultProfile
        }
    }

    suspend fun saveProfile(profile: BusinessProfile) {
        dataStore.edit { preferences ->
            val profileJson = json.encodeToString(profile)
            preferences[PROFILE_KEY] = profileJson
        }
    }
}
