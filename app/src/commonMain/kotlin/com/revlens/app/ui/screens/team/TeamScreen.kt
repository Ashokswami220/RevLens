package com.revlens.app.ui.screens.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.TextLinkButton
import com.revlens.app.ui.components.display.MemberRow
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.components.display.UsageMeter
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TeamEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                TeamEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Team & Permissions",
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HERO SEAT USAGE CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RevLensTheme.colors.surface)
                    .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Team Capacity",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Collaborate on baseline models and investor reports",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    val canInvite = state.members.size < state.totalSeats
                    Button(
                        onClick = { viewModel.onEvent(TeamEvent.OpenInviteSheet) },
                        enabled = canInvite,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RevLensTheme.colors.brandPrimary,
                            contentColor = RevLensTheme.colors.background,
                            disabledContainerColor = RevLensTheme.colors.surfaceElevated,
                            disabledContentColor = RevLensTheme.colors.textTertiary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Invite",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Invite",
                                style = RevLensTypography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                UsageMeter(
                    label = "Seats Allocated",
                    used = state.members.size,
                    limit = state.totalSeats,
                    unitLabel = "seats"
                )
            }

            // TABS (Members, Invites, Permissions)
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = RevLensTheme.colors.surface,
                contentColor = RevLensTheme.colors.brandPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[state.activeTab.ordinal]
                        ),
                        color = RevLensTheme.colors.brandPrimary
                    )
                }
            ) {
                TeamTab.entries.forEach { tab ->
                    val tabTitle = when (tab) {
                        TeamTab.Members -> "Members (${state.members.size})"
                        TeamTab.Invites -> "Invites (${state.invites.size})"
                        TeamTab.Permissions -> "Permissions"
                    }
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.onEvent(TeamEvent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = tabTitle,
                                style = RevLensTypography.labelMedium,
                                fontWeight = if (state.activeTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // TAB CONTENT
            when (state.activeTab) {
                TeamTab.Members -> {
                    if (state.members.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RevLensTheme.colors.surface)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No team members found",
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.members.forEach { member ->
                                MemberRow(
                                    member = member,
                                    canManage = state.userRole == Role.Owner || state.userRole == Role.Admin,
                                    onRoleChange = { newRole ->
                                        viewModel.onEvent(
                                            TeamEvent.ChangeMemberRole(member.id, newRole)
                                        )
                                    },
                                    onRemove = {
                                        viewModel.onEvent(TeamEvent.PromptRemoveMember(member))
                                    }
                                )
                            }
                        }
                    }
                }

                TeamTab.Invites -> {
                    if (state.invites.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RevLensTheme.colors.surface)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No pending invitations",
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.invites.forEach { invite ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RevLensTheme.colors.surfaceElevated)
                                        .border(
                                            1.dp, RevLensTheme.colors.borderDefault,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(RevLensTheme.colors.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Mail,
                                                contentDescription = null,
                                                tint = RevLensTheme.colors.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = invite.email,
                                                    style = RevLensTypography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = RevLensTheme.colors.textPrimary
                                                )
                                                RoleChip(role = invite.role)
                                            }
                                            Text(
                                                text = "${invite.sentAt} · by ${invite.invitedBy}",
                                                style = RevLensTypography.labelSmall,
                                                color = RevLensTheme.colors.textSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextLinkButton(
                                            text = "Resend",
                                            onClick = {
                                                viewModel.onEvent(
                                                    TeamEvent.ResendInvite(invite.id)
                                                )
                                            }
                                        )
                                        IconButton(
                                            onClick = {
                                                viewModel.onEvent(
                                                    TeamEvent.RevokeInvite(invite.id)
                                                )
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Revoke",
                                                tint = RevLensTheme.colors.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TeamTab.Permissions -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(RevLensTheme.colors.surface)
                            .border(
                                1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Role Capabilities Matrix",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )

                        RolePermissions.matrix.forEach { perm ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RevLensTheme.colors.surfaceElevated)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = perm.name,
                                    style = RevLensTypography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = perm.description,
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RolePermissionBadge("Owner", perm.owner)
                                    RolePermissionBadge("Admin", perm.admin)
                                    RolePermissionBadge("Editor", perm.editor)
                                    RolePermissionBadge("Viewer", perm.viewer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // INVITE MEMBER BOTTOM SHEET
    if (state.isInviteSheetVisible) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(TeamEvent.DismissInviteSheet) },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Invite Team Member",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )

                RevLensTextField(
                    value = state.inviteEmail,
                    onValueChange = { viewModel.onEvent(TeamEvent.InviteEmailChanged(it)) },
                    label = "Work Email Address",
                    placeholder = "colleague@company.com"
                )

                Text(
                    text = "Assign Role",
                    style = RevLensTypography.labelMedium,
                    color = RevLensTheme.colors.textSecondary
                )

                listOf(Role.Admin, Role.Editor, Role.Viewer).forEach { role ->
                    val isSelected = state.inviteRole == role
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) RevLensTheme.colors.brandPrimary.copy(
                                    alpha = 0.12f
                                ) else RevLensTheme.colors.surfaceElevated
                            )
                            .clickable { viewModel.onEvent(TeamEvent.InviteRoleChanged(role)) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = role.name,
                                style = RevLensTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary
                            )
                            Text(
                                text = when (role) {
                                    Role.Admin -> "Can invite members, manage integrations, and edit models"
                                    Role.Editor -> "Can create and run simulation scenarios and models"
                                    Role.Viewer -> "Read-only access to dashboards and export reports"
                                    else -> ""
                                },
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error ?: "",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.error
                    )
                }

                PrimaryButton(
                    text = if (state.isInviting) "Sending Invite..." else "Send Invitation",
                    onClick = { viewModel.onEvent(TeamEvent.SubmitInvite) },
                    enabled = !state.isInviting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // REMOVE MEMBER CONFIRMATION DIALOG
    state.memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TeamEvent.DismissRemoveMember) },
            title = {
                Text(
                    text = "Remove ${member.name}?",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "This member will lose access to this workspace and all private scenarios immediately.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            },
            confirmButton = {
                DestructiveButton(
                    text = "Remove Member",
                    onClick = { viewModel.onEvent(TeamEvent.ConfirmRemoveMember) }
                )
            },
            dismissButton = {
                TextLinkButton(
                    text = "Cancel",
                    onClick = { viewModel.onEvent(TeamEvent.DismissRemoveMember) }
                )
            },
            containerColor = RevLensTheme.colors.surface
        )
    }
}

@Composable
private fun RolePermissionBadge(roleName: String, hasPermission: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (hasPermission) RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)
                else RevLensTheme.colors.surface
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$roleName: ${if (hasPermission) "✓" else "✕"}",
            style = RevLensTypography.labelSmall,
            color = if (hasPermission) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textDisabled,
            fontWeight = if (hasPermission) FontWeight.Bold else FontWeight.Normal
        )
    }
}
