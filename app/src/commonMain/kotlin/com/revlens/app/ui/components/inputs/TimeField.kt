package com.revlens.app.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun TimeField(
    label: String,
    time: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RevLensTheme.colors.surfaceElevated)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = label,
                    style = RevLensTypography.labelSmall,
                    color = RevLensTheme.colors.textSecondary
                )
                Text(
                    text = time,
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            }
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = RevLensTheme.colors.brandPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showDialog) {
        val parts = time.split(":")
        var hour by remember { mutableStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 22) }
        var min by remember { mutableStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 0) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = RevLensTheme.colors.surface,
            title = {
                Text(
                    text = "Select $label",
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.textPrimary
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { hour = (hour + 1) % 24 }) {
                            Icon(Icons.Filled.Add, contentDescription = "Hour +", tint = RevLensTheme.colors.textPrimary)
                        }
                        Text(
                            text = if (hour < 10) "0$hour" else "$hour",
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        IconButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Hour -", tint = RevLensTheme.colors.textPrimary)
                        }
                    }

                    Text(
                        text = ":",
                        style = RevLensTypography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Minute picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { min = (min + 15) % 60 }) {
                            Icon(Icons.Filled.Add, contentDescription = "Min +", tint = RevLensTheme.colors.textPrimary)
                        }
                        Text(
                            text = if (min < 10) "0$min" else "$min",
                            style = RevLensTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RevLensTheme.colors.textPrimary
                        )
                        IconButton(onClick = { min = if (min < 15) 45 else min - 15 }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Min -", tint = RevLensTheme.colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hStr = if (hour < 10) "0$hour" else "$hour"
                        val mStr = if (min < 10) "0$min" else "$min"
                        onTimeChange("$hStr:$mStr")
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RevLensTheme.colors.brandPrimary,
                        contentColor = RevLensTheme.colors.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = RevLensTheme.colors.textSecondary)
                }
            }
        )
    }
}
