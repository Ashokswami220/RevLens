package com.example.revlens.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.model.BusinessProfile
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.NumberField
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    
    var churnRateStr by remember { mutableStateOf("") }
    var growthRateStr by remember { mutableStateOf("") }
    var cacStr by remember { mutableStateOf("") }
    var fixedCostsStr by remember { mutableStateOf("") }
    var varCostsStr by remember { mutableStateOf("") }
    
    // Load initial values when profile becomes available
    LaunchedEffect(profile) {
        profile?.let {
            if (churnRateStr.isEmpty()) churnRateStr = (it.monthlyChurnRate * 100).toString()
            if (growthRateStr.isEmpty()) growthRateStr = (it.monthlyGrowthRate * 100).toString()
            if (cacStr.isEmpty()) cacStr = it.cac.toPlainString()
            if (fixedCostsStr.isEmpty()) fixedCostsStr = it.fixedCosts.toPlainString()
            if (varCostsStr.isEmpty()) varCostsStr = it.variableCostPerCustomer.toPlainString()
        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Core Metrics") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value = churnRateStr,
                        onValueChange = { churnRateStr = it },
                        label = "Monthly Churn Rate",
                        suffix = "%"
                    )
                    
                    NumberField(
                        value = growthRateStr,
                        onValueChange = { growthRateStr = it },
                        label = "Monthly Growth Rate",
                        suffix = "%"
                    )
                }
            }

            SectionCard(title = "Costs & Expenses") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value = cacStr,
                        onValueChange = { cacStr = it },
                        label = "Customer Acquisition Cost (CAC)",
                        suffix = "$"
                    )
                    
                    NumberField(
                        value = fixedCostsStr,
                        onValueChange = { fixedCostsStr = it },
                        label = "Monthly Fixed Costs",
                        suffix = "$"
                    )
                    
                    NumberField(
                        value = varCostsStr,
                        onValueChange = { varCostsStr = it },
                        label = "Variable Cost per User",
                        suffix = "$"
                    )
                }
            }

            SectionCard(title = "Pricing Plans") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile?.plans?.forEach { plan ->
                        com.example.revlens.ui.components.SectionCard(
                            title = plan.name,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\$${plan.price} / ${plan.billingPeriod.name.lowercase()}",
                                style = RevLensTypography.bodyLarge,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Text(
                                text = "${plan.customerCount} Customers",
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textTertiary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save Data",
                onClick = {
                    val currentProfile = profile
                    if (currentProfile != null) {
                        val newProfile = currentProfile.copy(
                            monthlyChurnRate = churnRateStr.toDoubleOrNull()?.div(100.0) ?: currentProfile.monthlyChurnRate,
                            monthlyGrowthRate = growthRateStr.toDoubleOrNull()?.div(100.0) ?: currentProfile.monthlyGrowthRate,
                            cac = cacStr.toBigDecimalOrNull() ?: currentProfile.cac,
                            fixedCosts = fixedCostsStr.toBigDecimalOrNull() ?: currentProfile.fixedCosts,
                            variableCostPerCustomer = varCostsStr.toBigDecimalOrNull() ?: currentProfile.variableCostPerCustomer
                        )
                        viewModel.updateProfile(newProfile)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
