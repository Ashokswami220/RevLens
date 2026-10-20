package com.revlens.app.ui.components.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.components.DestructiveButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.components.RevLensTextField
import com.revlens.app.ui.components.TextLinkButton
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun TypedConfirmDialog(
    title: String,
    body: String,
    requiredWord: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = true
) {
    var typedText by rememberSaveable { mutableStateOf("") }
    val isMatch = typedText.trim() == requiredWord.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = RevLensTypography.headlineMedium,
                color = RevLensTheme.colors.textPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    text = body,
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Type \"$requiredWord\" to confirm:",
                    style = RevLensTypography.labelMedium,
                    color = RevLensTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                RevLensTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    label = "",
                    placeholder = requiredWord
                )
            }
        },
        confirmButton = {
            if (isDestructive) {
                DestructiveButton(
                    text = confirmText,
                    onClick = {
                        if (isMatch) {
                            onConfirm()
                            onDismiss()
                        }
                    },
                    enabled = isMatch,
                    modifier = Modifier.width(130.dp)
                )
            } else {
                PrimaryButton(
                    text = confirmText,
                    onClick = {
                        if (isMatch) {
                            onConfirm()
                            onDismiss()
                        }
                    },
                    enabled = isMatch,
                    modifier = Modifier.width(130.dp)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}
