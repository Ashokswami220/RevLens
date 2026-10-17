package com.example.revlens.ui.screens.reports

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
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.model.PeriodPreset
import com.example.revlens.domain.model.ReportFormat
import com.example.revlens.domain.model.ReportSection
import com.example.revlens.domain.model.ReportTemplate
import com.example.revlens.ui.components.FilterChip
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.components.display.CodeBlock
import com.example.revlens.ui.components.display.ReportCard
import com.example.revlens.ui.components.display.ToolTile
import com.example.revlens.ui.components.feedback.UpgradePromptSheet
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ReportsEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports & Exports",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
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
                onClick = { viewModel.onEvent(ReportsEvent.OpenGenerateSheet()) },
                containerColor = RevLensTheme.colors.brandPrimary,
                contentColor = RevLensTheme.colors.surface,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = {
                    Text(
                        "New Report",
                        style = RevLensTypography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        containerColor = RevLensTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Create a Report (Template Quick Tiles)
            Text(
                text = "Create a Report",
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolTile(
                    title = "Investor Update",
                    description = "Executive KPIs, runway & revenue trajectory",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    onClick = { viewModel.onEvent(ReportsEvent.OpenGenerateSheet(ReportTemplate.INVESTOR_UPDATE)) },
                    modifier = Modifier.weight(1f)
                )
                ToolTile(
                    title = "Monthly Revenue",
                    description = "MRR bridge, churn decay & plan breakdown",
                    icon = Icons.Filled.Assessment,
                    onClick = { viewModel.onEvent(ReportsEvent.OpenGenerateSheet(ReportTemplate.MONTHLY_REVENUE)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolTile(
                    title = "Scenario Comparison",
                    description = "Side-by-side impact across pricing levers",
                    icon = Icons.AutoMirrored.Outlined.CompareArrows,
                    onClick = { viewModel.onEvent(ReportsEvent.OpenGenerateSheet(ReportTemplate.SCENARIO_COMPARISON)) },
                    modifier = Modifier.weight(1f)
                )
                ToolTile(
                    title = "Forecast Summary",
                    description = "12-month projections & target milestones",
                    icon = Icons.Filled.Timeline,
                    onClick = { viewModel.onEvent(ReportsEvent.OpenGenerateSheet(ReportTemplate.FORECAST_SUMMARY)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 2: Recent Reports & Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Reports",
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReportFilter.entries.forEach { f ->
                        FilterChip(
                            text = f.label,
                            selected = state.filter == f,
                            onClick = { viewModel.onEvent(ReportsEvent.SelectFilter(f)) }
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RevLensTheme.colors.brandPrimary)
                }
            } else if (state.reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RevLensTheme.colors.surface)
                        .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = RevLensTheme.colors.textTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No reports generated in this category",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (report in state.reports) {
                        ReportCard(
                            report = report,
                            onClick = { viewModel.onEvent(ReportsEvent.ViewReport(report)) },
                            onShare = { viewModel.onEvent(ReportsEvent.ShareReport(report)) },
                            onDelete = { viewModel.onEvent(ReportsEvent.DeleteReport(report.id)) }
                        )
                    }
                }
            }

            // Section 3: Scheduled Reports Card (Locked for Guests / Pro feature)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RevLensTheme.colors.surface)
                    .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Scheduled Automated Reports",
                            style = RevLensTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PRO / TEAM",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }
                }

                Text(
                    text = "Automatically generate and email monthly shareholder decks and weekly revenue digests directly to your executive team.",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )

                Button(
                    onClick = { viewModel.onEvent(ReportsEvent.ClickUpgradeScheduled) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RevLensTheme.colors.surface,
                        contentColor = RevLensTheme.colors.brandPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RevLensTheme.colors.brandPrimary, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        "Upgrade to Configure Schedule",
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal Bottom Sheet: Generate Report
    if (state.showGenerateSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ReportsEvent.DismissGenerateSheet) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generate Report",
                        style = RevLensTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    IconButton(onClick = { viewModel.onEvent(ReportsEvent.DismissGenerateSheet) }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = RevLensTheme.colors.textSecondary
                        )
                    }
                }

                // Template Choice Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Report Template",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReportTemplate.entries.take(2).forEach { t ->
                            FilterChip(
                                text = t.displayName,
                                selected = state.sheetTemplate == t,
                                onClick = { viewModel.onEvent(ReportsEvent.UpdateTemplate(t)) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReportTemplate.entries.drop(2).forEach { t ->
                            FilterChip(
                                text = t.displayName,
                                selected = state.sheetTemplate == t,
                                onClick = { viewModel.onEvent(ReportsEvent.UpdateTemplate(t)) }
                            )
                        }
                    }
                }

                // Report Title
                RevLensTextField(
                    value = state.sheetTitle,
                    onValueChange = { viewModel.onEvent(ReportsEvent.UpdateTitle(it)) },
                    label = "Report Title",
                    modifier = Modifier.fillMaxWidth()
                )

                // Period Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Reporting Period",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PeriodPreset.entries.forEach { p ->
                            FilterChip(
                                text = p.displayName,
                                selected = state.sheetPreset == p,
                                onClick = { viewModel.onEvent(ReportsEvent.UpdatePeriodPreset(p)) }
                            )
                        }
                    }
                }

                // Format Picker (PDF vs CSV)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Export Format",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportFormat.entries.forEach { fmt ->
                            val isSelected = state.sheetFormat == fmt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)
                                        else RevLensTheme.colors.surface
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.borderDefault,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.onEvent(ReportsEvent.UpdateFormat(fmt)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fmt.label,
                                    style = RevLensTypography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Included Sections Checklist
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Include Sections",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )

                    ReportSection.entries.forEach { section ->
                        val isChecked = state.sheetSections.contains(section)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.surface)
                                .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp))
                                .clickable { viewModel.onEvent(ReportsEvent.ToggleSection(section)) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = section.title,
                                    style = RevLensTypography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = section.description,
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { viewModel.onEvent(ReportsEvent.ToggleSection(section)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RevLensTheme.colors.surface,
                                    checkedTrackColor = RevLensTheme.colors.brandPrimary,
                                    uncheckedThumbColor = RevLensTheme.colors.textSecondary,
                                    uncheckedTrackColor = RevLensTheme.colors.borderDefault
                                )
                            )
                        }
                    }
                }

                // Strategic Notes
                RevLensTextField(
                    value = state.sheetNotes,
                    onValueChange = { viewModel.onEvent(ReportsEvent.UpdateNotes(it)) },
                    label = "Executive Notes (Optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Generate Button
                Button(
                    onClick = { viewModel.onEvent(ReportsEvent.GenerateReport) },
                    enabled = !state.isGenerating && state.sheetSections.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RevLensTheme.colors.brandPrimary,
                        contentColor = RevLensTheme.colors.surface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            color = RevLensTheme.colors.surface,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Generate ${state.sheetFormat.name} Report",
                            style = RevLensTypography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Bottom Sheet: Preview Report
    val preview = state.previewReport
    if (preview != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ReportsEvent.DismissViewReport) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preview.title,
                            style = RevLensTypography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "${preview.format.label} · ${preview.periodStart} to ${preview.periodEnd}",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(ReportsEvent.DismissViewReport) }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = RevLensTheme.colors.textSecondary
                        )
                    }
                }

                // Summary Metrics Grid
                if (preview.summaryMetrics.isNotEmpty()) {
                    Text(
                        text = "Executive Summary",
                        style = RevLensTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RevLensTheme.colors.background)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((metric, value) in preview.summaryMetrics) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = metric,
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                                Text(
                                    text = value,
                                    style = RevLensTypography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Data Preview (CSV Snippet / Raw Content)
                Text(
                    text = "Raw Content Preview",
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                CodeBlock(
                    code = preview.csvData.ifBlank { "No text content generated." }
                )

                // Share / Action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(ReportsEvent.ShareReport(preview)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RevLensTheme.colors.brandPrimary,
                            contentColor = RevLensTheme.colors.surface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Document", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Upgrade Prompt Sheet
    if (state.showUpgradePrompt) {
        UpgradePromptSheet(
            featureName = "Automated Scheduled Reports",
            description = "Schedule recurring PDF and CSV exports delivered directly to your inbox.",
            onSeePlans = { viewModel.onEvent(ReportsEvent.DismissUpgradePrompt) },
            onDismiss = { viewModel.onEvent(ReportsEvent.DismissUpgradePrompt) }
        )
    }
}

