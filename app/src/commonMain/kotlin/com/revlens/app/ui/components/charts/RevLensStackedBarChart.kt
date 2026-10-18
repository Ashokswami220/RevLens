package com.revlens.app.ui.components.charts

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.ui.theme.RevLensTheme
import kotlin.math.max

data class StackedBarItem(
    val label: String,
    val segmentValues: List<Float> // e.g. [paid, referral, organic]
)

@Composable
fun RevLensStackedBarChart(
    items: List<StackedBarItem>,
    segmentColors: List<Color>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 140.dp,
    barWidth: Dp? = null
) {
    if (items.isEmpty()) return

    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    val axisColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val defaultColor = RevLensTheme.colors.brandPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .padding(vertical = 4.dp)
            .drawBehind {
                val width = size.width
                val fullHeight = size.height
                val labelHeight = 28f
                val chartAreaHeight = (fullHeight - labelHeight).coerceAtLeast(20f)

                val totals = items.map { it.segmentValues.sum() }
                val maxVal = max(1f, totals.maxOrNull() ?: 1f)

                val barCount = items.size
                val spacing = width / barCount
                val maxBarW = if (barCount <= 4) 48.dp.toPx() else 24.dp.toPx()
                val calculatedBarWidth = (spacing * 0.6f).coerceAtMost(maxBarW)
                val actualBarWidth = barWidth?.toPx() ?: calculatedBarWidth

                val zeroY = chartAreaHeight

                // Draw base axis line
                drawLine(
                    color = axisColor,
                    start = Offset(0f, zeroY),
                    end = Offset(width, zeroY),
                    strokeWidth = 1.dp.toPx()
                )

                items.forEachIndexed { barIndex, item ->
                    val centerX = (barIndex * spacing) + (spacing / 2)
                    val left = centerX - (actualBarWidth / 2)
                    val right = left + actualBarWidth

                    var currentBottomY = zeroY

                    // Non-zero segments indices
                    val nonZeroIndices =
                        item.segmentValues.mapIndexedNotNull { i, v -> if (v > 0f) i else null }
                    val topIndex = nonZeroIndices.lastOrNull()

                    item.segmentValues.forEachIndexed { segIndex, value ->
                        if (value > 0f) {
                            val segHeight = (value / maxVal * chartAreaHeight).coerceAtLeast(2f)
                            val segTop = (currentBottomY - segHeight).coerceAtLeast(0f)
                            val color = segmentColors.getOrElse(
                                segIndex % segmentColors.size
                            ) { defaultColor }

                            val isTopSegment = segIndex == topIndex
                            val cornerRadiusPx = if (isTopSegment) 4.dp.toPx() else 0f

                            val roundRect = RoundRect(
                                left = left,
                                top = segTop,
                                right = right,
                                bottom = currentBottomY,
                                topLeftCornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                topRightCornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                bottomLeftCornerRadius = CornerRadius.Zero,
                                bottomRightCornerRadius = CornerRadius.Zero
                            )

                            val path = Path().apply { addRoundRect(roundRect) }
                            drawPath(path = path, color = color)

                            currentBottomY = segTop
                        }
                    }

                    // Draw X-axis label
                    if (item.label.isNotEmpty()) {
                        val measured = textMeasurer.measure(
                            text = item.label,
                            style = TextStyle(
                                color = labelColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                centerX - (measured.size.width / 2f),
                                fullHeight - measured.size.height
                            )
                        )
                    }
                }
            }
    )
}
