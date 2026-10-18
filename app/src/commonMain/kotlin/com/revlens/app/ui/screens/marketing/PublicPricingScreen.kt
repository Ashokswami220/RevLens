package com.revlens.app.ui.screens.marketing

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.PlanTier
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.display.PricingTierCard
import com.revlens.app.ui.screens.marketing.components.FaqItem
import com.revlens.app.ui.screens.marketing.components.FaqSection
import com.revlens.app.ui.screens.marketing.components.FeatureComparisonMatrix
import com.revlens.app.ui.screens.marketing.components.MarketingFooter
import com.revlens.app.ui.screens.marketing.components.MarketingTopBar
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun PublicPricingScreen(
    viewModel: PublicPricingViewModel,
    onNavigateToAuth: (PlanTier?) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PublicPricingEffect.NavigateToAuth -> onNavigateToAuth(effect.selectedTier)
                PublicPricingEffect.NavigateToHome -> onNavigateToHome()
                is PublicPricingEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val faqs = listOf(
        FaqItem(
            question = "Can I switch or upgrade my plan at any time?",
            answer = "Yes! You can upgrade, downgrade, or switch between monthly and annual billing at any point from your workspace settings. Prorated credits are automatically applied."
        ),
        FaqItem(
            question = "What happens to my scenarios if I downgrade to Starter?",
            answer = "All your created scenarios and model simulations remain safe and accessible. You will only be restricted from creating additional scenarios beyond the Starter limit of 3."
        ),
        FaqItem(
            question = "Can I use RevLens completely offline / locally?",
            answer = "Absolutely. RevLens uses a local-first architecture. All calculations, goal forecasting, and scenario comparisons run directly on your device."
        ),
        FaqItem(
            question = "What payment methods do you accept?",
            answer = "We accept all major credit and debit cards (Visa, Mastercard, American Express) powered securely through Stripe and Paddle."
        )
    )

    Scaffold(
        topBar = {
            MarketingTopBar(
                onNavigateHome = { viewModel.onEvent(PublicPricingEvent.ClickHome) },
                onNavigatePricing = { /* already on pricing */ },
                onNavigateLogin = { viewModel.onEvent(PublicPricingEvent.ClickLogin) }
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                // HERO HEADER
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "FLEXIBLE PRICING",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }

                    Text(
                        text = "Predictable, Scalable Pricing for High-Growth SaaS",
                        style = RevLensTypography.headlineLarge.copy(fontSize = 38.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = RevLensTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Start free with local scenario modeling, then upgrade to collaborate with your team and sync live customer metrics.",
                        style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(620.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // BILLING PERIOD SWITCHER
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RevLensTheme.colors.surfaceElevated)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BillingPeriod.entries.forEach { period ->
                            val isSelected = state.billingPeriod == period
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) RevLensTheme.colors.primaryAction else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.onEvent(
                                            PublicPricingEvent.SelectBillingPeriod(period)
                                        )
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = period.label,
                                    style = RevLensTypography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) RevLensTheme.colors.onPrimaryAction else RevLensTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // PRICING CARDS ROW / GRID
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PlanTier.entries.forEach { tier ->
                        PricingTierCard(
                            tier = tier,
                            period = state.billingPeriod,
                            isCurrentPlan = false,
                            onSelectPlan = {
                                viewModel.onEvent(
                                    PublicPricingEvent.SelectPlan(tier)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // DETAILED COMPARISON TABLE
                FeatureComparisonMatrix()

                Spacer(modifier = Modifier.height(20.dp))

                // FAQ ACCORDION
                FaqSection(
                    faqList = faqs,
                    expandedIndices = state.expandedFaqIndices,
                    onToggle = { viewModel.onEvent(PublicPricingEvent.ToggleFaq(it)) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PRE-FOOTER BANNER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(RevLensTheme.colors.surface)
                        .padding(36.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ready to take control of your revenue trajectory?",
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Join thousands of SaaS founders simulating pricing changes with RevLens.",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        PrimaryButton(
                            text = "Get Started Free with Google",
                            onClick = { viewModel.onEvent(PublicPricingEvent.ClickLogin) }
                        )
                    }
                }
            }

            MarketingFooter(
                onNavigatePricing = { /* already here */ },
                onNavigateHome = { viewModel.onEvent(PublicPricingEvent.ClickHome) }
            )
        }
    }
}
