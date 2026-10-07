package com.example.revlens.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = CircleShape, // Pill shape
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

@Composable
fun RevLensIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = RevLensTheme.colors.surfaceElevated,
            contentColor = RevLensTheme.colors.textPrimary
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}
