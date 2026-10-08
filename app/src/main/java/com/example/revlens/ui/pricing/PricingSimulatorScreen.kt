package com.example.revlens.ui.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.PricingSimulationEngine
import com.example.revlens.model.Assumptions
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.ChoiceChip
import com.example.revlens.ui.components.DeltaChip
import com.example.revlens.ui.components.DeltaType
import com.example.revlens.ui.components.LabeledSlider
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.components.SegmentedTabs
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingSimulatorScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToWhatIf: () -> Unit,
    onNavigateToBreakEven: () -> Unit,
    onNavigateToMetrics: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    var selectedPlanId by remember { mutableStateOf<String?>(null) }
    
    // Sliders state
    var priceChangePercent by remember { mutableStateOf(0f) }
    var churnChangePercent by remember { mutableStateOf(0f) }
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var scenarioName by remember { mutableStateOf("") }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 0
        }
    }

    // Assumptions object for simulation
    val assumptions by remember {
        derivedStateOf {
            Assumptions(
                priceChangePercent = priceChangePercent / 100.0,
                churnChangePercent = churnChangePercent / 100.0
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Simulate",
                actions = {
                    IconButton(onClick = onNavigateToMetrics) {
                        Icon(
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Segmented Tabs
            SegmentedTabs(
                options = listOf("Pricing", "What-If", "Break-Even"),
                selectedIndex = 0,
                onOptionSelected = { index ->
                    when (index) {
                        1 -> onNavigateToWhatIf()
                        2 -> onNavigateToBreakEven()
                    }
                }
            )

            if (profile != null) {
                // Initialize selected plan if null
                if (selectedPlanId == null && profile!!.plans.isNotEmpty()) {
                    selectedPlanId = profile!!.plans.first().id
                }

                // Plan Chips
                Text("Plan", style = RevLensTypography.titleMedium, color = RevLensTheme.colors.textPrimary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile!!.plans.forEach { plan ->
                        ChoiceChip(
                            text = plan.name,
                            selected = selectedPlanId == plan.id,
                            onClick = { selectedPlanId = plan.id }
                        )
                    }
                }

                val currentPlan = profile!!.plans.find { it.id == selectedPlanId }

                if (currentPlan != null) {
                    SectionCard(title = "Price") {
                        val currentPrice = currentPlan.price.toDouble()
                        val newPrice = currentPrice * (1.0 + assumptions.priceChangePercent)
                        
                        LabeledSlider(
                            label = "New price",
                            value = priceChangePercent,
                            onValueChange = { priceChangePercent = it },
                            valueRange = -50f..100f,
                            valueString = "$${newPrice.toInt()}",
                            caption = "Current: $${currentPrice.toInt()}"
                        )
                    }

                    SectionCard(title = "Assumptions") {
                        LabeledSlider(
                            label = "Churn change",
                            value = churnChangePercent,
                            onValueChange = { churnChangePercent = it },
                            valueRange = -10f..10f,
                            valueString = "${String.format(Locale.US, "%.1f", churnChangePercent)}%",
                            caption = "Projected impact on churn"
                        )
                    }
                }

                // Result Hero Card
                val projected = PricingSimulationEngine.simulate(profile!!, assumptions)
                val mrrDelta = if (profile!!.currentMrr > BigDecimal.ZERO) {
                    ((projected.currentMrr - profile!!.currentMrr) / profile!!.currentMrr).toDouble() * 100
                } else 0.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(RevLensTheme.colors.surfaceElevated)
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("New MRR (12 mo)", style = RevLensTypography.bodyLarge, color = RevLensTheme.colors.textSecondary)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                currencyFormatter.format(projected.currentMrr),
                                style = RevLensTypography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            
                            val deltaType = when {
                                mrrDelta > 0 -> DeltaType.POSITIVE
                                mrrDelta < 0 -> DeltaType.NEGATIVE
                                else -> DeltaType.NEUTRAL
                            }
                            
                            val sign = if (mrrDelta > 0) "+" else ""
                            DeltaChip(
                                delta = "$sign${String.format(Locale.US, "%.1f", mrrDelta)}%",
                                type = deltaType
                            )
                        }

                        // Chart Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RevLensTheme.colors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Before / After Bars", color = RevLensTheme.colors.textSecondary)
                        }
                    }
                }

                PrimaryButton(
                    text = "Save as Scenario",
                    onClick = { showSaveDialog = true }
                )
            } else {
                Text("No profile data available. Please set up your business baseline.", color = RevLensTheme.colors.textSecondary)
            }
        }
        
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = {
                    Text(
                        "Save Scenario", style = RevLensTypography.titleLarge,
                        color = RevLensTheme.colors.textPrimary
                    )
                },
                text = {
                    TextField(
                        value = scenarioName,
                        onValueChange = { scenarioName = it },
                        label = { Text("Scenario Name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        profile?.let { p ->
                            viewModel.saveScenario(
                                com.example.revlens.model.Scenario(
                                    name = scenarioName.ifEmpty { "New Pricing Scenario" },
                                    baselineProfile = p,
                                    assumptions = assumptions
                                )
                            )
                        }
                        showSaveDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
