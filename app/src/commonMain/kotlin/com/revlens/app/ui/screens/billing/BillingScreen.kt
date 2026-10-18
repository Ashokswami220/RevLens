package com.revlens.app.ui.screens.billing

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.PlanTier
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.components.display.PricingTierCard
import com.revlens.app.ui.components.display.UsageMeter
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BillingEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                BillingEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Billing & Subscription",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // CURRENT PLAN HERO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
                border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = RevLensTheme.colors.brandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.subscription.planTier.title,
                                    style = RevLensTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (state.subscription.planTier == PlanTier.FREE) {
                                        "Free Plan"
                                    } else {
                                        "$${if (state.subscription.period == BillingPeriod.ANNUAL) state.subscription.planTier.annualPriceUsd else state.subscription.planTier.monthlyPriceUsd} / month • ${state.subscription.period.label}"
                                    },
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (state.subscription.cancelAtPeriodEnd) {
                                        RevLensTheme.colors.warning.copy(alpha = 0.15f)
                                    } else {
                                        RevLensTheme.colors.success.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.subscription.cancelAtPeriodEnd) "CANCELLING" else "ACTIVE",
                                style = RevLensTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.subscription.cancelAtPeriodEnd) {
                                    RevLensTheme.colors.warning
                                } else {
                                    RevLensTheme.colors.success
                                }
                            )
                        }
                    }

                    if (state.subscription.cancelAtPeriodEnd) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.warning.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Your subscription is set to cancel at the end of the current billing period (${state.subscription.renewsAt}). You will not be charged again.",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.warning
                            )
                        }
                    }

                    // Metadata details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                tint = RevLensTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.subscription.cancelAtPeriodEnd) "Access ends:" else "Renews on:",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.subscription.renewsAt,
                                style = RevLensTypography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreditCard,
                                contentDescription = null,
                                tint = RevLensTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment method:",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.subscription.paymentMethod,
                                style = RevLensTypography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryButton(
                            text = "Change Plan",
                            onClick = { viewModel.onEvent(BillingEvent.OpenChangePlanSheet) },
                            modifier = Modifier.weight(1f)
                        )

                        if (state.subscription.cancelAtPeriodEnd) {
                            SecondaryButton(
                                text = "Reactivate",
                                onClick = {
                                    viewModel.onEvent(
                                        BillingEvent.ReactivateSubscription
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else if (state.subscription.planTier != PlanTier.FREE) {
                            SecondaryButton(
                                text = "Cancel",
                                onClick = {
                                    viewModel.onEvent(
                                        BillingEvent.PromptCancelSubscription
                                    )
                                },
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    }
                }
            }

            // ENTITLEMENTS & USAGE METERS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resource Usage & Limits",
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
                    border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UsageMeter(
                            label = "Team Seats",
                            used = state.usedSeats,
                            limit = state.subscription.planTier.maxSeats,
                            unitLabel = "seats"
                        )

                        UsageMeter(
                            label = "Simulation Scenarios",
                            used = state.usedScenarios,
                            limit = state.subscription.planTier.maxScenarios,
                            unitLabel = "scenarios"
                        )
                    }
                }
            }

            // INVOICES & BILLING HISTORY
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Billing History",
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                if (state.invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surface)
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No invoices available",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = RevLensTheme.colors.surface
                        ),
                        border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            state.invoices.forEachIndexed { index, invoice ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = invoice.date,
                                            style = RevLensTypography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RevLensTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = invoice.id,
                                            style = RevLensTypography.labelSmall,
                                            color = RevLensTheme.colors.textSecondary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = invoice.amountFormatted,
                                            style = RevLensTypography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RevLensTheme.colors.textPrimary
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(
                                                    RevLensTheme.colors.success.copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = invoice.status,
                                                style = RevLensTypography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = RevLensTheme.colors.success
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.onEvent(
                                                    BillingEvent.DownloadInvoice(invoice.id)
                                                )
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Download,
                                                contentDescription = "Download invoice",
                                                tint = RevLensTheme.colors.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < state.invoices.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(
                                                RevLensTheme.colors.borderDefault.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CHANGE PLAN MODAL BOTTOM SHEET
    if (state.isChangePlanSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(BillingEvent.DismissChangePlanSheet) },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Choose Your Plan",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                // BILLING PERIOD SWITCHER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RevLensTheme.colors.surfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BillingPeriod.entries.forEach { period ->
                        val isSelected = state.selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) RevLensTheme.colors.primaryAction else Color.Transparent
                                )
                                .clickable {
                                    viewModel.onEvent(
                                        BillingEvent.SelectBillingPeriod(period)
                                    )
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.label,
                                style = RevLensTypography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RevLensTheme.colors.onPrimaryAction else RevLensTheme.colors.textSecondary
                            )
                        }
                    }
                }

                // TIERS LIST
                PlanTier.entries.forEach { tier ->
                    PricingTierCard(
                        tier = tier,
                        period = state.selectedPeriod,
                        isCurrentPlan = state.subscription.planTier == tier,
                        onSelectPlan = { viewModel.onEvent(BillingEvent.SelectPlan(tier)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // CANCEL SUBSCRIPTION CONFIRMATION DIALOG
    if (state.isCancelDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(BillingEvent.DismissCancelSubscription) },
            title = {
                Text(
                    text = "Cancel Subscription?",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel your subscription? You will retain access to your plan's benefits until the end of your current billing period (${state.subscription.renewsAt}).",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            },
            confirmButton = {
                DestructiveButton(
                    text = "Cancel Subscription",
                    onClick = { viewModel.onEvent(BillingEvent.ConfirmCancelSubscription) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(BillingEvent.DismissCancelSubscription) }) {
                    Text(
                        text = "Keep Plan",
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
