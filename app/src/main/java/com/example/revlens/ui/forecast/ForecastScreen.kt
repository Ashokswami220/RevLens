package com.example.revlens.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.ForecastEngine
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToGoalPlanner: () -> Unit,
    onNavigateToMetrics: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    var selectedMonths by remember { mutableStateOf(12) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US)
            .apply {
                currency = Currency.getInstance("USD")
                maximumFractionDigits = 0
            }
    }

    val forecastData = remember(profile, selectedMonths) {
        profile?.let { ForecastEngine.forecast(it, selectedMonths) } ?: emptyList()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Revenue Forecast",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Tabs / Chips for Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(6, 12, 24).forEach { months ->
                    FilterChip(
                        selected = selectedMonths == months,
                        onClick = { selectedMonths = months },
                        label = { Text("$months Months", style = RevLensTypography.labelLarge) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RevLensTheme.colors.brandPrimary,
                            selectedLabelColor = RevLensTheme.colors.background,
                            containerColor = RevLensTheme.colors.surface,
                            labelColor = RevLensTheme.colors.textSecondary
                        )
                    )
                }
            }

            // Summary Card
            if (forecastData.isNotEmpty()) {
                val finalMonth = forecastData.last()
                SectionCard(title = "End of Period Summary (Month $selectedMonths)") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Projected MRR", style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Text(
                                currencyFormatter.format(finalMonth.mrr),
                                style = RevLensTypography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.brandPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Customers", style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Text(
                                "${finalMonth.customers}", style = RevLensTypography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            PrimaryButton(
                text = "Set MRR Goal",
                onClick = onNavigateToGoalPlanner
            )

            Text(
                "Month by Month Breakdown", style = RevLensTypography.titleMedium,
                color = RevLensTheme.colors.textPrimary
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(forecastData) { data ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RevLensTheme.colors.surface, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (data.monthIndex == 0) "Today" else "Month ${data.monthIndex}",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = currencyFormatter.format(data.mrr),
                            style = RevLensTypography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${data.customers} users",
                            style = RevLensTypography.bodyMedium,
                            color = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
