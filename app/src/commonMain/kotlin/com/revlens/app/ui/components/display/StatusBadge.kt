package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

enum class StatusBadgeType {
    DRAFT,
    RUNNING,
    WON,
    ACTIVE,
    PAID,
    CONNECTED,
    LOST,
    FAILED,
    PAST_DUE,
    ERROR,
    INCONCLUSIVE,
    TRIAL,
    PENDING,
    WARNING
}

@Composable
fun StatusBadge(
    type: StatusBadgeType,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val text = customText ?: when (type) {
        StatusBadgeType.DRAFT -> "Draft"
        StatusBadgeType.RUNNING -> "Running"
        StatusBadgeType.WON -> "Won"
        StatusBadgeType.ACTIVE -> "Active"
        StatusBadgeType.PAID -> "Paid"
        StatusBadgeType.CONNECTED -> "Connected"
        StatusBadgeType.LOST -> "Lost"
        StatusBadgeType.FAILED -> "Failed"
        StatusBadgeType.PAST_DUE -> "Past due"
        StatusBadgeType.ERROR -> "Error"
        StatusBadgeType.INCONCLUSIVE -> "Inconclusive"
        StatusBadgeType.TRIAL -> "Trial"
        StatusBadgeType.PENDING -> "Pending"
        StatusBadgeType.WARNING -> "Warning"
    }

    val (bgColor, contentColor) = when (type) {
        StatusBadgeType.DRAFT -> RevLensTheme.colors.surfaceElevated to RevLensTheme.colors.textSecondary
        StatusBadgeType.RUNNING -> RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f) to RevLensTheme.colors.brandPrimaryText
        StatusBadgeType.WON, StatusBadgeType.ACTIVE, StatusBadgeType.PAID, StatusBadgeType.CONNECTED ->
            RevLensTheme.colors.success to Color.White
        StatusBadgeType.LOST, StatusBadgeType.FAILED, StatusBadgeType.PAST_DUE, StatusBadgeType.ERROR ->
            RevLensTheme.colors.error to Color.White
        StatusBadgeType.INCONCLUSIVE, StatusBadgeType.TRIAL, StatusBadgeType.PENDING, StatusBadgeType.WARNING ->
            RevLensTheme.colors.warning to Color.Black
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(24.dp)
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = text,
            style = RevLensTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
