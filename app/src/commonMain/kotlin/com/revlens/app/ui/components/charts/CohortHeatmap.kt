package com.revlens.app.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revlens.app.domain.calc.CohortCell
import com.revlens.app.domain.calc.CohortRow
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun CohortHeatmap(
    rows: List<CohortRow>,
    averageCurve: List<Double>,
    onCellClick: ((CohortRow, CohortCell) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxOffsets: Int = 12
) {
    if (rows.isEmpty()) return

    val horizontalScrollState = rememberScrollState()
    val cellWidth = 56.dp
    val headerCellWidth = 110.dp
    val cellHeight = 36.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Sticky First Column: Cohort & Size
            Column(modifier = Modifier.width(headerCellWidth)) {
                // Header cell
                Box(
                    modifier = Modifier
                        .height(cellHeight)
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Cohort · Size",
                        style = RevLensTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textSecondary
                    )
                }

                // Cohort rows
                rows.forEach { row ->
                    Box(
                        modifier = Modifier
                            .height(cellHeight)
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = row.cohortLabel,
                                style = RevLensTypography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${row.initialSize}",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textTertiary
                            )
                        }
                    }
                }

                // Average row header
                Box(
                    modifier = Modifier
                        .height(cellHeight)
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Average",
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.brandPrimary
                    )
                }
            }

            // Horizontally Scrollable Heatmap Grid
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column {
                    // Header row: M0, M1, M2 ...
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 0 until maxOffsets) {
                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                                    .padding(bottom = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "M$i",
                                    style = RevLensTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RevLensTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    // Cohort data cells
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            for (i in 0 until maxOffsets) {
                                val cell = row.cells.getOrNull(i)
                                if (cell == null || cell.isYounger) {
                                    // Empty younger cell
                                    Box(
                                        modifier = Modifier
                                            .width(cellWidth)
                                            .height(cellHeight)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RevLensTheme.colors.surfaceElevated.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "—",
                                            style = RevLensTypography.labelSmall,
                                            color = RevLensTheme.colors.textTertiary
                                        )
                                    }
                                } else {
                                    // Rendered Retention Cell with Cyan intensity
                                    val alpha = (cell.retentionPercent / 100.0).toFloat().coerceIn(0.12f, 0.95f)
                                    val cellBg = RevLensTheme.colors.brandPrimary.copy(alpha = alpha)
                                    val textColor = if (alpha > 0.5f) Color.White else RevLensTheme.colors.textPrimary

                                    Box(
                                        modifier = Modifier
                                            .width(cellWidth)
                                            .height(cellHeight)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cellBg)
                                            .clickable(enabled = onCellClick != null) {
                                                onCellClick?.invoke(row, cell)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${cell.retentionPercent.toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Average curve row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        for (i in 0 until maxOffsets) {
                            val avg = averageCurve.getOrNull(i) ?: 0.0
                            val alpha = (avg / 100.0).toFloat().coerceIn(0.12f, 0.95f)
                            val cellBg = RevLensTheme.colors.brandSecondary.copy(alpha = alpha)
                            val textColor = if (alpha > 0.5f) Color.White else RevLensTheme.colors.textPrimary

                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cellBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (avg > 0.0) "${avg.toInt()}%" else "—",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
