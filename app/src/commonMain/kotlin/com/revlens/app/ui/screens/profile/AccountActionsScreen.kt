package com.revlens.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.Role
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.feedback.TypedConfirmDialog
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActionsScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToCreateWorkspace: () -> Unit = {}
) {
    val activeWorkspace by viewModel.activeWorkspace.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLeaveBusinessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RevLensTopAppBar(
                title = "More",
                onBackClick = onNavigateBack
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
            SectionCard(title = "Session") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Sign out of your RevLens account on this device.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    ActionCardButton(
                        text = "Log Out",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = { viewModel.logout { onLogout() } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (userRole != Role.Owner && activeWorkspace != null) {
                SectionCard(title = "Workspace Membership") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Leave ${activeWorkspace?.name ?: "this workspace"} and return to workspace setup to create your own business or join another team.",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                        ActionCardButton(
                            text = "Leave Business",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            onClick = { showLeaveBusinessDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SectionCard(title = "Account Management") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Permanently delete your account and remove your personal profile from RevLens.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                    ActionCardButton(
                        text = "Delete Account",
                        icon = Icons.Filled.DeleteForever,
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showLeaveBusinessDialog) {
        TypedConfirmDialog(
            title = "Leave ${activeWorkspace?.name ?: "Business"}",
            body = "Type LEAVE to confirm leaving this workspace. You will immediately return to workspace onboarding.",
            requiredWord = "LEAVE",
            confirmText = "Leave Business",
            onConfirm = {
                showLeaveBusinessDialog = false
                viewModel.leaveBusiness(
                    onSuccess = { onNavigateToCreateWorkspace() },
                    onError = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            },
            onDismiss = { showLeaveBusinessDialog = false },
            isDestructive = true
        )
    }

    if (showDeleteAccountDialog) {
        TypedConfirmDialog(
            title = "Delete Account",
            body = "This action is permanent and cannot be undone. Type DELETE to permanently remove your account.",
            requiredWord = "DELETE",
            confirmText = "Delete Account",
            onConfirm = {
                showDeleteAccountDialog = false
                viewModel.deleteAccount {
                    onLogout()
                }
            },
            onDismiss = { showDeleteAccountDialog = false },
            isDestructive = true
        )
    }
}
