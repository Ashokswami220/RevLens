package com.example.revlens.ui.screens.navbarscreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.BreakEvenCalculator
import com.example.revlens.domain.calc.LtvCalculator
import com.example.revlens.model.BusinessProfile
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.ProgressRing
import com.example.revlens.ui.components.RevLensLineChart
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.components.Sparkline
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToInput: () -> Unit,
    onNavigateToWhatIf: () -> Unit,
    onNavigateToBreakEven: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMetrics: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "RevLens",
                actions = {
                    androidx.compose.material3.IconButton(onClick = onNavigateToMetrics) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Metrics",
                            tint = RevLensTheme.colors.textPrimary
                        )
                    }
                }
            )
        },
        containerColor = RevLensTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            profile?.let { currentProfile ->
                DashboardContent(
                    profile = currentProfile,
                    onNavigateToInput = onNavigateToInput,
                    onNavigateToWhatIf = onNavigateToWhatIf,
                    onNavigateToBreakEven = onNavigateToBreakEven
                )
            } ?: run {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to RevLens!",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Add your business metrics and pricing plans to get started.",
                        style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryButton(
                        text = "Add Data",
                        onClick = onNavigateToInput,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    profile: BusinessProfile,
    onNavigateToInput: () -> Unit,
    onNavigateToWhatIf: () -> Unit,
    onNavigateToBreakEven: () -> Unit
) {

    // Quick Actions
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = onNavigateToInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RevLensTheme.colors.textPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit Data",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Data", style = RevLensTypography.labelLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Quick Actions",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onNavigateToWhatIf,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RevLensTheme.colors.textPrimary
                )
            ) {
                Text("What-If", style = RevLensTypography.labelLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onNavigateToBreakEven,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RevLensTheme.colors.textPrimary
                )
            ) {
                Text(
                    "Break-Even", style = RevLensTypography.labelLarge, fontWeight = FontWeight.Bold
                )
            }
        }
    }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        .apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 0
        }

    val mrr = profile.currentMrr
    val arpu = profile.arpu
    val ltv = LtvCalculator.calculateLtv(arpu, profile.monthlyChurnRate)
    val cacPayback = BreakEvenCalculator.calculateCacPaybackPeriod(profile)

    // MRR Trend Chart
    SectionCard(title = "MRR Trend (Last 6 Months)") {
        val mrrFloat = mrr.toFloat()
        val gRate = profile.monthlyGrowthRate.toFloat()
        val trendData = listOf(
            mrrFloat * (1f - gRate * 5), mrrFloat * (1f - gRate * 4), mrrFloat * (1f - gRate * 3),
            mrrFloat * (1f - gRate * 2), mrrFloat * (1f - gRate * 1), mrrFloat
        )
        RevLensLineChart(
            data = listOf(trendData),
            colors = listOf(RevLensTheme.colors.brandPrimary)
        )
    }

    // KPI Grid
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val mrrGrowth = mrr.multiply(java.math.BigDecimal(profile.monthlyGrowthRate))
        KpiCard(
            title = "MRR",
            value = currencyFormatter.format(mrr),
            subValue = "+${currencyFormatter.format(mrrGrowth)} net",
            badgeText = "+${(profile.monthlyGrowthRate * 100).toInt()}% Total",
            chartColor = RevLensTheme.colors.brandPrimary,
            chartData = listOf(
                mrr.toFloat() * (1f - profile.monthlyGrowthRate.toFloat() * 2),
                mrr.toFloat() * (1f - profile.monthlyGrowthRate.toFloat()), mrr.toFloat()
            ),
            modifier = Modifier.weight(1f)
        )

        KpiCard(
            title = "ARPU",
            value = currencyFormatter.format(arpu),
            subValue = if (profile.monthlyGrowthRate > 0) "Growing" else "Stable",
            badgeText = "Avg/User",
            chartColor = RevLensTheme.colors.brandSecondary,
            chartData = listOf(arpu.toFloat() * 0.98f, arpu.toFloat() * 0.99f, arpu.toFloat()),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val ltvGrowth = "+${(profile.monthlyGrowthRate * 50).toInt()}%"
        KpiCard(
            title = "LTV",
            value = currencyFormatter.format(ltv),
            subValue = "Lifetime",
            badgeText = ltvGrowth,
            chartColor = RevLensTheme.colors.success,
            chartData = listOf(ltv.toFloat() * 0.9f, ltv.toFloat() * 0.95f, ltv.toFloat()),
            modifier = Modifier.weight(1f)
        )

        KpiCard(
            title = "CAC Payback",
            value = if (cacPayback != null) "${
                String.format(
                    Locale.US, "%.1f", cacPayback
                )
            } mo" else "Never",
            subValue = "-1.2 mo",
            badgeText = "Optimal",
            chartColor = if (cacPayback != null && cacPayback < 12.0) RevLensTheme.colors.success else RevLensTheme.colors.error,
            chartData = listOf(
                (cacPayback?.toFloat() ?: 0f) * 1.2f, (cacPayback?.toFloat() ?: 0f) * 1.1f,
                (cacPayback?.toFloat() ?: 0f)
            ),
            modifier = Modifier.weight(1f)
        )
    }

    // Goal Tracker
    SectionCard(title = "Annual MRR Goal") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ProgressRing(
                progress = 0.65f, // Mock progress
                goalText = "65% of \$10k goal",
                color = RevLensTheme.colors.brandSecondary
            )
        }
    }

}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subValue: String,
    badgeText: String,
    chartColor: androidx.compose.ui.graphics.Color,
    chartData: List<Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = RevLensTypography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textSecondary
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(chartColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    style = RevLensTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = chartColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = RevLensTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = subValue,
                style = RevLensTypography.labelMedium,
                color = chartColor,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Sparkline(
            data = chartData,
            color = chartColor
        )
    }
}
