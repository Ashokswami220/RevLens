package com.example.revlens.ui.scenario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.PricingSimulationEngine
import com.example.revlens.model.Scenario
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioBuilderScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToCompare: () -> Unit
) {
    val scenarios by viewModel.scenarios.collectAsState()
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("USD")
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            RevLensTopAppBar(title = "Scenarios")
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            PrimaryButton(
                text = "+ Create New Scenario",
                onClick = onNavigateToCreate
            )

            if (scenarios.size >= 2) {
                PrimaryButton(
                    text = "Compare Scenarios",
                    onClick = onNavigateToCompare
                )
            }

            if (scenarios.isEmpty()) {
                Text(
                    text = "No scenarios created yet. Build one to see how changes impact your metrics.",
                    style = RevLensTypography.bodyLarge,
                    color = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scenarios, key = { it.id }) { scenario ->
                        ScenarioCard(
                            scenario = scenario,
                            currencyFormatter = currencyFormatter,
                            onDelete = { viewModel.deleteScenario(scenario.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: Scenario,
    currencyFormatter: NumberFormat,
    onDelete: () -> Unit
) {
    SectionCard(title = scenario.name) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Projected MRR
            val projected = PricingSimulationEngine.simulate(scenario.baselineProfile, scenario.assumptions)
            
            Text(
                text = "Projected MRR: ${currencyFormatter.format(projected.currentMrr)}",
                style = RevLensTypography.bodyLarge,
                color = RevLensTheme.colors.textPrimary
            )
            
            Text(
                text = "Assumptions: Price(${scenario.assumptions.priceChangePercent*100}%), Churn(${scenario.assumptions.churnChangePercent*100}%), CAC(${scenario.assumptions.cacChange})",
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textSecondary
            )
            
            PrimaryButton(
                text = "Delete",
                onClick = onDelete
            )
        }
    }
}
