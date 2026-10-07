package com.example.revlens.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = RevLensTheme.colors.brandPrimary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🖼️", style = RevLensTypography.displayLarge)
        }

        Text(
            text = title,
            style = RevLensTypography.titleMedium,
            color = RevLensTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = RevLensTypography.bodyMedium,
            color = RevLensTheme.colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = RevLensTypography.bodyMedium,
            color = RevLensTheme.colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        SecondaryButton(
            text = "Try again",
            onClick = onRetry
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = RevLensTypography.headlineMedium,
                color = RevLensTheme.colors.textPrimary
            )
        },
        text = {
            Text(
                text = body,
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textSecondary
            )
        },
        confirmButton = {
            if (isDestructive) {
                DestructiveButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.width(120.dp)
                )
            } else {
                PrimaryButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.width(120.dp)
                )
            }
        },
        dismissButton = {
            TextLinkButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = RevLensTheme.colors.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    )
}

@Composable
fun RevLensSnackbar(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor =
        if (isError) RevLensTheme.colors.error else RevLensTheme.colors.primaryAction
    val textColor = if (isError) Color.White else RevLensTheme.colors.onPrimaryAction

    androidx.compose.material3.Snackbar(
        modifier = modifier.padding(16.dp),
        containerColor = backgroundColor,
        contentColor = textColor,
        actionContentColor = RevLensTheme.colors.accentBlue,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        action = if (actionText != null) {
            {
                androidx.compose.material3.TextButton(onClick = { onAction?.invoke() }) {
                    Text(
                        text = actionText, style = RevLensTypography.labelLarge,
                        color = RevLensTheme.colors.accentBlue
                    )
                }
            }
        } else null
    ) {
        Text(text = message, style = RevLensTypography.bodyLarge, color = textColor)
    }
}
