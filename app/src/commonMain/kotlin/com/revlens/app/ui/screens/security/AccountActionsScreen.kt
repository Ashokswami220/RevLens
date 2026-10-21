package com.revlens.app.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ConfirmDialog
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.components.feedback.TypedConfirmDialog
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActionsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToCreateWorkspace: () -> Unit = onLogout
) {
    val session by viewModel.session.collectAsState()
    val isGuest = session !is SessionState.Authenticated
    val currentUser = (session as? SessionState.Authenticated)?.user
    val activeWorkspace by viewModel.activeWorkspace.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Account Actions",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // USER ACCOUNT OVERVIEW CARD
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
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGuest) RevLensTheme.colors.surfaceElevated
                            else RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser != null && currentUser.name.isNotBlank()) {
                        Text(
                            text = currentUser.name.take(1)
                                .uppercase(),
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (isGuest) Icons.Default.Security else Icons.Default.Person,
                            contentDescription = null,
                            tint = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isGuest) "Guest Session" else (currentUser?.name
                                ?: "User Profile"),
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        if (!isGuest) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = RevLensTheme.colors.success,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isGuest) "Temporary local explorer mode" else (currentUser?.email
                            ?: ""),
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            // SECTION 1: ACCOUNT SESSION & LOGOUT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RevLensTheme.colors.surface)
                    .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Account Session",
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isGuest)
                        "You are currently using RevLens in guest mode. Clearing your session will reset local data on this device."
                    else
                        "Signed in as ${currentUser?.email ?: "your account"}. Signing out will safely end your session on this device while preserving all synced workspaces in the cloud.",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                SecondaryButton(
                    text = if (isGuest) "Clear Guest Session" else "Sign Out from RevLens",
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SECTION: LEAVE BUSINESS (Non-owner roles only)
            if (!isGuest && currentUserRole != Role.Owner && activeWorkspace != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(RevLensTheme.colors.surface)
                        .border(
                            1.dp, RevLensTheme.colors.error.copy(alpha = 0.35f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = RevLensTheme.colors.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Leave Business",
                            style = RevLensTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You are currently joined to ${activeWorkspace?.name} with ${currentUserRole.name} access. Leaving this business will remove your membership and free your account so you can start your own business or accept a new workspace invitation.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DestructiveButton(
                        text = "Leave ${activeWorkspace?.name}",
                        onClick = { showLeaveDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // SECTION 2: DANGER ZONE & DELETE ACCOUNT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RevLensTheme.colors.surface)
                    .border(
                        1.dp, RevLensTheme.colors.error.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = RevLensTheme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Danger Zone",
                        style = RevLensTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Permanently delete your RevLens account. All active workspace associations, revenue models, scenario simulations, and personal modeling data will be permanently wiped.",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                DestructiveButton(
                    text = "Delete RevLens Account",
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Leave Business Typed Confirmation Dialog
    if (showLeaveDialog && activeWorkspace != null) {
        TypedConfirmDialog(
            title = "Leave ${activeWorkspace?.name}?",
            body = "Are you sure you want to leave this business? You will lose access to its metrics and can start your own business or accept another invitation.",
            requiredWord = "LEAVE",
            confirmText = "Leave Business",
            onConfirm = {
                showLeaveDialog = false
                viewModel.leaveBusiness(
                    onSuccess = { onNavigateToCreateWorkspace() },
                    onError = { }
                )
            },
            onDismiss = { showLeaveDialog = false },
            isDestructive = true
        )
    }

    // Log Out Dialog
    if (showLogoutDialog) {
        ConfirmDialog(
            title = if (isGuest) "Clear Local Session?" else "Log Out?",
            body = if (isGuest) "Your local session will be reset. Unsaved scenarios may be lost."
            else "Are you sure you want to log out? Synced workspaces will remain safely stored on the cloud.",
            confirmText = if (isGuest) "Clear & Reset" else "Log Out",
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                onLogout()
            },
            onDismiss = { showLogoutDialog = false },
            isDestructive = true
        )
    }

    // Delete Account Typed Confirmation Dialog
    if (showDeleteDialog) {
        TypedConfirmDialog(
            title = "Delete RevLens Account?",
            body = "This action is permanent and cannot be undone. All your saved models, workspaces, and preferences will be permanently wiped.",
            requiredWord = "DELETE",
            confirmText = "Delete Permanently",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount()
                onLogout()
            },
            onDismiss = { showDeleteDialog = false },
            isDestructive = true
        )
    }
}
