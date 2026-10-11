package com.example.revlens.ui.screens.detailscreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.NumberField
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme


data class EditablePlan(
    val id: String,
    val name: String,
    val priceStr: String,
    val customerCountStr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(
    viewModel: MainViewModel = hiltViewModel(),
    setupViewModel: BusinessSetupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val uiState by setupViewModel.uiState.collectAsState()

    // Load initial values when profile becomes available
    LaunchedEffect(profile) {
        setupViewModel.initFromProfile(profile)
    }

    Scaffold(
        topBar = {
            RevLensTopAppBar(
                title = "Business Setup",
                onBackClick = onNavigateBack
            )
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Core Metrics") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value = uiState.churnRateStr,
                        onValueChange = { setupViewModel.updateChurnRate(it) },
                        label = "Monthly Churn Rate",
                        suffix = "%"
                    )

                    NumberField(
                        value = uiState.growthRateStr,
                        onValueChange = { setupViewModel.updateGrowthRate(it) },
                        label = "Monthly Growth Rate",
                        suffix = "%"
                    )
                }
            }

            SectionCard(title = "Costs & Expenses") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value = uiState.cacStr,
                        onValueChange = { setupViewModel.updateCac(it) },
                        label = "Customer Acquisition Cost (CAC)",
                        suffix = "$"
                    )

                    NumberField(
                        value = uiState.fixedCostsStr,
                        onValueChange = { setupViewModel.updateFixedCosts(it) },
                        label = "Monthly Fixed Costs",
                        suffix = "$"
                    )

                    NumberField(
                        value = uiState.varCostsStr,
                        onValueChange = { setupViewModel.updateVarCosts(it) },
                        label = "Variable Cost per User",
                        suffix = "$"
                    )
                }
            }

            SectionCard(title = "Pricing Plans") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.editablePlans.forEachIndexed { index, plan ->
                        SectionCard(
                            title = plan.name,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                NumberField(
                                    value = plan.priceStr,
                                    onValueChange = { newVal ->
                                        setupViewModel.updatePlanPrice(
                                            index, newVal
                                        )
                                    },
                                    label = "Monthly Price",
                                    suffix = "$"
                                )
                                NumberField(
                                    value = plan.customerCountStr,
                                    onValueChange = { newVal ->
                                        setupViewModel.updatePlanCustomers(
                                            index, newVal
                                        )
                                    },
                                    label = "Active Customers",
                                    suffix = ""
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save Data",
                onClick = {
                    val currentProfile = profile
                    val newProfile = if (currentProfile != null) {
                        currentProfile.copy(
                            monthlyChurnRate = uiState.churnRateStr.toDoubleOrNull()
                                ?.div(100.0) ?: currentProfile.monthlyChurnRate,
                            monthlyGrowthRate = uiState.growthRateStr.toDoubleOrNull()
                                ?.div(100.0) ?: currentProfile.monthlyGrowthRate,
                            cac = uiState.cacStr.toBigDecimalOrNull() ?: currentProfile.cac,
                            fixedCosts = uiState.fixedCostsStr.toBigDecimalOrNull()
                                ?: currentProfile.fixedCosts,
                            variableCostPerCustomer = uiState.varCostsStr.toBigDecimalOrNull()
                                ?: currentProfile.variableCostPerCustomer,
                            plans = uiState.editablePlans.map { editablePlan ->
                                val originalPlan =
                                    currentProfile.plans.find { it.id == editablePlan.id }
                                if (originalPlan != null) {
                                    originalPlan.copy(
                                        price = editablePlan.priceStr.toBigDecimalOrNull()
                                            ?: originalPlan.price,
                                        customerCount = editablePlan.customerCountStr.toIntOrNull()
                                            ?: originalPlan.customerCount
                                    )
                                } else {
                                    com.example.revlens.model.Plan(
                                        id = editablePlan.id,
                                        name = editablePlan.name,
                                        price = editablePlan.priceStr.toBigDecimalOrNull()
                                            ?: java.math.BigDecimal.ZERO,
                                        customerCount = editablePlan.customerCountStr.toIntOrNull()
                                            ?: 0
                                    )
                                }
                            }
                        )
                    } else {
                        com.example.revlens.model.BusinessProfile(
                            monthlyChurnRate = uiState.churnRateStr.toDoubleOrNull()
                                ?.div(100.0) ?: 0.0,
                            monthlyGrowthRate = uiState.growthRateStr.toDoubleOrNull()
                                ?.div(100.0) ?: 0.0,
                            cac = uiState.cacStr.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO,
                            fixedCosts = uiState.fixedCostsStr.toBigDecimalOrNull()
                                ?: java.math.BigDecimal.ZERO,
                            variableCostPerCustomer = uiState.varCostsStr.toBigDecimalOrNull()
                                ?: java.math.BigDecimal.ZERO,
                            plans = uiState.editablePlans.map { editablePlan ->
                                com.example.revlens.model.Plan(
                                    id = editablePlan.id,
                                    name = editablePlan.name,
                                    price = editablePlan.priceStr.toBigDecimalOrNull()
                                        ?: java.math.BigDecimal.ZERO,
                                    customerCount = editablePlan.customerCountStr.toIntOrNull() ?: 0
                                )
                            }
                        )
                    }
                    viewModel.updateProfile(newProfile)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
