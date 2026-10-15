package com.example.revlens.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.revlens.ui.components.ActionCardButton
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

/**
 * Dedicated Intro / Welcome Screen.
 * Features an enlarged upper card containing the revenue graphic, title, and subtitle
 * with balanced, centered alignment, followed by feature highlights and a Get Started CTA.
 */
@Composable
fun IntroScreen(
    onGetStarted: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBorder = if (isDark) {
        BorderStroke(1.dp, Color(0xFF262C38))
    } else {
        BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
    }
    val cardBg = if (isDark) Color(0xFF161A22) else RevLensTheme.colors.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RevLensTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Upper Card: Bigger, enclosing revenue graphics, headline, and subtitle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(cardBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Stylized Metric Bars Graphic
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(RevLensTheme.colors.brandTertiary.copy(alpha = 0.75f))
                        )
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(RevLensTheme.colors.brandSecondary.copy(alpha = 0.9f))
                        )
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(60.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(RevLensTheme.colors.brandPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "See your revenue\nbefore it happens",
                        style = RevLensTypography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Interactive modeling for pricing changes, scenario planning, and financial forecasting.",
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Feature items list
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                IntroFeatureItem("Simulate price & churn impact", RevLensTheme.colors.brandPrimary)
                IntroFeatureItem("Compare growth scenarios", RevLensTheme.colors.brandSecondary)
                IntroFeatureItem("Plan MRR targets and unit economics", RevLensTheme.colors.brandTertiary)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom CTA Button
        ActionCardButton(
            text = "Get Started",
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun IntroFeatureItem(text: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = RevLensTypography.bodyLarge,
            color = RevLensTheme.colors.textSecondary
        )
    }
}
