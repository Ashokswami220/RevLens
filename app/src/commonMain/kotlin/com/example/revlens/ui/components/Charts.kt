package com.example.revlens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun RevLensLineChart(
    modifier: Modifier = Modifier,
    data: List<List<Float>>,
    colors: List<Color> = listOf(RevLensTheme.colors.brandPrimary),
    dashedIndices: List<Int> = emptyList(),
    xLabels: List<String>? = null,
    chartHeight: Dp = 175.dp
) {
    if (data.isEmpty() || data.all { it.isEmpty() }) return

    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    val lineGridColor = if (isDark) Color(0xFF2E384D).copy(alpha = 0.55f) else Color(0xFFCBD5E1).copy(alpha = 0.75f)
    val backgroundColor = RevLensTheme.colors.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .padding(vertical = 12.dp)
            .drawBehind {
                val width = size.width
                val fullHeight = size.height
                val labelPadding = 48f
                val height = fullHeight - labelPadding

                // Draw Grid (5 horizontal lines, dashed break-break like Dashboard)
                val gridSteps = 4
                val stepY = height / gridSteps
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                for (i in 0..gridSteps) {
                    val y = i * stepY
                    drawLine(
                        color = lineGridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Draw Series
                val flatData = data.flatten()
                val max = flatData.maxOrNull() ?: 1f
                val min = flatData.minOrNull() ?: 0f
                val range = (max - min).coerceAtLeast(1f)

                data.forEachIndexed { seriesIndex, seriesData ->
                    if (seriesData.isEmpty()) return@forEachIndexed

                    val stepX = width / (seriesData.size - 1).coerceAtLeast(1)
                    val fallbackColor =
                        Color(0xFF06B6D4) // fallback if out of bounds, shouldn't happen usually
                    val color = colors.getOrElse(seriesIndex) { fallbackColor }
                    val path = Path()

                    seriesData.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = height - ((value - min) / range * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // 1) Fill area under the line with gradient
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    val fillBrush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                    drawPath(path = fillPath, brush = fillBrush)

                    val isDashed = dashedIndices.contains(seriesIndex)
                    val lineDashEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) else null

                    if (!isDashed) {
                        // 2) Outer glow
                        drawPath(
                            path = path,
                            color = color.copy(alpha = 0.15f),
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // 3) Inner glow
                        drawPath(
                            path = path,
                            color = color.copy(alpha = 0.28f),
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    // 4) Core line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = (if (isDashed) 2.dp else 3.dp).toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = lineDashEffect
                        )
                    )

                    // 5) Draw points
                    val shouldDrawAllPoints = data.size == 1
                    seriesData.forEachIndexed { index, value ->
                        val isFirstOrLast = index == 0 || index == seriesData.size - 1
                        if (shouldDrawAllPoints || isFirstOrLast) {
                            val x = index * stepX
                            val y = height - ((value - min) / range * height)
                            drawCircle(
                                color = backgroundColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = color,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            if (index == seriesData.size - 1) {
                                drawCircle(
                                    color = color,
                                    radius = 2.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }

                val dataSize = if (data.isNotEmpty()) data[0].size else 0
                if (dataSize > 0) {
                    val labelStepX = width / (dataSize - 1).coerceAtLeast(1)
                    for (i in 0 until dataSize) {
                        // Skip some labels if there are too many
                        if (dataSize > 12 && i % 2 != 0 && i != dataSize - 1) continue
                        val label = xLabels?.getOrNull(i) ?: "M${i}"
                        val measured = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                color = Color(0xFF888888),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(i * labelStepX - (measured.size.width / 2f), fullHeight - measured.size.height)
                        )
                    }
                }
            }
    )
}

@Composable
fun RevLensBarChart(
    modifier: Modifier = Modifier,
    data: List<Float>,
    colors: List<Color> = listOf(RevLensTheme.colors.brandPrimary),
    labels: List<String>? = null,
    valueLabels: List<String>? = null,
    barWidth: Dp? = null
) {
    if (data.isEmpty()) return

    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    val axisColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(125.dp)
            .padding(vertical = 4.dp)
            .drawBehind {
                val width = size.width
                val fullHeight = size.height
                val labelHeight = if (labels != null) 34f else 0f
                val valueLabelHeight = if (valueLabels != null) 26f else 0f
                val chartHeight = (fullHeight - labelHeight - valueLabelHeight).coerceAtLeast(20f)

                val rawMax = data.maxOrNull() ?: 0f
                val rawMin = data.minOrNull() ?: 0f

                val max = rawMax.coerceAtLeast(0.1f)
                val min = rawMin.coerceAtMost(-0.1f)
                val range = (max - min).coerceAtLeast(1f)

                val barCount = data.size
                val spacing = width / barCount
                val maxBarW = if (barCount <= 2) 64.dp.toPx() else 32.dp.toPx()
                val calculatedBarWidth = (spacing * 0.52f).coerceAtMost(maxBarW)
                val actualBarWidth = barWidth?.toPx() ?: calculatedBarWidth

                val zeroY = valueLabelHeight + (chartHeight - ((0f - min) / range * chartHeight))

                // Draw horizontal zero baseline
                drawLine(
                    color = axisColor,
                    start = Offset(0f, zeroY),
                    end = Offset(width, zeroY),
                    strokeWidth = 1.dp.toPx()
                )

                val cr = if (actualBarWidth > 40.dp.toPx()) 6.dp.toPx() else 4.dp.toPx()
                val cornerRadius = CornerRadius(cr, cr)
                val zeroCorner = CornerRadius(0f, 0f)

                data.forEachIndexed { index, value ->
                    val centerX = (index * spacing) + (spacing / 2)
                    val x = centerX - (actualBarWidth / 2)
                    val y = valueLabelHeight + (chartHeight - ((value - min) / range * chartHeight))

                    val rawBarHeight = kotlin.math.abs(zeroY - y)
                    val barHeight = if (value != 0f) rawBarHeight.coerceAtLeast(3.dp.toPx()) else 0f
                    val topY = if (value >= 0) (zeroY - barHeight) else zeroY

                    val fallbackColor = Color(0xFF06B6D4)
                    val color = colors.getOrElse(index % colors.size) { fallbackColor }

                    if (barHeight > 0f) {
                        val gradient = Brush.verticalGradient(
                            colors = if (value >= 0) {
                                listOf(color, color.copy(alpha = 0.45f))
                            } else {
                                listOf(color.copy(alpha = 0.45f), color)
                            },
                            startY = topY,
                            endY = topY + barHeight
                        )

                        val roundRect = if (value >= 0) {
                            RoundRect(
                                left = x,
                                top = topY,
                                right = x + actualBarWidth,
                                bottom = zeroY,
                                topLeftCornerRadius = cornerRadius,
                                topRightCornerRadius = cornerRadius,
                                bottomLeftCornerRadius = zeroCorner,
                                bottomRightCornerRadius = zeroCorner
                            )
                        } else {
                            RoundRect(
                                left = x,
                                top = zeroY,
                                right = x + actualBarWidth,
                                bottom = topY + barHeight,
                                topLeftCornerRadius = zeroCorner,
                                topRightCornerRadius = zeroCorner,
                                bottomLeftCornerRadius = cornerRadius,
                                bottomRightCornerRadius = cornerRadius
                            )
                        }

                        val path = Path().apply { addRoundRect(roundRect) }
                        drawPath(path = path, brush = gradient)
                    } else {
                        // Subtle pill for 0%
                        drawRoundRect(
                            color = axisColor.copy(alpha = 0.7f),
                            topLeft = Offset(x, zeroY - 1.5.dp.toPx()),
                            size = Size(actualBarWidth, 3.dp.toPx()),
                            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )
                    }

                    // Value label above or below the bar
                    valueLabels?.getOrNull(index)?.let { vLabel ->
                        val measured = textMeasurer.measure(
                            text = vLabel,
                            style = TextStyle(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        )
                        val textY = if (value >= 0) {
                            (topY - measured.size.height - 2f).coerceAtLeast(0f)
                        } else {
                            (topY + barHeight + 2f).coerceAtMost(fullHeight - labelHeight)
                        }
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(centerX - (measured.size.width / 2f), textY)
                        )
                    }

                    // X-Axis Label below chart
                    val label = labels?.getOrNull(index) ?: if (labels != null) "" else "M${index}"
                    if (label.isNotEmpty()) {
                        val measured = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(centerX - (measured.size.width / 2f), fullHeight - measured.size.height)
                        )
                    }
                }
            }
    )
}

@Composable
fun ChartLegend(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(item.second)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.first,
                    style = RevLensTypography.labelMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            }
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    data: List<Float>,
    color: Color = RevLensTheme.colors.brandPrimary
) {
    if (data.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .drawBehind {
                val max = data.maxOrNull() ?: 1f
                val min = data.minOrNull() ?: 0f
                val range = (max - min).coerceAtLeast(1f)

                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1).coerceAtLeast(1)

                val path = Path()
                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value - min) / range * height)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    if (index == data.size - 1) {
                        drawCircle(
                            color = color,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // Fill area under sparkline
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                val fillBrush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
                drawPath(path = fillPath, brush = fillBrush)

                // Glow
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.2f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                // Core
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
    )
}

@Composable
fun ProgressRing(
    progress: Float,
    goalText: String,
    modifier: Modifier = Modifier,
    color: Color = RevLensTheme.colors.brandPrimary
) {
    Box(
        modifier = modifier
            .size(160.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            drawArc(
                color = color.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 270f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = RevLensTypography.displayLarge,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = goalText,
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.textSecondary
            )
        }
    }
}
