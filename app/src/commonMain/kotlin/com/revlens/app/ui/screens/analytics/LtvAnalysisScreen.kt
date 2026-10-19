package com.revlens.app.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.model.BigDecimal
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.DeltaChip
import com.revlens.app.ui.components.DeltaType
import com.revlens.app.ui.components.LabeledSlider
import com.revlens.app.ui.components.display.DataSourceBadge
import com.revlens.app.ui.components.display.StatusBadge
import com.revlens.app.ui.components.display.StatusBadgeType
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LtvAnalysisScreen(
    viewModel: LtvAnalysisViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWhatIf: (Double) -> Unit,
    onNavigateToBaseline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LtvAnalysisEffect.NavigateToWhatIf -> onNavigateToWhatIf(effect.simulatedChurn)
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
                            text = "Customer Lifetime Value",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Unit economics & churn sensitivity",
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
            // Method Toggle
            LtvMethodToggle(
                selectedMethod = state.method,
                onSelectMethod = { viewModel.onEvent(LtvAnalysisEvent.SelectMethod(it)) }
            )

            // Hero KPI Card
            LtvHeroCard(state = state)

            // Unit Economics 3-Metric Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UnitMetricTile(
                    title = "LTV : CAC",
                    value = "${state.ltvToCacRatio}x",
                    statusType = when {
                        state.ltvToCacRatio >= 4.0 -> StatusBadgeType.ACTIVE
                        state.ltvToCacRatio >= 3.0 -> StatusBadgeType.WON
                        state.ltvToCacRatio >= 1.5 -> StatusBadgeType.PENDING
                        else -> StatusBadgeType.ERROR
                    },
                    statusText = when {
                        state.ltvToCacRatio >= 3.0 -> "Healthy (>3x)"
                        state.ltvToCacRatio >= 1.5 -> "Acceptable"
                        else -> "Critical (<1.5x)"
                    },
                    modifier = Modifier.weight(1f)
                )

                UnitMetricTile(
                    title = "Payback Period",
                    value = "${state.paybackPeriodMonths} mo",
                    statusType = when {
                        state.paybackPeriodMonths <= 12.0 -> StatusBadgeType.WON
                        state.paybackPeriodMonths <= 18.0 -> StatusBadgeType.PENDING
                        else -> StatusBadgeType.ERROR
                    },
                    statusText = if (state.paybackPeriodMonths <= 12.0) "<12 mo" else ">12 mo",
                    modifier = Modifier.weight(1f)
                )

                val lifespanMonths = if (state.simulatedChurnPercent > 0) {
                    (100.0 / state.simulatedChurnPercent).toInt()
                } else 60
                UnitMetricTile(
                    title = "Avg Lifespan",
                    value = "$lifespanMonths mo",
                    statusType = StatusBadgeType.ACTIVE,
                    statusText = "1 ÷ Churn",
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive Churn Slider Card
            ChurnSensitivityCard(
                baseChurn = state.baseChurnPercent,
                simulatedChurn = state.simulatedChurnPercent,
                onChurnChanged = { viewModel.onEvent(LtvAnalysisEvent.UpdateSimulatedChurn(it)) },
                onResetClick = { viewModel.onEvent(LtvAnalysisEvent.ResetSimulation) }
            )

            // Sensitivity Curve Chart Card
            SensitivityCurveCard(
                points = state.sensitivityCurve,
                activeChurn = state.simulatedChurnPercent
            )

            // Quick CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCardButton(
                    text = "Open in What-If",
                    icon = Icons.Filled.Tune,
                    onClick = { viewModel.onEvent(LtvAnalysisEvent.OpenInWhatIf) },
                    modifier = Modifier.weight(1f)
                )

                ActionCardButton(
                    text = "Adjust Baseline",
                    icon = Icons.Filled.Info,
                    onClick = onNavigateToBaseline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LtvMethodToggle(
    selectedMethod: LtvMethod,
    onSelectMethod: (LtvMethod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LtvMethod.entries.forEach { method ->
            val isSelected = selectedMethod == method
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) RevLensTheme.colors.brandPrimary else Color.Transparent
                    )
                    .clickable { onSelectMethod(method) }
                    .padding(vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = method.title,
                        style = RevLensTypography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else RevLensTheme.colors.textPrimary
                    )
                    Text(
                        text = method.subtitle,
                        style = RevLensTypography.labelSmall,
                        color = if (isSelected) Color.White.copy(
                            alpha = 0.85f
                        ) else RevLensTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LtvHeroCard(state: LtvAnalysisUiState) {
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
                        text = "Estimated Customer Lifetime Value",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Text(
                        text = "${state.currencySymbol}${state.simulatedLtv}",
                        style = RevLensTypography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }

                if (state.deltaPercent != 0.0) {
                    val isPos = state.deltaPercent > 0
                    DeltaChip(
                        delta = "${if (isPos) "+" else ""}${state.deltaPercent.toInt()}%",
                        type = if (isPos) DeltaType.POSITIVE else DeltaType.NEGATIVE
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RevLensTheme.colors.surfaceElevated)
                    .padding(10.dp)
            ) {
                val sym = state.currencySymbol
                val formulaStr = when (state.method) {
                    LtvMethod.MARGIN_ADJUSTED -> "($sym${state.baseArpu} ARPU × ${state.grossMarginPercent.toInt()}% Margin) ÷ ${state.simulatedChurnPercent}% Churn = $sym${state.simulatedLtv}"
                    LtvMethod.SIMPLE -> "$sym${state.baseArpu} ARPU ÷ ${state.simulatedChurnPercent}% Churn = $sym${state.simulatedLtv}"
                }
                Text(
                    text = "Formula: $formulaStr",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun UnitMetricTile(
    title: String,
    value: String,
    statusType: StatusBadgeType,
    statusText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = RevLensTypography.labelSmall,
                color = RevLensTheme.colors.textSecondary
            )
            Text(
                text = value,
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            StatusBadge(
                type = statusType,
                customText = statusText
            )
        }
    }
}

@Composable
private fun ChurnSensitivityCard(
    baseChurn: Double,
    simulatedChurn: Double,
    onChurnChanged: (Double) -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Churn Rate Sensitivity Lever",
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = "Baseline: ${(baseChurn * 10).toInt() / 10.0}%/mo",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }

            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset",
                    tint = RevLensTheme.colors.brandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LabeledSlider(
            label = "Simulated Monthly Churn Rate",
            value = simulatedChurn.toFloat(),
            onValueChange = { onChurnChanged(it.toDouble()) },
            valueRange = 0.5f..10.0f,
            valueString = "${((simulatedChurn * 10).toInt() / 10.0)}%"
        )
    }
}

@Composable
private fun SensitivityCurveCard(
    points: List<Pair<Double, BigDecimal>>,
    activeChurn: Double
) {
    if (points.isEmpty()) return

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
            text = "LTV Sensitivity Curve",
            style = RevLensTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        val maxVal = points.maxOfOrNull { it.second.toDouble() }
            ?.coerceAtLeast(1.0) ?: 1.0
        val lineColor = RevLensTheme.colors.brandPrimary
        val activeDotColor = RevLensTheme.colors.success

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val w = size.width
            val h = size.height
            val pad = 12.dp.toPx()
            val effectiveH = h - (pad * 2)

            val path = Path()
            points.forEachIndexed { idx, (churn, ltv) ->
                val x = (idx.toFloat() / (points.size - 1)) * w
                val y = pad + effectiveH - ((ltv.toDouble() / maxVal).toFloat() * effectiveH)
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points
            points.forEachIndexed { idx, (churn, ltv) ->
                val x = (idx.toFloat() / (points.size - 1)) * w
                val y = pad + effectiveH - ((ltv.toDouble() / maxVal).toFloat() * effectiveH)
                val isClosest = kotlin.math.abs(churn - activeChurn) < 0.6
                drawCircle(
                    color = if (isClosest) activeDotColor else lineColor,
                    radius = if (isClosest) 5.dp.toPx() else 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // X labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (churn, _) ->
                Text(
                    text = "${churn}%",
                    style = RevLensTypography.labelSmall,
                    fontSize = 9.sp,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}
