package com.revlens.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InviteAcceptanceScreen(
    viewModel: InviteAcceptanceViewModel = koinViewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToCreateWorkspace: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InviteAcceptanceEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.text.asString())
                    }
                }

                InviteAcceptanceEffect.NavigateToDashboard -> onNavigateToDashboard()
                InviteAcceptanceEffect.NavigateToCreateWorkspace -> onNavigateToCreateWorkspace()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        if (state.isLoading && state.invites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RevLensTheme.colors.brandPrimary)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                val invite = state.invites.firstOrNull()
                if (invite != null) {
                    val roleHighlights = remember(invite.role) {
                        RolePermissions.roleSummary(invite.role)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.14f))
                                .border(
                                    1.dp,
                                    RevLensTheme.colors.brandPrimary.copy(alpha = 0.35f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MailOutline,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TEAM INVITATION",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.brandPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Join ${invite.workspaceName}",
                                style = RevLensTypography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            if (state.userEmail.isNotBlank()) {
                                Text(
                                    text = "Signed in as ${state.userEmail}",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }

                        SectionCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Invited By",
                                            style = RevLensTypography.labelMedium,
                                            color = RevLensTheme.colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = invite.invitedBy,
                                            style = RevLensTypography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RevLensTheme.colors.textPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Assigned Role",
                                            style = RevLensTypography.labelMedium,
                                            color = RevLensTheme.colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        RoleChip(role = invite.role)
                                    }
                                }

                                HorizontalDivider(color = RevLensTheme.colors.divider)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = RevLensTheme.colors.brandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "What you can do as ${invite.role.name}",
                                        style = RevLensTypography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RevLensTheme.colors.textPrimary
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    roleHighlights.forEach { bullet ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = RevLensTheme.colors.brandTertiary,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(16.dp)
                                            )
                                            Text(
                                                text = bullet,
                                                style = RevLensTypography.bodyMedium,
                                                color = RevLensTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PrimaryButton(
                                text = "Accept & Open ${invite.workspaceName}",
                                onClick = {
                                    viewModel.onEvent(
                                        InviteAcceptanceEvent.AcceptInvite(invite.inviteId)
                                    )
                                },
                                isLoading = state.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )

                            SecondaryButton(
                                text = "Decline & Set Up My Own Business",
                                onClick = {
                                    viewModel.onEvent(
                                        InviteAcceptanceEvent.DeclineInvite(invite.inviteId)
                                    )
                                },
                                enabled = !state.isLoading
                            )
                        }
                    }
                }
            }
        }
    }
}
