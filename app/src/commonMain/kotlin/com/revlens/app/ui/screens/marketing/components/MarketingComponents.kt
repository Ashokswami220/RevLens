package com.revlens.app.ui.screens.marketing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
fun MarketingTopBar(
    onNavigateHome: () -> Unit,
    onNavigatePricing: () -> Unit,
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RevLensTheme.colors.background)
            .border(
                BorderStroke(1.dp, RevLensTheme.colors.borderDefault.copy(alpha = 0.5f))
            )
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { onNavigateHome() }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RevLensTheme.colors.brandPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RL",
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = RevLensTheme.colors.background
                    )
                }
                Text(
                    text = "RevLens",
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            }

            // Navigation Links & Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(onClick = onNavigateHome) {
                    Text(
                        text = "Features",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                }

                TextButton(onClick = onNavigatePricing) {
                    Text(
                        text = "Pricing",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(onClick = onNavigateLogin) {
                    Text(
                        text = "Sign In",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textPrimary
                    )
                }

                PrimaryButton(
                    text = "Get Started",
                    onClick = onNavigateLogin
                )
            }
        }
    }
}

@Composable
fun FaqSection(
    faqList: List<FaqItem>,
    expandedIndices: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Frequently Asked Questions",
            style = RevLensTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        faqList.forEachIndexed { index, item ->
            val isExpanded = expandedIndices.contains(index)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggle(index) },
                colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
                border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.question,
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RevLensTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = RevLensTheme.colors.textSecondary
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Text(
                            text = item.answer,
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureComparisonMatrix(modifier: Modifier = Modifier) {
    data class MatrixRow(val title: String, val free: String, val pro: String, val enterprise: String)

    val rows = listOf(
        MatrixRow("Team Members & Seats", "1 Seat", "Up to 5 Seats", "Up to 25 Seats"),
        MatrixRow("Simulation Scenarios", "3 Scenarios", "20 Scenarios", "100 Scenarios"),
        MatrixRow("MRR & ARR Dynamic Engine", "Basic", "Advanced", "Full Multi-Currency"),
        MatrixRow("Cohort & CAC Analytics", "—", "Included", "Included"),
        MatrixRow("Stripe & Paddle Sync", "—", "Unlimited", "Unlimited Real-time"),
        MatrixRow("Investor PDF Exports", "—", "Unlimited", "White-labeled"),
        MatrixRow("Developer API & Webhooks", "—", "—", "Full Access"),
        MatrixRow("Support Level", "Community", "Priority Email", "Dedicated Partner")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
        border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Detailed Plan Comparison",
                style = RevLensTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RevLensTheme.colors.surfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Feature",
                    style = RevLensTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.weight(1.4f)
                )
                Text(
                    text = "Starter",
                    style = RevLensTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Growth Pro",
                    style = RevLensTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.brandPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Scale & Team",
                    style = RevLensTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Table Body
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.title,
                        style = RevLensTypography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RevLensTheme.colors.textPrimary,
                        modifier = Modifier.weight(1.4f)
                    )
                    Text(
                        text = row.free,
                        style = RevLensTypography.bodySmall,
                        color = if (row.free == "—") RevLensTheme.colors.textTertiary else RevLensTheme.colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.pro,
                        style = RevLensTypography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = RevLensTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.enterprise,
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (index < rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(RevLensTheme.colors.borderDefault.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
fun MarketingFooter(
    onNavigatePricing: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RevLensTheme.colors.surfaceElevated)
            .border(BorderStroke(1.dp, RevLensTheme.colors.borderDefault.copy(alpha = 0.5f)))
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RevLens",
                        style = RevLensTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Text(
                        text = "The modern revenue intelligence and scenario engine for high-growth SaaS.",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary,
                        modifier = Modifier.width(320.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Product",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Simulator",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.clickable { onNavigateHome() }
                        )
                        Text(
                            text = "Pricing",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.clickable { onNavigatePricing() }
                        )
                        Text(
                            text = "Integrations",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Security & Compliance",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Local-First Architecture",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                        Text(
                            text = "Biometric Protection",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                        Text(
                            text = "Privacy Policy",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RevLensTheme.colors.borderDefault.copy(alpha = 0.3f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "© 2026 RevLens, Inc. All rights reserved.",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textTertiary
                )
                Text(
                    text = "Built with Kotlin Multiplatform & Compose Wasm",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textTertiary
                )
            }
        }
    }
}
