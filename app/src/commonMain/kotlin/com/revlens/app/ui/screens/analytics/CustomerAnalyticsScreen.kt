package com.revlens.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.AnalyticsPeriod
import com.revlens.app.domain.calc.ConcentrationHealth
import com.revlens.app.domain.calc.CustomerRecordItem
import com.revlens.app.domain.calc.CustomerSegmentBy
import com.revlens.app.domain.calc.CustomerSegmentShare
import com.revlens.app.ui.components.ChartLegend
import com.revlens.app.ui.components.FilterChip
import com.revlens.app.ui.components.RevLensLineChart
import com.revlens.app.ui.components.SectionCard
import com.revlens.app.ui.components.charts.RevLensStackedBarChart
import com.revlens.app.ui.components.charts.StackedBarItem
import com.revlens.app.ui.components.display.DataSourceBadge
import com.revlens.app.ui.components.display.StatusBadge
import com.revlens.app.ui.components.display.StatusBadgeType
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAnalyticsScreen(
    viewModel: CustomerAnalyticsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDataImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CustomerAnalyticsEffect.NavigateToDataImport -> onNavigateToDataImport()
                is CustomerAnalyticsEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val result = state.result

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RevLensTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Customers",
                            style = RevLensTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = "Counts, growth, segments & concentration",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RevLensTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    if (result != null) {
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            DataSourceBadge(mode = result.sourceMode)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RevLensTheme.colors.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsPeriod.entries.forEach { p ->
                    FilterChip(
                        text = p.label,
                        selected = state.period == p,
                        onClick = { viewModel.onEvent(CustomerAnalyticsEvent.SelectPeriod(p)) }
                    )
                }
            }

            if (result != null) {
                // 2x2 KPI Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            label = "Active Customers",
                            value = formatCount(result.activeCustomers),
                            icon = Icons.Default.People,
                            tint = RevLensTheme.colors.brandPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        KpiSummaryCard(
                            label = "New Added",
                            value = "+${formatCount(result.newCustomers)}",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            tint = RevLensTheme.colors.success,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            label = "Churned",
                            value = "-${formatCount(result.churnedCustomers)}",
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            tint = RevLensTheme.colors.error,
                            modifier = Modifier.weight(1f)
                        )
                        val netSign = if (result.netCustomers >= 0) "+" else ""
                        KpiSummaryCard(
                            label = "Net Growth",
                            value = "$netSign${formatCount(result.netCustomers)}",
                            icon = Icons.Default.Person,
                            tint = if (result.netCustomers >= 0) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Customer Growth Chart
                SectionCard(title = "Customer Growth Trend") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartLegend(
                            items = listOf(
                                "Active Total" to RevLensTheme.colors.brandPrimary,
                                "New" to RevLensTheme.colors.success,
                                "Churned" to RevLensTheme.colors.error
                            )
                        )

                        val activeSeries = result.growthTimeline.map { it.activeCustomers.toFloat() }
                        val xLabels = result.growthTimeline.map { it.monthLabel }

                        RevLensLineChart(
                            data = listOf(activeSeries),
                            colors = listOf(RevLensTheme.colors.brandPrimary),
                            xLabels = xLabels,
                            chartHeight = 150.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Stacked Bar for New vs Churned
                        val stackedItems = result.growthTimeline.map { pt ->
                            StackedBarItem(
                                label = pt.monthLabel,
                                segmentValues = listOf(pt.newCustomers.toFloat(), pt.churnedCustomers.toFloat())
                            )
                        }

                        RevLensStackedBarChart(
                            items = stackedItems,
                            segmentColors = listOf(RevLensTheme.colors.success, RevLensTheme.colors.error),
                            chartHeight = 120.dp
                        )
                    }
                }

                // Segmentation Section
                SectionCard(
                    title = "Customer Segmentation",
                    trailingAction = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CustomerSegmentBy.entries.forEach { seg ->
                                FilterChip(
                                    text = seg.name.lowercase().replaceFirstChar { it.uppercase() },
                                    selected = state.segmentBy == seg,
                                    onClick = { viewModel.onEvent(CustomerAnalyticsEvent.SelectSegmentBy(seg)) }
                                )
                            }
                        }
                    }
                ) {
                    if (result.isChannelOrCountryAvailable && result.segments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            result.segments.forEach { item ->
                                SegmentRow(item = item, currencySymbol = state.currencySymbol)
                            }
                        }
                    } else {
                        // Locked Section for Modeled Mode
                        LockedAnalyticsCard(
                            message = "Import custom CSV or connect an integration to break down customers by acquisition channel and geographic country.",
                            onImportClick = { viewModel.onEvent(CustomerAnalyticsEvent.ImportDataClicked) }
                        )
                    }
                }

                // MRR Concentration Card
                SectionCard(title = "MRR Concentration") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Top 10% of Customers",
                                    style = RevLensTypography.labelMedium,
                                    color = RevLensTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${result.concentrationTop10PercentMrrShare}% of MRR",
                                    style = RevLensTypography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textPrimary
                                )
                            }

                            when (result.concentrationBenchmark) {
                                ConcentrationHealth.HEALTHY -> {
                                    StatusBadge(type = StatusBadgeType.ACTIVE, customText = "Healthy (<30%)")
                                }
                                ConcentrationHealth.MODERATE -> {
                                    StatusBadge(type = StatusBadgeType.WARNING, customText = "Moderate (30-50%)")
                                }
                                ConcentrationHealth.HIGH_RISK -> {
                                    StatusBadge(type = StatusBadgeType.FAILED, customText = "High Risk (>50%)")
                                }
                            }
                        }

                        Text(
                            text = if (result.concentrationBenchmark == ConcentrationHealth.HEALTHY) {
                                "Great revenue distribution. No single customer group dominates your MRR, mitigating enterprise churn risk."
                            } else {
                                "Elevated concentration. A small cohort of high-value accounts drives over half of your MRR — consider expanding mid-tier customer acquisition."
                            },
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }

                // Top Customers List (Masked)
                SectionCard(title = "Top Customers (Masked)") {
                    if (result.topCustomers.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            result.topCustomers.forEach { customer ->
                                CustomerRowItem(
                                    customer = customer,
                                    currencySymbol = state.currencySymbol,
                                    onClick = { viewModel.onEvent(CustomerAnalyticsEvent.SelectCustomer(customer)) }
                                )
                            }
                        }
                    } else {
                        LockedAnalyticsCard(
                            message = "Account-level contract tracking requires imported subscription data.",
                            onImportClick = { viewModel.onEvent(CustomerAnalyticsEvent.ImportDataClicked) }
                        )
                    }
                }

                // Recently Downgraded / Churned (Actual only)
                if (result.recentlyDowngraded.isNotEmpty()) {
                    SectionCard(title = "Recently Downgraded / Churned") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            result.recentlyDowngraded.forEach { customer ->
                                CustomerRowItem(
                                    customer = customer,
                                    currencySymbol = state.currencySymbol,
                                    onClick = { viewModel.onEvent(CustomerAnalyticsEvent.SelectCustomer(customer)) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Customer Detail Sheet
    state.selectedCustomer?.let { customer ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(CustomerAnalyticsEvent.DismissCustomerSheet) },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            CustomerDetailSheet(
                customer = customer,
                currencySymbol = state.currencySymbol,
                onDismiss = { viewModel.onEvent(CustomerAnalyticsEvent.DismissCustomerSheet) }
            )
        }
    }
}

@Composable
private fun KpiSummaryCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.textSecondary
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = RevLensTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )
    }
}

