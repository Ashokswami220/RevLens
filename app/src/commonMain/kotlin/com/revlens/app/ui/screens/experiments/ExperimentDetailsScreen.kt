package com.revlens.app.ui.screens.experiments

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.revlens.app.domain.model.ExperimentStatus
import com.revlens.app.ui.components.ChartLegend
import com.revlens.app.ui.components.DeltaChip
import com.revlens.app.ui.components.DeltaType
import com.revlens.app.ui.components.NumberField
import com.revlens.app.ui.components.RevLensLineChart
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.display.StatusBadge
import com.revlens.app.ui.components.display.StatusBadgeType
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentDetailsScreen(
    experimentId: String,
    viewModel: ExperimentDetailsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(experimentId) {
        viewModel.onEvent(ExperimentDetailsEvent.LoadExperiment(experimentId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExperimentDetailsEffect.ShowToast -> snackbarHostState.showSnackbar(
                    effect.message
                )

                ExperimentDetailsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    val experiment = state.experiment
    val analysis = state.analysis

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RevLensTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = experiment?.name ?: "Experiment",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = experiment?.let { "${it.type.displayName} · ${it.startDate}" }
                                ?: "",
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
                    experiment?.let {
                        val badge = when (it.status) {
                            ExperimentStatus.DRAFT -> StatusBadgeType.DRAFT
                            ExperimentStatus.RUNNING -> StatusBadgeType.RUNNING
                            ExperimentStatus.WON -> StatusBadgeType.WON
                            ExperimentStatus.LOST -> StatusBadgeType.LOST
                            ExperimentStatus.INCONCLUSIVE -> StatusBadgeType.INCONCLUSIVE
                        }
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            StatusBadge(type = badge)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RevLensTheme.colors.background
                )
            )
        }
    ) { paddingValues ->
        if (experiment != null && analysis != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Timeline Progress",
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                        Text(
                            text = "Day ${analysis.daysElapsed} of ${experiment.plannedDurationDays}",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RevLensTheme.colors.textPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { analysis.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RevLensTheme.colors.brandPrimary,
                        trackColor = RevLensTheme.colors.borderDefault
                    )
                }

                // Result Hero Card
                SectionCard(title = "Statistical Result") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Observed Lift",
                                    style = RevLensTypography.labelMedium,
                                    color = RevLensTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${if (analysis.actualLiftPercent >= 0) "+" else ""}${analysis.actualLiftPercent}%",
                                    style = RevLensTypography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }

                            val pSign = if (experiment.predictedLiftPercent >= 0) "+" else ""
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Predicted",
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textTertiary
                                )
                                DeltaChip(
                                    delta = "$pSign${experiment.predictedLiftPercent}%",
                                    type = if (experiment.predictedLiftPercent >= 0) DeltaType.POSITIVE else DeltaType.NEGATIVE
                                )
                            }
                        }

                        // Confidence & Significance status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.surfaceElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Statistical Confidence",
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                                Text(
                                    text = if (analysis.isEnoughData) "${analysis.confidencePercent}%" else "Insufficient data",
                                    style = RevLensTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }

                            if (!analysis.isEnoughData) {
                                StatusBadge(
                                    type = StatusBadgeType.DRAFT, customText = "Need more samples"
                                )
                            } else if (analysis.isSignificant) {
                                StatusBadge(
                                    type = StatusBadgeType.ACTIVE,
                                    customText = "Significant (p < 0.05)"
                                )
                            } else {
                                StatusBadge(
                                    type = StatusBadgeType.WARNING,
                                    customText = "Not yet significant"
                                )
                            }
                        }

                        // Sample Counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = RevLensTheme.colors.surfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "Control Avg", style = RevLensTypography.labelSmall,
                                        color = RevLensTheme.colors.textSecondary
                                    )
                                    Text(
                                        "${analysis.controlTotal} (n=${analysis.controlSampleSize})",
                                        style = RevLensTypography.titleSmall,
                                        color = RevLensTheme.colors.textPrimary
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = RevLensTheme.colors.surfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "Treatment Avg", style = RevLensTypography.labelSmall,
                                        color = RevLensTheme.colors.textSecondary
                                    )
                                    Text(
                                        "${analysis.treatmentTotal} (n=${analysis.treatmentSampleSize})",
                                        style = RevLensTypography.titleSmall,
                                        color = RevLensTheme.colors.brandPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Timeline Chart (if observations >= 2)
                if (experiment.observations.size >= 2) {
                    SectionCard(title = "Metric Over Time") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ChartLegend(
                                items = listOf(
                                    "Control" to RevLensTheme.colors.brandSecondary,
                                    "Treatment" to RevLensTheme.colors.brandPrimary
                                )
                            )

                            val controlPts =
                                experiment.observations.map { it.controlValue.toFloat() }
                            val treatmentPts =
                                experiment.observations.map { it.treatmentValue.toFloat() }
                            val xLabels = experiment.observations.map { it.date.takeLast(5) }

                            RevLensLineChart(
                                data = listOf(controlPts, treatmentPts),
                                colors = listOf(
                                    RevLensTheme.colors.brandSecondary,
                                    RevLensTheme.colors.brandPrimary
                                ),
                                xLabels = xLabels,
                                chartHeight = 150.dp
                            )
                        }
                    }
                }

                // Observations List
                SectionCard(
                    title = "Observations (${experiment.observations.size})",
                    trailingAction = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(
                                    ExperimentDetailsEvent.OpenLogObservationSheet
                                )
                            }) {
                            Icon(
                                Icons.Default.Add, contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Data")
                        }
                    }
                ) {
                    if (experiment.observations.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            experiment.observations.forEach { obs ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RevLensTheme.colors.surfaceElevated)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            obs.date, style = RevLensTypography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RevLensTheme.colors.textPrimary
                                        )
                                        if (obs.notes.isNotBlank()) {
                                            Text(
                                                obs.notes, style = RevLensTypography.bodySmall,
                                                color = RevLensTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            "C: ${obs.controlValue} (n=${obs.controlSampleSize})",
                                            style = RevLensTypography.bodySmall,
                                            color = RevLensTheme.colors.textSecondary
                                        )
                                        Text(
                                            "T: ${obs.treatmentValue} (n=${obs.treatmentSampleSize})",
                                            style = RevLensTypography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RevLensTheme.colors.brandPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No observations logged yet. Log your first sample to begin statistical comparison.",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textTertiary
                        )
                    }
                }

                // Setup Details
                SectionCard(title = "Hypothesis & Variant Setup") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Control: ${experiment.controlDescription.ifBlank { "Standard baseline" }}",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                        Text(
                            "Treatment: ${experiment.treatmentDescription.ifBlank { "Test variation" }}",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Success Threshold: +${experiment.successThresholdPercent}% lift",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textTertiary
                        )
                    }
                }

                // Complete Experiment Button
                if (experiment.status == ExperimentStatus.RUNNING) {
                    Button(
                        onClick = { viewModel.onEvent(ExperimentDetailsEvent.OpenCompleteDialog) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RevLensTheme.colors.primaryAction
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Complete Experiment & Declare Verdict")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Complete Experiment Dialog
    if (state.showCompleteDialog && analysis != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ExperimentDetailsEvent.DismissCompleteDialog) },
            containerColor = RevLensTheme.colors.surface,
            title = {
                Text(
                    "Complete Experiment", style = RevLensTypography.titleMedium,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Suggested Verdict: ${analysis.suggestedVerdict.displayName}",
                        style = RevLensTypography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.brandPrimary
                    )
                    Text(
                        text = "Based on ${analysis.confidencePercent}% confidence and ${analysis.actualLiftPercent}% observed lift vs ${state.experiment?.successThresholdPercent}% threshold.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onEvent(
                                    ExperimentDetailsEvent.CompleteExperiment(ExperimentStatus.WON)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RevLensTheme.colors.success
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Won")
                        }
                        Button(
                            onClick = {
                                viewModel.onEvent(
                                    ExperimentDetailsEvent.CompleteExperiment(ExperimentStatus.LOST)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RevLensTheme.colors.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lost")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ExperimentDetailsEvent.DismissCompleteDialog) }) {
                    Text("Cancel", color = RevLensTheme.colors.textSecondary)
                }
            }
        )
    }

    // Log Observation Bottom Sheet
    if (state.showLogObservationSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onEvent(
                    ExperimentDetailsEvent.DismissLogObservationSheet
                )
            },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            LogObservationSheet(
                onLog = { date, cVal, cN, tVal, tN, notes ->
                    viewModel.onEvent(
                        ExperimentDetailsEvent.LogObservation(
                            date = date,
                            controlVal = cVal,
                            controlN = cN,
                            treatmentVal = tVal,
                            treatmentN = tN,
                            notes = notes
                        )
                    )
                },
                onDismiss = { viewModel.onEvent(ExperimentDetailsEvent.DismissLogObservationSheet) }
            )
        }
    }
}

