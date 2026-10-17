package com.example.revlens.ui.components.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun UsageMeter(
    label: String,
    used: Int,
    limit: Int,
    modifier: Modifier = Modifier,
    unitLabel: String? = null
) {
    val isUnlimited = limit <= 0
    val progress = if (isUnlimited) 0f else (used.toFloat() / limit).coerceIn(0f, 1f)
    val ratio = if (isUnlimited) 0f else used.toFloat() / limit

    val barColor = when {
        isUnlimited -> RevLensTheme.colors.brandPrimary
        ratio >= 1.0f -> RevLensTheme.colors.error
        ratio >= 0.8f -> RevLensTheme.colors.warning
        else -> RevLensTheme.colors.brandPrimary
    }

    val valueText = if (isUnlimited) {
        "$used / ∞"
    } else {
        "$used of $limit" + if (unitLabel != null) " $unitLabel" else ""
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textPrimary
            )
            Text(
                text = valueText,
                style = RevLensTypography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (ratio >= 1.0f && !isUnlimited) RevLensTheme.colors.error else RevLensTheme.colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = RevLensTheme.colors.surfaceElevated,
            strokeCap = StrokeCap.Round
        )
    }
}
