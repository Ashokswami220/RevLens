package com.example.revlens.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import com.example.revlens.model.Scenario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository
) : ViewModel() {

    // The single source of truth for the app's current business profile
    val profile: StateFlow<BusinessProfile?> = repository.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val scenarios: StateFlow<List<Scenario>> = scenarioRepository.scenariosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateProfile(updatedProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
        }
    }

    // Helper functions for common edits to avoid full object recreation in UI
    fun addPlan(plan: Plan) {
        val current = profile.value ?: return
        val newPlans = current.plans + plan
        updateProfile(current.copy(plans = newPlans))
    }

    fun updatePlan(updatedPlan: Plan) {
        val current = profile.value ?: return
        val newPlans = current.plans.map { if (it.id == updatedPlan.id) updatedPlan else it }
        updateProfile(current.copy(plans = newPlans))
    }

    fun removePlan(planId: String) {
        val current = profile.value ?: return
        val newPlans = current.plans.filter { it.id != planId }
        updateProfile(current.copy(plans = newPlans))
    }

    fun updateCac(newCac: BigDecimal) {
        profile.value?.let { current ->
            updateProfile(current.copy(cac = newCac))
        }
    }

    fun updateFixedCosts(newCosts: BigDecimal) {
        profile.value?.let { current ->
            updateProfile(current.copy(fixedCosts = newCosts))
        }
    }

    fun saveScenario(scenario: Scenario) {
        viewModelScope.launch {
            scenarioRepository.saveScenario(scenario)
        }
    }

    fun deleteScenario(scenarioId: String) {
        viewModelScope.launch {
            scenarioRepository.deleteScenario(scenarioId)
        }
    }
}
