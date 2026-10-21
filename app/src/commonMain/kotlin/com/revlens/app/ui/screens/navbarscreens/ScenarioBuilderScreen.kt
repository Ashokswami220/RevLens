package com.revlens.app.ui.screens.navbarscreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.PricingSimulationEngine
import com.revlens.app.model.Scenario
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import com.revlens.app.utils.CurrencyUtils
import com.revlens.app.utils.RevLensFormatter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioBuilderScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToMetrics: () -> Unit,
    onNavigateToExperiments: () -> Unit = {}
) {
    val scenarios by viewModel.scenarios.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val isViewer = userRole == com.revlens.app.domain.model.Role.Viewer

    val profile by viewModel.profile.collectAsState()
    val currencyFormatter = remember(profile?.currencyCode) {
        CurrencyUtils.getFormatter(profile?.currencyCode)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Scenarios",
                onBackClick = null,
                actions = {
                    IconButton(onClick = onNavigateToExperiments) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = "Experiments",
                            tint = RevLensTheme.colors.brandPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToMetrics) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Metrics",
                            tint = RevLensTheme.colors.textPrimary
                        )
                    }
                }
            )
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

            if (!isViewer) {
                ActionCardButton(
                    text = "Create New Scenario",
                    icon = Icons.Outlined.AddCircleOutline,
                    onClick = onNavigateToCreate
                )
            }

            if (scenarios.size >= 2) {
                ActionCardButton(
                    text = "Compare Scenarios",
                    icon = Icons.AutoMirrored.Outlined.CompareArrows,
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
                            canDelete = !isViewer,
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
    currencyFormatter: RevLensFormatter,
    canDelete: Boolean = true,
    onDelete: () -> Unit
) {
    SectionCard(title = scenario.name) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Projected MRR
            val projected =
                PricingSimulationEngine.simulate(scenario.baselineProfile, scenario.assumptions)

            Text(
                text = "Projected MRR: ${currencyFormatter.format(projected.currentMrr)}",
                style = RevLensTypography.bodyLarge,
                color = RevLensTheme.colors.textPrimary
            )

            Text(
                text = "Assumptions: Price(${scenario.assumptions.priceChangePercent * 100}%), Churn(${scenario.assumptions.churnChangePercent * 100}%), CAC(${scenario.assumptions.cacChange})",
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textSecondary
            )

            if (canDelete) {
                PrimaryButton(
                    text = "Delete",
                    onClick = onDelete
                )
            }
        }
    }
}
