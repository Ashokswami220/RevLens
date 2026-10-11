package com.example.revlens.ui.screens.detailscreens

import androidx.lifecycle.ViewModel
import com.example.revlens.model.BusinessProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BusinessSetupViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessSetupUiState())
    val uiState: StateFlow<BusinessSetupUiState> = _uiState.asStateFlow()

    private var isInitialized = false

    fun initFromProfile(profile: BusinessProfile?) {
        if (isInitialized || profile == null) return
        isInitialized = true

        _uiState.update {
            it.copy(
                churnRateStr = (profile.monthlyChurnRate * 100).toString(),
                growthRateStr = (profile.monthlyGrowthRate * 100).toString(),
                cacStr = profile.cac.toPlainString(),
                fixedCostsStr = profile.fixedCosts.toPlainString(),
                varCostsStr = profile.variableCostPerCustomer.toPlainString(),
                editablePlans = profile.plans.map { plan ->
                    EditablePlan(
                        plan.id, plan.name, plan.price.toPlainString(),
                        plan.customerCount.toString()
                    )
                }
            )
        }
    }

    fun updateChurnRate(value: String) = _uiState.update { it.copy(churnRateStr = value) }
    fun updateGrowthRate(value: String) = _uiState.update { it.copy(growthRateStr = value) }
    fun updateCac(value: String) = _uiState.update { it.copy(cacStr = value) }
    fun updateFixedCosts(value: String) = _uiState.update { it.copy(fixedCostsStr = value) }
    fun updateVarCosts(value: String) = _uiState.update { it.copy(varCostsStr = value) }

    fun updatePlanPrice(index: Int, priceStr: String) {
        _uiState.update { state ->
            val newPlans = state.editablePlans.toMutableList()
            if (index in newPlans.indices) {
                newPlans[index] = newPlans[index].copy(priceStr = priceStr)
            }
            state.copy(editablePlans = newPlans)
        }
    }

    fun updatePlanCustomers(index: Int, customersStr: String) {
        _uiState.update { state ->
            val newPlans = state.editablePlans.toMutableList()
            if (index in newPlans.indices) {
                newPlans[index] = newPlans[index].copy(customerCountStr = customersStr)
            }
            state.copy(editablePlans = newPlans)
        }
    }
}

data class BusinessSetupUiState(
    val churnRateStr: String = "",
    val growthRateStr: String = "",
    val cacStr: String = "",
    val fixedCostsStr: String = "",
    val varCostsStr: String = "",
    val editablePlans: List<EditablePlan> = listOf(
        EditablePlan("1", "Starter", "", ""),
        EditablePlan("2", "Pro", "", "")
    )
)
