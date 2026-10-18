package com.revlens.app.ui.screens.integrations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.ConnectionStatus
import com.revlens.app.domain.model.IntegrationCategory
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.display.IntegrationCard
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsScreen(
    viewModel: IntegrationsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is IntegrationsEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                IntegrationsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Integrations",
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Banner
            val connectedCount = state.connections.count { it.status == ConnectionStatus.CONNECTED }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RevLensTheme.colors.surfaceElevated)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Connected Sources",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Sync subscriptions, invoices, and alerts automatically",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$connectedCount of ${state.connections.size} Active",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }
                }
            }

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RevLensTheme.colors.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val categories = listOf(null) + IntegrationCategory.entries
                categories.forEach { cat ->
                    val isSelected = state.selectedCategory == cat
                    val label = cat?.label ?: "All"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) RevLensTheme.colors.primaryAction else Color.Transparent)
                            .clickable { viewModel.onEvent(IntegrationsEvent.SelectCategory(cat)) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = RevLensTypography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) RevLensTheme.colors.onPrimaryAction else RevLensTheme.colors.textSecondary
                        )
                    }
                }
            }

            // Cards List
            val filteredList = if (state.selectedCategory == null) {
                state.connections
            } else {
                state.connections.filter { it.provider.category == state.selectedCategory }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                filteredList.forEach { connection ->
                    IntegrationCard(
                        connection = connection,
                        isSyncing = state.syncingProviders.contains(connection.provider),
                        onConnect = { viewModel.onEvent(IntegrationsEvent.ConnectIntegration(connection.provider)) },
                        onDisconnect = { viewModel.onEvent(IntegrationsEvent.PromptDisconnectIntegration(connection.provider)) },
                        onSync = { viewModel.onEvent(IntegrationsEvent.SyncIntegration(connection.provider)) }
                    )
                }
            }
        }
    }

    // Disconnect Confirm Dialog
    if (state.disconnectingProvider != null) {
        val provider = state.disconnectingProvider!!
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(IntegrationsEvent.DismissDisconnectIntegration) },
            title = {
                Text(
                    text = "Disconnect ${provider.displayName}?",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to disconnect ${provider.displayName}? Automatic sync for recurring revenue and event notifications will stop immediately.",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            },
            confirmButton = {
                DestructiveButton(
                    text = "Disconnect",
                    onClick = { viewModel.onEvent(IntegrationsEvent.ConfirmDisconnectIntegration) }
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(IntegrationsEvent.DismissDisconnectIntegration) }) {
                    Text(
                        text = "Cancel",
                        style = RevLensTypography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RevLensTheme.colors.textPrimary
                    )
                }
            },
            containerColor = RevLensTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
