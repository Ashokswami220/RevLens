package com.revlens.app.ui.screens.navbarscreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.domain.calc.GrowthEngine
import com.revlens.app.domain.calc.PricingSimulationEngine
import com.revlens.app.domain.model.Role
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.Scenario
import com.revlens.app.model.ScenarioType
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import com.revlens.app.utils.CurrencyUtils
import com.revlens.app.utils.RevLensFormatter
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioBuilderScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToMetrics: () -> Unit,
    onNavigateToExperiments: () -> Unit = {},
    onOpenScenario: (Scenario) -> Unit = {}
) {
    val scenarios by viewModel.scenarios.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val isViewer = userRole == Role.Viewer

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
                            onDelete = { viewModel.deleteScenario(scenario.id) },
                            onOpen = { onOpenScenario(scenario) }
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
    onDelete: () -> Unit,
    onOpen: () -> Unit = {}
) {
    SectionCard(title = scenario.name) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Prominent scenario kind name
            Text(
                text = scenario.type.displayName().uppercase(),
                style = RevLensTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.brandPrimary,
                letterSpacing = 0.8.sp
            )

            // Projected MRR based on scenario type
            val projectedMrrText = if (scenario.type == ScenarioType.GROWTH && scenario.assumptions.growth != null) {
                val growthResult = GrowthEngine.simulate(
                    startingCustomers = scenario.baselineProfile.totalCustomers,
                    startingMrrMinor = (scenario.baselineProfile.currentMrr.toDouble() * 100).toLong(),
                    monthlyChurnPercent = scenario.baselineProfile.monthlyChurnRate,
                    assumptions = scenario.assumptions.growth
                )
                "Projected MRR (${scenario.assumptions.growth.timeHorizonMonths}mo): ${currencyFormatter.format(BigDecimal((growthResult.finalMrrMinor / 100).toString()))}"
            } else {
                val projected =
                    PricingSimulationEngine.simulate(scenario.baselineProfile, scenario.assumptions)
                "Projected MRR: ${currencyFormatter.format(projected.currentMrr)}"
            }

            Text(
                text = projectedMrrText,
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = RevLensTheme.colors.textPrimary
            )

            // Assumptions summary
            val assumptionsSummary = when (scenario.type) {
                ScenarioType.GROWTH -> {
                    val channelsCount = scenario.assumptions.growth?.channels?.count { it.isEnabled } ?: 0
                    val horizon = scenario.assumptions.growth?.timeHorizonMonths ?: 12
                    "Active Channels: $channelsCount • Horizon: ${horizon}mo • Referral: ${scenario.assumptions.growth?.referralRatePercent}%"
                }
                ScenarioType.PRICING -> {
                    "Price Change: ${(scenario.assumptions.priceChangePercent * 100).roundToInt()}% • Churn Impact: ${(scenario.assumptions.churnChangePercent * 100).roundToInt()}%"
                }
                ScenarioType.WHAT_IF -> {
                    "Growth: ${(scenario.assumptions.growthChangePercent * 100).roundToInt()}% • Churn: ${(scenario.assumptions.churnChangePercent * 100).roundToInt()}% • CAC: ${scenario.assumptions.cacChange}"
                }
            }

            Text(
                text = assumptionsSummary,
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )

            // Actions row: Open in Simulator and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RevLensTheme.colors.primaryAction,
                        contentColor = RevLensTheme.colors.onPrimaryAction
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Simulator", style = RevLensTypography.labelMedium)
                }

                if (canDelete) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = RevLensTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}
