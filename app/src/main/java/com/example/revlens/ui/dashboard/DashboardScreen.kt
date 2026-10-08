package com.example.revlens.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
        }

        SectionCard(
            title = "CAC Payback",
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (cacPayback != null) "${
                    String.format(
                        Locale.US, "%.1f", cacPayback
                    )
                } mo" else "Never",
                style = RevLensTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (cacPayback != null && cacPayback < 12.0) RevLensTheme.colors.success else RevLensTheme.colors.error
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
