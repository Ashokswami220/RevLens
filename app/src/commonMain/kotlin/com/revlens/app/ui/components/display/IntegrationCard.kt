package com.revlens.app.ui.components.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.ConnectionStatus
import com.revlens.app.domain.model.IntegrationConnection
import com.revlens.app.domain.model.IntegrationProvider
import com.revlens.app.ui.components.ActionCardButton
import com.revlens.app.ui.components.PrimaryButton
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun IntegrationCard(
    connection: IntegrationConnection,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
    isSyncing: Boolean = false
) {
    val isConnected = connection.status == ConnectionStatus.CONNECTED
    val provider = connection.provider

    val providerBadgeColor = when (provider) {
        IntegrationProvider.STRIPE -> Color(0xFF635BFF)
        IntegrationProvider.PADDLE -> Color(0xFF00D1B2)
        IntegrationProvider.GOOGLE_SHEETS -> Color(0xFF34A853)
        IntegrationProvider.SLACK -> Color(0xFFECB22E)
    }

    val initials = when (provider) {
        IntegrationProvider.STRIPE -> "ST"
        IntegrationProvider.PADDLE -> "PD"
        IntegrationProvider.GOOGLE_SHEETS -> "GS"
        IntegrationProvider.SLACK -> "SL"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RevLensTheme.colors.surface),
        border = BorderStroke(
            1.dp,
            if (isConnected) RevLensTheme.colors.brandPrimary.copy(
                alpha = 0.5f
            ) else RevLensTheme.colors.borderDefault
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(providerBadgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = providerBadgeColor
                        )
                    }

                    Column {
                        Text(
                            text = provider.displayName,
                            style = RevLensTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        Text(
                            text = provider.category.label,
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.textSecondary
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isConnected) RevLensTheme.colors.success.copy(alpha = 0.15f)
                            else RevLensTheme.colors.surfaceElevated
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isConnected) "CONNECTED" else "NOT CONNECTED",
                        style = RevLensTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) RevLensTheme.colors.success else RevLensTheme.colors.textSecondary
                    )
                }
            }

            // Description
            Text(
                text = provider.description,
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textSecondary
            )

            // Connected details
            if (isConnected) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RevLensTheme.colors.surfaceElevated)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (connection.accountName != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Account:",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Text(
                                text = connection.accountName,
                                style = RevLensTypography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }

                    if (connection.lastSyncAt != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Last synced:",
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary
                            )
                            Text(
                                text = connection.lastSyncAt,
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionCardButton(
                        text = if (isSyncing) "Syncing..." else "Sync Now",
                        onClick = onSync,
                        icon = if (isSyncing) null else Icons.Filled.Refresh,
                        isLoading = isSyncing,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = onDisconnect,
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text(
                            text = "Disconnect",
                            style = RevLensTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RevLensTheme.colors.error
                        )
                    }
                }
            } else {
                PrimaryButton(
                    text = "Connect ${provider.displayName}",
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
