package com.example.revlens.ui.screens.security

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.revlens.domain.model.LockTimeout
import com.example.revlens.ui.components.FilterChip
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.components.display.StatusBadge
import com.example.revlens.ui.components.display.StatusBadgeType
import com.example.revlens.ui.components.feedback.UpgradePromptSheet
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: SecuritySettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SecuritySettingsEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Security & App Lock",
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
            // SECTION 1: APP LOCK (LOCAL DEVICE PROTECTION)
            SectionCard(title = "App Lock (Device Protection)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Biometric Unlock Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.brandPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Biometric Lock",
                                    style = RevLensTypography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (state.isBiometricSupported) "Require fingerprint or face to open RevLens" else "Biometrics not enrolled on device",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = state.isBiometricEnabled,
                            onCheckedChange = { viewModel.onEvent(SecuritySettingsEvent.ToggleBiometric(it)) },
                            enabled = state.isBiometricSupported,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RevLensTheme.colors.surface,
                                checkedTrackColor = RevLensTheme.colors.brandPrimary,
                                uncheckedThumbColor = RevLensTheme.colors.textSecondary,
                                uncheckedTrackColor = RevLensTheme.colors.borderDefault
                            )
                        )
                    }

                    if (state.isBiometricEnabled) {
                        // Timeout Selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Auto-Lock Timeout",
                                style = RevLensTypography.labelMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (timeout in LockTimeout.entries) {
                                    FilterChip(
                                        text = timeout.displayName,
                                        selected = state.timeout == timeout,
                                        onClick = { viewModel.onEvent(SecuritySettingsEvent.ChangeTimeout(timeout)) }
                                    )
                                }
                            }
                        }

                        // Lock Now Button
                        Button(
                            onClick = { viewModel.onEvent(SecuritySettingsEvent.LockNow) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RevLensTheme.colors.surfaceElevated,
                                contentColor = RevLensTheme.colors.brandPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lock App Immediately", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // SECTION 2: ACCOUNT & CLOUD SECURITY (PASSWORD & 2FA)
            SectionCard(title = "Account Credentials & 2FA") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (state.isGuest) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.08f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Guest Local Mode",
                                    style = RevLensTypography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Create an account to protect cloud models with passwords and two-factor authentication.",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // Change Password Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEvent(SecuritySettingsEvent.OpenPasswordSheet) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RevLensTheme.colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Change Password",
                                    style = RevLensTypography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (state.isGuest) "Locked for Guests" else "Last changed 45 days ago",
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = RevLensTheme.colors.textSecondary
                        )
                    }

                    // Two-Factor Authentication Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RevLensTheme.colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Two-Factor Auth (TOTP)",
                                    style = RevLensTypography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (state.twoFactorEnabled) "Enabled via Authenticator" else "Extra layer of account defense",
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = state.twoFactorEnabled,
                            onCheckedChange = { viewModel.onEvent(SecuritySettingsEvent.ToggleTwoFactor) },
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

            // SECTION 3: ACTIVE SESSIONS
            SectionCard(title = "Active Sessions") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (session in state.sessions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.background)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = session.deviceName,
                                        style = RevLensTypography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RevLensTheme.colors.textPrimary
                                    )
                                    if (session.isCurrent) {
                                        StatusBadge(type = StatusBadgeType.RUNNING)
                                    }
                                }
                                Text(
                                    text = "${session.location} · ${session.lastActive}",
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }

                            if (!session.isCurrent) {
                                Button(
                                    onClick = { viewModel.onEvent(SecuritySettingsEvent.RevokeSession(session.id)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RevLensTheme.colors.surface,
                                        contentColor = RevLensTheme.colors.error
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Sign Out", style = RevLensTypography.labelSmall)
                                }
                            }
                        }
                    }

                    if (state.sessions.size > 1) {
                        Button(
                            onClick = { viewModel.onEvent(SecuritySettingsEvent.RevokeAllOtherSessions) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RevLensTheme.colors.surface,
                                contentColor = RevLensTheme.colors.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RevLensTheme.colors.error.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Text("Sign Out of All Other Devices", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (state.showUpgradePrompt) {
        UpgradePromptSheet(
            featureName = "Cloud Security & Multi-Device Sessions",
            description = "Create a free RevLens account to enable cloud passwords, 2FA authenticator apps, and session management.",
            onSeePlans = {
                viewModel.onEvent(SecuritySettingsEvent.DismissUpgradePrompt)
                onNavigateToSignUp()
            },
            onDismiss = { viewModel.onEvent(SecuritySettingsEvent.DismissUpgradePrompt) }
        )
    }
}
