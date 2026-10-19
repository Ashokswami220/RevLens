package com.revlens.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import com.revlens.app.model.toBigDecimalOrNull
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.NumberField
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.screens.detailscreens.EditablePlan
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import com.revlens.app.utils.AppCurrency
import com.revlens.app.utils.CurrencyUtils
import kotlinx.coroutines.launch


@Composable
fun OnboardingDataScreen(
    onNavigateBack: () -> Unit,
    onFinishOnboarding: (BusinessProfile) -> Unit
) {
    // 0: Currency & Core Metrics, 1: Costs & Expenses, 2: Pricing Plans
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    // Form State - start blank so entry is mandatory
    var currencyCode by remember { mutableStateOf("USD") }
    var churnRateStr by remember { mutableStateOf("") }
    var growthRateStr by remember { mutableStateOf("") }

    var cacStr by remember { mutableStateOf("") }
    var fixedCostsStr by remember { mutableStateOf("") }
    var varCostsStr by remember { mutableStateOf("") }

    val editablePlans = remember {
        mutableStateListOf(
            EditablePlan(id = "1", name = "", priceStr = "", customerCountStr = "")
        )
    }

    val currentCurrency = remember(currencyCode) {
        CurrencyUtils.getCurrency(currencyCode)
    }

    // Step validation rules
    val isStep1Valid = churnRateStr.isNotBlank() &&
            churnRateStr.toDoubleOrNull() != null &&
            (churnRateStr.toDoubleOrNull() ?: -1.0) >= 0.0 &&
            growthRateStr.isNotBlank() &&
            growthRateStr.toDoubleOrNull() != null &&
            (growthRateStr.toDoubleOrNull() ?: -1.0) >= 0.0

    val isStep2Valid = cacStr.isNotBlank() &&
            cacStr.toBigDecimalOrNull() != null &&
            fixedCostsStr.isNotBlank() &&
            fixedCostsStr.toBigDecimalOrNull() != null &&
            varCostsStr.isNotBlank() &&
            varCostsStr.toBigDecimalOrNull() != null

    val isStep3Valid = editablePlans.isNotEmpty() && editablePlans.all { plan ->
        plan.name.isNotBlank() &&
                plan.priceStr.isNotBlank() &&
                plan.priceStr.toBigDecimalOrNull() != null &&
                plan.customerCountStr.isNotBlank() &&
                plan.customerCountStr.toIntOrNull() != null
    }

    val isCurrentStepValid = when (pagerState.currentPage) {
        0 -> isStep1Valid
        1 -> isStep2Valid
        2 -> isStep3Valid
        else -> true
    }

    fun buildProfile(): BusinessProfile {
        val churn = (churnRateStr.toDoubleOrNull() ?: 0.0) / 100.0
        val growth = (growthRateStr.toDoubleOrNull() ?: 0.0) / 100.0
        val cac = cacStr.toBigDecimalOrNull() ?: BigDecimal("0.00")
        val fixedCosts = fixedCostsStr.toBigDecimalOrNull() ?: BigDecimal("0.00")
        val varCosts = varCostsStr.toBigDecimalOrNull() ?: BigDecimal("0.00")

        val plans = editablePlans.mapIndexed { idx, p ->
            Plan(
                id = p.id.ifEmpty { (idx + 1).toString() },
                name = p.name.ifEmpty { "Plan ${idx + 1}" },
                price = p.priceStr.toBigDecimalOrNull() ?: BigDecimal("0.00"),
                customerCount = p.customerCountStr.toIntOrNull() ?: 0
            )
        }

        return BusinessProfile(
            monthlyChurnRate = churn,
            monthlyGrowthRate = growth,
            cac = cac,
            fixedCosts = fixedCosts,
            variableCostPerCustomer = varCosts,
            plans = plans,
            mrrGoal = BigDecimal("25000.00"),
            currencyCode = currencyCode
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RevLensTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header: Title and Circular Progress Indicator (1/3, 2/3, 3/3)
            val currentStepNumber = pagerState.currentPage + 1
            val progress = currentStepNumber / 3f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter Data",
                    style = RevLensTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                // Progress Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = RevLensTheme.colors.brandPrimary,
                        trackColor = RevLensTheme.colors.surfaceElevated,
                        strokeWidth = 3.5.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "$currentStepNumber/3",
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }
            }

            // Steps Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> Step1CurrencyAndCoreMetrics(
                        currencyCode = currencyCode,
                        onCurrencyChange = { currencyCode = it },
                        currentCurrency = currentCurrency,
                        churnRateStr = churnRateStr,
                        onChurnRateChange = { churnRateStr = it },
                        growthRateStr = growthRateStr,
                        onGrowthRateChange = { growthRateStr = it }
                    )

                    1 -> Step2CostAndExpenses(
                        cacStr = cacStr,
                        onCacChange = { cacStr = it },
                        fixedCostsStr = fixedCostsStr,
                        onFixedCostsChange = { fixedCostsStr = it },
                        varCostsStr = varCostsStr,
                        onVarCostsChange = { varCostsStr = it },
                        currencySymbol = currentCurrency.symbol
                    )

                    2 -> Step3PricingPlans(
                        editablePlans = editablePlans,
                        currencySymbol = currentCurrency.symbol,
                        onAddPlan = {
                            editablePlans.add(
                                EditablePlan(
                                    id = (editablePlans.size + 1).toString(),
                                    name = "",
                                    priceStr = "",
                                    customerCountStr = ""
                                )
                            )
                        },
                        onRemovePlan = { index ->
                            if (editablePlans.size > 1) {
                                editablePlans.removeAt(index)
                            }
                        }
                    )
                }
            }

            // Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Back Button
                val borderColor =
                    if (isDark) Color(0xFF2E3646) else RevLensTheme.colors.borderDefault
                val backgroundColor = if (isDark) Color(0xFF1E232E) else RevLensTheme.colors.surface

                Surface(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = backgroundColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RevLensTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Forward Action Button (disabled until fields are valid)
                ActionCardButton(
                    text = when (pagerState.currentPage) {
                        0 -> "Continue to Step 2"
                        1 -> "Continue to Step 3"
                        else -> "Complete Setup"
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    enabled = isCurrentStepValid,
                    onClick = {
                        if (!isCurrentStepValid) return@ActionCardButton
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishOnboarding(buildProfile())
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 1: CURRENCY & CORE METRICS
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step1CurrencyAndCoreMetrics(
    currencyCode: String,
    onCurrencyChange: (String) -> Unit,
    currentCurrency: AppCurrency,
    churnRateStr: String,
    onChurnRateChange: (String) -> Unit,
    growthRateStr: String,
    onGrowthRateChange: (String) -> Unit
) {
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Step 1: Currency & Core Metrics",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.brandPrimary
        )

        // Currency Card
        SectionCard(title = "Currency") {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RevLensTheme.colors.surfaceElevated)
                        .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp))
                        .clickable { currencyDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentCurrency.name} (${currentCurrency.code})",
                        style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select Currency",
                        tint = RevLensTheme.colors.textSecondary
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
                        CompositionLocalProvider(
                            LocalLayoutDirection provides LayoutDirection.Ltr
                        ) {
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
                                                fontWeight = if (curr.code == currencyCode) FontWeight.Bold else FontWeight.Normal,
                                                color = if (curr.code == currencyCode) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary
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
                                        onCurrencyChange(curr.code)
                                        currencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Core Metrics Card
        SectionCard(title = "Core Metrics") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NumberField(
                    value = churnRateStr,
                    onValueChange = onChurnRateChange,
                    label = "Monthly Churn Rate (%)",
                    placeholder = "e.g. 3.5",
                    suffix = "%"
                )

                NumberField(
                    value = growthRateStr,
                    onValueChange = onGrowthRateChange,
                    label = "Monthly Growth Rate (%)",
                    placeholder = "e.g. 8.0",
                    suffix = "%"
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 2: COST & EXPENSES
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step2CostAndExpenses(
    cacStr: String,
    onCacChange: (String) -> Unit,
    fixedCostsStr: String,
    onFixedCostsChange: (String) -> Unit,
    varCostsStr: String,
    onVarCostsChange: (String) -> Unit,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Step 2: Costs & Expenses",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.brandPrimary
        )

        SectionCard(title = "Acquisition & Operating Costs") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NumberField(
                    value = cacStr,
                    onValueChange = onCacChange,
                    label = "Customer Acquisition Cost (CAC)",
                    placeholder = "e.g. 150",
                    suffix = currencySymbol
                )

                NumberField(
                    value = fixedCostsStr,
                    onValueChange = onFixedCostsChange,
                    label = "Monthly Fixed Operating Cost",
                    placeholder = "e.g. 4500",
                    suffix = currencySymbol
                )

                NumberField(
                    value = varCostsStr,
                    onValueChange = onVarCostsChange,
                    label = "Variable Cost per Customer",
                    placeholder = "e.g. 12",
                    suffix = currencySymbol
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STEP 3: PRICING PLANS
// -------------------------------------------------------------------------------------------------
@Composable
private fun Step3PricingPlans(
    editablePlans: MutableList<EditablePlan>,
    currencySymbol: String,
    onAddPlan: () -> Unit,
    onRemovePlan: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Step 3: Pricing Plans",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.brandPrimary
        )

        SectionCard(title = "Plans") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                editablePlans.forEachIndexed { index, plan ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .border(
                                1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (plan.name.isNotBlank()) "Plan ${index + 1}: ${plan.name}" else "Plan ${index + 1}",
                                style = RevLensTypography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary
                            )

                            if (editablePlans.size > 1) {
                                IconButton(
                                    onClick = { onRemovePlan(index) },
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
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                RevLensTextField(
                                    value = plan.name,
                                    onValueChange = { newName ->
                                        editablePlans[index] = plan.copy(name = newName)
                                    },
                                    label = "Plan Name",
                                    placeholder = when (index) {
                                        0 -> "e.g. Starter"
                                        1 -> "e.g. Pro"
                                        else -> "e.g. Enterprise"
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                NumberField(
                                    value = plan.priceStr,
                                    onValueChange = { newPrice ->
                                        editablePlans[index] = plan.copy(priceStr = newPrice)
                                    },
                                    label = "Price",
                                    placeholder = "e.g. 29",
                                    suffix = currencySymbol
                                )
                            }
                        }

                        NumberField(
                            value = plan.customerCountStr,
                            onValueChange = { newCount ->
                                editablePlans[index] = plan.copy(customerCountStr = newCount)
                            },
                            label = "Active Customers",
                            placeholder = "e.g. 100",
                            suffix = "users"
                        )
                    }
                }

                Button(
                    onClick = onAddPlan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RevLensTheme.colors.surfaceElevated,
                        contentColor = RevLensTheme.colors.brandPrimary
                    ),
                    border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add, contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Another Plan",
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
