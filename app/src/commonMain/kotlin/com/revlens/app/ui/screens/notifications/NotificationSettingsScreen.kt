package com.revlens.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.components.NumberField
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.inputs.TimeField
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val prefs = state.prefs
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotificationSettingsEffect.ShowToast -> snackbarHostState.showSnackbar(
                    effect.message
                )
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications & Alerts",
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
            // CLOUD SYNC STATUS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RevLensTheme.colors.surface)
                    .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (state.syncStatus) {
                                    NotificationSyncStatus.Synced -> RevLensTheme.colors.success
                                    NotificationSyncStatus.Syncing -> RevLensTheme.colors.brandPrimary
                                    NotificationSyncStatus.Error -> RevLensTheme.colors.error
                                }
                            )
                    )
                    Text(
                        text = when (state.syncStatus) {
                            NotificationSyncStatus.Synced -> "All notification preferences synced to cloud"
                            NotificationSyncStatus.Syncing -> "Syncing preferences to cloud..."
                            NotificationSyncStatus.Error -> "Cloud sync offline · saved locally"
                        },
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                }

                if (state.syncStatus == NotificationSyncStatus.Error) {
                    Text(
                        text = "Retry",
                        style = RevLensTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.brandPrimary,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(
                                NotificationSettingsEvent.RetrySync
                            )
                        }
                    )
                }
            }

            // SECTION 1: DELIVERY CHANNELS
            SectionCard(title = "Delivery Channels") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PreferenceSwitchRow(
                        title = "Push Notifications",
                        subtitle = "Instant system alerts on critical metric anomalies",
                        checked = prefs.pushEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.TogglePush(it)
                            )
                        }
                    )
                    PreferenceSwitchRow(
                        title = "Email Notifications",
                        subtitle = "Weekly executive digests and monthly reports",
                        checked = prefs.emailEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleEmail(it)
                            )
                        }
                    )
                }
            }

            // SECTION 2: METRIC ALERT RULES
            SectionCard(title = "Metric Alerts & Thresholds") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PreferenceSwitchRow(
                        title = "Goal Milestones",
                        subtitle = "Notify when MRR crosses 25%, 50%, 75%, and 100% of target",
                        checked = prefs.goalMilestones,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleGoalMilestones(it)
                            )
                        }
                    )

                    PreferenceSwitchRow(
                        title = "Off-Track Warnings",
                        subtitle = "Alert when growth velocity falls below planned scenario run-rate",
                        checked = prefs.offTrackWarnings,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleOffTrackWarnings(it)
                            )
                        }
                    )

                    // Churn Spike with Inline NumberField
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreferenceSwitchRow(
                            title = "Churn Spike Alert",
                            subtitle = "Trigger alert when monthly customer churn spikes above threshold",
                            checked = prefs.churnSpikeEnabled,
                            onCheckedChange = {
                                viewModel.onEvent(
                                    NotificationSettingsEvent.ToggleChurnSpike(it)
                                )
                            }
                        )
                        if (prefs.churnSpikeEnabled) {
                            NumberField(
                                value = prefs.churnSpikeThresholdPercent.toString(),
                                onValueChange = {
                                    val d = it.toDoubleOrNull() ?: 0.0
                                    viewModel.onEvent(
                                        NotificationSettingsEvent.UpdateChurnThreshold(d)
                                    )
                                },
                                label = "Churn Alert Threshold",
                                suffix = "% / mo",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // MRR Drop with Inline NumberField
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreferenceSwitchRow(
                            title = "MRR Drop Warning",
                            subtitle = "Trigger warning when net monthly expansion falls below limit",
                            checked = prefs.mrrDropEnabled,
                            onCheckedChange = {
                                viewModel.onEvent(
                                    NotificationSettingsEvent.ToggleMrrDrop(it)
                                )
                            }
                        )
                        if (prefs.mrrDropEnabled) {
                            NumberField(
                                value = prefs.mrrDropThresholdPercent.toString(),
                                onValueChange = {
                                    val d = it.toDoubleOrNull() ?: 0.0
                                    viewModel.onEvent(
                                        NotificationSettingsEvent.UpdateMrrDropThreshold(d)
                                    )
                                },
                                label = "MRR Drop Threshold",
                                suffix = "% / mo",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    PreferenceSwitchRow(
                        title = "Experiment Completed",
                        subtitle = "Notify when statistical sample size reaches statistical confidence",
                        checked = prefs.experimentFinished,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleExperimentFinished(it)
                            )
                        }
                    )
                }
            }

            // SECTION 3: DIGESTS & SCHEDULES
            SectionCard(title = "Scheduled Summaries") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PreferenceSwitchRow(
                        title = "Weekly Summary Digest",
                        subtitle = "Send recap of net additions, top plans & ARR velocity (${prefs.weeklyDay} at ${prefs.weeklyTime})",
                        checked = prefs.weeklySummaryEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleWeeklySummary(it)
                            )
                        }
                    )

                    PreferenceSwitchRow(
                        title = "Monthly Report Ready",
                        subtitle = "Notify automatically when monthly revenue reconciliation finishes",
                        checked = prefs.monthlyReportReady,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleMonthlyReport(it)
                            )
                        }
                    )
                }
            }

            // SECTION 4: QUIET HOURS
            SectionCard(title = "Quiet Hours") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PreferenceSwitchRow(
                        title = "Enable Quiet Hours",
                        subtitle = "Mute push notifications overnight during selected window",
                        checked = prefs.quietHoursEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(
                                NotificationSettingsEvent.ToggleQuietHours(it)
                            )
                        }
                    )

                    if (prefs.quietHoursEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TimeField(
                                label = "Quiet Start",
                                time = prefs.quietHoursStart,
                                onTimeChange = {
                                    viewModel.onEvent(
                                        NotificationSettingsEvent.UpdateQuietHoursTimes(
                                            it, prefs.quietHoursEnd
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            TimeField(
                                label = "Quiet End",
                                time = prefs.quietHoursEnd,
                                onTimeChange = {
                                    viewModel.onEvent(
                                        NotificationSettingsEvent.UpdateQuietHoursTimes(
                                            prefs.quietHoursStart, it
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // SECTION 5: ALWAYS ON (SECURITY & BILLING)
            SectionCard(title = "Mandatory Alerts") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Security & Billing Notices",
                                style = RevLensTypography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = RevLensTheme.colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Failed payments, login from new device, and password updates cannot be disabled.",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    Switch(
                        checked = true,
                        onCheckedChange = null,
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            disabledCheckedThumbColor = RevLensTheme.colors.brandPrimary,
                            disabledCheckedTrackColor = RevLensTheme.colors.brandPrimary.copy(
                                alpha = 0.3f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = RevLensTypography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RevLensTheme.colors.surface,
                checkedTrackColor = RevLensTheme.colors.brandPrimary,
                uncheckedThumbColor = RevLensTheme.colors.textSecondary,
                uncheckedTrackColor = RevLensTheme.colors.borderDefault
            )
        )
    }
}
