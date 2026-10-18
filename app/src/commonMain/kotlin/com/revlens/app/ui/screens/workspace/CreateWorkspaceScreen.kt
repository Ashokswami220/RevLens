package com.revlens.app.ui.screens.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.BusinessModel
import com.revlens.app.ui.components.ChoiceChip
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkspaceScreen(
    onNavigateToOnboarding: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    viewModel: CreateWorkspaceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CreateWorkspaceEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                CreateWorkspaceEffect.NavigateBack -> onBackClick?.invoke()
                is CreateWorkspaceEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    effect.text.asString()
                )
            }
        }
    }

    val currencies = listOf("USD", "EUR", "GBP", "INR", "CAD", "AUD")

    Scaffold(
        topBar = {
            RevLensTopAppBar(
                title = "Create Workspace",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RevLensTheme.colors.surface)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                PrimaryButton(
                    text = "Continue to Business Setup",
                    onClick = { viewModel.onEvent(CreateWorkspaceEvent.Submit) },
                    enabled = state.canSubmit,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header icon + title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Business,
                        contentDescription = null,
                        tint = RevLensTheme.colors.brandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Name your workspace",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Text(
                        text = "Every revenue model belongs to a workspace.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            // Workspace Name Input
            SectionCard(title = "Workspace Name") {
                RevLensTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(CreateWorkspaceEvent.NameChanged(it)) },
                    label = "Company or Product Name *",
                    placeholder = "e.g. Acme Corp or RevLens",
                    isError = state.nameError != null,
                    errorMessage = state.nameError?.asString()
                )
            }

            // Business Model Selection
            SectionCard(title = "Business Model") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select your primary revenue structure to tune SaaS benchmarks.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChoiceChip(
                            text = "B2B SaaS",
                            selected = state.businessModel == BusinessModel.B2bSaas,
                            onClick = {
                                viewModel.onEvent(
                                    CreateWorkspaceEvent.BusinessModelChanged(BusinessModel.B2bSaas)
                                )
                            }
                        )
                        ChoiceChip(
                            text = "B2C SaaS",
                            selected = state.businessModel == BusinessModel.B2cSaas,
                            onClick = {
                                viewModel.onEvent(
                                    CreateWorkspaceEvent.BusinessModelChanged(BusinessModel.B2cSaas)
                                )
                            }
                        )
                        ChoiceChip(
                            text = "Other",
                            selected = state.businessModel == BusinessModel.Other,
                            onClick = {
                                viewModel.onEvent(
                                    CreateWorkspaceEvent.BusinessModelChanged(BusinessModel.Other)
                                )
                            }
                        )
                    }
                }
            }

            // Currency Selection
            SectionCard(title = "Default Currency") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "All pricing plans, CAC, and MRR metrics will be formatted in this currency.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currencies.take(3)
                            .forEach { curr ->
                                ChoiceChip(
                                    text = curr,
                                    selected = state.currency == curr,
                                    onClick = {
                                        viewModel.onEvent(
                                            CreateWorkspaceEvent.CurrencyChanged(curr)
                                        )
                                    }
                                )
                            }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currencies.drop(3)
                            .forEach { curr ->
                                ChoiceChip(
                                    text = curr,
                                    selected = state.currency == curr,
                                    onClick = {
                                        viewModel.onEvent(
                                            CreateWorkspaceEvent.CurrencyChanged(curr)
                                        )
                                    }
                                )
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
