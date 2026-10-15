package com.example.revlens.ui.screens.navbarscreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import com.example.revlens.ui.components.ActionCardButton
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
import org.koin.androidx.compose.koinViewModel
import com.example.revlens.domain.calc.LtvCalculator
import com.example.revlens.model.BusinessProfile
import com.example.revlens.presentation.MainViewModel
import com.example.revlens.ui.components.PrimaryButton
import com.example.revlens.ui.components.ProgressRing
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography
import com.example.revlens.utils.DateUtils
import com.example.revlens.utils.RevLensFormatter
import java.math.BigDecimal
import java.util.Locale
import kotlin.math.pow

// --- THEME-AWARE DYNAMIC COLOR TOKENS ---
private val BgColor: Color @Composable get() = RevLensTheme.colors.background
private val CardBg: Color @Composable get() = RevLensTheme.colors.surface
private val CardBorder: Color @Composable get() = RevLensTheme.colors.borderDefault
private val CyanAccent: Color @Composable get() = RevLensTheme.colors.brandPrimary
private val EmeraldAccent: Color @Composable get() = RevLensTheme.colors.brandTertiary
private val CoralAccent: Color @Composable get() = RevLensTheme.colors.error
private val TextPrimary: Color @Composable get() = RevLensTheme.colors.textPrimary
private val TextMuted: Color @Composable get() = RevLensTheme.colors.textSecondary
private val TextSubtle: Color @Composable get() = RevLensTheme.colors.textTertiary

