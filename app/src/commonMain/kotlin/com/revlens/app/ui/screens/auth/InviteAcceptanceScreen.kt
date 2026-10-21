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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InviteAcceptanceEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.text.asString())
                    }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                val invite = state.invites.firstOrNull()
                if (invite != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Minimal Icon badge
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MailOutline,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Title & Subtitle (Clean & Minimal)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Join ${invite.workspaceName}",
                                style = RevLensTypography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Invited by ${invite.invitedBy} as",
                                    style = RevLensTypography.bodyMedium,
                                    color = RevLensTheme.colors.textSecondary
                                )
                                RoleChip(role = invite.role)
                            }
                            Text(
                                text = "Collaborate with your team on revenue forecasting, SaaS metrics, and financial models.",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Standard PrimaryButton & Decline Link (Direct layout, no card)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PrimaryButton(
                                text = "Accept Invitation",
                                onClick = { viewModel.onEvent(InviteAcceptanceEvent.AcceptInvite(invite.inviteId)) },
                                isLoading = state.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )

                            TextLinkButton(
                                text = "Decline & create my own business",
                                onClick = { viewModel.onEvent(InviteAcceptanceEvent.DeclineInvite(invite.inviteId)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