@Composable
private fun SegmentRow(
    item: CustomerSegmentShare,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = RevLensTypography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = "${item.customerCount} · ${item.percentShare}%",
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.textSecondary
            )
        }

        LinearProgressIndicator(
            progress = { (item.percentShare / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = RevLensTheme.colors.brandPrimary,
            trackColor = RevLensTheme.colors.borderDefault
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MRR: $currencySymbol${item.totalMrrMinor / 100}",
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )
            Text(
                text = "Churn: ${item.churnRatePercent}%",
                style = RevLensTypography.bodySmall,
                color = if (item.churnRatePercent > 5.0) RevLensTheme.colors.error else RevLensTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun CustomerRowItem(
    customer: CustomerRecordItem,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = customer.displayName,
                style = RevLensTypography.titleSmall,
                color = RevLensTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${customer.planName} · Joined ${customer.startDate}",
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$currencySymbol${customer.mrrMinor / 100} / mo",
                style = RevLensTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.brandPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = RevLensTheme.colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LockedAnalyticsCard(
    message: String,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = RevLensTheme.colors.textTertiary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = message,
            style = RevLensTypography.bodySmall,
            color = RevLensTheme.colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            onClick = onImportClick,
            colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.primaryAction),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Import Data", style = RevLensTypography.labelMedium)
        }
    }
}

@Composable
private fun CustomerDetailSheet(
    customer: CustomerRecordItem,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = customer.displayName,
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = "ID: ${customer.id}",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textTertiary
                )
            }
            if (customer.isActive) {
                StatusBadge(type = StatusBadgeType.ACTIVE, customText = "Active")
            } else {
                StatusBadge(type = StatusBadgeType.FAILED, customText = "Churned")
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = RevLensTheme.colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(label = "Current Plan", value = customer.planName)
                DetailRow(label = "Monthly MRR", value = "$currencySymbol${customer.mrrMinor / 100} / mo")
                DetailRow(label = "Acquisition Channel", value = customer.channel ?: "Direct / Organic")
                DetailRow(label = "Country", value = customer.country ?: "Global")
                DetailRow(label = "Subscribed Since", value = customer.startDate)
                if (customer.endDate != null) {
                    DetailRow(label = "Churned On", value = customer.endDate)
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RevLensTheme.colors.surfaceElevated)
        ) {
            Text(text = "Close", color = RevLensTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = RevLensTypography.bodyMedium,
            color = RevLensTheme.colors.textSecondary
        )
        Text(
            text = value,
            style = RevLensTypography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = RevLensTheme.colors.textPrimary
        )
    }
}

private fun formatCount(number: Int): String {
    val s = kotlin.math.abs(number).toString()
    val sb = StringBuilder()
    val len = s.length
    for (i in 0 until len) {
        if (i > 0 && (len - i) % 3 == 0) {
            sb.append(',')
        }
        sb.append(s[i])
    }
    return if (number < 0) "-$sb" else sb.toString()
}
