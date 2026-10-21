package com.revlens.app.ui.screens.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.policy.RolePermissions
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.ui.components.ChoiceChip
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.TextLinkButton
import com.revlens.app.ui.components.display.RoleChip
import com.revlens.app.ui.components.feedback.TypedConfirmDialog
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceManagementScreen(
    onBackClick: () -> Unit,
    onNavigateToBaseline: () -> Unit,
    onNavigateToCreateWorkspace: () -> Unit,
    viewModel: WorkspaceManagementViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val teamRepository: TeamRepository = koinInject()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WorkspaceManagementEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString())
                }

                WorkspaceManagementEffect.NavigateToBaseline -> onNavigateToBaseline()
                WorkspaceManagementEffect.NavigateToCreateWorkspace -> onNavigateToCreateWorkspace()
            }
        }
    }

    val workspace = state.activeWorkspace
    val wsId = workspace?.id ?: "ws_default"
    val members by remember(wsId) { teamRepository.getMembers(wsId) }.collectAsState(
        initial = emptyList()
    )
    var showTransferOwnershipDialog by remember { mutableStateOf(false) }

    val canManageSettings =
        state.currentUserRole == Role.Owner || state.currentUserRole == Role.Admin
    val canEditBaseline = RolePermissions.canEditBaselinesAndScenarios(state.currentUserRole)
    val canTransferOwnership = RolePermissions.canTransferOwnership(state.currentUserRole)
    val canDeleteWorkspace = RolePermissions.canDeleteWorkspace(state.currentUserRole)

    Scaffold(
        topBar = {
            RevLensTopAppBar(
                title = "Workspace Management",
                onBackClick = onBackClick
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
            // Single Active Workspace Header (No multi-workspace switcher)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RevLensTheme.colors.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        val initials = workspace?.name?.take(2)
                            ?.uppercase() ?: "WS"
                        Text(
                            text = initials,
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.background
                        )
                    }

                    Column {
                        Text(
                            text = workspace?.name ?: "Workspace",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Single Active Workspace · ${if (workspace?.businessModel == BusinessModel.B2cSaas) "B2C SaaS" else "B2B SaaS"}",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }
            }

            // General Section
            SectionCard(title = "General Settings") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    WorkspaceSettingRow(
                        title = "Workspace Name",
                        value = workspace?.name ?: "",
                        onClick = if (canManageSettings) {
                            { viewModel.onEvent(WorkspaceManagementEvent.ShowEditNameDialog) }
                        } else null
                    )

                    Column {
                        Text(
                            text = "Business Model",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChoiceChip(
                                text = "B2B SaaS",
                                selected = workspace?.businessModel == BusinessModel.B2bSaas,
                                onClick = {
                                    if (canManageSettings && workspace?.businessModel != BusinessModel.B2bSaas) {
                                        viewModel.onEvent(
                                            WorkspaceManagementEvent.UpdateBusinessModel(
                                                BusinessModel.B2bSaas
                                            )
                                        )
                                    }
                                }
                            )
                            ChoiceChip(
                                text = "B2C SaaS",
                                selected = workspace?.businessModel == BusinessModel.B2cSaas,
                                onClick = {
                                    if (canManageSettings && workspace?.businessModel != BusinessModel.B2cSaas) {
                                        viewModel.onEvent(
                                            WorkspaceManagementEvent.UpdateBusinessModel(
                                                BusinessModel.B2cSaas
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Data Section
            SectionCard(title = "Data Management") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    WorkspaceSettingRow(
                        title = "Business Baseline",
                        value = if (canEditBaseline) "Edit plans & baseline parameters" else "Read-only access (Viewer)",
                        onClick = if (canEditBaseline) onNavigateToBaseline else null
                    )
                }
            }

            // Ownership & Team Roles
            SectionCard(title = "Ownership & Access") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your Role",
                                style = RevLensTypography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            val roleSubtitle = when (state.currentUserRole) {
                                Role.Viewer -> "Read-only access to metrics and reports"
                                Role.Editor -> "Can modify scenarios and business baselines"
                                Role.Admin -> "Administrative and team management access"
                                Role.Owner -> "Full administrative, ownership, and billing permissions"
                            }
                            Text(
                                text = roleSubtitle,
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                        RoleChip(role = state.currentUserRole)
                    }

                    if (canTransferOwnership) {
                        WorkspaceSettingRow(
                            title = "Transfer Workspace Ownership",
                            value = "Pass Owner role to another team member",
                            valueColor = RevLensTheme.colors.textSecondary,
                            onClick = { showTransferOwnershipDialog = true }
                        )
                    }
                }
            }

            // Danger Zone (Owner: Delete Workspace | Non-Owner: Leave Workspace)
            if (canDeleteWorkspace) {
                SectionCard(title = "Danger Zone") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        WorkspaceSettingRow(
                            title = "Delete Workspace",
                            value = "Permanently delete this workspace and return to onboarding",
                            valueColor = RevLensTheme.colors.error,
                            onClick = {
                                viewModel.onEvent(WorkspaceManagementEvent.ShowDeleteDialog)
                            }
                        )
                    }
                }
            } else {
                SectionCard(title = "Leave Business") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        WorkspaceSettingRow(
                            title = "Leave Workspace",
                            value = "Relinquish your ${state.currentUserRole.name} access and return to workspace onboarding",
                            valueColor = RevLensTheme.colors.error,
                            onClick = {
                                viewModel.onEvent(WorkspaceManagementEvent.ShowLeaveDialog)
                            }
                        )
                    }
                }
            }
        }
    }

    // Transfer Ownership Dialog (Owner Only)
    if (showTransferOwnershipDialog) {
        val eligibleMembers = members.filter { it.role != Role.Owner }
        AlertDialog(
            onDismissRequest = { showTransferOwnershipDialog = false },
            title = {
                Text(
                    text = "Transfer Workspace Ownership",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (eligibleMembers.isEmpty()) {
                        Text(
                            text = "Invite at least one team member in Team & Permissions before transferring ownership.",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                    } else {
                        Text(
                            text = "Select a team member to become the new Workspace Owner. Your role will be updated to Admin.",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                        eligibleMembers.forEach { candidate ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RevLensTheme.colors.surfaceElevated)
                                    .clickable {
                                        showTransferOwnershipDialog = false
                                        scope.launch {
                                            teamRepository.transferOwnership(wsId, candidate.id)
                                                .onSuccess {
                                                    snackbarHostState.showSnackbar(
                                                        "Transferred ownership to ${candidate.name}"
                                                    )
                                                }
                                                .onFailure { err ->
                                                    snackbarHostState.showSnackbar(
                                                        err.message ?: "Transfer failed"
                                                    )
                                                }
                                        }
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = candidate.name,
                                        style = RevLensTypography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RevLensTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = candidate.email,
                                        style = RevLensTypography.bodySmall,
                                        color = RevLensTheme.colors.textSecondary
                                    )
                                }
                                RoleChip(role = candidate.role)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextLinkButton(
                    text = "Close",
                    onClick = { showTransferOwnershipDialog = false }
                )
            },
            containerColor = RevLensTheme.colors.surface
        )
    }

    // Edit Name Dialog
    if (state.isEditNameDialogVisible) {
        var newName by rememberSaveable { mutableStateOf(workspace?.name ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(WorkspaceManagementEvent.HideEditNameDialog) },
            title = {
                Text(
                    "Rename Workspace",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    RevLensTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = "Workspace Name",
                        placeholder = "e.g. Acme Corp"
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Save",
                    onClick = { viewModel.onEvent(WorkspaceManagementEvent.UpdateName(newName)) },
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                TextLinkButton(
                    text = "Cancel",
                    onClick = { viewModel.onEvent(WorkspaceManagementEvent.HideEditNameDialog) }
                )
            },
            containerColor = RevLensTheme.colors.surface
        )
    }

    // Delete Workspace Confirmation Dialog
    if (state.isDeleteDialogVisible && workspace != null) {
        TypedConfirmDialog(
            title = "Delete ${workspace.name}",
            body = "This will permanently remove this workspace and its associated revenue models.",
            requiredWord = workspace.name,
            confirmText = "Delete",
            onConfirm = { viewModel.onEvent(WorkspaceManagementEvent.ConfirmDeleteWorkspace) },
            onDismiss = { viewModel.onEvent(WorkspaceManagementEvent.HideDeleteDialog) },
            isDestructive = true
        )
    }

    // Leave Workspace Confirmation Dialog
    if (state.isLeaveDialogVisible && workspace != null) {
        TypedConfirmDialog(
            title = "Leave ${workspace.name}",
            body = "Are you sure you want to leave this business? You will lose access to its metrics and can create your own business or join another team.",
            requiredWord = "LEAVE",
            confirmText = "Leave Business",
            onConfirm = { viewModel.onEvent(WorkspaceManagementEvent.ConfirmLeaveWorkspace) },
            onDismiss = { viewModel.onEvent(WorkspaceManagementEvent.HideLeaveDialog) },
            isDestructive = true
        )
    }
}

@Composable
private fun WorkspaceSettingRow(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
    valueColor: Color = RevLensTheme.colors.textSecondary,
    badge: @Composable (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = RevLensTypography.bodyLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                if (badge != null) {
                    badge()
                }
            }
            Text(
                text = value,
                style = RevLensTypography.bodySmall,
                color = valueColor
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = RevLensTheme.colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
