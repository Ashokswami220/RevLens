package com.example.revlens.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun ActionCardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF2E3646) else RevLensTheme.colors.borderDefault
    val backgroundColor = if (isDark) Color(0xFF1E232E) else RevLensTheme.colors.surface

    val actualBg = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.4f)
    val actualBorder = if (enabled) borderColor else borderColor.copy(alpha = 0.3f)
    val textColor =
        if (enabled) RevLensTheme.colors.textPrimary else RevLensTheme.colors.textTertiary
    val iconColor =
        if (enabled) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textTertiary

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        color = actualBg,
        border = BorderStroke(1.dp, actualBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = RevLensTheme.colors.brandPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val border = if (isDark) null else BorderStroke(1.dp, Color(0x33334155))
    val elevation = if (isDark) {
        ButtonDefaults.buttonElevation()
    } else {
        ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = CircleShape, // Pill shape
        border = border,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = RevLensTheme.colors.primaryAction,
            contentColor = RevLensTheme.colors.onPrimaryAction,
            disabledContainerColor = RevLensTheme.colors.surfaceElevated,
            disabledContentColor = RevLensTheme.colors.textDisabled
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = RevLensTheme.colors.onPrimaryAction,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = RevLensTypography.titleMedium
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = RevLensTheme.colors.surfaceElevated,
            contentColor = RevLensTheme.colors.textPrimary,
            disabledContainerColor = RevLensTheme.colors.surfaceElevated,
            disabledContentColor = RevLensTheme.colors.textDisabled
        )
    ) {
        Text(
            text = text,
            style = RevLensTypography.titleMedium
        )
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = RevLensTheme.colors.error,
            contentColor = Color.White,
            disabledContainerColor = RevLensTheme.colors.surfaceElevated,
            disabledContentColor = RevLensTheme.colors.textDisabled
        )
    ) {
        Text(
            text = text,
            style = RevLensTypography.titleMedium
        )
    }
}

@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            style = RevLensTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = RevLensTheme.colors.accentBlue
        )
    }
}
