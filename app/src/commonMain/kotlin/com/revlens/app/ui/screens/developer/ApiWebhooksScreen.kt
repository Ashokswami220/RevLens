package com.revlens.app.ui.screens.developer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.ApiKeyScope
import com.revlens.app.domain.model.WebhookSupportedEvents
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.display.ApiKeyRow
import com.revlens.app.ui.components.display.CodeBlock
import com.revlens.app.ui.components.display.WebhookRow
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApiWebhooksScreen(
    viewModel: ApiWebhooksViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ApiWebhooksEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                ApiWebhooksEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "API & Webhooks",
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
            // TABS ROW
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
                DeveloperTab.entries.forEach { tab ->
                    val tabTitle = when (tab) {
                        DeveloperTab.API_KEYS -> "API Keys (${state.apiKeys.size})"
                        DeveloperTab.WEBHOOKS -> "Webhooks (${state.webhooks.size})"
                        DeveloperTab.DELIVERIES -> "Deliveries (${state.deliveries.size})"
                    }
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.onEvent(ApiWebhooksEvent.SelectTab(tab)) },
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

            // TAB 1: API KEYS
            if (state.activeTab == DeveloperTab.API_KEYS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Secret API Keys",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Authenticate requests directly to the RevLens API",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    PrimaryButton(
                        text = "+ Create Key",
                        onClick = { viewModel.onEvent(ApiWebhooksEvent.OpenCreateKeySheet) }
                    )
                }

                if (state.apiKeys.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surface)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active API keys found",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.apiKeys.forEach { key ->
                            ApiKeyRow(
                                apiKey = key,
                                onDelete = {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.PromptDeleteKey(key)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // TAB 2: WEBHOOKS
            if (state.activeTab == DeveloperTab.WEBHOOKS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Webhook Endpoints",
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Receive live HTTPS POST payloads when events trigger",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    PrimaryButton(
                        text = "+ Add Endpoint",
                        onClick = { viewModel.onEvent(ApiWebhooksEvent.OpenCreateWebhookSheet) }
                    )
                }

                if (state.webhooks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surface)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No webhook endpoints configured",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.webhooks.forEach { endpoint ->
                            WebhookRow(
                                endpoint = endpoint,
                                onRotateSecret = {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.RotateWebhookSecret(endpoint.id)
                                    )
                                },
                                onDelete = {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.PromptDeleteWebhook(endpoint)
                                    )
                                },
                                onCopySecret = {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.CopyText(endpoint.secret, "Signing Secret")
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // TAB 3: DELIVERIES
            if (state.activeTab == DeveloperTab.DELIVERIES) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Recent Delivery Attempts",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )

                    if (state.deliveries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RevLensTheme.colors.surface)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No deliveries recorded yet",
                                style = RevLensTypography.bodyMedium,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                    } else {
                        state.deliveries.forEach { delivery ->
                            val isSuccess = delivery.statusCode in 200..299
                            val isSelected = state.selectedDelivery?.id == delivery.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(
                                            ApiWebhooksEvent.SelectDelivery(
                                                if (isSelected) null else delivery
                                            )
                                        )
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = RevLensTheme.colors.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.borderDefault
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSuccess) RevLensTheme.colors.success.copy(
                                                            alpha = 0.15f
                                                        )
                                                        else RevLensTheme.colors.error.copy(
                                                            alpha = 0.15f
                                                        )
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${delivery.statusCode}",
                                                    style = RevLensTypography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSuccess) RevLensTheme.colors.success else RevLensTheme.colors.error
                                                )
                                            }

                                            Text(
                                                text = delivery.event,
                                                style = RevLensTypography.bodyMedium,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = RevLensTheme.colors.textPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${delivery.durationMs}ms",
                                                style = RevLensTypography.labelSmall,
                                                color = RevLensTheme.colors.textSecondary
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.onEvent(
                                                        ApiWebhooksEvent.RetryDelivery(delivery.id)
                                                    )
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Refresh,
                                                    contentDescription = "Retry delivery",
                                                    tint = RevLensTheme.colors.brandPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = delivery.timestamp,
                                        style = RevLensTypography.labelSmall,
                                        color = RevLensTheme.colors.textTertiary
                                    )

                                    if (isSelected) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Payload:",
                                                style = RevLensTypography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = RevLensTheme.colors.textSecondary
                                            )
                                            CodeBlock(
                                                code = delivery.payloadJson,
                                                onCopied = {
                                                    viewModel.onEvent(
                                                        ApiWebhooksEvent.CopyText(
                                                            delivery.payloadJson, "Payload"
                                                        )
                                                    )
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Response:",
                                                style = RevLensTypography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = RevLensTheme.colors.textSecondary
                                            )
                                            CodeBlock(
                                                code = delivery.responseBody,
                                                onCopied = {
                                                    viewModel.onEvent(
                                                        ApiWebhooksEvent.CopyText(
                                                            delivery.responseBody, "Response"
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE API KEY BOTTOM SHEET
    if (state.isCreateKeySheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ApiWebhooksEvent.DismissCreateKeySheet) },
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
                    text = "Generate New API Key",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                OutlinedTextField(
                    value = state.newKeyName,
                    onValueChange = { viewModel.onEvent(ApiWebhooksEvent.NewKeyNameChanged(it)) },
                    label = { Text("Key Name (e.g. GitHub Actions, Stripe Sync)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RevLensTheme.colors.textPrimary,
                        unfocusedTextColor = RevLensTheme.colors.textPrimary,
                        focusedBorderColor = RevLensTheme.colors.brandPrimary,
                        unfocusedBorderColor = RevLensTheme.colors.borderDefault
                    ),
                    singleLine = true
                )

                Text(
                    text = "Key Scope:",
                    style = RevLensTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RevLensTheme.colors.surfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ApiKeyScope.entries.forEach { scope ->
                        val isSelected = state.newKeyScope == scope
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) RevLensTheme.colors.primaryAction else Color.Transparent
                                )
                                .clickable {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.NewKeyScopeChanged(scope)
                                    )
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = scope.label,
                                style = RevLensTypography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RevLensTheme.colors.onPrimaryAction else RevLensTheme.colors.textSecondary
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.error
                    )
                }

                PrimaryButton(
                    text = "Generate Key",
                    onClick = { viewModel.onEvent(ApiWebhooksEvent.SubmitCreateKey) },
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ONE-TIME KEY REVEAL ALERT DIALOG
    if (state.createdKeyToken != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ApiWebhooksEvent.DismissKeyRevealDialog) },
            title = {
                Text(
                    text = "API Key Generated",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Please copy your API key now. For security purposes, you will not be able to see it again.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )

                    CodeBlock(
                        code = state.createdKeyToken!!,
                        onCopied = {
                            viewModel.onEvent(
                                ApiWebhooksEvent.CopyText(state.createdKeyToken!!, "API Key")
                            )
                        }
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "I've Saved This Key",
                    onClick = { viewModel.onEvent(ApiWebhooksEvent.DismissKeyRevealDialog) }
                )
            },
            containerColor = RevLensTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // CREATE WEBHOOK BOTTOM SHEET
    if (state.isCreateWebhookSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ApiWebhooksEvent.DismissCreateWebhookSheet) },
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
                    text = "Register Webhook Endpoint",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                OutlinedTextField(
                    value = state.newWebhookUrl,
                    onValueChange = {
                        viewModel.onEvent(
                            ApiWebhooksEvent.NewWebhookUrlChanged(it)
                        )
                    },
                    label = { Text("Endpoint URL (must begin with https://)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RevLensTheme.colors.textPrimary,
                        unfocusedTextColor = RevLensTheme.colors.textPrimary,
                        focusedBorderColor = RevLensTheme.colors.brandPrimary,
                        unfocusedBorderColor = RevLensTheme.colors.borderDefault
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.newWebhookDesc,
                    onValueChange = {
                        viewModel.onEvent(
                            ApiWebhooksEvent.NewWebhookDescChanged(it)
                        )
                    },
                    label = { Text("Description (e.g. Slack bot, Data warehouse)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RevLensTheme.colors.textPrimary,
                        unfocusedTextColor = RevLensTheme.colors.textPrimary,
                        focusedBorderColor = RevLensTheme.colors.brandPrimary,
                        unfocusedBorderColor = RevLensTheme.colors.borderDefault
                    ),
                    singleLine = true
                )

                Text(
                    text = "Events to send:",
                    style = RevLensTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    WebhookSupportedEvents.ALL.forEach { event ->
                        val isChecked = state.selectedEvents.contains(event)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.ToggleWebhookEvent(event)
                                    )
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    viewModel.onEvent(
                                        ApiWebhooksEvent.ToggleWebhookEvent(event)
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RevLensTheme.colors.brandPrimary
                                )
                            )
                            Text(
                                text = event,
                                style = RevLensTypography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.error
                    )
                }

                PrimaryButton(
                    text = "Register Endpoint",
                    onClick = { viewModel.onEvent(ApiWebhooksEvent.SubmitCreateWebhook) },
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // DELETE API KEY CONFIRM DIALOG
    if (state.keyToDelete != null) {
        val key = state.keyToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ApiWebhooksEvent.DismissDeleteKey) },
            title = {
                Text(
                    text = "Revoke API Key?",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to revoke key '${key.name}'? Any scripts or tools utilizing this key will immediately lose access.",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            },
            confirmButton = {
                DestructiveButton(
                    text = "Revoke Key",
                    onClick = { viewModel.onEvent(ApiWebhooksEvent.ConfirmDeleteKey) }
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ApiWebhooksEvent.DismissDeleteKey) }) {
                    Text(
                        text = "Cancel",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textPrimary
                    )
                }
            },
            containerColor = RevLensTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // DELETE WEBHOOK CONFIRM DIALOG
    if (state.webhookToDelete != null) {
        val endpoint = state.webhookToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ApiWebhooksEvent.DismissDeleteWebhook) },
            title = {
                Text(
                    text = "Remove Webhook Endpoint?",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove endpoint '${endpoint.description}' (${endpoint.url})? Event notifications will no longer be delivered.",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            },
            confirmButton = {
                DestructiveButton(
                    text = "Remove Endpoint",
                    onClick = { viewModel.onEvent(ApiWebhooksEvent.ConfirmDeleteWebhook) }
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ApiWebhooksEvent.DismissDeleteWebhook) }) {
                    Text(
                        text = "Cancel",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textPrimary
                    )
                }
            },
            containerColor = RevLensTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
