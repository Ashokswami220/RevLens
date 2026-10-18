package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.ExperimentAnalysis
import com.revlens.app.domain.calc.ExperimentAnalyzer
import com.revlens.app.domain.model.Experiment
import com.revlens.app.domain.model.ExperimentStatus
import com.revlens.app.ui.components.DeltaChip
import com.revlens.app.ui.components.DeltaType
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun ExperimentCard(
    experiment: Experiment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analysis: ExperimentAnalysis = ExperimentAnalyzer.analyze(experiment)

    val badgeType = when (experiment.status) {
        ExperimentStatus.DRAFT -> StatusBadgeType.DRAFT
        ExperimentStatus.RUNNING -> StatusBadgeType.RUNNING
        ExperimentStatus.WON -> StatusBadgeType.WON
        ExperimentStatus.LOST -> StatusBadgeType.LOST
        ExperimentStatus.INCONCLUSIVE -> StatusBadgeType.INCONCLUSIVE
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title & Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = experiment.name,
                style = RevLensTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RevLensTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(type = badgeType)
        }

        // Subtitle: Type & Timeline Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${experiment.type.displayName} · ${experiment.metric.displayName}",
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )
            Text(
                text = "Day ${analysis.daysElapsed} of ${experiment.plannedDurationDays}",
                style = RevLensTypography.labelSmall,
                color = RevLensTheme.colors.textTertiary
            )
        }

        LinearProgressIndicator(
            progress = { analysis.progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = RevLensTheme.colors.brandPrimary,
            trackColor = RevLensTheme.colors.borderDefault
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Predicted vs Actual Delta Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Predicted:",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
                val predSign = if (experiment.predictedLiftPercent >= 0) "+" else ""
                DeltaChip(
                    delta = "$predSign${experiment.predictedLiftPercent}%",
                    type = if (experiment.predictedLiftPercent >= 0) DeltaType.POSITIVE else DeltaType.NEGATIVE
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Actual:",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
                val actSign = if (analysis.actualLiftPercent >= 0) "+" else ""
                DeltaChip(
                    delta = "$actSign${analysis.actualLiftPercent}%",
                    type = if (analysis.actualLiftPercent >= 0) DeltaType.POSITIVE else DeltaType.NEGATIVE
                )
            }
        }
    }
}
