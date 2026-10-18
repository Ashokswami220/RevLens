package com.revlens.app.ui.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.calc.ValidationReport
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun ValidationSummary(
    report: ValidationReport,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Validation Summary",
            style = RevLensTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.textPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ValidationStatTile(
                count = report.validCount,
                label = "Valid Rows",
                color = RevLensTheme.colors.success,
                modifier = Modifier.weight(1f)
            )

            ValidationStatTile(
                count = report.warningCount,
                label = "Warnings",
                color = RevLensTheme.colors.warning,
                modifier = Modifier.weight(1f)
            )

            ValidationStatTile(
                count = report.errorCount,
                label = "Errors",
                color = RevLensTheme.colors.error,
                modifier = Modifier.weight(1f)
            )
        }

        if (report.errorMessages.isNotEmpty() || report.warningMessages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide details ▴" else "View error & warning details ▾",
                    style = RevLensTypography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.brandPrimary
                )
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.errorMessages.take(10).forEach { err ->
                        Text(
                            text = "• $err",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.error
                        )
                    }
                    report.warningMessages.take(5).forEach { warn ->
                        Text(
                            text = "• $warn",
                            style = RevLensTypography.bodySmall,
                            color = RevLensTheme.colors.warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationStatTile(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = RevLensTypography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = RevLensTypography.labelSmall,
            color = RevLensTheme.colors.textSecondary
        )
    }
}
