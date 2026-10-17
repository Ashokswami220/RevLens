package com.example.revlens.ui.screens.workspace

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.model.BusinessModel
import com.example.revlens.domain.model.Workspace
import com.example.revlens.ui.components.ChoiceChip
import com.example.revlens.ui.components.DestructiveButton
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTextField
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.components.TextLinkButton
import com.example.revlens.ui.components.feedback.TypedConfirmDialog
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
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
            // Header Row: Avatar + Name + Switch
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
                        val initials = workspace?.name?.take(2)?.uppercase() ?: "WS"
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
                            text = "Currency: ${workspace?.currency ?: "USD"}",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }

                if (state.allWorkspaces.size > 1) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.surfaceElevated)
                            .clickable { viewModel.onEvent(WorkspaceManagementEvent.ShowSwitchSheet) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Switch",
                            tint = RevLensTheme.colors.primaryAction,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Switch",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RevLensTheme.colors.textPrimary
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
                        onClick = { viewModel.onEvent(WorkspaceManagementEvent.ShowEditNameDialog) }
                    )

                    WorkspaceSettingRow(
                        title = "Default Currency",
                        value = workspace?.currency ?: "USD",
                        onClick = { viewModel.onEvent(WorkspaceManagementEvent.ShowCurrencyDialog) }
                    )

                    WorkspaceSettingRow(
                        title = "Time Zone",
                        value = workspace?.timeZoneId ?: "UTC",
                        onClick = { }
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
                                    viewModel.onEvent(
                                        WorkspaceManagementEvent.UpdateBusinessModel(BusinessModel.B2bSaas)
                                    )
                                }
                            )
                            ChoiceChip(
                                text = "B2C SaaS",
                                selected = workspace?.businessModel == BusinessModel.B2cSaas,
                                onClick = {
                                    viewModel.onEvent(
                                        WorkspaceManagementEvent.UpdateBusinessModel(BusinessModel.B2cSaas)
                                    )
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
                        value = "Edit plans & baseline parameters",
                        onClick = onNavigateToBaseline
                    )

                    WorkspaceSettingRow(
                        title = "Reset Local Scenarios",
                        value = "Clear scenarios and simulation drafts",
                        valueColor = RevLensTheme.colors.error,
                        onClick = { viewModel.onEvent(WorkspaceManagementEvent.ShowResetDialog) }
                    )
                }
            }

            // Danger Zone
            if (state.allWorkspaces.size > 1) {
                SectionCard(title = "Danger Zone") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        WorkspaceSettingRow(
                            title = "Delete Workspace",
                            value = "Permanently remove this workspace",
                            valueColor = RevLensTheme.colors.error,
                            onClick = { viewModel.onEvent(WorkspaceManagementEvent.ShowDeleteDialog) }
                        )
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (state.isEditNameDialogVisible) {
        var newName by remember { mutableStateOf(workspace?.name ?: "") }
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
                RevLensTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = "Workspace Name",
                    placeholder = "e.g. Acme Corp"
                )
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

    // Currency Dialog
    if (state.isCurrencyDialogVisible) {
        val currencies = listOf("USD", "EUR", "GBP", "INR", "CAD", "AUD")
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(WorkspaceManagementEvent.HideCurrencyDialog) },
            title = {
                Text(
                    "Select Currency",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Note: Changing currency updates symbol displays only. Existing values will not be converted.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    currencies.forEach { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (workspace?.currency == curr) RevLensTheme.colors.surfaceElevated else RevLensTheme.colors.surface)
                                .clickable {
                                    viewModel.onEvent(WorkspaceManagementEvent.UpdateCurrency(curr))
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(curr, style = RevLensTypography.bodyLarge, color = RevLensTheme.colors.textPrimary)
                            if (workspace?.currency == curr) {
                                Text("Active", style = RevLensTypography.labelSmall, color = RevLensTheme.colors.brandPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextLinkButton(
                    text = "Close",
                    onClick = { viewModel.onEvent(WorkspaceManagementEvent.HideCurrencyDialog) }
                )
            },
            containerColor = RevLensTheme.colors.surface
        )
    }

    // Reset Confirmation Dialog
    if (state.isResetDialogVisible) {
        TypedConfirmDialog(
            title = "Reset Local Data",
            body = "This will clear local simulation scenarios. Baseline settings are preserved.",
            requiredWord = "RESET",
            confirmText = "Reset",
            onConfirm = { viewModel.onEvent(WorkspaceManagementEvent.ConfirmResetData) },
            onDismiss = { viewModel.onEvent(WorkspaceManagementEvent.HideResetDialog) },
            isDestructive = true
        )
    }

    // Delete Workspace Confirmation Dialog
    if (state.isDeleteDialogVisible && workspace != null) {
        TypedConfirmDialog(
            title = "Delete ${workspace.name}",
            body = "This will permanently remove this workspace and its associated data.",
            requiredWord = workspace.name,
            confirmText = "Delete",
            onConfirm = { viewModel.onEvent(WorkspaceManagementEvent.ConfirmDeleteWorkspace) },
            onDismiss = { viewModel.onEvent(WorkspaceManagementEvent.HideDeleteDialog) },
            isDestructive = true
        )
    }

    // Workspace Switcher Bottom Sheet
    if (state.isSwitchSheetVisible) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(WorkspaceManagementEvent.HideSwitchSheet) },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Switch Workspace",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.allWorkspaces.forEach { ws ->
                    val isActive = ws.id == workspace?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) RevLensTheme.colors.surfaceElevated else RevLensTheme.colors.surface)
                            .clickable { viewModel.onEvent(WorkspaceManagementEvent.SwitchWorkspace(ws.id)) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = ws.name,
                                style = RevLensTypography.bodyLarge,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = RevLensTheme.colors.textPrimary
                            )
                            Text(
                                text = ws.currency,
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                        if (isActive) {
                            Text("Current", style = RevLensTypography.labelSmall, color = RevLensTheme.colors.brandPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                PrimaryButton(
                    text = "+ New workspace",
                    onClick = {
                        viewModel.onEvent(WorkspaceManagementEvent.HideSwitchSheet)
                        onNavigateToCreateWorkspace()
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkspaceSettingRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    valueColor: androidx.compose.ui.graphics.Color = RevLensTheme.colors.textSecondary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = RevLensTypography.bodyLarge,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = value,
                style = RevLensTypography.bodySmall,
                color = valueColor
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
