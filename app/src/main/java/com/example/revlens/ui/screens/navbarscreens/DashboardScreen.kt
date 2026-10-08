package com.example.revlens.ui.screens.navbarscreens

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
                title = "RevLens Dashboard",
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
        SectionCard(
            title = "MRR",
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = currencyFormatter.format(mrr),
                style = RevLensTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.brandPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Sparkline(
                data = listOf(mrr.toFloat() * 0.8f, mrr.toFloat() * 0.9f, mrr.toFloat()),
                color = RevLensTheme.colors.brandPrimary
            )
        }

        SectionCard(
            title = "ARPU",
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = currencyFormatter.format(arpu),
                style = RevLensTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Sparkline(
                data = listOf(arpu.toFloat() * 0.95f, arpu.toFloat() * 0.98f, arpu.toFloat()),
                color = RevLensTheme.colors.brandSecondary
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(
            title = "LTV",
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = currencyFormatter.format(ltv),
                style = RevLensTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Sparkline(
                data = listOf(ltv.toFloat() * 0.9f, ltv.toFloat() * 0.95f, ltv.toFloat()),
                color = RevLensTheme.colors.success
            )
        }

        SectionCard(
            title = "CAC Payback",
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (cacPayback != null) "${String.format(Locale.US, "%.1f", cacPayback)} mo" else "Never",
                style = RevLensTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (cacPayback != null && cacPayback < 12.0) RevLensTheme.colors.success else RevLensTheme.colors.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Sparkline(
                data = listOf((cacPayback?.toFloat() ?: 0f) * 1.2f, (cacPayback?.toFloat() ?: 0f) * 1.1f, (cacPayback?.toFloat() ?: 0f)),
                color = if (cacPayback != null && cacPayback < 12.0) RevLensTheme.colors.success else RevLensTheme.colors.error
            )
        }
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

    // Actions
    SectionCard(title = "Quick Actions") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryButton(
                text = "Edit Data",
                onClick = onNavigateToInput,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "What-If",
                onClick = onNavigateToWhatIf,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Break-Even",
                onClick = onNavigateToBreakEven,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
