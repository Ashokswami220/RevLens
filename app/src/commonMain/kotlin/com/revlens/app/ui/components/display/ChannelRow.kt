package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.ChannelPlan
import com.revlens.app.ui.components.LabeledSlider
import com.revlens.app.ui.components.NumberField
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun ChannelRow(
    channel: ChannelPlan,
    currencySymbol: String = "$",
    onSpendChange: (Long) -> Unit,
    onCacChange: (Long) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Top row: Name, Switch, Delete button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = channel.name,
                style = RevLensTypography.titleMedium,
                color = if (channel.isEnabled) RevLensTheme.colors.textPrimary else RevLensTheme.colors.textTertiary,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = channel.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = RevLensTheme.colors.surface,
                        checkedTrackColor = RevLensTheme.colors.brandPrimary,
                        uncheckedThumbColor = RevLensTheme.colors.textTertiary,
                        uncheckedTrackColor = RevLensTheme.colors.surfaceElevated
                    )
                )

                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove channel",
                            tint = RevLensTheme.colors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Monthly Budget Slider
        val spendDollars = (channel.monthlySpendMinor / 100).toFloat()
        LabeledSlider(
            label = "Monthly Budget",
            value = spendDollars,
            onValueChange = { onSpendChange((it.toLong() * 100L)) },
            valueRange = 0f..25000f,
            valueString = "$currencySymbol${spendDollars.toLong()}",
            enabled = channel.isEnabled
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Target CAC Input & Estimated Customers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cacDollars = (channel.targetCacMinor / 100).toString()
            NumberField(
                value = cacDollars,
                onValueChange = {
                    val raw = it.toDoubleOrNull() ?: 0.0
                    onCacChange((raw * 100).toLong())
                },
                label = "Target CAC",
                suffix = currencySymbol,
                modifier = Modifier.weight(1f)
            )

            // Result pill
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RevLensTheme.colors.surfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Est. Acquired",
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
                Text(
                    text = "${channel.estimatedCustomers} / mo",
                    style = RevLensTypography.titleSmall,
                    color = if (channel.isEnabled && channel.estimatedCustomers > 0) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textTertiary
                )
            }
        }
    }
}
