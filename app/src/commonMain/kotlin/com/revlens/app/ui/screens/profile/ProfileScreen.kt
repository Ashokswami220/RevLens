package com.revlens.app.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.components.feedback.TypedConfirmDialog
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

private val TIMEZONES = listOf(
    "UTC",
    "America/New_York (EST)",
    "America/Los_Angeles (PST)",
    "America/Chicago (CST)",
    "Europe/London (GMT)",
    "Europe/Berlin (CET)",
    "Asia/Kolkata (IST)",
    "Asia/Singapore (SGT)",
    "Asia/Tokyo (JST)",
    "Australia/Sydney (AEST)"
)

private val CURRENCIES = listOf(
    "USD" to "$ (US Dollar)",
    "EUR" to "€ (Euro)",
    "GBP" to "£ (British Pound)",
    "CAD" to "$ (Canadian Dollar)",
    "AUD" to "$ (Australian Dollar)",
    "INR" to "₹ (Indian Rupee)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                ProfileEffect.NavigateBack -> onNavigateBack()
                ProfileEffect.LoggedOut -> onNavigateToLogin()
                ProfileEffect.AccountDeleted -> onNavigateToLogin()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Account",
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
            // GOOGLE IDENTITY CARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RevLensTheme.colors.surface)
                    .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(RevLensTheme.colors.brandPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    val initialChar = if (state.name.isNotBlank()) state.name.take(1)
                        .uppercase() else "G"
                    Text(
                        text = initialChar,
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (state.isGuest) "Guest User" else state.name.ifBlank { "User" },
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        RoleChip(role = state.role)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!state.isGuest) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = RevLensTheme.colors.success,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Google Account Verified",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.success,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "Unlinked Guest Session",
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }
            }

            // GUEST UPGRADE CALLOUT (IF GUEST)
            if (state.isGuest) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.08f))
                        .border(
                            1.dp, RevLensTheme.colors.brandPrimary.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Sync Your Models with Google",
                            style = RevLensTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                    }
                    Text(
                        text = "Sign in with Google to back up your workspaces, collaborate with your team, and access revenue analytics across all devices.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    PrimaryButton(
                        text = "Continue with Google",
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // PERSONAL DETAILS
            ProfileSection(title = "Account Details") {
                RevLensTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(ProfileEvent.NameChanged(it)) },
                    label = "Full Name",
                    placeholder = "e.g. Alex Vance",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Google Account Email",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .border(
                                1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = RevLensTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.email,
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                    }
                    Text(
                        text = "Email is managed by Google Sign-In and cannot be edited manually.",
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textTertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                RevLensTextField(
                    value = state.jobTitle,
                    onValueChange = { viewModel.onEvent(ProfileEvent.JobTitleChanged(it)) },
                    label = "Job Title / Role",
                    placeholder = "e.g. Founder & CEO",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // WORKSPACE & REGIONAL PREFERENCES
            ProfileSection(title = "Preferences & Regional") {
                // Timezone picker
                var timezoneMenuOpen by remember { mutableStateOf(false) }
                Column {
                    Text(
                        text = "Workspace Timezone",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RevLensTheme.colors.surfaceElevated)
                                .border(
                                    1.dp, RevLensTheme.colors.borderDefault,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { timezoneMenuOpen = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.brandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.timezone,
                                    style = RevLensTypography.bodyMedium,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = timezoneMenuOpen,
                            onDismissRequest = { timezoneMenuOpen = false }
                        ) {
                            TIMEZONES.forEach { tz ->
                                DropdownMenuItem(
                                    text = { Text(tz) },
                                    onClick = {
                                        viewModel.onEvent(ProfileEvent.TimezoneChanged(tz))
                                        timezoneMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency picker
                var currencyMenuOpen by remember { mutableStateOf(false) }
                Column {
                    Text(
                        text = "Default Currency",
                        style = RevLensTypography.labelMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RevLensTheme.colors.surfaceElevated)
                                .border(
                                    1.dp, RevLensTheme.colors.borderDefault,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { currencyMenuOpen = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.brandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                val label = CURRENCIES.find { it.first == state.currency }?.second
                                    ?: state.currency
                                Text(
                                    text = label,
                                    style = RevLensTypography.bodyMedium,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = currencyMenuOpen,
                            onDismissRequest = { currencyMenuOpen = false }
                        ) {
                            CURRENCIES.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.onEvent(ProfileEvent.CurrencyChanged(code))
                                        currencyMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SAVE CHANGES BUTTON
            PrimaryButton(
                text = if (state.isSaving) "Saving Changes..." else "Save Profile Changes",
                onClick = { viewModel.onEvent(ProfileEvent.SaveChanges) },
                enabled = state.hasChanges && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // SESSION & LOGOUT SECTION
            if (!state.isGuest) {
                ProfileSection(title = "Account Session") {
                    Text(
                        text = "Signed in as ${state.email}. You can sign out from this device at any time.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SecondaryButton(
                        text = "Sign Out from RevLens",
                        onClick = { viewModel.onEvent(ProfileEvent.LogoutClicked) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                ProfileSection(title = "Guest Mode") {
                    Text(
                        text = "You are currently exploring RevLens in Guest mode. Sign in with your Google account to sync your forecasts and workspaces to the cloud.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    PrimaryButton(
                        text = "Sign In with Google Account",
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // DANGER ZONE
            if (!state.isGuest) {
                ProfileSection(title = "Danger Zone") {
                    Text(
                        text = "Permanently delete your RevLens account. All active workspace associations and personal modeling data will be removed.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DestructiveButton(
                        text = "Delete RevLens Account",
                        onClick = { viewModel.onEvent(ProfileEvent.OpenDeleteDialog) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        TypedConfirmDialog(
            title = "Delete Account?",
            body = "This action is permanent and cannot be undone. All your saved models and preferences will be permanently wiped.",
            requiredWord = "DELETE",
            confirmText = "Delete Permanently",
            onConfirm = { viewModel.onEvent(ProfileEvent.ConfirmDeleteAccount("DELETE")) },
            onDismiss = { viewModel.onEvent(ProfileEvent.DismissDeleteDialog) },
            isDestructive = true
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = RevLensTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}
