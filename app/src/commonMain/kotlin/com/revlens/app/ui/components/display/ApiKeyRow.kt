package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
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
import com.revlens.app.domain.model.ApiKey
import com.revlens.app.domain.model.ApiKeyScope
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun ApiKeyRow(
    apiKey: ApiKey,
    onDelete: () -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        tint = RevLensTheme.colors.brandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = apiKey.name,
                            style = RevLensTypography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (apiKey.scope == ApiKeyScope.READ_WRITE) {
                                        RevLensTheme.colors.accentBlue.copy(alpha = 0.15f)
                                    } else {
                                        RevLensTheme.colors.surfaceElevated
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = apiKey.scope.label,
                                style = RevLensTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (apiKey.scope == ApiKeyScope.READ_WRITE) {
                                    RevLensTheme.colors.accentBlue
                                } else {
                                    RevLensTheme.colors.textSecondary
                                }
                            )
                        }
                    }

                    Text(
                        text = "${apiKey.prefix}••••••••••••••••",
                        style = RevLensTypography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = RevLensTheme.colors.textSecondary
                    )

                    Text(
                        text = "Created: ${apiKey.createdAt} • Last used: ${apiKey.lastUsedAt ?: "Never"}",
                        style = RevLensTypography.labelSmall,
                        color = RevLensTheme.colors.textTertiary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete API key",
                    tint = RevLensTheme.colors.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
