package com.example.revlens.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

data class DonutSlice(
    val label: String,
    val value: Long,
    val percentage: Double,
    val color: Color,
    val subtitle: String? = null
)

@Composable
fun RevLensDonutChart(
    slices: List<DonutSlice>,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 160.dp,
    strokeWidth: Dp = 22.dp
) {
    val defaultRingColor = RevLensTheme.colors.surfaceElevated

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Donut Chart Container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(chartSize)
        ) {
            Canvas(modifier = Modifier.size(chartSize)) {
                val strokePx = strokeWidth.toPx()
                val radius = (size.minDimension - strokePx) / 2f
                val topLeft = Offset((size.width - 2 * radius) / 2f, (size.height - 2 * radius) / 2f)
                val arcSize = Size(radius * 2, radius * 2)

                if (slices.isEmpty() || slices.all { it.percentage <= 0.0 }) {
                    drawArc(
                        color = defaultRingColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx)
                    )
                } else {
                    var currentAngle = -90f
                    for (slice in slices) {
                        val sweep = ((slice.percentage / 100.0) * 360f).toFloat()
                        // Small gap between slices if multiple
                        val effectiveSweep = if (slices.size > 1 && sweep > 4f) sweep - 2f else sweep
                        drawArc(
                            color = slice.color,
                            startAngle = currentAngle,
                            sweepAngle = effectiveSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        currentAngle += sweep
                    }
                }
            }

            // Center Callout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = centerValue,
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = centerTitle,
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Legend Breakdown List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEach { slice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RevLensTheme.colors.surfaceElevated.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Column {
                            Text(
                                text = slice.label,
                                style = RevLensTypography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            if (slice.subtitle != null) {
                                Text(
                                    text = slice.subtitle,
                                    style = RevLensTypography.labelSmall,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$${slice.value / 100}",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(slice.color.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${slice.percentage.toInt()}%",
                                style = RevLensTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = slice.color
                            )
                        }
                    }
                }
            }
        }
    }
}
