package com.example.revlens.ui.scenario

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.BreakEvenCalculator
import com.example.revlens.domain.calc.LtvCalculator
import com.example.revlens.domain.calc.PricingSimulationEngine
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.Scenario
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.ChoiceChip
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun ScenarioComparisonScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val scenarios by viewModel.scenarios.collectAsState()
    val profile by viewModel.profile.collectAsState()

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US)
            .apply {
                currency = Currency.getInstance("USD")
                maximumFractionDigits = 0
            }
    }

    val percentFormatter = remember {
        NumberFormat.getPercentInstance(Locale.US)
            .apply {
                minimumFractionDigits = 1
                maximumFractionDigits = 1
            }
    }

    // Default to the first two scenarios
    val selectedScenarioIds = remember(scenarios) {
        val initial = scenarios.take(3)
            .map { it.id }
        val state = mutableStateListOf<String>()
        state.addAll(initial)
        state
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Compare",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Choice Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val availableColors = listOf(
                    RevLensTheme.colors.brandPrimary,
                    RevLensTheme.colors.brandSecondary,
                    RevLensTheme.colors.brandTertiary
                )
                scenarios.forEachIndexed { index, scenario ->
                    val isSelected = selectedScenarioIds.contains(scenario.id)
                    val color = availableColors[index % availableColors.size]
                    ChoiceChip(
                        text = scenario.name,
                        selected = isSelected,
                        colorDot = color,
                        onClick = {
                            if (isSelected) {
                                if (selectedScenarioIds.size > 1) {
                                    selectedScenarioIds.remove(scenario.id)
                                }
                            } else {
                                if (selectedScenarioIds.size < 3) {
                                    selectedScenarioIds.add(scenario.id)
                                }
                            }
                        }
                    )
                }
            }

            // Chart Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RevLensTheme.colors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chart Placeholder (Overlay Line Chart)",
                    color = RevLensTheme.colors.textSecondary,
                    style = RevLensTypography.bodyMedium
                )
            }

            // Comparison Table
            if (profile != null) {
                val selectedScenarios = scenarios.filter { selectedScenarioIds.contains(it.id) }
                ComparisonTable(
                    profile = profile!!,
                    scenarios = selectedScenarios,
                    currencyFormatter = currencyFormatter,
                    percentFormatter = percentFormatter
                )
            }
        }
    }
}

@Composable
private fun ComparisonTable(
    profile: BusinessProfile,
    scenarios: List<Scenario>,
    currencyFormatter: NumberFormat,
    percentFormatter: NumberFormat
) {
    val results = scenarios.map { scenario ->
        PricingSimulationEngine.simulate(scenario.baselineProfile, scenario.assumptions)
    }

    val availableColors = listOf(
        RevLensTheme.colors.brandPrimary,
        RevLensTheme.colors.brandSecondary,
        RevLensTheme.colors.brandTertiary
    )

    SectionCard(title = "Metrics Comparison") {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            // First column (Labels)
            Column(modifier = Modifier.width(100.dp)) {
                Spacer(modifier = Modifier.height(48.dp)) // Header space
                TableLabel("MRR (12m)")
                TableLabel("ARR")
                TableLabel("Churn")
                TableLabel("LTV")
                TableLabel("Payback")
            }

            // Data columns
            scenarios.forEachIndexed { index, scenario ->
                val result = results[index]
                val color = availableColors[scenarios.indexOf(scenario) % availableColors.size]
                Column(
                    modifier = Modifier.width(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                        Text(
                            text = scenario.name,
                            style = RevLensTypography.labelLarge,
                            color = RevLensTheme.colors.textPrimary,
                            maxLines = 1
                        )
                    }

                    val arr = result.currentMrr.multiply(BigDecimal(12))
                    val ltv = LtvCalculator.calculateLtv(result.arpu, result.monthlyChurnRate)
                    val payback = BreakEvenCalculator.calculateCacPaybackPeriod(result)

                    // Values
                    TableValue(currencyFormatter.format(result.currentMrr))
                    TableValue(currencyFormatter.format(arr))
                    TableValue(percentFormatter.format(result.monthlyChurnRate))
                    TableValue(currencyFormatter.format(ltv))
                    TableValue(
                        if (payback != null) String.format(Locale.US, "%.1f mo", payback) else "N/A"
                    )
                }
            }
        }
    }
}

@Composable
private fun TableLabel(text: String) {
    Box(
        modifier = Modifier.height(48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = RevLensTypography.bodyMedium,
            color = RevLensTheme.colors.textSecondary
        )
    }
}

@Composable
private fun TableValue(text: String) {
    Box(
        modifier = Modifier.height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = RevLensTypography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )
    }
}
