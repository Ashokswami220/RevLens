package com.revlens.app.ui.screens.navbarscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.TextLinkButton
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.components.feedback.UpgradePromptSheet
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToWorkspace: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToBaseline: () -> Unit = {},
    onNavigateToDataImport: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToIntegrations: () -> Unit = {},
    onNavigateToApiWebhooks: () -> Unit = {},
    onNavigateToBilling: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToMore: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val activeWorkspace by viewModel.activeWorkspace.collectAsState()
    val session by viewModel.session.collectAsState()
    val isGuest = session !is SessionState.Authenticated

    var darkModeEnabled by remember { mutableStateOf(true) }
    var lockedFeaturePrompt by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Settings",
                onBackClick = null
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile & Account Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(RevLensTheme.colors.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val currentUser = (session as? SessionState.Authenticated)?.user
                    val displayName =
                        if (isGuest) "Guest User" else (currentUser?.name?.ifBlank { null }
                            ?: "User")
                    val displayEmail =
                        if (isGuest) "Local-only session" else (currentUser?.email ?: "")
                    val initials = if (isGuest) {
                        "GU"
                    } else {
                        displayName.split(" ")
                            .filter { it.isNotBlank() }
                            .mapNotNull {
                                it.firstOrNull()
                                    ?.toString()
                            }
                            .take(2)
                            .joinToString("")
                            .uppercase()
                            .ifBlank { "U" }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.brandPrimary)
                    ) {
                        Text(
                            text = initials,
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.background
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = displayName,
                                style = RevLensTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            RoleChip(role = Role.Owner)
                        }
                        Text(
                            text = displayEmail,
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }

                if (isGuest) {
                    TextLinkButton(
                        text = "Sign In",
                        onClick = onNavigateToSignUp
                    )
                } else {
                    TextLinkButton(
                        text = "Edit",
                        onClick = onNavigateToProfile
                    )
                }
            }

            // Guest Cloud Sync Prompt (if guest)
            if (isGuest) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.08f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Sync & Collaboration",
                            style = RevLensTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Create an account to backup your data and invite teammates.",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        text = "Sign Up",
                        onClick = onNavigateToSignUp,
                        modifier = Modifier.width(100.dp)
                    )
                }
            }

            // WORKSPACE SECTION
            SectionCard(title = "Workspace") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsNavigationRow(
                        title = "Workspace Details",
                        subtitle = activeWorkspace?.name ?: "My Workspace",
                        onClick = onNavigateToWorkspace
                    )
                    SettingsNavigationRow(
                        title = "Team & Permissions",
                        subtitle = if (isGuest) "1 member (Local)" else "3 of 5 seats used",
                        isLocked = isGuest,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Team & Permissions"
                            else onNavigateToTeam()
                        }
                    )
                    SettingsNavigationRow(
                        title = "Business Baseline",
                        subtitle = "Edit plans, pricing & cost parameters",
                        onClick = onNavigateToBaseline
                    )
                }
            }

            // DATA SECTION
            SectionCard(title = "Data & Growth") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsNavigationRow(
                        title = "Import Data (CSV)",
                        subtitle = "Upload real subscriptions & spend history",
                        onClick = onNavigateToDataImport
                    )
                    SettingsNavigationRow(
                        title = "Reports & Exports",
                        subtitle = "Generate investor updates & summaries",
                        onClick = onNavigateToReports
                    )
                    SettingsNavigationRow(
                        title = "Integrations",
                        subtitle = "Stripe, Paddle, Google Sheets, Slack",
                        isLocked = isGuest,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Integrations"
                            else onNavigateToIntegrations()
                        }
                    )
                    SettingsNavigationRow(
                        title = "API & Webhooks",
                        subtitle = "API keys, developer webhooks & delivery logs",
                        isLocked = isGuest,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Developer API"
                            else onNavigateToApiWebhooks()
                        }
                    )
                }
            }

            // ACCOUNT & SECURITY SECTION
            SectionCard(title = "Account & Security") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsNavigationRow(
                        title = "Billing & Subscription",
                        subtitle = "Free plan · Manage subscription",
                        isLocked = isGuest,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Billing & Subscriptions"
                            else onNavigateToBilling()
                        }
                    )
                    SettingsNavigationRow(
                        title = "Security & App Lock",
                        subtitle = "Biometric lock, two-factor auth & sessions",
                        onClick = onNavigateToSecurity
                    )
                    SettingsNavigationRow(
                        title = "Notifications & Alerts",
                        subtitle = "Milestone alerts, churn spikes & quiet hours",
                        onClick = onNavigateToNotifications
                    )
                    SettingsNavigationRow(
                        title = "More",
                        subtitle = "Log out, session actions & delete account",
                        onClick = onNavigateToMore
                    )
                }
            }

            // APPEARANCE SECTION
            SectionCard(title = "Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Dark Mode",
                                style = RevLensTypography.bodyLarge,
                                color = RevLensTheme.colors.textPrimary
                            )
                            Text(
                                text = "Toggle theme appearance",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                    }

                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RevLensTheme.colors.background,
                            checkedTrackColor = RevLensTheme.colors.brandPrimary
                        )
                    )
                }
            }

            // ABOUT & LOG OUT SECTION
            SectionCard(title = "About RevLens") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Version",
                                style = RevLensTypography.bodyLarge,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                        Text(
                            text = "1.0.0 (KMP)",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    // Locked Feature Prompt Bottom Sheet
    lockedFeaturePrompt?.let { feature ->
        UpgradePromptSheet(
            featureName = feature,
            description = "Create a free RevLens account to enable $feature and sync your revenue models across devices.",
            onSeePlans = onNavigateToSignUp,
            onDismiss = { lockedFeaturePrompt = null }
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = RevLensTypography.bodyLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = RevLensTheme.colors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = RevLensTheme.colors.textTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
