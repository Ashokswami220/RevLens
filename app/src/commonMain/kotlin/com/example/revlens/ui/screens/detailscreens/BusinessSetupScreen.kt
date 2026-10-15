package com.example.revlens.ui.screens.detailscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.example.revlens.model.BigDecimal
import com.example.revlens.model.toBigDecimalOrNull
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.NumberField
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import com.example.revlens.utils.CurrencyUtils

data class EditablePlan(
    val id: String,
    val name: String,
    val priceStr: String,
    val customerCountStr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(
    viewModel: MainViewModel = koinViewModel(),
    setupViewModel: BusinessSetupViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val uiState by setupViewModel.uiState.collectAsState()

    val currentCurrency = CurrencyUtils.getCurrency(uiState.currencyCode)
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

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
            SectionCard(title = "Currency") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .border(
                                1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp)
                            )
                            .clickable { currencyDropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Base Currency",
                                style = RevLensTypography.labelMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentCurrency.name} (${currentCurrency.code})",
                                style = RevLensTypography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Currency",
                            tint = RevLensTheme.colors.textPrimary
                        )
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        DropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false },
                            modifier = Modifier
                                .background(RevLensTheme.colors.surface)
                                .border(
                                    1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp)
                                )
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                CurrencyUtils.supportedCurrencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = curr.name,
                                                    fontWeight = if (curr.code == uiState.currencyCode) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (curr.code == uiState.currencyCode) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = curr.symbol,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RevLensTheme.colors.brandPrimary
                                                )
                                            }
                                        },
                                        onClick = {
                                            setupViewModel.updateCurrencyCode(curr.code)
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

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
                        suffix = currentCurrency.symbol
                    )

                    NumberField(
                        value = uiState.fixedCostsStr,
                        onValueChange = { setupViewModel.updateFixedCosts(it) },
                        label = "Monthly Fixed Costs",
                        suffix = currentCurrency.symbol
                    )

                    NumberField(
                        value = uiState.varCostsStr,
                        onValueChange = { setupViewModel.updateVarCosts(it) },
                        label = "Variable Cost per User",
                        suffix = currentCurrency.symbol
                    )
                }
            }

            SectionCard(title = "Pricing Plans") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.editablePlans.forEachIndexed { index, plan ->
                        SectionCard(
                            title = if (plan.name.isNotBlank()) plan.name else "Plan ${index + 1}",
                            trailingAction = if (uiState.editablePlans.size > 1) {
                                {
                                    IconButton(
                                        onClick = { setupViewModel.removePlan(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Remove Plan",
                                            tint = RevLensTheme.colors.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                RevLensTextField(
                                    value = plan.name,
                                    onValueChange = { newVal ->
                                        setupViewModel.updatePlanName(index, newVal)
                                    },
                                    label = "Plan Name",
                                    placeholder = when (index) {
                                        0 -> "e.g. Starter"
                                        1 -> "e.g. Pro"
                                        else -> "e.g. Plan ${index + 1}"
                                    }
                                )
                                NumberField(
                                    value = plan.priceStr,
                                    onValueChange = { newVal ->
                                        setupViewModel.updatePlanPrice(
                                            index, newVal
                                        )
                                    },
                                    label = "Monthly Price",
                                    placeholder = "e.g. 29",
                                    suffix = currentCurrency.symbol
                                )
                                NumberField(
                                    value = plan.customerCountStr,
                                    onValueChange = { newVal ->
                                        setupViewModel.updatePlanCustomers(
                                            index, newVal
                                        )
                                    },
                                    label = "Active Customers",
                                    placeholder = "e.g. 100",
                                    suffix = "users"
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { setupViewModel.addPlan() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RevLensTheme.colors.surfaceElevated,
                            contentColor = RevLensTheme.colors.brandPrimary
                        ),
                        border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Another Plan",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
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
                            currencyCode = uiState.currencyCode,
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
                                        name = editablePlan.name.ifEmpty { originalPlan.name },
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
                                            ?: BigDecimal.ZERO,
                                        customerCount = editablePlan.customerCountStr.toIntOrNull()
                                            ?: 0
                                    )
                                }
                            }
                        )
                    } else {
                        com.example.revlens.model.BusinessProfile(
                            currencyCode = uiState.currencyCode,
                            monthlyChurnRate = uiState.churnRateStr.toDoubleOrNull()
                                ?.div(100.0) ?: 0.0,
                            monthlyGrowthRate = uiState.growthRateStr.toDoubleOrNull()
                                ?.div(100.0) ?: 0.0,
                            cac = uiState.cacStr.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                            fixedCosts = uiState.fixedCostsStr.toBigDecimalOrNull()
                                ?: BigDecimal.ZERO,
                            variableCostPerCustomer = uiState.varCostsStr.toBigDecimalOrNull()
                                ?: BigDecimal.ZERO,
                            plans = uiState.editablePlans.map { editablePlan ->
                                com.example.revlens.model.Plan(
                                    id = editablePlan.id,
                                    name = editablePlan.name,
                                    price = editablePlan.priceStr.toBigDecimalOrNull()
                                        ?: BigDecimal.ZERO,
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
