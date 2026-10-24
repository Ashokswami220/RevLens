package com.revlens.app.ui.screens.detailscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.revlens.app.model.ScenarioType
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
import com.revlens.app.domain.calc.WhatIfEngine
import com.revlens.app.model.Assumptions
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.MetricResult
import com.revlens.app.model.Scenario
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.LabeledSlider
import com.revlens.app.ui.components.RevLensBarChart
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import com.revlens.app.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatIfScreen(viewModel: MainViewModel) {
    val profile by viewModel.profile.collectAsState()

    // Sliders state
    var priceChangePercent by rememberSaveable { mutableStateOf(0f) }
    var churnChangePercent by rememberSaveable { mutableStateOf(0f) }
    var growthChangePercent by rememberSaveable { mutableStateOf(0f) }
    var cacChange by rememberSaveable { mutableStateOf(0f) }

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var scenarioName by rememberSaveable { mutableStateOf("") }

    val selectedScenario by viewModel.selectedScenario.collectAsState()
    LaunchedEffect(selectedScenario) {
        val s = selectedScenario
        if (s != null && s.type == ScenarioType.WHAT_IF) {
            priceChangePercent = (s.assumptions.priceChangePercent * 100).toFloat()
            churnChangePercent = (s.assumptions.churnChangePercent * 100).toFloat()
            growthChangePercent = (s.assumptions.growthChangePercent * 100).toFloat()
            cacChange = s.assumptions.cacChange.toFloat()
            scenarioName = s.name
        }
    }

    val assumptions by remember {
        derivedStateOf {
            Assumptions(
                priceChangePercent = priceChangePercent.toDouble() / 100.0,
                churnChangePercent = churnChangePercent.toDouble() / 100.0,
                growthChangePercent = growthChangePercent.toDouble() / 100.0,
                cacChange = BigDecimal(cacChange.toString()),
                fixedCostChange = BigDecimal.ZERO // Simplified for MVP sliders
            )
        }
    }

    val results = remember(profile, assumptions) {
        profile?.let { p ->
            val projected = PricingSimulationEngine.simulate(p, assumptions)
            WhatIfEngine.compare(p, projected)
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segmented Tabs


        // Live Impact Section
        SectionCard(title = "Live Impact") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val chartDeltas = results.map { result ->
                    val rawDelta = (result.deltaPercent ?: 0.0).toFloat()
                    // Align direction: for CAC Payback, fewer months is positive business improvement (+)
                    if (result.label == "CAC Payback") -rawDelta else rawDelta
                }
                val chartLabels = results.map { result ->
                    when (result.label) {
                        "CAC Payback" -> "Payback"
                        "12-Month Projected MRR" -> "12-Mo"
                        else -> result.label
                    }
                }
                val chartValueLabels = chartDeltas.map { delta ->
                    val sign = if (delta > 0) "+" else ""
                    "$sign${CurrencyUtils.formatDecimal(delta.toDouble(), 0)}%"
                }
                val chartColors = chartDeltas.map { delta ->
                    if (delta >= 0f) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.error
                }

                RevLensBarChart(
                    data = chartDeltas,
                    colors = chartColors,
                    labels = chartLabels,
                    valueLabels = chartValueLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                )

                // 2 numbers per row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    results.chunked(2)
                        .forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { metric ->
                                    CompactMetricItem(
                                        result = metric,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                }
            }
        }

        // Adjustments Section
        SectionCard(title = "Assumptions") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledSlider(
                    label = "Price Change",
                    value = priceChangePercent,
                    onValueChange = { priceChangePercent = it },
                    valueRange = -50f..100f,
                    valueString = "${if (priceChangePercent > 0) "+" else ""}${priceChangePercent.toInt()}%"
                )

                LabeledSlider(
                    label = "Churn Rate",
                    value = churnChangePercent,
                    onValueChange = { churnChangePercent = it },
                    valueRange = -10f..10f,
                    valueString = "${if (churnChangePercent > 0) "+" else ""}${
                        CurrencyUtils.formatDecimal(
                            churnChangePercent.toDouble(), 1
                        )
                    }%"
                )

                LabeledSlider(
                    label = "Growth Rate",
                    value = growthChangePercent,
                    onValueChange = { growthChangePercent = it },
                    valueRange = -20f..50f,
                    valueString = "${if (growthChangePercent > 0) "+" else ""}${growthChangePercent.toInt()}%"
                )

                val currSymbol = CurrencyUtils.getCurrency(profile?.currencyCode).symbol
                val cacInt = cacChange.toInt()
                val cacFormatted =
                    "${if (cacInt > 0) "+$currSymbol" else if (cacInt < 0) "-$currSymbol" else currSymbol}${
                        kotlin.math.abs(cacInt)
                    }"
                LabeledSlider(
                    label = "CAC",
                    value = cacChange,
                    onValueChange = { cacChange = it },
                    valueRange = -500f..500f,
                    valueString = cacFormatted
                )
            }
        }

        ActionCardButton(
            text = "Save as Scenario",
            icon = Icons.Outlined.BookmarkAdd,
            onClick = { showSaveDialog = true }
        )
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
                                name = scenarioName.ifEmpty { "New Scenario" },
                                baselineProfile = p,
                                assumptions = assumptions,
                                type = ScenarioType.WHAT_IF
                            )
                        )
                    }
                    showSaveDialog = false
                    // Or navigate to Scenario builder directly
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = RevLensTheme.colors.surface
        )


    }
}

@Composable
private fun CompactMetricItem(
    result: MetricResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surfaceElevated.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = result.label,
            style = RevLensTypography.labelMedium,
            color = RevLensTheme.colors.textTertiary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = result.formattedValue,
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            if (result.deltaPercent != null && result.deltaPercent != 0.0) {
                val color =
                    if (result.isPositive) RevLensTheme.colors.success else RevLensTheme.colors.error
                val sign = if (result.deltaPercent > 0) "+" else ""
                Text(
                    text = "$sign${CurrencyUtils.formatDecimal(result.deltaPercent, 1)}%",
                    style = RevLensTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}
