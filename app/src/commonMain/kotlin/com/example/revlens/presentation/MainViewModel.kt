package com.example.revlens.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revlens.data.ProfileRepository
import com.example.revlens.data.ScenarioRepository
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Scenario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ProfileRepository,
    private val scenarioRepository: ScenarioRepository
) : ViewModel() {

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

    val isOnboardingCompleted: StateFlow<Boolean?> = repository.isOnboardingCompletedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun updateProfile(updatedProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(updatedProfile)
        }
    }

    fun completeOnboarding(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            repository.setOnboardingCompleted(true)
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
