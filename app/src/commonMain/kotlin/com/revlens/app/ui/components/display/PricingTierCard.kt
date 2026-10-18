package com.revlens.app.ui.components.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.PlanTier
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.SecondaryButton
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun PricingTierCard(
    tier: PlanTier,
    period: BillingPeriod,
    isCurrentPlan: Boolean,
    onSelectPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPopular = tier == PlanTier.PRO
    val priceUsd = if (period == BillingPeriod.ANNUAL) tier.annualPriceUsd else tier.monthlyPriceUsd
    val borderColor = when {
        isCurrentPlan -> RevLensTheme.colors.brandPrimary
        isPopular -> RevLensTheme.colors.brandPrimary.copy(alpha = 0.6f)
        else -> RevLensTheme.colors.borderDefault
    }
    val borderWidth = if (isCurrentPlan || isPopular) 2.dp else 1.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RevLensTheme.colors.surface
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tier.title,
                    style = RevLensTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )

                if (isCurrentPlan) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CURRENT PLAN",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }
                } else if (isPopular) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.accentBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MOST POPULAR",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.accentBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (priceUsd == 0) "$0" else "$$priceUsd",
                    style = RevLensTypography.headlineMedium.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = " / month",
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            if (period == BillingPeriod.ANNUAL && priceUsd > 0) {
                Text(
                    text = "Billed annually ($${priceUsd * 12}/year) • Save 20%",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.success,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Limits highlight
            Text(
                text = "${tier.maxSeats} Team ${if (tier.maxSeats == 1) "Seat" else "Seats"} • ${tier.maxScenarios} Scenarios",
                style = RevLensTypography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = RevLensTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Features list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tier.features.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isCurrentPlan) {
                SecondaryButton(
                    text = "Current Plan",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = "Switch to ${tier.title}",
                    onClick = onSelectPlan,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
