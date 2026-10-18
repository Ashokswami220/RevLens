package com.revlens.app.ui.screens.data

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.CanonicalField
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.components.display.CsvPreviewTable
import com.revlens.app.ui.components.feedback.ValidationSummary
import com.revlens.app.ui.components.inputs.ColumnMapperRow
import com.revlens.app.ui.components.inputs.FilePickerCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportScreen(
    viewModel: DataImportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DataImportEffect.NavigateToAnalytics -> onNavigateToAnalytics()
                is DataImportEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.text.asString())
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
                            text = "Data Import Wizard",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Ingest subscription and marketing records",
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
        bottomBar = {
            if (state.step < 4) {
                WizardBottomBar(
                    step = state.step,
                    canProceed = when (state.step) {
                        0 -> state.canProceedFromStep0
                        1 -> state.canProceedFromStep1
                        2 -> state.canProceedFromStep2
                        3 -> state.canCommit
                        else -> true
                    },
                    isLoading = state.isLoading,
                    onPreviousClick = { viewModel.onEvent(DataImportEvent.PreviousStep) },
                    onNextClick = { viewModel.onEvent(DataImportEvent.NextStep) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Step Progress Indicator
            StepProgressHeader(activeStep = state.step)

            // Step Content
            when (state.step) {
                0 -> StepTypeSelection(
                    selectedType = state.datasetType,
                    onSelectType = { viewModel.onEvent(DataImportEvent.SelectDatasetType(it)) }
                )
                1 -> StepUploadFile(
                    pickedFile = state.pickedFile,
                    parsedCsv = state.parsedCsv,
                    onPickClick = { viewModel.onEvent(DataImportEvent.PickFile) },
                    onRemoveClick = { viewModel.onEvent(DataImportEvent.RemoveFile) }
                )
                2 -> StepColumnMapping(
                    datasetType = state.datasetType,
                    headers = state.parsedCsv?.headers ?: emptyList(),
                    sampleRow = state.parsedCsv?.rows?.firstOrNull() ?: emptyList(),
                    mapping = state.mapping,
                    autoMappedFields = state.autoMappedFields,
                    onMappingChanged = { field, header ->
                        viewModel.onEvent(DataImportEvent.UpdateMapping(field, header))
                    }
                )
                3 -> StepReviewValidation(
                    report = state.validationReport,
                    skipErrors = state.skipErrors,
                    updateBaseline = state.updateBaseline,
                    onToggleSkipErrors = { viewModel.onEvent(DataImportEvent.ToggleSkipErrors(it)) },
                    onToggleUpdateBaseline = { viewModel.onEvent(DataImportEvent.ToggleUpdateBaseline(it)) }
                )
                4 -> StepSuccess(
                    filename = state.pickedFile?.name ?: "import.csv",
                    recordCount = state.validationReport?.validCount ?: 0,
                    datasetType = state.datasetType,
                    onGoToAnalytics = onNavigateToAnalytics,
                    onImportAnother = { viewModel.onEvent(DataImportEvent.ResetWizard) }
                )
            }
        }
    }
}

@Composable
private fun StepProgressHeader(activeStep: Int) {
    val steps = listOf("Type", "Upload", "Mapping", "Review")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val isCompleted = activeStep > index
            val isActive = activeStep == index

            val circleColor = when {
                isCompleted -> RevLensTheme.colors.success
                isActive -> RevLensTheme.colors.brandPrimary
                else -> RevLensTheme.colors.surfaceElevated
            }
            val textColor = when {
                isActive -> RevLensTheme.colors.brandPrimary
                isCompleted -> RevLensTheme.colors.textPrimary
                else -> RevLensTheme.colors.textSecondary
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else RevLensTheme.colors.textSecondary
                        )
                    }
                }
                Text(
                    text = title,
                    style = RevLensTypography.labelMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
            if (index < steps.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .padding(horizontal = 8.dp)
                        .background(RevLensTheme.colors.borderDefault)
                )
            }
        }
    }
}

@Composable
private fun StepTypeSelection(
    selectedType: ImportDatasetType,
    onSelectType: (ImportDatasetType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Select Dataset Type",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )
        Text(
            text = "Choose the type of records you would like to import. RevLens supports recurring subscriptions and monthly marketing spend.",
            style = RevLensTypography.bodySmall,
            color = RevLensTheme.colors.textSecondary
        )

        DatasetTypeOptionCard(
            title = "Customer Subscriptions",
            subtitle = "Customer IDs, plan tiers, MRR amount, start/end dates, acquisition channels, and country codes.",
            icon = Icons.Filled.CreditCard,
            isSelected = selectedType == ImportDatasetType.SUBSCRIPTIONS,
            onClick = { onSelectType(ImportDatasetType.SUBSCRIPTIONS) }
        )

        DatasetTypeOptionCard(
            title = "Marketing & Acquisition Spend",
            subtitle = "Monthly marketing ad spend, paid channels (Google, Meta, LinkedIn), and new customer counts.",
            icon = Icons.Filled.Campaign,
            isSelected = selectedType == ImportDatasetType.MARKETING_SPEND,
            onClick = { onSelectType(ImportDatasetType.MARKETING_SPEND) }
        )
    }
}

