package com.revlens.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    valueString: String = value.toString(),
    caption: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()

    val activeColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val inactiveColor = if (isDark) Color(0xFF262C38) else Color(0xFFE2E8F0)
    val dividerColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val trackHeight = 36.dp
    val dividerWidth = 3.5.dp
    val dividerHeight = 44.dp
    val gap = 6.dp
    val outerRadius = 10.dp
    val innerRadius = 3.dp

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty() || valueString.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = RevLensTypography.bodyLarge,
                        color = RevLensTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (valueString.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = valueString,
                        style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        val fraction = if (valueRange.endInclusive > valueRange.start) {
            ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(
                0f, 1f
            )
        } else 0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .pointerInput(enabled, valueRange) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val w = size.width.toFloat()
                        val divW = dividerWidth.toPx()
                        val gapPx = gap.toPx()
                        val minX = (divW / 2f) + gapPx + 4.dp.toPx()
                        val maxX = w - minX

                        fun updateFromX(x: Float) {
                            val f = if (maxX > minX) ((x - minX) / (maxX - minX)).coerceIn(
                                0f, 1f
                            ) else 0f
                            val newValue =
                                valueRange.start + f * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }

                        var isDrag = false
                        val touchSlop = viewConfiguration.touchSlop

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                if (!isDrag) {
                                    updateFromX(down.position.x)
                                }
                                break
                            }
                            val deltaX = change.position.x - down.position.x
                            val deltaY = change.position.y - down.position.y
                            if (!isDrag) {
                                if (kotlin.math.abs(deltaY) > touchSlop && kotlin.math.abs(
                                        deltaY
                                    ) > kotlin.math.abs(deltaX)
                                ) {
                                    break
                                } else if (kotlin.math.abs(deltaX) > touchSlop) {
                                    isDrag = true
                                }
                            }
                            if (isDrag) {
                                change.consume()
                                updateFromX(change.position.x)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dividerHeight)
            ) {
                val w = size.width
                val h = size.height
                val trackH = trackHeight.toPx()
                val topY = (h - trackH) / 2f
                val bottomY = topY + trackH

                val divW = dividerWidth.toPx()
                val gapPx = gap.toPx()
                val outerRPx = outerRadius.toPx()
                val innerRPx = innerRadius.toPx()

                val minX = (divW / 2f) + gapPx + 4.dp.toPx()
                val maxX = w - minX
                val dividerCenterX = minX + fraction * (maxX - minX)

                val leftSegRight = dividerCenterX - (divW / 2f) - gapPx
                val rightSegLeft = dividerCenterX + (divW / 2f) + gapPx

                // 1. Left active segment (only if fraction > 0)
                if (fraction > 0.005f && leftSegRight > 1f) {
                    val activePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = 0f,
                                top = topY,
                                right = leftSegRight,
                                bottom = bottomY,
                                topLeftCornerRadius = CornerRadius(outerRPx, outerRPx),
                                bottomLeftCornerRadius = CornerRadius(outerRPx, outerRPx),
                                topRightCornerRadius = CornerRadius(innerRPx, innerRPx),
                                bottomRightCornerRadius = CornerRadius(innerRPx, innerRPx)
                            )
                        )
                    }
                    drawPath(path = activePath, color = activeColor)
                }

                // 2. Right inactive segment (only if fraction < 1)
                if (fraction < 0.995f && w - rightSegLeft > 1f) {
                    val inactivePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = rightSegLeft,
                                top = topY,
                                right = w,
                                bottom = bottomY,
                                topLeftCornerRadius = CornerRadius(innerRPx, innerRPx),
                                bottomLeftCornerRadius = CornerRadius(innerRPx, innerRPx),
                                topRightCornerRadius = CornerRadius(outerRPx, outerRPx),
                                bottomRightCornerRadius = CornerRadius(outerRPx, outerRPx)
                            )
                        )
                    }
                    drawPath(path = inactivePath, color = inactiveColor)
                }

                // 3. Middle vertical divider line (extends full dividerHeight, rounded pill)
                val divLeft = dividerCenterX - (divW / 2f)
                drawRoundRect(
                    color = dividerColor,
                    topLeft = Offset(divLeft, 0f),
                    size = Size(divW, h),
                    cornerRadius = CornerRadius(divW / 2f, divW / 2f)
                )
            }

            if (icon != null) {
                val iconCovered = fraction > 0.85f
                val iconTint = if (iconCovered) {
                    if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                } else {
                    RevLensTheme.colors.textSecondary
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(18.dp)
                )
            }
        }

        if (caption != null) {
            Text(
                text = caption,
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}