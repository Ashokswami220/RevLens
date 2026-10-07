package com.example.revlens.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revlens.data.ProfileRepository
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Plan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    // The single source of truth for the app's current business profile
    val profile: StateFlow<BusinessProfile?> = repository.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
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
}
