package com.example.revlens.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.domain.calc.CanonicalField
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@Composable
fun ColumnMapperRow(
    field: CanonicalField,
    availableHeaders: List<String>,
    selectedHeader: String?,
    sampleValue: String?,
    isAutoMapped: Boolean,
    onHeaderSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = field.displayName,
                    style = RevLensTypography.titleSmall,
                    color = RevLensTheme.colors.textPrimary
                )
                if (field.isRequired) {
                    Text(
                        text = "*",
                        style = RevLensTypography.titleSmall,
                        color = RevLensTheme.colors.error
                    )
                }
                if (isAutoMapped && selectedHeader != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Auto",
                            style = RevLensTypography.labelSmall,
                            color = RevLensTheme.colors.brandPrimary
                        )
                    }
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RevLensTheme.colors.surfaceElevated)
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedHeader ?: "Select column ▾",
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedHeader != null) RevLensTheme.colors.textPrimary else RevLensTheme.colors.textSecondary
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = RevLensTheme.colors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(RevLensTheme.colors.surfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Skip field)", color = RevLensTheme.colors.textSecondary) },
                        onClick = {
                            onHeaderSelected(null)
                            expanded = false
                        }
                    )
                    availableHeaders.forEach { header ->
                        DropdownMenuItem(
                            text = { Text(header, color = RevLensTheme.colors.textPrimary) },
                            onClick = {
                                onHeaderSelected(header)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (sampleValue != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Preview: \"$sampleValue\"",
                style = RevLensTypography.bodySmall,
                color = RevLensTheme.colors.textTertiary
            )
        }
    }
}
