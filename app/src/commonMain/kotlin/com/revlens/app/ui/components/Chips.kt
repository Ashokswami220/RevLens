package com.revlens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorDot: Color? = null
) {
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        if (selected) RevLensTheme.colors.primaryAction else RevLensTheme.colors.surfaceElevated,
        label = "chipBg"
    )
    val contentColor by androidx.compose.animation.animateColorAsState(
        if (selected) RevLensTheme.colors.onPrimaryAction else RevLensTheme.colors.textPrimary,
        label = "chipContent"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        if (colorDot != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colorDot)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = RevLensTypography.labelLarge,
            color = contentColor
        )
    }
}

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorDot: Color? = null
) {
    FilterChip(
        text = text,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        colorDot = colorDot
    )
}

enum class DeltaType { POSITIVE, NEGATIVE, NEUTRAL }

@Composable
fun DeltaChip(
    delta: String,
    type: DeltaType,
    modifier: Modifier = Modifier,
    lowerIsBetter: Boolean = false
) {
    val isPositiveGood = !lowerIsBetter
    val backgroundColor = when (type) {
        DeltaType.POSITIVE -> if (isPositiveGood) RevLensTheme.colors.success else RevLensTheme.colors.error
        DeltaType.NEGATIVE -> if (isPositiveGood) RevLensTheme.colors.error else RevLensTheme.colors.success
        DeltaType.NEUTRAL -> RevLensTheme.colors.surfaceElevated
    }
    val contentColor = when (type) {
        DeltaType.POSITIVE, DeltaType.NEGATIVE -> Color.White
        DeltaType.NEUTRAL -> RevLensTheme.colors.textSecondary
    }
    val icon = when (type) {
        DeltaType.POSITIVE -> Icons.Filled.ArrowDropUp
        DeltaType.NEGATIVE -> Icons.Filled.ArrowDropDown
        DeltaType.NEUTRAL -> null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = delta,
            style = RevLensTypography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}