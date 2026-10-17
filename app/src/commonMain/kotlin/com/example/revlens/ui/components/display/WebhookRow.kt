package com.example.revlens.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.model.WebhookEndpoint
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WebhookRow(
    endpoint: WebhookEndpoint,
    onRotateSecret: () -> Unit,
    onDelete: () -> Unit,
    onCopySecret: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = endpoint.description,
                        style = RevLensTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.success.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = RevLensTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.success
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete webhook",
                        tint = RevLensTheme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // URL
            Text(
                text = endpoint.url,
                style = RevLensTypography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = RevLensTheme.colors.accentBlue
            )

            // Subscribed Events
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Subscribed Events:",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    endpoint.events.forEach { event ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RevLensTheme.colors.surfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event,
                                style = RevLensTypography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Signing Secret Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RevLensTheme.colors.surfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Secret:",
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                    Text(
                        text = "${endpoint.secret.take(8)}••••••••",
                        style = RevLensTypography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = RevLensTheme.colors.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopySecret,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy secret",
                            tint = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onRotateSecret,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Rotate secret",
                            tint = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
