package com.revlens.app.ui.screens.navbarscreens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.domain.calc.ForecastEngine
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.RoundingMode
import com.revlens.app.presentation.MainViewModel
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.ChartLegend
import com.revlens.app.ui.components.RevLensLineChart
import com.revlens.app.ui.components.RevLensTopAppBar
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.SegmentedTabs
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import com.revlens.app.utils.CurrencyUtils
import com.revlens.app.utils.DateUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToGoalPlanner: () -> Unit,
    onNavigateToMetrics: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    var selectedMonths by rememberSaveable { mutableStateOf(12) }
    var isBreakdownExpanded by rememberSaveable { mutableStateOf(false) }

    val currencyFormatter = remember(profile?.currencyCode) {
        CurrencyUtils.getFormatter(profile?.currencyCode)
    }

    val forecastData = remember(profile, selectedMonths) {
        profile?.let { ForecastEngine.forecast(it, selectedMonths) } ?: emptyList()
    }

    // Dynamic Calendar Month Labels (e.g. "Nov 2026", "Dec 2026")
    val monthLabels = remember(selectedMonths) {
        DateUtils.getForecastMonthLabels(selectedMonths)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "Revenue Forecast",
                onBackClick = null,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sleek Segmented Duration Switcher
            val durationOptions = listOf("6 Months", "12 Months", "24 Months")
            val durationValues = listOf(6, 12, 24)
            val selectedIndex = durationValues.indexOf(selectedMonths)
                .coerceAtLeast(0)

            SegmentedTabs(
                options = durationOptions,
                selectedIndex = selectedIndex,
                onOptionSelected = { idx -> selectedMonths = durationValues[idx] }
            )

            // 2. Executive End of Period Summary Cards (2-Column KPI Grid)
            if (forecastData.isNotEmpty()) {
                val finalMonth = forecastData.last()
                val currentMrr = profile?.currentMrr ?: BigDecimal.ZERO
                val mrrGrowthPct = if (currentMrr > BigDecimal.ZERO) {
                    (finalMonth.mrr - currentMrr)
                        .divide(currentMrr, 4, RoundingMode.HALF_UP)
                        .toDouble() * 100
                } else 0.0
                val netCustomers = finalMonth.customers - (profile?.totalCustomers ?: 0)

                SectionCard(title = "End of Period Summary (${selectedMonths}M)") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ForecastKpiTile(
                                label = "PROJECTED MRR",
                                value = currencyFormatter.format(finalMonth.mrr),
                                badge = "+${
                                    CurrencyUtils.formatPercent(
                                        mrrGrowthPct, isFraction = false, maxFractionDigits = 1
                                    )
                                }",
                                badgeColor = RevLensTheme.colors.success,
                                modifier = Modifier.weight(1f)
                            )
                            ForecastKpiTile(
                                label = "TOTAL SUBSCRIBERS",
                                value = "${finalMonth.customers}",
                                badge = "+$netCustomers net",
                                badgeColor = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ForecastKpiTile(
                                label = "CUMULATIVE REVENUE",
                                value = currencyFormatter.format(finalMonth.cumulativeRevenue),
                                badge = "${selectedMonths}M Total",
                                badgeColor = RevLensTheme.colors.textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            ForecastKpiTile(
                                label = "ARR RUN-RATE",
                                value = currencyFormatter.format(
                                    finalMonth.mrr.multiply(BigDecimal(12))
                                ),
                                badge = "Annualized",
                                badgeColor = RevLensTheme.colors.brandPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Forecast Trajectory Chart
            if (forecastData.isNotEmpty()) {
                SectionCard(title = "MRR Trajectory ($selectedMonths Months)") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val baseMrrData = forecastData.map { it.mrr.toFloat() }
                        val optimisticData =
                            baseMrrData.mapIndexed { index, mrr -> mrr * (1f + (index * 0.015f)) }
                        val conservativeData =
                            baseMrrData.mapIndexed { index, mrr -> mrr * (1f - (index * 0.01f)) }

                        RevLensLineChart(
                            data = listOf(optimisticData, baseMrrData, conservativeData),
                            colors = listOf(
                                RevLensTheme.colors.brandTertiary,
                                RevLensTheme.colors.brandPrimary,
                                RevLensTheme.colors.brandSecondary
                            ),
                            dashedIndices = listOf(0, 2),
                            xLabels = monthLabels.map { it.second },
                            chartHeight = 160.dp
                        )

                        ChartLegend(
                            items = listOf(
                                "Optimistic (+1.5%/mo)" to RevLensTheme.colors.brandTertiary,
                                "Baseline" to RevLensTheme.colors.brandPrimary,
                                "Conservative (-1.0%/mo)" to RevLensTheme.colors.brandSecondary
                            )
                        )
                    }
                }
            }

            // 4. Set MRR Goal Action Card Button
            ActionCardButton(
                text = "Set MRR Goal",
                icon = Icons.Outlined.TrackChanges,
                onClick = onNavigateToGoalPlanner
            )

            // 5. Rich Month-by-Month Dynamic Breakdown
            if (forecastData.isNotEmpty()) {
                val displayList = if (isBreakdownExpanded || forecastData.size <= 6) {
                    forecastData
                } else {
                    forecastData.take(6)
                }

                SectionCard(title = "Month-by-Month Breakdown") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Header labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PERIOD",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textTertiary,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                text = "PROJECTED MRR",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textTertiary,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "SUBSCRIBERS",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textTertiary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )
                        }

                        HorizontalDivider(
                            color = RevLensTheme.colors.borderDefault.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        // Data rows
                        displayList.forEachIndexed { index, data ->
                            val monthName = monthLabels.getOrNull(data.monthIndex)?.first
                                ?: "Month ${data.monthIndex}"
                            val prevMonth = if (data.monthIndex > 0) forecastData.getOrNull(
                                data.monthIndex - 1
                            ) else null
                            val monthlyGain = if (prevMonth != null) data.mrr.subtract(
                                prevMonth.mrr
                            ) else BigDecimal.ZERO
                            val netNewUsers =
                                if (prevMonth != null) data.customers - prevMonth.customers else 0

                            ForecastRowItem(
                                monthTitle = monthName,
                                monthSubtitle = if (data.monthIndex == 0) "Baseline" else "Month ${data.monthIndex}",
                                mrrFormatted = currencyFormatter.format(data.mrr),
                                mrrGainFormatted = if (data.monthIndex > 0) "+${
                                    currencyFormatter.format(
                                        monthlyGain
                                    )
                                }" else "Current",
                                usersFormatted = "${data.customers} users",
                                usersGainFormatted = if (data.monthIndex > 0) "+$netNewUsers net" else "Active base",
                                isCurrent = data.monthIndex == 0
                            )

                            if (index < displayList.lastIndex) {
                                HorizontalDivider(
                                    color = RevLensTheme.colors.borderDefault.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                            }
                        }

                        // Expand / Collapse button if over 6 months
                        if (forecastData.size > 6) {
                            TextButton(
                                onClick = { isBreakdownExpanded = !isBreakdownExpanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isBreakdownExpanded) {
                                            "Show Less (First 6 Months)"
                                        } else {
                                            "Show All ${forecastData.size} Months"
                                        },
                                        style = RevLensTypography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RevLensTheme.colors.brandPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isBreakdownExpanded) {
                                            Icons.Filled.KeyboardArrowUp
                                        } else {
                                            Icons.Filled.KeyboardArrowDown
                                        },
                                        contentDescription = null,
                                        tint = RevLensTheme.colors.brandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastKpiTile(
    label: String,
    value: String,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surfaceElevated.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = RevLensTypography.labelSmall,
                color = RevLensTheme.colors.textTertiary,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badge,
                style = RevLensTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        Text(
            text = value,
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun ForecastRowItem(
    monthTitle: String,
    monthSubtitle: String,
    mrrFormatted: String,
    mrrGainFormatted: String,
    usersFormatted: String,
    usersGainFormatted: String,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column 1: Period
        Column(modifier = Modifier.weight(1.1f)) {
            Text(
                text = monthTitle,
                style = RevLensTypography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary
            )
            Text(
                text = monthSubtitle,
                style = RevLensTypography.labelSmall,
                color = RevLensTheme.colors.textTertiary
            )
        }

        // Column 2: Projected MRR
        Column(modifier = Modifier.weight(1.3f)) {
            Text(
                text = mrrFormatted,
                style = RevLensTypography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = mrrGainFormatted,
                style = RevLensTypography.labelSmall,
                color = if (isCurrent) RevLensTheme.colors.textTertiary else RevLensTheme.colors.success,
                fontWeight = if (isCurrent) FontWeight.Normal else FontWeight.SemiBold
            )
        }

        // Column 3: Subscribers
        Column(
            modifier = Modifier.weight(1.1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = usersFormatted,
                style = RevLensTypography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = RevLensTheme.colors.textPrimary,
                textAlign = TextAlign.End
            )
            Text(
                text = usersGainFormatted,
                style = RevLensTypography.labelSmall,
                color = if (isCurrent) RevLensTheme.colors.textTertiary else RevLensTheme.colors.brandPrimary,
                textAlign = TextAlign.End
            )
        }
    }
}
