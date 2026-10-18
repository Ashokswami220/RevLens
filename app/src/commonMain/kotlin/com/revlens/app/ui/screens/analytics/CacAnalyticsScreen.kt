package com.revlens.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.CacAnalysisResult
import com.revlens.app.domain.calc.ChannelEfficiency
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.LabeledSlider
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.charts.FunnelChart
import com.revlens.app.ui.components.display.DataSourceBadge
import com.revlens.app.ui.components.display.StatusBadge
import com.revlens.app.ui.components.display.StatusBadgeType
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacAnalyticsScreen(
    viewModel: CacAnalyticsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGrowthSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CacAnalyticsEffect.NavigateToGrowthSimulator -> onNavigateToGrowthSimulator()
                is CacAnalyticsEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RevLensTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CAC & Acquisition",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Blended CAC & funnel drop-off",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RevLensTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        DataSourceBadge(mode = state.dataSourceMode)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RevLensTheme.colors.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            val result = state.result
            if (result != null) {
                // Hero KPI Card
                CacHeroCard(result = result)

                // Funnel Chart
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Acquisition Conversion Funnel",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    FunnelChart(stages = result.funnelStages)
                }

                // Channel Efficiency Ledger
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Channel Efficiency",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        IconButton(
                            onClick = {
                                viewModel.onEvent(
                                    CacAnalyticsEvent.ToggleAddSpendSheet(true)
                                )
                            }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Spend",
                                tint = RevLensTheme.colors.brandPrimary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.channelBreakdown.forEach { channel ->
                            ChannelRow(channel = channel)
                        }
                    }
                }

                // Budget Simulation Lever
                BudgetSimulationCard(
                    simulatedBudget = state.simulatedBudgetMinor ?: result.totalSpendMinor,
                    onBudgetChanged = {
                        viewModel.onEvent(
                            CacAnalyticsEvent.UpdateSimulatedBudget(it)
                        )
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No acquisition spend data available.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            // Quick CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCardButton(
                    text = "Add Spend Entry",
                    icon = Icons.Filled.Campaign,
                    onClick = { viewModel.onEvent(CacAnalyticsEvent.ToggleAddSpendSheet(true)) },
                    modifier = Modifier.weight(1f)
                )

                ActionCardButton(
                    text = "Growth Simulator",
                    icon = Icons.Filled.Tune,
                    onClick = onNavigateToGrowthSimulator,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (state.isAddSpendSheetOpen) {
        AddSpendModalSheet(
            onDismiss = { viewModel.onEvent(CacAnalyticsEvent.ToggleAddSpendSheet(false)) },
            onAdd = { channel, amountMinor, newCust ->
                viewModel.onEvent(CacAnalyticsEvent.AddManualSpend(channel, amountMinor, newCust))
            }
        )
    }
}

@Composable
private fun CacHeroCard(result: CacAnalysisResult) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RevLensTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Blended Customer Acquisition Cost",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Text(
                        text = "$${result.blendedCacMinor / 100}",
                        style = RevLensTypography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }

                StatusBadge(
                    type = if (result.paybackPeriodMonths <= 12.0) StatusBadgeType.ACTIVE else StatusBadgeType.ERROR,
                    customText = "${result.paybackPeriodMonths} mo Payback"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Total Ad Spend",
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Text(
                        text = "$${result.totalSpendMinor / 100}",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }

                Column {
                    Text(
                        text = "Acquired Customers",
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Text(
                        text = "${result.totalNewCustomers}",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(channel: ChannelEfficiency) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = channel.channel,
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = "$${channel.totalSpendMinor / 100} spend · ${channel.newCustomers} signups (${channel.sharePercent.toInt()}%)",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "$${channel.cacMinor / 100} CAC",
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
                StatusBadge(
                    type = when (channel.efficiencyRating) {
                        "Top Tier" -> StatusBadgeType.ACTIVE
                        "Strong" -> StatusBadgeType.RUNNING
                        else -> StatusBadgeType.ERROR
                    },
                    customText = channel.efficiencyRating
                )
            }
        }
    }
}

@Composable
private fun BudgetSimulationCard(
    simulatedBudget: Long,
    onBudgetChanged: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Simulate Monthly Spend Impact",
            style = RevLensTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        val budgetDollars = (simulatedBudget / 100).toFloat()
        LabeledSlider(
            label = "Marketing Budget",
            value = budgetDollars,
            onValueChange = { onBudgetChanged((it * 100).toLong()) },
            valueRange = 500f..15000f,
            valueString = "$${budgetDollars.toInt()}/mo"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSpendModalSheet(
    onDismiss: () -> Unit,
    onAdd: (channel: String, amountMinor: Long, newCustomers: Int) -> Unit
) {
    var channelName by remember { mutableStateOf("Google Ads") }
    var spendDollars by remember { mutableStateOf("1000") }
    var newCustStr by remember { mutableStateOf("15") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RevLensTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Add Acquisition Spend Entry",
                style = RevLensTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )

            OutlinedTextField(
                value = channelName,
                onValueChange = { channelName = it },
                label = { Text("Channel Name (e.g. Meta Ads, Google)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RevLensTheme.colors.brandPrimary,
                    unfocusedBorderColor = RevLensTheme.colors.borderDefault
                )
            )

            OutlinedTextField(
                value = spendDollars,
                onValueChange = { spendDollars = it },
                label = { Text("Spend Amount ($)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RevLensTheme.colors.brandPrimary,
                    unfocusedBorderColor = RevLensTheme.colors.borderDefault
                )
            )

            OutlinedTextField(
                value = newCustStr,
                onValueChange = { newCustStr = it },
                label = { Text("New Customers Acquired") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RevLensTheme.colors.brandPrimary,
                    unfocusedBorderColor = RevLensTheme.colors.borderDefault
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            PrimaryButton(
                text = "Save Spend Record",
                onClick = {
                    val amountMinor = ((spendDollars.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val cust = newCustStr.toIntOrNull() ?: 1
                    onAdd(channelName, amountMinor, cust)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
