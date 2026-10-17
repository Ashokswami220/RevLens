package com.example.revlens.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.revlens.domain.calc.WaterfallStep
import com.example.revlens.ui.theme.RevLensTheme

@Composable
fun WaterfallChart(
    steps: List<WaterfallStep>,
    modifier: Modifier = Modifier,
    height: Dp = 230.dp,
    onStepSelected: ((WaterfallStep) -> Unit)? = null
) {
    if (steps.isEmpty()) return

    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val neutralBarColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val successColor = RevLensTheme.colors.success
    val errorColor = RevLensTheme.colors.error
    val warningColor = Color(0xFFF59E0B)
    val brandColor = RevLensTheme.colors.brandPrimary
    val gridLineColor = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = RevLensTheme.colors.textPrimary
    val textSecondary = RevLensTheme.colors.textSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(steps) {
                    detectTapGestures { offset ->
                        val barWidthWithSpacing = size.width / steps.size
                        val tappedIdx = (offset.x / barWidthWithSpacing).toInt().coerceIn(0, steps.size - 1)
                        selectedIndex = tappedIdx
                        onStepSelected?.invoke(steps[tappedIdx])
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bottomPadding = 36.dp.toPx()
            val topPadding = 24.dp.toPx()
            val chartHeight = canvasHeight - bottomPadding - topPadding

            // Determine running max across all points to scale Y
            var running = 0L
            var maxVal = 1L
            for (step in steps) {
                if (step.isTotal) {
                    running = step.amountMinor
                } else if (step.isPositive) {
                    running += step.amountMinor
                } else {
                    running -= step.amountMinor
                }
                if (running > maxVal) maxVal = running
            }
            maxVal = (maxVal * 1.15).toLong().coerceAtLeast(1000L) // add 15% headroom

            val n = steps.size
            val barSlotWidth = canvasWidth / n
            val barWidth = (barSlotWidth * 0.65f).coerceAtLeast(16.dp.toPx())
            val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

            // Draw baseline / grid line
            val baseY = topPadding + chartHeight
            drawLine(
                color = gridLineColor,
                start = Offset(0f, baseY),
                end = Offset(canvasWidth, baseY),
                strokeWidth = 1.dp.toPx()
            )

            var currentLevel = 0L
            var prevEndY: Float? = null

            for (i in 0 until n) {
                val step = steps[i]
                val slotCenterX = i * barSlotWidth + (barSlotWidth / 2f)
                val left = slotCenterX - (barWidth / 2f)

                val (barTopY, barBottomY, newLevel, barColor) = if (step.isTotal) {
                    val h = (step.amountMinor.toFloat() / maxVal) * chartHeight
                    val top = baseY - h
                    val color = if (i == 0) neutralBarColor else brandColor
                    FourTuple(top, baseY, step.amountMinor, color)
                } else if (step.isPositive) {
                    val bottomY = baseY - (currentLevel.toFloat() / maxVal) * chartHeight
                    val nextLevel = currentLevel + step.amountMinor
                    val topY = baseY - (nextLevel.toFloat() / maxVal) * chartHeight
                    FourTuple(topY, bottomY, nextLevel, successColor)
                } else {
                    val topY = baseY - (currentLevel.toFloat() / maxVal) * chartHeight
                    val nextLevel = (currentLevel - step.amountMinor).coerceAtLeast(0L)
                    val bottomY = baseY - (nextLevel.toFloat() / maxVal) * chartHeight
                    val color = if (step.title.contains("Contraction", ignoreCase = true)) warningColor else errorColor
                    FourTuple(topY, bottomY, nextLevel, color)
                }

                // Draw connector line from prev bar
                if (prevEndY != null) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(slotCenterX - barSlotWidth + (barWidth / 2f), prevEndY),
                        end = Offset(left, prevEndY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Draw bar
                val rectHeight = (barBottomY - barTopY).coerceAtLeast(4.dp.toPx())
                val isSelected = selectedIndex == i
                val actualColor = if (isSelected) barColor else barColor.copy(alpha = 0.9f)

                drawRoundRect(
                    color = actualColor,
                    topLeft = Offset(left, barTopY),
                    size = Size(barWidth, rectHeight),
                    cornerRadius = cornerRadius
                )

                // Label on top of bar
                val prefix = if (!step.isTotal) (if (step.isPositive) "+" else "-") else ""
                val amountDollars = step.amountMinor / 100.0
                val formattedAmount = formatCompact(prefix, amountDollars)

                val valLayout = textMeasurer.measure(
                    text = formattedAmount,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                )
                val textY = (barTopY - valLayout.size.height - 4f).coerceAtLeast(2f)
                drawText(
                    textMeasurer = textMeasurer,
                    text = formattedAmount,
                    topLeft = Offset(slotCenterX - (valLayout.size.width / 2f), textY),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                )

                // Label below X axis
                val shortTitle = when {
                    step.title.startsWith("Starting") -> "Start"
                    step.title.startsWith("New") -> "+New"
                    step.title.startsWith("Expansion") -> "+Exp"
                    step.title.startsWith("Contraction") -> "-Con"
                    step.title.startsWith("Churn") -> "-Churn"
                    step.title.startsWith("Ending") -> "End"
                    else -> step.title.take(5)
                }

                val titleLayout = textMeasurer.measure(
                    text = shortTitle,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) brandColor else textSecondary
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = shortTitle,
                    topLeft = Offset(slotCenterX - (titleLayout.size.width / 2f), baseY + 8.dp.toPx()),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) brandColor else textSecondary
                    )
                )

                currentLevel = newLevel
                prevEndY = if (step.isTotal) barTopY else (if (step.isPositive) barTopY else barBottomY)
            }
        }
    }
}

private data class FourTuple(val top: Float, val bottom: Float, val level: Long, val color: Color)

private fun formatCompact(prefix: String, value: Double): String {
    return when {
        value >= 1_000_000 -> "$prefix$${(value / 1_000_000).toInt()}M"
        value >= 1_000 -> "$prefix$${(value / 1_000).toInt()}k"
        else -> "$prefix$${value.toInt()}"
    }
}
