package com.example.revlens.ui.screens.experiments

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
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.revlens.domain.model.ExperimentMetric
import com.example.revlens.domain.model.ExperimentType
import com.example.revlens.ui.components.FilterChip
import com.example.revlens.ui.components.NumberField
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.components.display.ExperimentCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentsScreen(
    viewModel: ExperimentsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExperimentsEffect.NavigateToDetails -> onNavigateToDetails(effect.experimentId)
                is ExperimentsEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
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
                            text = "Experiments",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Validate predictions against reality",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RevLensTheme.colors.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onEvent(ExperimentsEvent.OpenCreateSheet) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Experiment", style = RevLensTypography.labelLarge) },
                containerColor = RevLensTheme.colors.primaryAction,
                contentColor = RevLensTheme.colors.onPrimaryAction
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
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExperimentFilter.entries.forEach { f ->
                    FilterChip(
                        text = f.label,
                        selected = state.filter == f,
                        onClick = { viewModel.onEvent(ExperimentsEvent.SelectFilter(f)) }
                    )
                }
            }

            if (state.experiments.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.experiments.forEach { experiment ->
                        ExperimentCard(
                            experiment = experiment,
                            onClick = { viewModel.onEvent(ExperimentsEvent.ClickExperiment(experiment.id)) }
                        )
                    }
                }
            } else {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "No experiments found",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Text(
                        text = "Start testing live hypotheses to compare observed revenue with model predictions.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Button(
                        onClick = { viewModel.onEvent(ExperimentsEvent.OpenCreateSheet) },
                        colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Experiment")
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Create Experiment Bottom Sheet
    if (state.showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ExperimentsEvent.DismissCreateSheet) },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            CreateExperimentSheet(
                onCreate = { name, type, metric, predLift, thresh, days, cDesc, tDesc ->
                    viewModel.onEvent(
                        ExperimentsEvent.CreateExperiment(
                            name = name,
                            type = type,
                            metric = metric,
                            predictedLift = predLift,
                            threshold = thresh,
                            durationDays = days,
                            controlDesc = cDesc,
                            treatmentDesc = tDesc
                        )
                    )
                },
                onDismiss = { viewModel.onEvent(ExperimentsEvent.DismissCreateSheet) }
            )
        }
    }
}

@Composable
private fun CreateExperimentSheet(
    onCreate: (String, ExperimentType, ExperimentMetric, Double, Double, Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ExperimentType.PRICE_TEST) }
    var selectedMetric by remember { mutableStateOf(ExperimentMetric.MRR) }
    var predictedLiftInput by remember { mutableStateOf("12.0") }
    var thresholdInput by remember { mutableStateOf("5.0") }
    var durationDaysInput by remember { mutableStateOf("30") }
    var controlDesc by remember { mutableStateOf("") }
    var treatmentDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Create New Experiment",
            style = RevLensTypography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        RevLensTextField(
            value = name,
            onValueChange = { name = it },
            label = "Experiment Name",
            placeholder = "e.g. Pro Price Increase to $39"
        )

        // Type selection
        Text(
            text = "Experiment Type",
            style = RevLensTypography.labelMedium,
            color = RevLensTheme.colors.textSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ExperimentType.entries.take(3).forEach { t ->
                FilterChip(
                    text = t.displayName,
                    selected = selectedType == t,
                    onClick = { selectedType = t }
                )
            }
        }

        // Primary Metric
        Text(
            text = "Primary Metric",
            style = RevLensTypography.labelMedium,
            color = RevLensTheme.colors.textSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ExperimentMetric.entries.forEach { m ->
                FilterChip(
                    text = m.displayName,
                    selected = selectedMetric == m,
                    onClick = { selectedMetric = m }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NumberField(
                value = predictedLiftInput,
                onValueChange = { predictedLiftInput = it },
                label = "Predicted Lift",
                suffix = "%",
                modifier = Modifier.weight(1f)
            )
            NumberField(
                value = thresholdInput,
                onValueChange = { thresholdInput = it },
                label = "Success Threshold",
                suffix = "%",
                modifier = Modifier.weight(1f)
            )
        }

        NumberField(
            value = durationDaysInput,
            onValueChange = { durationDaysInput = it },
            label = "Planned Duration",
            suffix = "days"
        )

        RevLensTextField(
            value = controlDesc,
            onValueChange = { controlDesc = it },
            label = "Control (Current Baseline)",
            placeholder = "e.g. Standard $29 pricing"
        )

        RevLensTextField(
            value = treatmentDesc,
            onValueChange = { treatmentDesc = it },
            label = "Treatment (Test Cohort)",
            placeholder = "e.g. New $39 pricing"
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                val pLift = predictedLiftInput.toDoubleOrNull() ?: 10.0
                val thresh = thresholdInput.toDoubleOrNull() ?: 5.0
                val days = durationDaysInput.toIntOrNull() ?: 30
                onCreate(name, selectedType, selectedMetric, pLift, thresh, days, controlDesc, treatmentDesc)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction)
        ) {
            Text("Start Experiment", style = RevLensTypography.labelLarge)
        }
    }
}
