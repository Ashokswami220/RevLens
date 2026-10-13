package com.example.revlens.ui.screens.navbarscreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.revlens.domain.calc.LtvCalculator
import com.example.revlens.model.BusinessProfile
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.ProgressRing
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

// --- COLOR PALETTE TOKENS ---
private val BgDark = Color(0xFF0E131C)
private val CardBg = Color(0xFF1C2028)
private val CardBorder = Color(0xFF262F40)
private val CyanAccent = Color(0xFF06B6D4)
private val EmeraldAccent = Color(0xFF10B981)
private val CoralAccent = Color(0xFFF43F5E)
private val TextWhite = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)
private val TextSubtle = Color(0xFF64748B)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToInput: () -> Unit,
    onNavigateToWhatIf: () -> Unit,
    onNavigateToBreakEven: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMetrics: () -> Unit,
    onNavigateToGoalPlanner: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RevLensTopAppBar(
                title = "RevLens",
                actions = {
                    IconButton(onClick = onNavigateToMetrics) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Metrics",
                            tint = CyanAccent
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .width(1.dp)
                            .background(CardBorder)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onNavigateToInput,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Data",
                            tint = CyanAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Edit Data",
                            color = CyanAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            profile?.let { currentProfile ->
                DashboardContent(
                    profile = currentProfile,
                    onNavigateToWhatIf = onNavigateToWhatIf,
                    onNavigateToBreakEven = onNavigateToBreakEven,
                    onNavigateToGoalPlanner = onNavigateToGoalPlanner
                )
            } ?: run {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to RevLens!",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Add your business metrics and pricing plans to get started.",
                        style = RevLensTypography.bodyLarge,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryButton(
                        text = "Add Data",
                        onClick = onNavigateToInput,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    profile: BusinessProfile,
    onNavigateToWhatIf: () -> Unit,
    onNavigateToBreakEven: () -> Unit,
    onNavigateToGoalPlanner: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 0
        }
    }

    // 1. Top Compact Simulator & Break-Even Action Buttons
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onNavigateToWhatIf,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CardBg,
                contentColor = TextWhite
            )
        ) {
            Text("What-If", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onNavigateToBreakEven,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CardBg,
                contentColor = TextWhite
            )
        ) {
            Text("Break-Even", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    // 2. Hero MRR & ARR Card (Borderless, Rounded 16.dp)
    HeroRevenueCard(profile = profile, currencyFormatter = currencyFormatter)

    // 3. Growth Optimization Banner (Borderless, Rounded 16.dp)
    GrowthOptimizationCard(
        profile = profile,
        currencyFormatter = currencyFormatter,
        onSimulate = onNavigateToWhatIf
    )

    // 4. Metric Grid 2x2 (Customers, ARPU, User Churn, LTV)
    val customersGrowth = (profile.totalCustomers * profile.monthlyGrowthRate).toInt()
    val growthPercent = (profile.monthlyGrowthRate * 100).toInt()

    val ltv = LtvCalculator.calculateLtv(profile.arpu, profile.monthlyChurnRate, profile.variableCostPerCustomer)
    val ltvToCac = if (profile.cac > BigDecimal.ZERO) {
        ltv.toDouble() / profile.cac.toDouble()
    } else 0.0
    val ltvBadge = if (ltvToCac > 0) "${String.format(Locale.US, "%.1f", ltvToCac)}x CAC" else "Healthy"
    val lifetimeMonths = if (profile.monthlyChurnRate > 0) {
        "${(1.0 / profile.monthlyChurnRate).toInt()} mo"
    } else {
        "60+ mo"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricKpiCard(
            title = "CUSTOMERS",
            value = profile.totalCustomers.toString(),
            subValue = "+$customersGrowth net",
            badge = "+$growthPercent%",
            badgeColor = EmeraldAccent,
            sparklineType = SparklineType.UPWARD_GREEN,
            modifier = Modifier.weight(1f)
        )

        MetricKpiCard(
            title = "ARPU",
            value = currencyFormatter.format(profile.arpu),
            subValue = "/ sub",
            badge = if (profile.plans.size > 1) "Blended" else "Single Tier",
            badgeColor = CyanAccent,
            sparklineType = SparklineType.UPWARD_CYAN,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricKpiCard(
            title = "USER CHURN",
            value = "${String.format(Locale.US, "%.1f", profile.monthlyChurnRate * 100)}%",
            subValue = "benchmark <3%",
            badge = if (profile.monthlyChurnRate <= 0.03) "Low" else "${String.format(Locale.US, "%.1f", profile.monthlyChurnRate * 100)}%",
            badgeColor = EmeraldAccent,
            sparklineType = SparklineType.DOWNWARD_GREEN,
            modifier = Modifier.weight(1f)
        )

        MetricKpiCard(
            title = "LTV",
            value = currencyFormatter.format(ltv),
            subValue = lifetimeMonths,
            badge = ltvBadge,
            badgeColor = if (ltvToCac >= 3.0 || ltvToCac == 0.0) EmeraldAccent else CyanAccent,
            sparklineType = SparklineType.UPWARD_GREEN,
            modifier = Modifier.weight(1f)
        )
    }

    // 5. MRR Trajectory Chart Card (Borderless, Rounded 16.dp)
    MrrTrajectoryChartCard(profile = profile, currencyFormatter = currencyFormatter)

    // 6. Customer Count Trend Card (Borderless, Rounded 16.dp)
    CustomerCountTrendCard(profile = profile)

    // 7. Annual MRR Goal Tracker (Borderless, Rounded 16.dp)
    GoalTrackerCard(
        profile = profile,
        currencyFormatter = currencyFormatter,
        onNavigateToGoalPlanner = onNavigateToGoalPlanner
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun HeroRevenueCard(
    profile: BusinessProfile,
    currencyFormatter: NumberFormat
) {
    val mrr = profile.currentMrr
    val arr = mrr.multiply(BigDecimal(12))
    val netNew = mrr.multiply(BigDecimal.valueOf(profile.monthlyGrowthRate))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MONTHLY RECURRING REVENUE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .background(EmeraldAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "↗ +${String.format(Locale.US, "%.1f", profile.monthlyGrowthRate * 100)}%",
                        color = EmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currencyFormatter.format(mrr),
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "USD / mo",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANNUALIZED ARR: ${currencyFormatter.format(arr)}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "+${currencyFormatter.format(netNew)} net new",
                    color = EmeraldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dynamically computed segment weights from profile
            val churnWeight = (profile.monthlyChurnRate).coerceIn(0.02, 0.35)
            val newWeight = (profile.monthlyGrowthRate).coerceIn(0.04, 0.45)
            val expWeight = (1.0 - churnWeight - newWeight).coerceAtLeast(0.20)
            val totalWeight = expWeight + newWeight + churnWeight

            val fracExp = (expWeight / totalWeight).toFloat()
            val fracNew = (newWeight / totalWeight).toFloat()
            val fracChurn = (churnWeight / totalWeight).toFloat()

            val pctExp = (fracExp * 100).toInt()
            val pctNew = (fracNew * 100).toInt()
            val pctChurn = 100 - pctExp - pctNew

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(fracExp.coerceAtLeast(0.05f))
                        .background(CyanAccent)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(fracNew.coerceAtLeast(0.05f))
                        .background(EmeraldAccent)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(fracChurn.coerceAtLeast(0.05f))
                        .background(CoralAccent)
                )
            }

            // Bar Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = CyanAccent, label = "Exp. $pctExp%")
                LegendItem(color = EmeraldAccent, label = "New $pctNew%")
                LegendItem(color = CoralAccent, label = "Churn $pctChurn%")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun GrowthOptimizationCard(
    profile: BusinessProfile,
    currencyFormatter: NumberFormat,
    onSimulate: () -> Unit
) {
    val potentialGrowth = BigDecimal.valueOf(5L * profile.totalCustomers.coerceAtLeast(1))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column {
                    Text(
                        text = "Growth Optimization",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+$5 ARPU adds +${currencyFormatter.format(potentialGrowth)} MRR at current volume",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF222B3A))
                    .clickable(onClick = onSimulate)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Simulate ↗",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

enum class SparklineType { UPWARD_GREEN, UPWARD_CYAN, DOWNWARD_GREEN }

@Composable
private fun MetricKpiCard(
    title: String,
    value: String,
    subValue: String,
    badge: String,
    badgeColor: Color,
    sparklineType: SparklineType,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subValue,
                    color = TextSubtle,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // Custom Canvas Sparkline
            val sparkColor = when (sparklineType) {
                SparklineType.UPWARD_CYAN -> CyanAccent
                else -> EmeraldAccent
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                val path = Path()
                val w = size.width
                val h = size.height

                when (sparklineType) {
                    SparklineType.UPWARD_GREEN, SparklineType.UPWARD_CYAN -> {
                        path.moveTo(0f, h * 0.85f)
                        path.cubicTo(w * 0.35f, h * 0.80f, w * 0.65f, h * 0.55f, w, h * 0.15f)
                    }
                    SparklineType.DOWNWARD_GREEN -> {
                        path.moveTo(0f, h * 0.25f)
                        path.cubicTo(w * 0.35f, h * 0.30f, w * 0.65f, h * 0.70f, w, h * 0.80f)
                    }
                }

                // Fill gradient shadow / glow underneath the sparkline curve
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(sparkColor.copy(alpha = 0.28f), Color.Transparent)
                    )
                )

                // Stroke line
                drawPath(
                    path = path,
                    color = sparkColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun MrrTrajectoryChartCard(
    profile: BusinessProfile,
    currencyFormatter: NumberFormat
) {
    val mrr = profile.currentMrr
    val mrrFloat = mrr.toFloat()
    val gRate = profile.monthlyGrowthRate.toFloat()

    val trendData = remember(mrrFloat, gRate) {
        (5 downTo 0).map { monthsAgo ->
            if (monthsAgo == 0) {
                mrrFloat
            } else {
                (mrrFloat / Math.pow(1.0 + gRate.toDouble(), monthsAgo.toDouble())).toFloat()
            }
        }
    }

    val past6Months = remember {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("MMM", Locale.US)
        cal.add(Calendar.MONTH, -5)
        (0 until 6).mapIndexed { idx, _ ->
            val name = fmt.format(cal.time)
            cal.add(Calendar.MONTH, 1)
            if (idx == 5) "$name (Now)" else name
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "MRR Trajectory",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trailed 6-Month Realized Growth",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF142436), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "• ${currencyFormatter.format(trendData.first())} → ${currencyFormatter.format(mrr)}",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle horizontal gridlines
                    for (i in 1..3) {
                        val y = h * (i * 0.25f)
                        drawLine(
                            color = CardBorder.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Curved Path
                    val path = Path().apply {
                        moveTo(w * 0.05f, h * 0.85f)
                        cubicTo(
                            w * 0.35f, h * 0.78f,
                            w * 0.65f, h * 0.45f,
                            w * 0.95f, h * 0.20f
                        )
                    }

                    // Fill Gradient
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(w * 0.95f, h)
                        lineTo(w * 0.05f, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanAccent.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Stroke line
                    drawPath(
                        path = path,
                        color = CyanAccent,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Key milestone dots
                    val dots = listOf(
                        Offset(w * 0.05f, h * 0.85f),
                        Offset(w * 0.45f, h * 0.65f),
                        Offset(w * 0.75f, h * 0.38f),
                        Offset(w * 0.95f, h * 0.20f)
                    )
                    dots.forEach { pt ->
                        drawCircle(color = BgDark, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = CyanAccent, radius = 3.5.dp.toPx(), center = pt)
                    }
                }

                // Callout Tooltip Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp)
                        .background(Color(0xFF202C3D), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Today: ${currencyFormatter.format(mrr)}",
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Month X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                past6Months.forEach { m ->
                    val isCurrent = m.contains("(Now)")
                    Text(
                        text = m,
                        color = if (isCurrent) CyanAccent else TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerCountTrendCard(profile: BusinessProfile) {
    val total = profile.totalCustomers.coerceAtLeast(1)
    val gRate = profile.monthlyGrowthRate.coerceAtLeast(0.01)
    val subsM1 = (total / (1.0 + gRate)).toInt().coerceAtLeast(1)
    val subsM0 = (total / Math.pow(1.0 + gRate, 2.0)).toInt().coerceAtLeast(1)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GroupAdd,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Customer Count Trend",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "+${(profile.monthlyGrowthRate * 100).toInt()}% Trailing",
                    color = EmeraldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.8f)
                    cubicTo(w * 0.4f, h * 0.75f, w * 0.7f, h * 0.65f, w, h * 0.15f)
                }
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(EmeraldAccent.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                drawPath(
                    path = path,
                    color = EmeraldAccent,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(color = BgDark, radius = 5.dp.toPx(), center = Offset(w, h * 0.15f))
                drawCircle(color = EmeraldAccent, radius = 3.5.dp.toPx(), center = Offset(w, h * 0.15f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$subsM0 Subs (-2 Mo)", color = TextSubtle, fontSize = 10.sp)
                Text(text = "$subsM1 Subs (-1 Mo)", color = TextSubtle, fontSize = 10.sp)
                Text(text = "$total Subs (Now)", color = EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GoalTrackerCard(
    profile: BusinessProfile,
    currencyFormatter: NumberFormat,
    onNavigateToGoalPlanner: () -> Unit
) {
    val mrr = profile.currentMrr
    val targetGoal = profile.mrrGoal
    val hasGoal = targetGoal != null && targetGoal > BigDecimal.ZERO
    val rawProgress = if (hasGoal) {
        (mrr.toDouble() / targetGoal!!.toDouble()).toFloat()
    } else {
        0f
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANNUAL MRR GOAL",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF222B3A))
                        .clickable(onClick = onNavigateToGoalPlanner)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (hasGoal) "Edit Goal ↗" else "Set Goal ↗",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (hasGoal) {
                val percentInt = (rawProgress * 100).toInt()
                val goalText = "$percentInt% of ${currencyFormatter.format(targetGoal)} goal"
                val ringColor = when {
                    rawProgress >= 1f -> EmeraldAccent
                    rawProgress >= 0.5f -> CyanAccent
                    else -> Color(0xFF6366F1)
                }

                ProgressRing(
                    progress = rawProgress.coerceIn(0f, 1f),
                    goalText = goalText,
                    color = ringColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current: ${currencyFormatter.format(mrr)}",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target: ${currencyFormatter.format(targetGoal)}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    val remaining = targetGoal!!.subtract(mrr).coerceAtLeast(BigDecimal.ZERO)
                    Text(
                        text = "${currencyFormatter.format(remaining)} to go",
                        color = if (remaining > BigDecimal.ZERO) TextMuted else EmeraldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "No annual goal set yet. Plan your target MRR and timeline.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Button(
                    onClick = onNavigateToGoalPlanner,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Your MRR Goal", color = Color(0xFF0A1118), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
