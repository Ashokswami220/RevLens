package com.revlens.app.ui.screens.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
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
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.SegmentedTabs
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateWorkspaceScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit = onNavigateToOnboarding,
    onBackClick: (() -> Unit)? = null,
    viewModel: CreateWorkspaceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CreateWorkspaceEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                CreateWorkspaceEffect.NavigateToDashboard -> onNavigateToDashboard()
                CreateWorkspaceEffect.NavigateBack -> onBackClick?.invoke()
                is CreateWorkspaceEffect.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.text.asString())
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (onBackClick != null) {
                RevLensTopAppBar(
                    title = "Workspace Setup",
                    onBackClick = onBackClick
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Set Up Your Workspace",
                    style = RevLensTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = "You can have one active workspace. Create your own business workspace or join an existing team with an invite code from the owner.",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            }

            SegmentedTabs(
                options = listOf("Create My Business", "Join Team via Code"),
                selectedIndex = if (state.mode == WorkspaceSetupMode.CREATE_BUSINESS) 0 else 1,
                onOptionSelected = { idx ->
                    val targetMode = if (idx == 0) {
                        WorkspaceSetupMode.CREATE_BUSINESS
                    } else {
                        WorkspaceSetupMode.JOIN_TEAM
                    }
                    viewModel.onEvent(CreateWorkspaceEvent.ModeChanged(targetMode))
                }
            )

            if (state.mode == WorkspaceSetupMode.CREATE_BUSINESS) {
                SectionCard(title = "Business Details") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        RevLensTextField(
                            value = state.name,
                            onValueChange = { viewModel.onEvent(CreateWorkspaceEvent.NameChanged(it)) },
                            label = "Business / Workspace Name",
                            placeholder = "e.g. Acme SaaS Inc.",
                            isError = state.nameError != null,
                            errorMessage = state.nameError?.asString()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Business Model",
                                style = RevLensTypography.labelMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    BusinessModel.B2bSaas to "B2B SaaS",
                                    BusinessModel.B2cSaas to "B2C SaaS",
                                    BusinessModel.Other to "Other"
                                ).forEach { (model, label) ->
                                    val selected = state.businessModel == model
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (selected) RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)
                                                else RevLensTheme.colors.surfaceElevated
                                            )
                                            .border(
                                                1.dp,
                                                if (selected) RevLensTheme.colors.brandPrimary
                                                else RevLensTheme.colors.borderDefault,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                viewModel.onEvent(CreateWorkspaceEvent.BusinessModelChanged(model))
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = RevLensTypography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) RevLensTheme.colors.brandPrimary
                                            else RevLensTheme.colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PrimaryButton(
                    text = "Continue to Baseline Setup",
                    onClick = { viewModel.onEvent(CreateWorkspaceEvent.Submit) },
                    enabled = state.canSubmit,
                    isLoading = state.isLoading
                )
            } else {
                SectionCard(title = "Join Existing Team or Business") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GroupAdd,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.brandPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enter Team Join Code",
                                    style = RevLensTypography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Didn't receive an invite or declined it? Enter the Team Join Code from your workspace owner (found in Settings → Team & Permissions).",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }

                        RevLensTextField(
                            value = state.joinCode,
                            onValueChange = { viewModel.onEvent(CreateWorkspaceEvent.JoinCodeChanged(it)) },
                            label = "Team Join Code / Invite Code",
                            placeholder = "e.g. RL-8F3A2B",
                            isError = state.joinCodeError != null,
                            errorMessage = state.joinCodeError?.asString()
                        )
                    }
                }

                PrimaryButton(
                    text = "Join Team Workspace",
                    onClick = { viewModel.onEvent(CreateWorkspaceEvent.SubmitJoinCode) },
                    enabled = state.canJoinByCode,
                    isLoading = state.isLoading
                )
            }
        }
    }
}
