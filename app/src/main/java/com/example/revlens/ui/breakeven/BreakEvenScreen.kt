package com.example.revlens.ui.breakeven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.BreakEvenCalculator
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.LabeledSlider
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.components.SegmentedTabs
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakEvenScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onNavigateToWhatIf: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    var cacSimulated by remember { mutableStateOf(profile?.cac?.toFloat() ?: 100f) }
    var fixedCostsSimulated by remember { mutableStateOf(profile?.fixedCosts?.toFloat() ?: 5000f) }
    var variableCostSimulated by remember {
        mutableStateOf(
            profile?.variableCostPerCustomer?.toFloat() ?: 10f
        )
    }

    // If profile changes, we might want to update initial sliders, but for simplicity we rely on initial value.

    val simulatedProfile by remember(
        profile, cacSimulated, fixedCostsSimulated, variableCostSimulated
    ) {
        derivedStateOf {
            profile?.copy(
                cac = BigDecimal(cacSimulated.toString()),
                fixedCosts = BigDecimal(fixedCostsSimulated.toString()),
                variableCostPerCustomer = BigDecimal(variableCostSimulated.toString())
            )
        }
    }

    val cacPayback = remember(simulatedProfile) {
        simulatedProfile?.let { BreakEvenCalculator.calculateCacPaybackPeriod(it) }
    }

    val breakEvenCustomers = remember(simulatedProfile) {
        simulatedProfile?.let { BreakEvenCalculator.calculateBreakEvenCustomers(it) }
    }

    Scaffold(
        topBar = {
            RevLensTopAppBar(
                title = "Break-Even Calculator",
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
            // Segmented Tabs
            SegmentedTabs(
                options = listOf("Pricing", "What-If", "Break-Even"),
                selectedIndex = 2,
                onOptionSelected = { index ->
                    when (index) {
                        0 -> onNavigateToPricing()
                        1 -> onNavigateToWhatIf()
                    }
                }
            )

            SectionCard(title = "Break-Even Results") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val breakEven = breakEvenCustomers ?: 0
                    val maxCustomers = if (breakEven > 0) breakEven * 2 else 100
                    
                    val revenueData = mutableListOf<Float>()
                    val costData = mutableListOf<Float>()
                    
                    simulatedProfile?.let { p ->
                        val arpu = p.currentMrr.toFloat() / (if (p.totalCustomers > 0) p.totalCustomers else 1)
                        for (i in 0..10) {
                            val customers = (maxCustomers * i / 10f).toInt()
                            val revenue = arpu * customers
                            val costs = p.fixedCosts.toFloat() + (p.variableCostPerCustomer.toFloat() * customers)
                            revenueData.add(revenue)
                            costData.add(costs)
                        }
                    }

                    if (revenueData.isNotEmpty()) {
                        com.example.revlens.ui.components.RevLensLineChart(
                            data = listOf(revenueData, costData),
                            colors = listOf(RevLensTheme.colors.brandPrimary, RevLensTheme.colors.error)
                        )
                        com.example.revlens.ui.components.ChartLegend(
                            items = listOf(
                                "Revenue" to RevLensTheme.colors.brandPrimary,
                                "Costs" to RevLensTheme.colors.error
                            )
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    }

                    val paybackStr =
                        cacPayback?.let { String.format(Locale.US, "%.1f months", it) } ?: "Never"
                    ResultRow(label = "CAC Payback Period", value = paybackStr)

                    val customersStr = breakEvenCustomers?.let { "$it customers" } ?: "Never"
                    ResultRow(label = "Break-Even Customers", value = customersStr)
                }
            }

            SectionCard(title = "Simulate Costs") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledSlider(
                        label = "Simulated CAC",
                        value = cacSimulated,
                        onValueChange = { cacSimulated = it },
                        valueRange = 0f..2000f,
                        valueString = "$${cacSimulated.toInt()}",
                        caption = "Cost to acquire a single customer"
                    )

                    LabeledSlider(
                        label = "Simulated Fixed Costs",
                        value = fixedCostsSimulated,
                        onValueChange = { fixedCostsSimulated = it },
                        valueRange = 0f..50000f,
                        valueString = "$${fixedCostsSimulated.toInt()}",
                        caption = "Monthly base expenses"
                    )

                    LabeledSlider(
                        label = "Simulated Variable Cost",
                        value = variableCostSimulated,
                        onValueChange = { variableCostSimulated = it },
                        valueRange = 0f..500f,
                        valueString = "$${variableCostSimulated.toInt()}",
                        caption = "Cost per customer"
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = RevLensTypography.bodyLarge,
            color = RevLensTheme.colors.textSecondary
        )
        Text(
            text = value,
            style = RevLensTypography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )
    }
}
