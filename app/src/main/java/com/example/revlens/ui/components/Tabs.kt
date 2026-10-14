package com.example.revlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val containerBg = if (isDark) Color(0xFF131722) else RevLensTheme.colors.surfaceElevated
    val containerBorder = RevLensTheme.colors.borderDefault

    val activePillBg =
        if (isDark) RevLensTheme.colors.surfaceElevated else RevLensTheme.colors.surface
    val activePillBorder = if (isDark) Color(0xFF2E384D) else RevLensTheme.colors.borderDefault

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .border(1.dp, containerBorder, RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val backgroundColor = if (isSelected) activePillBg else Color.Transparent
            val borderModifier = if (isSelected) {
                Modifier.border(1.dp, activePillBorder, RoundedCornerShape(9.dp))
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(backgroundColor)
                    .then(borderModifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onOptionSelected(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = RevLensTypography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) RevLensTheme.colors.textPrimary else RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}