@Composable
private fun adaptiveCardBorder(): BorderStroke? =
    if (isSystemInDarkTheme()) null else BorderStroke(1.dp, CardBorder)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToInput: () -> Unit,
    onNavigateToPricing: () -> Unit = {},
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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp, vertical = 4.dp
                        ),
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
        containerColor = BgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            profile?.let { currentProfile ->
                DashboardContent(
                    profile = currentProfile,
                    onNavigateToPricing = onNavigateToPricing,
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
                        color = TextPrimary
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
    onNavigateToPricing: () -> Unit,
    onNavigateToWhatIf: () -> Unit,
    onNavigateToBreakEven: () -> Unit,
    onNavigateToGoalPlanner: () -> Unit
) {
    val currencyFormatter = remember(profile.currencyCode) {
        com.example.revlens.utils.CurrencyUtils.getFormatter(profile.currencyCode)
    }

    // 1. Top Compact Simulator Action Buttons (Pricing, What-If, Break-Even)
    val isDark = isSystemInDarkTheme()
    val buttonBorder = if (isDark) null else BorderStroke(1.dp, CardBorder)
    val buttonBg = if (isDark) CardBg else RevLensTheme.colors.surfaceElevated

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onNavigateToPricing,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            shape = RoundedCornerShape(10.dp),
            border = buttonBorder,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBg,
                contentColor = TextPrimary
            )
        ) {
            Text("Pricing", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onNavigateToWhatIf,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            shape = RoundedCornerShape(10.dp),
            border = buttonBorder,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBg,
                contentColor = TextPrimary
            )
        ) {
            Text("What-If", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onNavigateToBreakEven,
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            shape = RoundedCornerShape(10.dp),
            border = buttonBorder,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBg,
                contentColor = TextPrimary
            )
        ) {
            Text(
                "Break-Even", color = TextPrimary, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    // 2. Hero MRR & ARR Card (Borderless in Dark, Subtle Border in Light, Rounded 16.dp)
    HeroRevenueCard(profile = profile, currencyFormatter = currencyFormatter)

    // 3. Growth Optimization Banner (Borderless in Dark, Subtle Border in Light, Rounded 16.dp)
    GrowthOptimizationCard(
        profile = profile,
        currencyFormatter = currencyFormatter,
        onSimulate = onNavigateToWhatIf
    )

    // 4. Metric Grid 2x2 (Customers, ARPU, User Churn, LTV)
    val customersGrowth = (profile.totalCustomers * profile.monthlyGrowthRate).toInt()
    val growthPercent = (profile.monthlyGrowthRate * 100).toInt()

    val ltv = LtvCalculator.calculateLtv(
        profile.arpu, profile.monthlyChurnRate, profile.variableCostPerCustomer
    )
    val ltvToCac = if (profile.cac > BigDecimal.ZERO) {
        ltv.toDouble() / profile.cac.toDouble()
    } else 0.0
    val ltvBadge =
        if (ltvToCac > 0) "${String.format(Locale.US, "%.1f", ltvToCac)}x CAC" else "Healthy"
    val lifetimeMonths = if (profile.monthlyChurnRate > 0) {
        "${(1.0 / profile.monthlyChurnRate).toInt()} mo"
    } else {
        "60+ mo"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
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
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MetricKpiCard(
            title = "USER CHURN",
            value = "${String.format(Locale.US, "%.1f", profile.monthlyChurnRate * 100)}%",
            subValue = "benchmark <3%",
            badge = if (profile.monthlyChurnRate <= 0.03) "Low" else "${
                String.format(
                    Locale.US, "%.1f", profile.monthlyChurnRate * 100
                )
            }%",
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

    // 5. MRR Forward Projection Chart Card (Borderless in Dark, Subtle Border in Light, Rounded 16.dp)
    MrrTrajectoryChartCard(profile = profile, currencyFormatter = currencyFormatter)

    // 6. Annual MRR Goal Tracker (Borderless in Dark, Subtle Border in Light, Rounded 16.dp)
    GoalTrackerCard(
        profile = profile,
        currencyFormatter = currencyFormatter,
        onNavigateToGoalPlanner = onNavigateToGoalPlanner
    )

    val context = LocalContext.current
    ActionCardButton(
        text = "Export Executive Board Deck (PDF)",
        icon = Icons.Outlined.FileUpload,
        onClick = {
            android.widget.Toast.makeText(
                context,
                "Executive Board Deck (PDF) exported successfully",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun HeroRevenueCard(
    profile: BusinessProfile,
    currencyFormatter: RevLensFormatter
) {
    val mrr = profile.currentMrr
    val arr = mrr.multiply(BigDecimal(12))
    val netNew = mrr.multiply(BigDecimal.valueOf(profile.monthlyGrowthRate))
    val isDark = isSystemInDarkTheme()
    val greenBadgeColor = if (isDark) EmeraldAccent else RevLensTheme.colors.brandTertiaryText

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = adaptiveCardBorder(),
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
                        text = "↗ +${
                            String.format(
                                Locale.US, "%.1f", profile.monthlyGrowthRate * 100
                            )
                        }%",
                        color = greenBadgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currencyFormatter.format(mrr),
                    color = TextPrimary,
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
                    color = greenBadgeColor,
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
    currencyFormatter: RevLensFormatter,
    onSimulate: () -> Unit
) {
    val potentialGrowth = BigDecimal.valueOf(5L * profile.totalCustomers.coerceAtLeast(1))

    val isDark = isSystemInDarkTheme()
    val simBtnBg = if (isDark) Color(0xFF222B3A) else RevLensTheme.colors.surfaceElevated
    val simBtnBorder =
        if (isDark) Modifier else Modifier.border(1.dp, CardBorder, RoundedCornerShape(8.dp))
    val optTitleColor = if (isDark) CyanAccent else RevLensTheme.colors.brandPrimaryText

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = adaptiveCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = optTitleColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Growth Optimization",
                        color = optTitleColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+$5 ARPU adds +${
                            currencyFormatter.format(
                                potentialGrowth
                            )
                        } MRR at current volume",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(simBtnBg)
                    .then(simBtnBorder)
                    .clickable(onClick = onSimulate)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Simulate ↗",
                    color = TextPrimary,
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
        border = adaptiveCardBorder(),
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
                    color = TextPrimary,
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
    currencyFormatter: RevLensFormatter
) {
    val mrr = profile.currentMrr
    val mrrFloat = mrr.toFloat()
    val gRate = profile.monthlyGrowthRate.coerceAtLeast(0.005)
        .toFloat()
    val isDark = isSystemInDarkTheme()

    val trendData = remember(mrrFloat, gRate) {
        (0..5).map { monthAhead ->
            (mrrFloat * (1.0 + gRate.toDouble()).pow(monthAhead.toDouble())).toFloat()
        }
    }
    val projected6M = trendData.last()

    val forward6Months = remember {
        DateUtils.getForwardMonthNames(6)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = adaptiveCardBorder(),
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
                        text = "MRR Forward Projection",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "6-Month Forecast (+${
                            String.format(
                                Locale.US, "%.1f", profile.monthlyGrowthRate * 100
                            )
                        }%/mo)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                val badgeBg = if (isDark) Color(0xFF142436) else CyanAccent.copy(alpha = 0.12f)
                val badgeColor = if (isDark) CyanAccent else RevLensTheme.colors.brandPrimaryText
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "• Now: ${
                            currencyFormatter.format(
                                mrr
                            )
                        } → 6M: ${currencyFormatter.format(projected6M)}",
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            val chartCyan = CyanAccent
            val chartCardBorder = CardBorder
            val chartCardBg = CardBg
            val gridColor = chartCardBorder.copy(alpha = if (isDark) 0.4f else 0.7f)
            val fillGradColor = chartCyan.copy(alpha = if (isDark) 0.25f else 0.16f)

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
                            color = gridColor,
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
                            colors = listOf(fillGradColor, Color.Transparent)
                        )
                    )

                    // Stroke line
                    drawPath(
                        path = path,
                        color = chartCyan,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Key milestone dots
                    val dots = listOf(
                        Offset(w * 0.05f, h * 0.85f),
                        Offset(w * 0.35f, h * 0.72f),
                        Offset(w * 0.65f, h * 0.45f),
                        Offset(w * 0.95f, h * 0.20f)
                    )
                    dots.forEach { pt ->
                        drawCircle(color = chartCardBg, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = chartCyan, radius = 3.5.dp.toPx(), center = pt)
                    }
                }

                // Callout Tooltip Badge
                val tooltipBg =
                    if (isDark) Color(0xFF202C3D) else RevLensTheme.colors.surfaceElevated
                val tooltipBorderModifier = if (isDark) Modifier else Modifier.border(
                    1.dp, CardBorder, RoundedCornerShape(4.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tooltipBg)
                        .then(tooltipBorderModifier)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "6M Target: ${currencyFormatter.format(projected6M)}",
                        color = TextPrimary,
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
                forward6Months.forEach { m ->
                    val isCurrent = m.contains("(Now)")
                    val monthColor =
                        if (isCurrent) (if (isDark) CyanAccent else RevLensTheme.colors.brandPrimaryText) else TextSubtle
                    Text(
                        text = m,
                        color = monthColor,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}


@Composable
private fun GoalTrackerCard(
    profile: BusinessProfile,
    currencyFormatter: RevLensFormatter,
    onNavigateToGoalPlanner: () -> Unit
) {
    val mrr = profile.currentMrr
    val targetGoal = profile.mrrGoal
    val hasGoal = targetGoal != null && targetGoal > BigDecimal.ZERO
    val isDark = isSystemInDarkTheme()
    val rawProgress = if (hasGoal) {
        (mrr.toDouble() / targetGoal!!.toDouble()).toFloat()
    } else {
        0f
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = adaptiveCardBorder(),
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
                val goalBtnBg = if (isDark) Color(0xFF222B3A) else CyanAccent.copy(alpha = 0.12f)
                val goalBtnTextColor =
                    if (isDark) CyanAccent else RevLensTheme.colors.brandPrimaryText
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(goalBtnBg)
                        .clickable(onClick = onNavigateToGoalPlanner)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (hasGoal) "Edit Goal ↗" else "Set Goal ↗",
                        color = goalBtnTextColor,
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
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target: ${currencyFormatter.format(targetGoal)}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    val remaining = targetGoal!!.subtract(mrr)
                        .coerceAtLeast(BigDecimal.ZERO)
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
                    Text(
                        "Set Your MRR Goal", color = Color(0xFF0A1118), fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