@Composable
private fun DatasetTypeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.borderDefault
    val bg = if (isSelected) RevLensTheme.colors.brandPrimary.copy(alpha = 0.08f) else RevLensTheme.colors.surface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.surfaceElevated
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else RevLensTheme.colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun StepUploadFile(
    pickedFile: com.revlens.app.core.platform.PickedFile?,
    parsedCsv: com.revlens.app.domain.calc.ParsedCsv?,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Upload CSV File",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        FilePickerCard(
            file = pickedFile,
            onPickFileClick = onPickClick,
            onRemoveFileClick = onRemoveClick
        )

        if (parsedCsv != null) {
            Text(
                text = "Preview First 5 Rows (${parsedCsv.rows.size} total rows parsed)",
                style = RevLensTypography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = RevLensTheme.colors.textPrimary
            )
            CsvPreviewTable(
                headers = parsedCsv.headers,
                rows = parsedCsv.rows
            )
        }
    }
}

@Composable
private fun StepColumnMapping(
    datasetType: ImportDatasetType,
    headers: List<String>,
    sampleRow: List<String>,
    mapping: Map<CanonicalField, String?>,
    autoMappedFields: Set<CanonicalField>,
    onMappingChanged: (CanonicalField, String?) -> Unit
) {
    val targetFields = when (datasetType) {
        ImportDatasetType.SUBSCRIPTIONS -> listOf(
            CanonicalField.CUSTOMER_ID,
            CanonicalField.PLAN,
            CanonicalField.START_DATE,
            CanonicalField.END_DATE,
            CanonicalField.MRR,
            CanonicalField.CHANNEL,
            CanonicalField.COUNTRY
        )
        ImportDatasetType.MARKETING_SPEND -> listOf(
            CanonicalField.MONTH,
            CanonicalField.SPEND,
            CanonicalField.CHANNEL,
            CanonicalField.NEW_CUSTOMERS
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(RevLensTheme.colors.surfaceElevated)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = RevLensTheme.colors.brandPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Headers were auto-matched with synonymous column names. You can adjust dropdowns below if needed.",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }

        targetFields.forEach { field ->
            val selected = mapping[field]
            val sampleVal = selected?.let { h ->
                val colIdx = headers.indexOf(h)
                if (colIdx >= 0) sampleRow.getOrNull(colIdx) else null
            }
            ColumnMapperRow(
                field = field,
                availableHeaders = headers,
                selectedHeader = selected,
                sampleValue = sampleVal,
                isAutoMapped = autoMappedFields.contains(field),
                onHeaderSelected = { header -> onMappingChanged(field, header) }
            )
        }
    }
}

@Composable
private fun StepReviewValidation(
    report: com.revlens.app.domain.calc.ValidationReport?,
    skipErrors: Boolean,
    updateBaseline: Boolean,
    onToggleSkipErrors: (Boolean) -> Unit,
    onToggleUpdateBaseline: (Boolean) -> Unit
) {
    if (report == null) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ValidationSummary(report = report)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RevLensTheme.colors.surface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSkipErrors(!skipErrors) }
            ) {
                Checkbox(
                    checked = skipErrors,
                    onCheckedChange = onToggleSkipErrors,
                    colors = CheckboxDefaults.colors(
                        checkedColor = RevLensTheme.colors.brandPrimary
                    )
                )
                Text(
                    text = "Skip invalid rows and import remaining valid records",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleUpdateBaseline(!updateBaseline) }
            ) {
                Checkbox(
                    checked = updateBaseline,
                    onCheckedChange = onToggleUpdateBaseline,
                    colors = CheckboxDefaults.colors(
                        checkedColor = RevLensTheme.colors.brandPrimary
                    )
                )
                Text(
                    text = "Update active workspace benchmark baseline",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun StepSuccess(
    filename: String,
    recordCount: Int,
    datasetType: ImportDatasetType,
    onGoToAnalytics: () -> Unit,
    onImportAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(RevLensTheme.colors.success.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = RevLensTheme.colors.success,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = "Import Successful!",
            style = RevLensTypography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        Text(
            text = "Successfully parsed and saved $recordCount records from '$filename' as ${datasetType.name.lowercase()} data.",
            style = RevLensTypography.bodyMedium,
            color = RevLensTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryButton(
            text = "View Analytics",
            onClick = onGoToAnalytics,
            modifier = Modifier.fillMaxWidth()
        )

        SecondaryButton(
            text = "Import Another Dataset",
            onClick = onImportAnother,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WizardBottomBar(
    step: Int,
    canProceed: Boolean,
    isLoading: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(
        color = RevLensTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                SecondaryButton(
                    text = "Back",
                    onClick = onPreviousClick,
                    modifier = Modifier.weight(1f)
                )
            }

            val nextTitle = when (step) {
                0 -> "Select File"
                1 -> "Map Columns"
                2 -> "Review"
                3 -> "Commit Import"
                else -> "Next"
            }

            PrimaryButton(
                text = nextTitle,
                onClick = onNextClick,
                enabled = canProceed,
                isLoading = isLoading,
                modifier = Modifier.weight(if (step > 0) 2f else 1f)
            )
        }
    }
}
