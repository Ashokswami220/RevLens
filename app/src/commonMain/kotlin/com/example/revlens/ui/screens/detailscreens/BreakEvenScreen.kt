package com.example.revlens.ui.screens.detailscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.calc.BreakEvenCalculator
import com.example.revlens.model.BigDecimal
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.ChartLegend
import com.example.revlens.ui.components.LabeledSlider
import com.example.revlens.ui.components.RevLensLineChart
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import com.example.revlens.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakEvenScreen(viewModel: MainViewModel) {
    val profile by viewModel.profile.collectAsState()

    var cacSimulated by remember { mutableStateOf(profile?.cac?.toFloat() ?: 100f) }
    var fixedCostsSimulated by remember { mutableStateOf(profile?.fixedCosts?.toFloat() ?: 5000f) }
    var variableCostSimulated by remember {
        mutableStateOf(
            profile?.variableCostPerCustomer?.toFloat() ?: 10f
        )
    }

    // If profile changes, we might want to update initial sliders, but for simplicity we rely on initial value.

    val simulatedProfile by remember(
        profile, cacSimulated, fixedCostsSimulated, variableCostSimulated
    ) {
        derivedStateOf {
            profile?.copy(
                cac = BigDecimal(cacSimulated.toString()),
                fixedCosts = BigDecimal(fixedCostsSimulated.toString()),
                variableCostPerCustomer = BigDecimal(variableCostSimulated.toString())
            )
        }
    }

    val cacPayback = remember(simulatedProfile) {
        simulatedProfile?.let { BreakEvenCalculator.calculateCacPaybackPeriod(it) }
    }

    val breakEvenCustomers = remember(simulatedProfile) {
        simulatedProfile?.let { BreakEvenCalculator.calculateBreakEvenCustomers(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segmented Tabs


        SectionCard(title = "Break-Even Results") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val breakEven = breakEvenCustomers ?: 0
                val maxCustomers = if (breakEven > 0) breakEven * 2 else 100

                val revenueData = mutableListOf<Float>()
                val costData = mutableListOf<Float>()

                simulatedProfile?.let { p ->
                    val arpu =
                        p.currentMrr.toFloat() / (if (p.totalCustomers > 0) p.totalCustomers else 1)
                    for (i in 0..10) {
                        val customers = (maxCustomers * i / 10f).toInt()
                        val revenue = arpu * customers
                        val costs =
                            p.fixedCosts.toFloat() + (p.variableCostPerCustomer.toFloat() * customers)
                        revenueData.add(revenue)
                        costData.add(costs)
                    }
                }

                if (revenueData.isNotEmpty()) {
                    val customerLabels = remember(maxCustomers) {
                        (0..10).map { "${(maxCustomers * it / 10f).toInt()}" }
                    }

                    RevLensLineChart(
                        data = listOf(revenueData, costData),
                        colors = listOf(
                            RevLensTheme.colors.brandPrimary, RevLensTheme.colors.error
                        ),
                        xLabels = customerLabels,
                        chartHeight = 150.dp
                    )
                    ChartLegend(
                        items = listOf(
                            "Revenue" to RevLensTheme.colors.brandPrimary,
                            "Costs" to RevLensTheme.colors.error
                        )
                    )
                }

                val paybackStr =
                    cacPayback?.let { "${CurrencyUtils.formatDecimal(it, 1)} months" } ?: "Never"
                val customersStr = breakEvenCustomers?.let { "$it customers" } ?: "Never"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResultCard(
                        label = "CAC Payback Period",
                        value = paybackStr,
                        modifier = Modifier.weight(1f)
                    )
                    ResultCard(
                        label = "Break-Even Customers",
                        value = customersStr,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        SectionCard(title = "Simulate Costs") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LabeledSlider(
                    label = "CAC",
                    value = cacSimulated,
                    onValueChange = { cacSimulated = it },
                    valueRange = 0f..2000f,
                    valueString = "$${cacSimulated.toInt()}",
                    caption = "Cost to acquire a single customer"
                )

                LabeledSlider(
                    label = "Fixed Costs",
                    value = fixedCostsSimulated,
                    onValueChange = { fixedCostsSimulated = it },
                    valueRange = 0f..50000f,
                    valueString = "$${fixedCostsSimulated.toInt()}",
                    caption = "Monthly base expenses"
                )

                LabeledSlider(
                    label = "Variable Cost",
                    value = variableCostSimulated,
                    onValueChange = { variableCostSimulated = it },
                    valueRange = 0f..500f,
                    valueString = "$${variableCostSimulated.toInt()}",
                    caption = "Cost per customer"
                )
            }
        }


    }
}

@Composable
private fun ResultCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surfaceElevated.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = RevLensTypography.labelMedium,
            color = RevLensTheme.colors.textTertiary
        )
        Text(
            text = value,
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )
    }
}
