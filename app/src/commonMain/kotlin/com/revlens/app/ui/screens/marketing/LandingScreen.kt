package com.revlens.app.ui.screens.marketing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.screens.marketing.components.MarketingFooter
import com.revlens.app.ui.screens.marketing.components.MarketingTopBar
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun LandingScreen(
    viewModel: LandingViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToDemo: () -> Unit,
    onNavigateToPricing: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LandingEffect.NavigateToAuth -> onNavigateToAuth()
                LandingEffect.NavigateToDemo -> onNavigateToDemo()
                LandingEffect.NavigateToPricing -> onNavigateToPricing()
            }
        }
    }

    Scaffold(
        topBar = {
            MarketingTopBar(
                onNavigateHome = { /* already here */ },
                onNavigatePricing = { viewModel.onEvent(LandingEvent.ClickPricing) },
                onNavigateLogin = { viewModel.onEvent(LandingEvent.ClickGetStarted) }
            )
        },
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
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // 1. HERO SECTION
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✨ REVENUE INTELLIGENCE & SCENARIO ENGINE",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }

                    Text(
                        text = "Turn Revenue Uncertainty Into Confident SaaS Growth",
                        style = RevLensTypography.headlineLarge.copy(fontSize = 42.sp, lineHeight = 48.sp),
                        fontWeight = FontWeight.Black,
                        color = RevLensTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Simulate price points, forecast runway, evaluate customer cohort churn, and export investor updates — completely private and local-first.",
                        style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(660.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryButton(
                            text = "Get Started Free with Google",
                            onClick = { viewModel.onEvent(LandingEvent.ClickGetStarted) }
                        )

                        SecondaryButton(
                            text = "Explore Live Simulator",
                            onClick = { viewModel.onEvent(LandingEvent.ClickExploreDemo) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ Zero setup required",
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.textTertiary
                        )
                        Text(
                            text = "✓ Local-first privacy",
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.textTertiary
                        )
                        Text(
                            text = "✓ Instant Web & Android sync",
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.textTertiary
                        )
                    }
                }

                // 2. LIVE SIMULATION HERO CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
                    border = BorderStroke(2.dp, RevLensTheme.colors.brandPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Live Growth Scenario Simulation",
                                    style = RevLensTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Model: Aggressive Expansion (Q4 2026)",
                                    style = RevLensTypography.bodySmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(RevLensTheme.colors.success.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PROJECTED LIFT: ${state.simulatedLift}",
                                    style = RevLensTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.success
                                )
                            }
                        }

                        // Metric Stat Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            MetricStatCard(
                                title = "Projected MRR",
                                value = state.simulatedMrr,
                                delta = state.simulatedLift,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Customer LTV",
                                value = state.simulatedLtv,
                                delta = "+18.2%",
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "CAC Payback",
                                value = state.simulatedPayback,
                                delta = "-1.4 mo",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. FEATURE PILLARS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Why High-Growth SaaS Teams Choose RevLens",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeaturePillarCard(
                            title = "Dynamic Pricing Simulator",
                            description = "Test price changes across customer tiers, evaluate conversion elasticity, and predict net revenue before changing live plans.",
                            modifier = Modifier.weight(1f)
                        )
                        FeaturePillarCard(
                            title = "Cohort Retention & Churn",
                            description = "Track acquisition vintages and pinpoint churn hot-spots to plug revenue leaks and increase customer lifetime value.",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeaturePillarCard(
                            title = "Side-by-Side Scenarios",
                            description = "Run conservative, target, and aggressive scenarios in parallel to present clear runways and growth forecasts to investors.",
                            modifier = Modifier.weight(1f)
                        )
                        FeaturePillarCard(
                            title = "Local-First Privacy & Speed",
                            description = "Instant responsiveness with zero latency. Sensitive revenue models stay private on your device with biometric lock.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. PRE-FOOTER CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(RevLensTheme.colors.surface)
                        .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(20.dp))
                        .padding(40.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Model Your SaaS Revenue with Clarity",
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Join modern software operators making data-driven pricing decisions with RevLens.",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        PrimaryButton(
                            text = "Get Started Free with Google",
                            onClick = { viewModel.onEvent(LandingEvent.ClickGetStarted) }
                        )
                    }
                }
            }

            // 5. FOOTER
            MarketingFooter(
                onNavigatePricing = { viewModel.onEvent(LandingEvent.ClickPricing) },
                onNavigateHome = { /* already here */ }
            )
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = RevLensTypography.labelSmall,
                color = RevLensTheme.colors.textSecondary
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = delta,
                    style = RevLensTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.success
                )
            }
        }
    }
}

@Composable
private fun FeaturePillarCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
        border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = description,
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textSecondary
            )
        }
    }
}
