package com.revlens.app.ui.screens.navbarscreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.PricingSimulationEngine
import com.revlens.app.model.Assumptions
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.RoundingMode
import com.revlens.app.model.Scenario
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.ChartLegend
import com.revlens.app.ui.components.ChoiceChip
import com.revlens.app.ui.components.DeltaChip
import com.revlens.app.ui.components.DeltaType
import com.revlens.app.ui.components.LabeledSlider
import com.revlens.app.ui.components.RevLensBarChart
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import com.revlens.app.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingSimulatorScreen(viewModel: MainViewModel) {
    val profile by viewModel.profile.collectAsState()

    var selectedPlanId by rememberSaveable { mutableStateOf<String?>(null) }

    // Sliders state
    var priceChangePercent by rememberSaveable { mutableStateOf(0f) }
    var churnChangePercent by rememberSaveable { mutableStateOf(0f) }

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var scenarioName by rememberSaveable { mutableStateOf("") }

    val currencyFormatter = remember(profile?.currencyCode) {
        CurrencyUtils.getFormatter(profile?.currencyCode)
    }

    // Assumptions object for simulation
    val assumptions by remember(priceChangePercent, churnChangePercent, selectedPlanId) {
        derivedStateOf {
            Assumptions(
                priceChangePercent = priceChangePercent / 100.0,
                churnChangePercent = churnChangePercent / 100.0,
                targetPlanId = selectedPlanId
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Segmented Tabs


        if (profile != null) {
            // Initialize selected plan if null or if previous plan no longer exists
            if (profile!!.plans.isNotEmpty() && (selectedPlanId == null || profile!!.plans.none { it.id == selectedPlanId })) {
                selectedPlanId = profile!!.plans.first().id
            }

            // Plan Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Plan",
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.textPrimary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    profile!!.plans.forEach { plan ->
                        ChoiceChip(
                            text = plan.name,
                            selected = selectedPlanId == plan.id,
                            onClick = { selectedPlanId = plan.id }
                        )
                    }
                }
            }

            // 1. New Projected MRR Hero Card (at the top)
            val projected = PricingSimulationEngine.simulate(profile!!, assumptions, selectedPlanId)
            val mrrDelta = if (profile!!.currentMrr > BigDecimal.ZERO) {
                (projected.currentMrr - profile!!.currentMrr)
                    .divide(profile!!.currentMrr, 4, RoundingMode.HALF_UP)
                    .toDouble() * 100
            } else 0.0

            val isDark = isSystemInDarkTheme()
            val cardBorder =
                if (isDark) null else BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
            val borderModifier = if (cardBorder != null) Modifier.border(
                cardBorder, RoundedCornerShape(16.dp)
            ) else Modifier

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RevLensTheme.colors.surface)
                    .then(borderModifier)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "New Projected MRR", style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            currencyFormatter.format(projected.currentMrr),
                            style = RevLensTypography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )

                        val deltaType = when {
                            mrrDelta > 0 -> DeltaType.POSITIVE
                            mrrDelta < 0 -> DeltaType.NEGATIVE
                            else -> DeltaType.NEUTRAL
                        }

                        val sign = if (mrrDelta > 0) "+" else ""
                        DeltaChip(
                            delta = "$sign${CurrencyUtils.formatDecimal(mrrDelta, 1)}%",
                            type = deltaType
                        )
                    }

                    // Before / After Chart
                    RevLensBarChart(
                        data = listOf(
                            profile!!.currentMrr.toFloat(),
                            projected.currentMrr.toFloat()
                        ),
                        colors = listOf(
                            RevLensTheme.colors.textTertiary,
                            if (mrrDelta >= 0) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.error
                        ),
                        barWidth = 64.dp
                    )
                    ChartLegend(
                        items = listOf(
                            "Current MRR" to RevLensTheme.colors.textTertiary,
                            "Projected MRR" to if (mrrDelta >= 0) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.error
                        )
                    )
                }
            }

            val currentPlan = profile!!.plans.find { it.id == selectedPlanId }

            if (currentPlan != null) {
                val currentPrice = currentPlan.price.toDouble()
                val newPrice = currentPrice * (1.0 + assumptions.priceChangePercent)

                // 2. Price Card (Compact, no redundant stacked labels)
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Price",
                                style = RevLensTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            val currSymbol = CurrencyUtils.getCurrency(profile?.currencyCode).symbol
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Current: $currSymbol${currentPrice.toInt()}",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textTertiary
                                )
                                Text(
                                    text = "→",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textTertiary
                                )
                                Text(
                                    text = "$currSymbol${newPrice.toInt()}",
                                    style = RevLensTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }
                        }

                        LabeledSlider(
                            label = "",
                            value = priceChangePercent,
                            onValueChange = { priceChangePercent = it },
                            valueRange = -50f..100f,
                            valueString = ""
                        )
                    }
                }

                // 3. Assumptions Card (Compact, no redundant stacked labels)
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Churn Impact",
                                style = RevLensTypography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            val churnSign = if (churnChangePercent > 0) "+" else ""
                            val churnColor = when {
                                churnChangePercent > 0 -> RevLensTheme.colors.error
                                churnChangePercent < 0 -> RevLensTheme.colors.success
                                else -> RevLensTheme.colors.textPrimary
                            }
                            Text(
                                text = "$churnSign${
                                    CurrencyUtils.formatDecimal(
                                        churnChangePercent.toDouble(), 1
                                    )
                                }%",
                                style = RevLensTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = churnColor
                            )
                        }

                        LabeledSlider(
                            label = "",
                            value = churnChangePercent,
                            onValueChange = { churnChangePercent = it },
                            valueRange = -10f..10f,
                            valueString = ""
                        )
                    }
                }
            }

            // 4. Save as Scenario Button
            ActionCardButton(
                text = "Save as Scenario",
                icon = Icons.Outlined.BookmarkAdd,
                onClick = { showSaveDialog = true }
            )
        } else {
            Text(
                "No profile data available. Please set up your business baseline.",
                color = RevLensTheme.colors.textSecondary
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    "Save Scenario", style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextField(
                        value = scenarioName,
                        onValueChange = { scenarioName = it },
                        label = { Text("Scenario Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    profile?.let { p ->
                        viewModel.saveScenario(
                            Scenario(
                                name = scenarioName.ifEmpty { "New Pricing Scenario" },
                                baselineProfile = p,
                                assumptions = assumptions
                            )
                        )
                    }
                    showSaveDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