@Composable
private fun LogObservationSheet(
    onLog: (String, Double, Int, Double, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var dateInput by remember { mutableStateOf("2026-10-17") }
    var controlValInput by remember { mutableStateOf("100.0") }
    var controlNInput by remember { mutableStateOf("100") }
    var treatmentValInput by remember { mutableStateOf("112.0") }
    var treatmentNInput by remember { mutableStateOf("100") }
    var notesInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Log New Observation", style = RevLensTypography.titleLarge,
            fontWeight = FontWeight.Bold, color = RevLensTheme.colors.textPrimary
        )

        RevLensTextField(
            value = dateInput, onValueChange = { dateInput = it },
            label = "Observation Date (YYYY-MM-DD)"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(
                value = controlValInput, onValueChange = { controlValInput = it },
                label = "Control Value", modifier = Modifier.weight(1f)
            )
            NumberField(
                value = controlNInput, onValueChange = { controlNInput = it },
                label = "Control Sample N", modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(
                value = treatmentValInput, onValueChange = { treatmentValInput = it },
                label = "Treatment Value", modifier = Modifier.weight(1f)
            )
            NumberField(
                value = treatmentNInput, onValueChange = { treatmentNInput = it },
                label = "Treatment Sample N", modifier = Modifier.weight(1f)
            )
        }

        RevLensTextField(
            value = notesInput, onValueChange = { notesInput = it }, label = "Notes (Optional)",
            placeholder = "e.g. Day 14 checkpoint"
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                val cVal = controlValInput.toDoubleOrNull() ?: 0.0
                val cN = controlNInput.toIntOrNull() ?: 1
                val tVal = treatmentValInput.toDoubleOrNull() ?: 0.0
                val tN = treatmentNInput.toIntOrNull() ?: 1
                onLog(dateInput, cVal, cN, tVal, tN, notesInput)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction)
        ) {
            Text("Save Observation")
        }
    }
}
