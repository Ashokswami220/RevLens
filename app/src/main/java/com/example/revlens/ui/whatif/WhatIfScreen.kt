package com.example.revlens.ui.whatif

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.PricingSimulationEngine
import com.example.revlens.domain.calc.WhatIfEngine
import com.example.revlens.model.Assumptions
import com.example.revlens.model.BusinessProfile
import com.example.revlens.model.MetricResult
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.LabeledSlider
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatIfScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    
    // Sliders state
    var priceChangePercent by remember { mutableStateOf(0f) }
    var churnChangePercent by remember { mutableStateOf(0f) }
    var growthChangePercent by remember { mutableStateOf(0f) }
    var cacChange by remember { mutableStateOf(0f) }
    
    val assumptions by remember {
        derivedStateOf {
            Assumptions(
                priceChangePercent = priceChangePercent.toDouble() / 100.0,
                churnChangePercent = churnChangePercent.toDouble() / 100.0,
                growthChangePercent = growthChangePercent.toDouble() / 100.0,
                cacChange = BigDecimal(cacChange.toString()),
                fixedCostChange = BigDecimal.ZERO // Simplified for MVP sliders
            )
        }
    }
    
    val results = remember(profile, assumptions) {
        profile?.let { p ->
            val projected = PricingSimulationEngine.simulate(p, assumptions)
            WhatIfEngine.compare(p, projected)
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            RevLensTopAppBar(
                title = "What-If Analysis",
                onBackClick = onNavigateBack
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Impact Section
            SectionCard(title = "Live Impact") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    results.forEach { result ->
                        MetricResultRow(result)
                    }
                }
            }

            // Adjustments Section
            SectionCard(title = "Assumptions") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledSlider(
                        label = "Price Change",
                        value = priceChangePercent,
                        onValueChange = { priceChangePercent = it },
                        valueRange = -50f..100f,
                        valueString = "${priceChangePercent.toInt()}%",
                        caption = "Adjust pricing across all plans"
                    )
                    
                    LabeledSlider(
                        label = "Churn Change",
                        value = churnChangePercent,
                        onValueChange = { churnChangePercent = it },
                        valueRange = -10f..10f,
                        valueString = "${churnChangePercent.toInt()}%",
                        caption = "Absolute change in churn rate"
                    )
                    
                    LabeledSlider(
                        label = "Growth Change",
                        value = growthChangePercent,
                        onValueChange = { growthChangePercent = it },
                        valueRange = -20f..50f,
                        valueString = "${growthChangePercent.toInt()}%",
                        caption = "Absolute change in MRR growth rate"
                    )
                    
                    LabeledSlider(
                        label = "CAC Change",
                        value = cacChange,
                        onValueChange = { cacChange = it },
                        valueRange = -500f..500f,
                        valueString = "$${cacChange.toInt()}",
                        caption = "Absolute dollar change in CAC"
                    )
                }
            }
        }
    }
}

@Composable
fun MetricResultRow(result: MetricResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = result.label,
                style = RevLensTypography.bodyLarge,
                color = RevLensTheme.colors.textSecondary
            )
            Text(
                text = result.formattedValue,
                style = RevLensTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
        }
        
        if (result.deltaPercent != null && result.deltaPercent != 0.0) {
            val isPositiveIndicator = result.isPositive
            val color = if (isPositiveIndicator) RevLensTheme.colors.success else RevLensTheme.colors.error
            val sign = if (result.deltaPercent > 0) "+" else ""
            Text(
                text = "$sign${String.format(Locale.US, "%.1f", result.deltaPercent)}%",
                style = RevLensTypography.bodyLarge,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
