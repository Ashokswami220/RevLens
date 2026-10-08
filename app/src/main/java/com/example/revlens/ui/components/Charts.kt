package com.example.revlens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun RevLensLineChart(
    modifier: Modifier = Modifier,
    data: List<List<Float>>,
    colors: List<Color> = listOf(RevLensTheme.colors.brandPrimary),
    dashedIndices: List<Int> = emptyList()
) {
    if (data.isEmpty() || data.all { it.isEmpty() }) return

    val gridColor = RevLensTheme.colors.chartGrid
    val backgroundColor = RevLensTheme.colors.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(vertical = 16.dp)
            .drawBehind {
                val width = size.width
                val fullHeight = size.height
                val labelPadding = 60f
                val height = fullHeight - labelPadding

                // Draw Grid (4 horizontal lines)
                val gridSteps = 4
                val stepY = height / gridSteps
                for (i in 0..gridSteps) {
                    val y = i * stepY
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
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
                    val fallbackColor = Color(0xFF06B6D4) // fallback if out of bounds, shouldn't happen usually
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

                    // 2) Outer glow
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.15f),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // 3) Inner glow
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.3f),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // 4) Core line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
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

                // X-Axis Labels
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#888888")
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                val dataSize = if (data.isNotEmpty()) data[0].size else 0
                if (dataSize > 0) {
                    val labelStepX = width / (dataSize - 1).coerceAtLeast(1)
                    for (i in 0 until dataSize) {
                        // Skip some labels if there are too many
                        if (dataSize > 12 && i % 2 != 0 && i != dataSize - 1) continue
                        val label = "M${i}"
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            i * labelStepX,
                            fullHeight - 10f,
                            paint
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
    colors: List<Color> = listOf(RevLensTheme.colors.brandPrimary)
) {
    if (data.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 16.dp)
            .drawBehind {
                val width = size.width
                val fullHeight = size.height
                val labelPadding = 60f
                val height = fullHeight - labelPadding
                
                val max = data.maxOrNull()?.coerceAtLeast(0f) ?: 1f
                val min = data.minOrNull()?.coerceAtMost(0f) ?: 0f
                val range = (max - min).coerceAtLeast(1f)
                
                val barWidth = (width / data.size) * 0.6f
                val spacing = (width / data.size)
                
                val zeroY = height - ((0f - min) / range * height)
                
                data.forEachIndexed { index, value ->
                    val x = (index * spacing) + (spacing - barWidth) / 2
                    val y = height - ((value - min) / range * height)
                    
                    val barHeight = kotlin.math.abs(zeroY - y)
                    val topY = if (value >= 0) y else zeroY
                    val fallbackColor = Color(0xFF06B6D4)
                    val color = colors.getOrElse(index % colors.size) { fallbackColor }
                    
                    val gradient = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.2f)),
                        startY = topY,
                        endY = topY + barHeight
                    )
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
                
                // X-Axis Labels
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#888888")
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                data.forEachIndexed { i, _ ->
                    if (data.size > 12 && i % 2 != 0 && i != data.size - 1) return@forEachIndexed
                    val x = (i * spacing) + (spacing / 2)
                    drawContext.canvas.nativeCanvas.drawText(
                        "M${i}",
                        x,
                        fullHeight - 10f,
                        paint
                    )
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
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
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
