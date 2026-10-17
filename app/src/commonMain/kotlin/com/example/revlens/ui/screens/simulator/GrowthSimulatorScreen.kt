package com.example.revlens.ui.screens.simulator

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.components.ChartLegend
import com.example.revlens.ui.components.DeltaChip
import com.example.revlens.ui.components.DeltaType
import com.example.revlens.ui.components.LabeledSlider
import com.example.revlens.ui.components.NumberField
import com.example.revlens.ui.components.RevLensLineChart
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.components.SegmentedTabs
import com.example.revlens.ui.components.charts.RevLensStackedBarChart
import com.example.revlens.ui.components.charts.StackedBarItem
import com.example.revlens.ui.components.display.ChannelRow
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthSimulatorScreen(
    viewModel: GrowthSimulatorViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val horizonIndex = when (state.assumptions.timeHorizonMonths) {
        24 -> 1
        36 -> 2
        else -> 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RevLensTheme.colors.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizon selector
        SegmentedTabs(
            options = listOf("12 Months", "24 Months", "36 Months"),
            selectedIndex = horizonIndex,
            onOptionSelected = { index ->
                val months = when (index) {
                    1 -> 24
                    2 -> 36
                    else -> 12
                }
                viewModel.onEvent(GrowthSimulatorIntent.ChangeHorizon(months))
            }
        )

        // Result Card (Sticky / Top Summary)
        val result = state.result
        if (result != null) {
            val finalMrrDollars = result.finalMrrMinor / 100
            val baselineMrrDollars = state.baselineMrrMinor / 100
            val gainPercent = if (baselineMrrDollars > 0) {
                ((finalMrrDollars - baselineMrrDollars).toDouble() / baselineMrrDollars * 100).roundToInt()
            } else 0

            SectionCard(
                title = "Simulated Growth Result"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Projected MRR in ${state.assumptions.timeHorizonMonths} Months",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${state.currencySymbol}${formatNumberWithCommas(finalMrrDollars)}",
                            style = RevLensTypography.displayLarge,
                            color = RevLensTheme.colors.textPrimary
                        )

                        DeltaChip(
                            delta = if (gainPercent >= 0) "+$gainPercent%" else "$gainPercent%",
                            type = if (gainPercent >= 0) DeltaType.POSITIVE else DeltaType.NEGATIVE
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3 Compact Metric Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Blended CAC
                        MetricTile(
                            title = "Blended CAC",
                            value = "${state.currencySymbol}${(result.blendedCacMinor / 100)}",
                            modifier = Modifier.weight(1f)
                        )

                        // Payback
                        val paybackStr = if (result.paybackPeriodMonths != null) {
                            "${(result.paybackPeriodMonths * 10).roundToInt() / 10.0} mo"
                        } else {
                            "—"
                        }
                        MetricTile(
                            title = "Payback",
                            value = paybackStr,
                            modifier = Modifier.weight(1f)
                        )

                        // Runway
                        val runwayStr = if (result.runwayMonths != null) {
                            "${result.runwayMonths} mo"
                        } else if (state.assumptions.startingCashMinor > 0) {
                            "Sustainable"
                        } else {
                            "Set Cash"
                        }
                        MetricTile(
                            title = "Runway",
                            value = runwayStr,
                            isActionable = state.assumptions.startingCashMinor <= 0,
                            onClick = { viewModel.onEvent(GrowthSimulatorIntent.OpenCashSheet) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Acquisition Channels Section
        SectionCard(
            title = "Acquisition Channels",
            trailingAction = {
                TextButton(onClick = { viewModel.onEvent(GrowthSimulatorIntent.OpenAddChannelSheet) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = RevLensTheme.colors.brandPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Channel",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.brandPrimary
                    )
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.assumptions.channels.forEach { channel ->
                    ChannelRow(
                        channel = channel,
                        onSpendChange = { newSpend ->
                            viewModel.onEvent(GrowthSimulatorIntent.UpdateChannelSpend(channel.id, newSpend))
                        },
                        onCacChange = { newCac ->
                            viewModel.onEvent(GrowthSimulatorIntent.UpdateChannelCac(channel.id, newCac))
                        },
                        onToggleEnabled = {
                            viewModel.onEvent(GrowthSimulatorIntent.ToggleChannelEnabled(channel.id))
                        },
                        onDelete = if (state.assumptions.channels.size > 1) {
                            { viewModel.onEvent(GrowthSimulatorIntent.RemoveChannel(channel.id)) }
                        } else null
                    )
                }
            }
        }

        // Organic & Referral Levers
        SectionCard(title = "Organic & Referral Levers") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledSlider(
                    label = "Organic Growth Rate",
                    value = state.assumptions.organicGrowthPercent.toFloat(),
                    onValueChange = { viewModel.onEvent(GrowthSimulatorIntent.UpdateOrganicGrowth(it.toDouble())) },
                    valueRange = 0f..10f,
                    valueString = "${(state.assumptions.organicGrowthPercent * 10).roundToInt() / 10.0}% / mo"
                )

                LabeledSlider(
                    label = "Referral Rate",
                    value = state.assumptions.referralRatePercent.toFloat(),
                    onValueChange = { viewModel.onEvent(GrowthSimulatorIntent.UpdateReferralRate(it.toDouble())) },
                    valueRange = 0f..10f,
                    valueString = "${(state.assumptions.referralRatePercent * 10).roundToInt() / 10.0}% / mo"
                )

                LabeledSlider(
                    label = "Expansion Revenue Rate",
                    value = state.assumptions.expansionRatePercent.toFloat(),
                    onValueChange = { viewModel.onEvent(GrowthSimulatorIntent.UpdateExpansionRate(it.toDouble())) },
                    valueRange = 0f..5f,
                    valueString = "${(state.assumptions.expansionRatePercent * 10).roundToInt() / 10.0}% / mo"
                )
            }
        }

        // Projections Charts Section
        if (result != null && result.monthlySnapshots.isNotEmpty()) {
            SectionCard(title = "MRR Projection") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegend(
                        items = listOf(
                            "Simulated MRR" to RevLensTheme.colors.brandPrimary,
                            "Baseline Flat" to RevLensTheme.colors.textTertiary
                        )
                    )

                    val simSeries = result.monthlySnapshots.map { (it.endingMrrMinor / 100).toFloat() }
                    val baseSeries = List(result.monthlySnapshots.size) { (state.baselineMrrMinor / 100).toFloat() }
                    val xLabels = result.monthlySnapshots.mapIndexed { i, _ -> "M${i + 1}" }

                    RevLensLineChart(
                        data = listOf(simSeries, baseSeries),
                        colors = listOf(RevLensTheme.colors.brandPrimary, RevLensTheme.colors.textTertiary),
                        dashedIndices = listOf(1),
                        xLabels = xLabels,
                        chartHeight = 160.dp
                    )
                }
            }

            // Stacked Bar Chart: New Customers / Month
            SectionCard(title = "New Customers per Month") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegend(
                        items = listOf(
                            "Paid" to RevLensTheme.colors.brandPrimary,
                            "Referral" to RevLensTheme.colors.brandSecondary,
                            "Organic" to RevLensTheme.colors.success
                        )
                    )

                    val stackedItems = result.monthlySnapshots.map { snapshot ->
                        StackedBarItem(
                            label = "M${snapshot.monthIndex}",
                            segmentValues = listOf(
                                snapshot.paidCustomers.toFloat(),
                                snapshot.referralCustomers.toFloat(),
                                snapshot.organicCustomers.toFloat()
                            )
                        )
                    }

                    RevLensStackedBarChart(
                        items = stackedItems,
                        segmentColors = listOf(
                            RevLensTheme.colors.brandPrimary,
                            RevLensTheme.colors.brandSecondary,
                            RevLensTheme.colors.success
                        ),
                        chartHeight = 140.dp
                    )
                }
            }
        }

        // Action Buttons Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.onEvent(GrowthSimulatorIntent.OpenSaveScenarioDialog) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RevLensTheme.colors.primaryAction,
                    contentColor = RevLensTheme.colors.onPrimaryAction
                )
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Save as Scenario",
                    style = RevLensTypography.labelLarge
                )
            }

            OutlinedButton(
                onClick = { viewModel.onEvent(GrowthSimulatorIntent.ResetToDefaults) },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = RevLensTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reset",
                    style = RevLensTypography.labelMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs & Sheets

    // Save Scenario Dialog
    if (state.showSaveScenarioDialog) {
        var scenarioName by remember { mutableStateOf(state.scenarioNameInput) }
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(GrowthSimulatorIntent.DismissSaveScenarioDialog) },
            containerColor = RevLensTheme.colors.surface,
            title = {
                Text(
                    text = "Save Growth Scenario",
                    style = RevLensTypography.titleMedium,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Name this growth projection scenario to compare and revisit anytime.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    RevLensTextField(
                        value = scenarioName,
                        onValueChange = {
                            scenarioName = it
                            viewModel.onEvent(GrowthSimulatorIntent.UpdateScenarioName(it))
                        },
                        label = "Scenario Name",
                        placeholder = "e.g. Aggressive Paid Scale Q3"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(GrowthSimulatorIntent.SaveScenario) },
                    colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(GrowthSimulatorIntent.DismissSaveScenarioDialog) }) {
                    Text("Cancel", color = RevLensTheme.colors.textSecondary)
                }
            }
        )
    }

    // Add Channel Dialog
    if (state.showAddChannelSheet) {
        var channelName by remember { mutableStateOf("") }
        var spendInput by remember { mutableStateOf("1000") }
        var cacInput by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { viewModel.onEvent(GrowthSimulatorIntent.DismissAddChannelSheet) },
            containerColor = RevLensTheme.colors.surface,
            title = {
                Text(
                    text = "Add Acquisition Channel",
                    style = RevLensTypography.titleMedium,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RevLensTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = "Channel Name",
                        placeholder = "e.g. YouTube Ads, Affiliates, TikTok"
                    )
                    NumberField(
                        value = spendInput,
                        onValueChange = { spendInput = it },
                        label = "Monthly Budget ($)",
                        suffix = "$"
                    )
                    NumberField(
                        value = cacInput,
                        onValueChange = { cacInput = it },
                        label = "Target CAC ($)",
                        suffix = "$"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val spendMinor = ((spendInput.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        val cacMinor = ((cacInput.toDoubleOrNull() ?: 1.0) * 100).toLong()
                        viewModel.onEvent(
                            GrowthSimulatorIntent.AddChannel(
                                name = channelName,
                                spendMinor = spendMinor,
                                cacMinor = cacMinor
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(GrowthSimulatorIntent.DismissAddChannelSheet) }) {
                    Text("Cancel", color = RevLensTheme.colors.textSecondary)
                }
            }
        )
    }

    // Cash Assumptions Dialog
    if (state.showCashSheet) {
        var cashInput by remember { mutableStateOf("${state.assumptions.startingCashMinor / 100}") }

        AlertDialog(
            onDismissRequest = { viewModel.onEvent(GrowthSimulatorIntent.DismissCashSheet) },
            containerColor = RevLensTheme.colors.surface,
            title = {
                Text(
                    text = "Starting Cash Runway Balance",
                    style = RevLensTypography.titleMedium,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter current liquid cash to calculate expected runway under this acquisition plan.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    NumberField(
                        value = cashInput,
                        onValueChange = { cashInput = it },
                        label = "Cash Balance ($)",
                        suffix = "$"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minor = ((cashInput.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        viewModel.onEvent(GrowthSimulatorIntent.UpdateStartingCash(minor))
                        viewModel.onEvent(GrowthSimulatorIntent.DismissCashSheet)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(GrowthSimulatorIntent.DismissCashSheet) }) {
                    Text("Cancel", color = RevLensTheme.colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isActionable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .then(clickableModifier)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = RevLensTypography.labelSmall,
            color = RevLensTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = RevLensTypography.titleSmall,
            color = if (isActionable) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary
        )
    }
}

private fun formatNumberWithCommas(value: Long): String {
    val s = value.toString()
    val isNegative = s.startsWith("-")
    val digits = if (isNegative) s.substring(1) else s
    val sb = StringBuilder()
    val len = digits.length
    for (i in 0 until len) {
        if (i > 0 && (len - i) % 3 == 0) {
            sb.append(',')
        }
        sb.append(digits[i])
    }
    return if (isNegative) "-$sb" else sb.toString()
}
