package com.revlens.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.RevenueBreakdownResult
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.DeltaChip
import com.revlens.app.ui.components.DeltaType
import com.revlens.app.ui.components.charts.DonutSlice
import com.revlens.app.ui.components.charts.RevLensDonutChart
import com.revlens.app.ui.components.charts.WaterfallChart
import com.revlens.app.ui.components.display.DataSourceBadge
import com.revlens.app.ui.components.display.StatusBadge
import com.revlens.app.ui.components.display.StatusBadgeType
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueBreakdownScreen(
    viewModel: RevenueBreakdownViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDataImport: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RevenueBreakdownEffect.NavigateToDataImport -> onNavigateToDataImport()
                RevenueBreakdownEffect.NavigateToSimulator -> onNavigateToSimulator()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RevLensTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Revenue Breakdown",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "MRR bridge movements & plan distribution",
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
                        DataSourceBadge(mode = state.activeDataSource)
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
            // Controls row: Source Mode + Period
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SourceModeSelector(
                    currentMode = state.sourceMode,
                    hasActualData = state.hasActualData,
                    onModeSelected = {
                        viewModel.onEvent(
                            RevenueBreakdownEvent.SelectSourceMode(it)
                        )
                    }
                )

                PeriodSelector(
                    selectedPeriod = state.period,
                    onPeriodSelected = { viewModel.onEvent(RevenueBreakdownEvent.SelectPeriod(it)) }
                )
            }

            val result = state.result
            if (result != null) {
                // Quick Ratio Hero Card
                QuickRatioHeroCard(result = result)

                // Waterfall Chart Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MRR Movement Bridge",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Tap a bar to inspect",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    WaterfallChart(
                        steps = result.waterfallSteps,
                        onStepSelected = {
                            viewModel.onEvent(
                                RevenueBreakdownEvent.SelectWaterfallStep(it)
                            )
                        }
                    )

                    // Step Detail Callout (if selected)
                    val selected = state.selectedStep
                    if (selected != null) {
                        StepDetailCard(step = selected)
                    }
                }

                // Plan Tier Donut Chart Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MRR by Plan Tier",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )

                    val sliceColors = listOf(
                        RevLensTheme.colors.brandPrimary,
                        RevLensTheme.colors.success,
                        Color(0xFF3B82F6),
                        Color(0xFFF59E0B),
                        Color(0xFF8B5CF6)
                    )

                    val donutSlices = result.planShares.mapIndexed { idx, share ->
                        DonutSlice(
                            label = share.planName,
                            value = share.mrrMinor,
                            percentage = share.percentage,
                            color = sliceColors[idx % sliceColors.size],
                            subtitle = "${share.customerCount} customers"
                        )
                    }

                    RevLensDonutChart(
                        slices = donutSlices,
                        centerTitle = "Total MRR",
                        centerValue = "$${result.endingMrrMinor / 100}"
                    )
                }

                // Bridge Movement Summary Grid
                BridgeMovementSummary(result = result)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No revenue data available for this workspace.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCardButton(
                    text = "Import CSV",
                    icon = Icons.Filled.UploadFile,
                    onClick = onNavigateToDataImport,
                    modifier = Modifier.weight(1f)
                )

                ActionCardButton(
                    text = "Simulate Pricing",
                    icon = Icons.Filled.Tune,
                    onClick = onNavigateToSimulator,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SourceModeSelector(
    currentMode: RevenueSourceMode,
    hasActualData: Boolean,
    onModeSelected: (RevenueSourceMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val modeOptions = listOf(
            RevenueSourceMode.MODELED to "Modeled",
            RevenueSourceMode.ACTUAL to "Actual CSV"
        )
        for ((mode, label) in modeOptions) {
            val isSelected = currentMode == mode
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) RevLensTheme.colors.brandPrimary else Color.Transparent
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    style = RevLensTypography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: BridgePeriod,
    onPeriodSelected: (BridgePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        BridgePeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) RevLensTheme.colors.surfaceElevated else Color.Transparent
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = period.label,
                    style = RevLensTypography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) RevLensTheme.colors.textPrimary else RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickRatioHeroCard(result: RevenueBreakdownResult) {
    val qr = result.quickRatio
    val (statusType, statusCustomText) = when {
        qr >= 4.0 -> StatusBadgeType.ACTIVE to "Top Tier (${(qr * 10).toInt() / 10.0}x)"
        qr >= 2.0 -> StatusBadgeType.RUNNING to "Strong (${(qr * 10).toInt() / 10.0}x)"
        qr >= 1.0 -> StatusBadgeType.PENDING to "Moderate (${(qr * 10).toInt() / 10.0}x)"
        else -> StatusBadgeType.ERROR to "Contracting (${(qr * 10).toInt() / 10.0}x)"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RevLensTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SaaS Quick Ratio",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Text(
                        text = "${(qr * 10).toInt() / 10.0}x",
                        style = RevLensTypography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }

                StatusBadge(type = statusType, customText = statusCustomText)
            }

            val isPositive = result.netChangeMinor >= 0
            val deltaPercent = if (result.startingMrrMinor > 0) {
                (result.netChangeMinor.toDouble() / result.startingMrrMinor.toDouble()) * 100
            } else 0.0
            val deltaText = "${if (isPositive) "+" else ""}${deltaPercent.toInt()}%"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeltaChip(
                    delta = deltaText,
                    type = if (result.netChangeMinor > 0) DeltaType.POSITIVE else if (result.netChangeMinor < 0) DeltaType.NEGATIVE else DeltaType.NEUTRAL
                )
                Text(
                    text = "${if (isPositive) "+$" else "-$"}${
                        kotlin.math.abs(
                            result.netChangeMinor
                        ) / 100
                    } net MRR movement",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RevLensTheme.colors.surfaceElevated)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Formula: (New $${result.newMrrMinor / 100} + Exp $${result.expansionMrrMinor / 100}) ÷ (Con $${result.contractionMrrMinor / 100} + Churn $${result.churnMrrMinor / 100})",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun StepDetailCard(step: com.revlens.app.domain.calc.WaterfallStep) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (step.isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (step.isPositive) RevLensTheme.colors.success else RevLensTheme.colors.error,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = step.title,
                        style = RevLensTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Text(
                        text = if (step.isTotal) "Total level" else (if (step.isPositive) "Revenue addition" else "Revenue deduction"),
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            Text(
                text = "${if (!step.isTotal) (if (step.isPositive) "+$" else "-$") else "$"}${step.amountMinor / 100}",
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (!step.isTotal) (if (step.isPositive) RevLensTheme.colors.success else RevLensTheme.colors.error) else RevLensTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun BridgeMovementSummary(result: RevenueBreakdownResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Movement Ledger",
            style = RevLensTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        MovementRow(
            "Starting MRR", "$${result.startingMrrMinor / 100}", RevLensTheme.colors.textSecondary
        )
        MovementRow("New MRR", "+$${result.newMrrMinor / 100}", RevLensTheme.colors.success)
        MovementRow(
            "Expansion MRR", "+$${result.expansionMrrMinor / 100}", RevLensTheme.colors.success
        )
        MovementRow("Contraction MRR", "-$${result.contractionMrrMinor / 100}", Color(0xFFF59E0B))
        MovementRow("Churn MRR", "-$${result.churnMrrMinor / 100}", RevLensTheme.colors.error)
        MovementRow(
            label = "Ending MRR",
            amount = "$${result.endingMrrMinor / 100}",
            color = RevLensTheme.colors.brandPrimary,
            isBold = true
        )
    }
}

@Composable
private fun MovementRow(
    label: String,
    amount: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RevLensTypography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = RevLensTheme.colors.textPrimary
        )
        Text(
            text = amount,
            style = RevLensTypography.titleSmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
