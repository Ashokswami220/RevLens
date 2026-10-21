package com.revlens.app.ui.screens.analytics

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.CohortGranularity
import com.revlens.app.domain.calc.CohortMetric
import com.revlens.app.ui.components.ChartLegend
import com.revlens.app.ui.components.FilterChip
import com.revlens.app.ui.components.RevLensLineChart
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.SegmentedTabs
import com.revlens.app.ui.components.charts.CohortHeatmap
import com.revlens.app.ui.components.display.DataSourceBadge
import com.revlens.app.ui.components.display.DataSourceMode
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CohortAnalysisScreen(
    viewModel: CohortAnalysisViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDataImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CohortAnalysisEffect.NavigateToDataImport -> onNavigateToDataImport()
            }
        }
    }

    val result = state.result

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RevLensTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cohorts",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Signup retention decay over time",
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
                    if (result != null) {
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            DataSourceBadge(mode = result.sourceMode)
                        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Tabs: Logo Retention vs Revenue Retention
            SegmentedTabs(
                options = listOf("Logo Retention", "Revenue Retention"),
                selectedIndex = if (state.metric == CohortMetric.LOGO_RETENTION) 0 else 1,
                onOptionSelected = { idx ->
                    val metric =
                        if (idx == 0) CohortMetric.LOGO_RETENTION else CohortMetric.REVENUE_RETENTION
                    viewModel.onEvent(CohortAnalysisEvent.SelectMetric(metric))
                }
            )

            // Granularity Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CohortGranularity.entries.forEach { g ->
                    FilterChip(
                        text = g.label,
                        selected = state.granularity == g,
                        onClick = { viewModel.onEvent(CohortAnalysisEvent.SelectGranularity(g)) }
                    )
                }
            }

            if (result != null) {
                // Insight Card
                SectionCard(title = "Retention Insight") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = result.bestCohortInsight,
                            style = RevLensTypography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = RevLensTheme.colors.textPrimary
                        )
                    }
                }

                // If Actual Data Available: Show Heatmap
                if (result.sourceMode == DataSourceMode.ACTUAL && result.rows.isNotEmpty()) {
                    SectionCard(title = "Retention Heatmap") {
                        CohortHeatmap(
                            rows = result.rows,
                            averageCurve = result.averageCurve,
                            onCellClick = { row, cell ->
                                viewModel.onEvent(CohortAnalysisEvent.SelectCell(row, cell))
                            }
                        )
                    }

                    // Retention Curves Chart
                    SectionCard(title = "Retention Curves") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val selectedRows = result.rows.filter {
                                state.selectedCohortNames.contains(
                                    it.cohortLabel
                                )
                            }
                            val curveColors = listOf(
                                RevLensTheme.colors.brandPrimary,
                                RevLensTheme.colors.brandSecondary,
                                RevLensTheme.colors.success
                            )

                            // Legend
                            val legendItems = selectedRows.mapIndexed { i, row ->
                                row.cohortLabel to curveColors.getOrElse(
                                    i
                                ) { RevLensTheme.colors.brandPrimary }
                            } + listOf("Average" to RevLensTheme.colors.textTertiary)

                            ChartLegend(items = legendItems)

                            // Series data
                            val seriesList = ArrayList<List<Float>>()
                            selectedRows.forEach { row ->
                                val pts =
                                    row.cells.map { if (it.isYounger) 0f else it.retentionPercent.toFloat() }
                                seriesList.add(pts)
                            }
                            // Average series (dashed)
                            seriesList.add(result.averageCurve.map { it.toFloat() })

                            val dashedIndices = listOf(seriesList.size - 1)
                            val colors = List(selectedRows.size) { i ->
                                curveColors.getOrElse(
                                    i
                                ) { RevLensTheme.colors.brandPrimary }
                            } + listOf(RevLensTheme.colors.textTertiary)
                            val xLabels = List(12) { "M$it" }

                            RevLensLineChart(
                                data = seriesList,
                                colors = colors,
                                dashedIndices = dashedIndices,
                                xLabels = xLabels,
                                chartHeight = 160.dp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Cohort Toggle Chips
                            Text(
                                text = "Compare up to 3 cohorts:",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                result.rows.forEach { row ->
                                    val isSelected =
                                        state.selectedCohortNames.contains(row.cohortLabel)
                                    FilterChip(
                                        text = row.cohortLabel,
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.onEvent(
                                                CohortAnalysisEvent.ToggleCohortSelection(
                                                    row.cohortLabel
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Modeled Mode: Expected Retention Decay Curve
                    SectionCard(title = "Expected Retention Curve (Modeled)") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Theoretical retention decay curve modeled from your monthly churn rate: (1 - Churn)^Month.",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )

                            val modeledSeries = result.averageCurve.map { it.toFloat() }
                            val xLabels = List(modeledSeries.size) { "M$it" }

                            RevLensLineChart(
                                data = listOf(modeledSeries),
                                colors = listOf(RevLensTheme.colors.brandPrimary),
                                xLabels = xLabels,
                                chartHeight = 160.dp
                            )

                            // Locked Heatmap Prompt
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RevLensTheme.colors.surfaceElevated)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = RevLensTheme.colors.textTertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Import subscription CSV data with customer start and churn dates to unlock the empirical cohort heatmap.",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        viewModel.onEvent(
                                            CohortAnalysisEvent.ImportDataClicked
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RevLensTheme.colors.primaryAction
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Import Data", style = RevLensTypography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Cell Detail Sheet
    state.selectedCellDetail?.let { (row, cell) ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(CohortAnalysisEvent.DismissCellDetail) },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "${row.cohortLabel} · Month ${cell.offsetIndex} (M${cell.offsetIndex})",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = RevLensTheme.colors.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailItem(
                            label = "Initial Cohort Size", value = "${row.initialSize} customers"
                        )
                        DetailItem(
                            label = "Retained Retention", value = "${cell.retentionPercent}%"
                        )
                        DetailItem(
                            label = if (state.metric == CohortMetric.LOGO_RETENTION) "Retained Customers" else "Retained MRR",
                            value = if (state.metric == CohortMetric.LOGO_RETENTION) "${cell.retainedValue.toInt()}" else "${state.currencySymbol}${cell.retainedValue.toInt()}"
                        )
                        if (cell.isSmallSample) {
                            Text(
                                text = "Note: Cohort has fewer than 20 customers. Percentages may fluctuate.",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.warning
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.onEvent(CohortAnalysisEvent.DismissCellDetail) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RevLensTheme.colors.surfaceElevated
                    )
                ) {
                    Text(text = "Close", color = RevLensTheme.colors.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = RevLensTypography.bodyMedium,
            color = RevLensTheme.colors.textSecondary
        )
        Text(
            text = value,
            style = RevLensTypography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = RevLensTheme.colors.textPrimary
        )
    }
}
