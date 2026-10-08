package com.example.revlens.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.MrrGoalPlanner
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.LabeledSlider
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalPlannerScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    
    val currentMrr = profile?.currentMrr?.toFloat() ?: 1000f
    var targetMrr by remember { mutableStateOf(currentMrr * 2) }
    var targetMonths by remember { mutableStateOf(12f) }

    val goalPlan = remember(profile, targetMrr, targetMonths) {
        profile?.let {
            MrrGoalPlanner.calculateRequiredMetrics(
                currentProfile = it,
                targetMrr = BigDecimal(targetMrr.toString()),
                months = targetMonths.roundToInt()
            )
        }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            RevLensTopAppBar(title = "MRR Goal Planner", onBackClick = onNavigateBack)
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            SectionCard(title = "Set Your Goal") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledSlider(
                        label = "Target MRR",
                        value = targetMrr,
                        onValueChange = { targetMrr = it },
                        valueRange = currentMrr..currentMrr * 10,
                        valueString = currencyFormatter.format(targetMrr),
                        caption = "Your revenue goal"
                    )

                    LabeledSlider(
                        label = "Timeframe",
                        value = targetMonths,
                        onValueChange = { targetMonths = it },
                        valueRange = 1f..60f,
                        valueString = "${targetMonths.toInt()} months",
                        caption = "Months to achieve this goal"
                    )
                }
            }

            if (goalPlan != null) {
                SectionCard(title = "What It Takes") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultRow(
                            label = "Required Monthly Growth (CMGR)",
                            value = "${(goalPlan.requiredMonthlyGrowthRate * 100).let { "%.1f".format(it) }}%"
                        )
                        
                        ResultRow(
                            label = "New Customers Needed / Month",
                            value = "${goalPlan.requiredNewCustomersPerMonth}",
                            caption = "(Assuming ARPU stays the same)"
                        )
                        
                        ResultRow(
                            label = "Or, Required ARPU",
                            value = currencyFormatter.format(goalPlan.requiredArpu),
                            caption = "(Assuming Customer count stays the same)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, caption: String? = null) {
    Column {
        Text(text = label, style = RevLensTypography.bodyMedium, color = RevLensTheme.colors.textSecondary)
        Text(text = value, style = RevLensTypography.headlineMedium, fontWeight = FontWeight.Bold, color = RevLensTheme.colors.brandPrimary)
        if (caption != null) {
            Text(text = caption, style = RevLensTypography.labelSmall, color = RevLensTheme.colors.textTertiary)
        }
    }
}
