package com.example.revlens.ui.screens.detailscreens

import androidx.lifecycle.ViewModel
import com.example.revlens.model.BusinessProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BusinessSetupViewModel : ViewModel() {

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
                currencyCode = profile.currencyCode,
                editablePlans = profile.plans.map { plan ->
                    EditablePlan(
                        plan.id, plan.name, plan.price.toPlainString(),
                        plan.customerCount.toString()
                    )
                }
            )
        }
    }

    fun updateCurrencyCode(value: String) = _uiState.update { it.copy(currencyCode = value) }
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
    val currencyCode: String = "USD",
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
