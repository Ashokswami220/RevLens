package com.revlens.app.ui.screens.navbarscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.domain.repository.AuthRepository
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.components.feedback.UpgradePromptSheet
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
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
    val userRole by viewModel.currentUserRole.collectAsState()

    val authRepository: AuthRepository = koinInject()
    val teamRepository: TeamRepository = koinInject()

    var directAuthUser by remember { mutableStateOf<UserAccount?>(null) }
    LaunchedEffect(session) {
        directAuthUser = (session as? SessionState.Authenticated)?.user
            ?: authRepository.getCurrentUser()
    }

    val currentUser = (session as? SessionState.Authenticated)?.user ?: directAuthUser
    val isGuest = currentUser == null

    val wsId = activeWorkspace?.id ?: "ws_default"
    val teamMembers by remember(wsId) { teamRepository.getMembers(wsId) }.collectAsState(initial = emptyList())
    val seatsUsed = teamMembers.size.coerceAtLeast(1)

    val canEditBaseline = RolePermissions.canEditBaselinesAndScenarios(userRole)
    val canManageApiAndIntegrations = RolePermissions.canManageApiAndIntegrations(userRole)
    val canManageBilling = RolePermissions.canManageBilling(userRole)

    var darkModeEnabled by remember { mutableStateOf(true) }
    var lockedFeaturePrompt by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showRoleDeniedToast(requiredRolesText: String) {
        scope.launch {
            snackbarHostState.showSnackbar("Permission denied (${userRole.name}): $requiredRolesText")
        }
    }

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
                    .clip(RoundedCornerShape(16.dp))
                    .background(RevLensTheme.colors.surface)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val displayName =
                    currentUser?.name?.ifBlank { currentUser.email.substringBefore("@") } ?: "Guest User"
                val displayEmail = currentUser?.email ?: "Local-only session"
                val initials = if (currentUser == null) {
                    "GU"
                } else {
                    displayName.split(" ")
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                        .ifBlank { "U" }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
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

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = displayName,
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayEmail,
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isGuest) {
                        Spacer(modifier = Modifier.height(2.dp))
                        RoleChip(role = userRole)
                    }
                }

                if (isGuest) {
                    OutlinedButton(
                        onClick = onNavigateToSignUp,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Sign In", style = RevLensTypography.labelMedium)
                    }
                } else {
                    OutlinedButton(
                        onClick = onNavigateToProfile,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", style = RevLensTypography.labelMedium)
                    }
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
                        subtitle = if (isGuest) "1 member (Local)" else "$seatsUsed of 5 seats used · Your role: ${userRole.name}",
                        isLocked = isGuest,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Team & Permissions"
                            else onNavigateToTeam()
                        }
                    )
                    SettingsNavigationRow(
                        title = "Business Baseline",
                        subtitle = if (canEditBaseline) "Edit plans, pricing & cost parameters" else "Read-only for Viewers",
                        isLocked = !canEditBaseline,
                        onClick = {
                            if (!canEditBaseline) {
                                showRoleDeniedToast("Only Owners, Admins, and Editors can edit baselines.")
                            } else {
                                onNavigateToBaseline()
                            }
                        }
                    )
                }
            }

            // DATA SECTION
            SectionCard(title = "Data & Growth") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsNavigationRow(
                        title = "Import Data (CSV)",
                        subtitle = if (canEditBaseline) "Upload real subscriptions & spend history" else "Requires Editor, Admin, or Owner role",
                        isLocked = !canEditBaseline,
                        onClick = {
                            if (!canEditBaseline) {
                                showRoleDeniedToast("Viewers cannot upload or overwrite workspace datasets.")
                            } else {
                                onNavigateToDataImport()
                            }
                        }
                    )
                    SettingsNavigationRow(
                        title = "Reports & Exports",
                        subtitle = "Generate investor updates & CSV summaries",
                        onClick = onNavigateToReports
                    )
                    SettingsNavigationRow(
                        title = "Integrations",
                        subtitle = if (canManageApiAndIntegrations) "Stripe, Paddle, Google Sheets, Slack" else "Requires Owner or Admin role",
                        isLocked = isGuest || !canManageApiAndIntegrations,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Integrations"
                            else if (!canManageApiAndIntegrations) {
                                showRoleDeniedToast("Only Workspace Owners and Admins can manage integrations.")
                            } else {
                                onNavigateToIntegrations()
                            }
                        }
                    )
                    SettingsNavigationRow(
                        title = "API & Webhooks",
                        subtitle = if (canManageApiAndIntegrations) "API keys, developer webhooks & delivery logs" else "Requires Owner or Admin role",
                        isLocked = isGuest || !canManageApiAndIntegrations,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Developer API"
                            else if (!canManageApiAndIntegrations) {
                                showRoleDeniedToast("Only Workspace Owners and Admins can manage API keys and webhooks.")
                            } else {
                                onNavigateToApiWebhooks()
                            }
                        }
                    )
                }
            }

            // ACCOUNT & SECURITY SECTION
            SectionCard(title = "Account & Security") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsNavigationRow(
                        title = "Billing & Subscription",
                        subtitle = if (canManageBilling) "Manage plan, seats & invoices" else "Only Workspace Owner can manage billing",
                        isLocked = isGuest || !canManageBilling,
                        onClick = {
                            if (isGuest) lockedFeaturePrompt = "Billing & Subscriptions"
                            else if (!canManageBilling) {
                                showRoleDeniedToast("Only the Workspace Owner can manage billing and subscriptions.")
                            } else {
                                onNavigateToBilling()
                            }
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
                        subtitle = "Session actions, leave business & delete account",
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
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                    if (!isGuest) {
                        DestructiveButton(
                            text = "Log Out",
                            onClick = {
                                viewModel.logout()
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }

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
                    color = if (isLocked) RevLensTheme.colors.textSecondary else RevLensTheme.colors.textPrimary
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
