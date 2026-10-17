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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.model.LockTimeout
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.SecondaryButton
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
            SecuritySection(title = "App Lock (Device Protection)") {
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
                                text = if (state.isBiometricSupported) "Require fingerprint or biometric scan to open RevLens" else "Biometrics not enrolled on device",
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
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RevLensTheme.colors.brandPrimary,
                            uncheckedThumbColor = RevLensTheme.colors.textSecondary,
                            uncheckedTrackColor = RevLensTheme.colors.surfaceElevated
                        )
                    )
                }

                if (state.isBiometricEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Timeout Selector
                    var timeoutMenuExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .clickable { timeoutMenuExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                text = "Lock when inactive for",
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                        Text(
                            text = state.timeout.displayName,
                            style = RevLensTypography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )

                        DropdownMenu(
                            expanded = timeoutMenuExpanded,
                            onDismissRequest = { timeoutMenuExpanded = false }
                        ) {
                            LockTimeout.entries.forEach { timeout ->
                                DropdownMenuItem(
                                    text = { Text(timeout.displayName) },
                                    onClick = {
                                        viewModel.onEvent(SecuritySettingsEvent.ChangeTimeout(timeout))
                                        timeoutMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lock Now Button
                    OutlinedButton(
                        onClick = { viewModel.onEvent(SecuritySettingsEvent.LockNow) },
                        colors = ButtonDefaults.outlinedButtonColors(
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

            // SECTION 2: CONNECTED GOOGLE ACCOUNT
            SecuritySection(title = "Connected Account & Authentication") {
                if (state.isGoogleConnected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                style = RevLensTypography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Google Account",
                                    style = RevLensTypography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.success,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = state.googleAccountEmail ?: "Connected",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Text(
                                text = state.googleLinkedDate,
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.brandPrimary
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.08f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Unlinked Guest Session",
                                style = RevLensTypography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                        Text(
                            text = "Connect a Google Account to enable multi-device sync, cloud backups, and cross-platform session management.",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                        PrimaryButton(
                            text = "Connect with Google",
                            onClick = onNavigateToSignUp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // SECTION 3: ACTIVE DEVICE SESSIONS
            SecuritySection(title = "Active Device Sessions") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.sessions.forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RevLensTheme.colors.surfaceElevated)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (session.isCurrent) Icons.Filled.Smartphone else Icons.Filled.Devices,
                                    contentDescription = null,
                                    tint = if (session.isCurrent) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = session.deviceName,
                                            style = RevLensTypography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RevLensTheme.colors.textPrimary
                                        )
                                        if (session.isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "This Device",
                                                    style = RevLensTypography.labelSmall,
                                                    color = RevLensTheme.colors.brandPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${session.location} · ${session.lastActive}",
                                        style = RevLensTypography.labelSmall,
                                        color = RevLensTheme.colors.textSecondary
                                    )
                                }
                            }

                            if (!session.isCurrent) {
                                Text(
                                    text = "Revoke",
                                    style = RevLensTypography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.error,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.onEvent(SecuritySettingsEvent.RevokeSession(session.id)) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (!state.isGuest && state.sessions.size > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        SecondaryButton(
                            text = "Sign Out Other Sessions",
                            onClick = { viewModel.onEvent(SecuritySettingsEvent.RevokeAllOtherSessions) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (state.showUpgradePrompt) {
        UpgradePromptSheet(
            featureName = "Multi-Device Sessions & Cloud Security",
            description = "Sign in with Google to manage multi-device sessions and cloud security policies.",
            onSeePlans = {
                viewModel.onEvent(SecuritySettingsEvent.DismissUpgradePrompt)
                onNavigateToSignUp()
            },
            onDismiss = { viewModel.onEvent(SecuritySettingsEvent.DismissUpgradePrompt) }
        )
    }
}

@Composable
private fun SecuritySection(
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
