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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.TextLinkButton
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InviteAcceptanceScreen(
    viewModel: InviteAcceptanceViewModel = koinViewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToCreateWorkspace: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InviteAcceptanceEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.text.asString())
                }
                InviteAcceptanceEffect.NavigateToDashboard -> {
                    onNavigateToDashboard()
                }
                InviteAcceptanceEffect.NavigateToCreateWorkspace -> {
                    onNavigateToCreateWorkspace()
                }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MailOutline,
                            contentDescription = null,
                            tint = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Workspace Invitation",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "You've been invited to join",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Connect with your team to review live SaaS metrics, scenarios, and financial plans.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                // Invitation Cards
                state.invites.forEach { invite ->
                    InviteCard(
                        invite = invite,
                        isLoading = state.isLoading,
                        onAccept = { viewModel.onEvent(InviteAcceptanceEvent.AcceptInvite(invite.inviteId)) },
                        onDecline = { viewModel.onEvent(InviteAcceptanceEvent.DeclineInvite(invite.inviteId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteCard(
    invite: TeamInviteDetail,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(16.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Workspace Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(RevLensTheme.colors.brandPrimary)
            ) {
                val initials = invite.workspaceName.take(2).uppercase().ifBlank { "WS" }
                Text(
                    text = initials,
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.background
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invite.workspaceName,
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = "Invited by ${invite.invitedBy} • ${invite.sentAt}",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }

            RoleChip(role = invite.role)
        }

        // Role explanation box
        val roleDescription = when (invite.role) {
            Role.Viewer -> "View-only access: You will have access to all real-time dashboards, charts, scenario simulations, and metrics without modifying financial baseline parameters."
            Role.Editor -> "Editor access: You can create new scenarios, model pricing variations, and forecast growth alongside the team."
            Role.Admin, Role.Owner -> "Administrative access: You can manage baseline metrics, invite team members, and configure workspace settings."
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RevLensTheme.colors.surfaceElevated)
                .padding(14.dp)
        ) {
            Text(
                text = roleDescription,
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )
        }

        // Single-tenancy notice
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.08f))
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = RevLensTheme.colors.brandPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "A RevLens account belongs to one business at a time. Accepting connects you to ${invite.workspaceName} and skips company setup.",
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryButton(
                text = if (isLoading) "Joining..." else "Accept & Go to Dashboard",
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            )

            TextLinkButton(
                text = "Decline Invitation (Create My Own Business)",
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
