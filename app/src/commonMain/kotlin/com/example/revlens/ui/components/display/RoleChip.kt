package com.example.revlens.ui.components.display

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.model.Role
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun RoleChip(
    role: Role,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor) = when (role) {
        Role.Owner -> RevLensTheme.colors.primaryAction to RevLensTheme.colors.onPrimaryAction
        Role.Admin -> RevLensTheme.colors.brandSecondary.copy(alpha = 0.15f) to RevLensTheme.colors.brandSecondaryText
        Role.Editor -> RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f) to RevLensTheme.colors.brandPrimaryText
        Role.Viewer -> RevLensTheme.colors.surfaceElevated to RevLensTheme.colors.textSecondary
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
            text = role.name,
            style = RevLensTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
