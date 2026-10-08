package com.example.revlens.ui.screens.navbarscreens

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.BreakEvenCalculator
import com.example.revlens.domain.calc.LtvCalculator
import com.example.revlens.model.BusinessProfile
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import com.example.revlens.ui.components.RevLensLineChart
import com.example.revlens.ui.components.Sparkline
import com.example.revlens.ui.components.ProgressRing
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
                // Loading or null state
                Text(
                    text = "Loading profile...",
                    color = RevLensTheme.colors.textPrimary,
                    style = RevLensTypography.bodyLarge
                )
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
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RevLensTheme.colors.textPrimary)
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
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RevLensTheme.colors.textPrimary)
            ) {
                Text("What-If", style = RevLensTypography.labelLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onNavigateToBreakEven,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RevLensTheme.colors.borderDefault),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RevLensTheme.colors.textPrimary)
            ) {
                Text("Break-Even", style = RevLensTypography.labelLarge, fontWeight = FontWeight.Bold)
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
        val trendData = listOf(
            mrrFloat * 0.7f, mrrFloat * 0.75f, mrrFloat * 0.8f, 
            mrrFloat * 0.9f, mrrFloat * 0.95f, mrrFloat
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
        KpiCard(
            title = "MRR",
            value = currencyFormatter.format(mrr),
            subValue = "+12% net",
            badgeText = "+69% Total",
            chartColor = RevLensTheme.colors.brandPrimary,
            chartData = listOf(mrr.toFloat() * 0.8f, mrr.toFloat() * 0.9f, mrr.toFloat()),
            modifier = Modifier.weight(1f)
        )

        KpiCard(
            title = "ARPU",
            value = currencyFormatter.format(arpu),
            subValue = "+$5 net",
            badgeText = "Stable",
            chartColor = RevLensTheme.colors.brandSecondary,
            chartData = listOf(arpu.toFloat() * 0.95f, arpu.toFloat() * 0.98f, arpu.toFloat()),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KpiCard(
            title = "LTV",
            value = currencyFormatter.format(ltv),
            subValue = "+$150 net",
            badgeText = "+5.4%",
            chartColor = RevLensTheme.colors.success,
            chartData = listOf(ltv.toFloat() * 0.9f, ltv.toFloat() * 0.95f, ltv.toFloat()),
            modifier = Modifier.weight(1f)
        )

        KpiCard(
            title = "CAC Payback",
            value = if (cacPayback != null) "${String.format(Locale.US, "%.1f", cacPayback)} mo" else "Never",
            subValue = "-1.2 mo",
            badgeText = "Optimal",
            chartColor = if (cacPayback != null && cacPayback < 12.0) RevLensTheme.colors.success else RevLensTheme.colors.error,
            chartData = listOf((cacPayback?.toFloat() ?: 0f) * 1.2f, (cacPayback?.toFloat() ?: 0f) * 1.1f, (cacPayback?.toFloat() ?: 0f)),
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
