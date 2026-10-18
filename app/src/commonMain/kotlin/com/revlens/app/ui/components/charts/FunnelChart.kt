package com.revlens.app.ui.components.charts

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.FunnelStage
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun FunnelChart(
    stages: List<FunnelStage>,
    modifier: Modifier = Modifier
) {
    if (stages.isEmpty()) return

    val stageColors = listOf(
        RevLensTheme.colors.brandPrimary,
        Color(0xFF3B82F6),
        RevLensTheme.colors.success
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val maxCount = stages.firstOrNull()?.count?.coerceAtLeast(1) ?: 1

        stages.forEachIndexed { index, stage ->
            val color = stageColors[index % stageColors.size]
            val widthFraction = (stage.count.toFloat() / maxCount.toFloat()).coerceIn(0.18f, 1f)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stage.stageName,
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${stage.count}",
                            style = RevLensTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        if (index > 0) {
                            Text(
                                text = "(${stage.conversionRateFromPrevious.toInt()}%)",
                                style = RevLensTypography.labelSmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                        }
                    }
                }

                // Tapered Funnel Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${stage.overallConversionRate.toInt()}% of top",
                        style = RevLensTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (index < stages.lastIndex) {
                val dropOff = 100 - stages[index + 1].conversionRateFromPrevious.toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = RevLensTheme.colors.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$dropOff% drop-off",
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.error
                    )
                }
            }
        }
    }
}
