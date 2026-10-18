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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.core.platform.PickedFile
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun FilePickerCard(
    file: PickedFile?,
    onPickFileClick: () -> Unit,
    onRemoveFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (file == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = RevLensTheme.colors.borderDefault,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(RevLensTheme.colors.surface)
                .clickable(onClick = onPickFileClick)
                .padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.UploadFile,
                        contentDescription = "Upload",
                        tint = RevLensTheme.colors.brandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Choose CSV file",
                    style = RevLensTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = "UTF-8 encoded up to 100,000 rows",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RevLensTheme.colors.surfaceElevated)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = RevLensTheme.colors.brandPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = file.name,
                        style = RevLensTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary
                    )
                    val sizeKb = (file.sizeBytes / 1024).coerceAtLeast(1)
                    Text(
                        text = "$sizeKb KB",
                        style = RevLensTypography.bodySmall,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            IconButton(onClick = onRemoveFileClick) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove file",
                    tint = RevLensTheme.colors.textSecondary
                )
            }
        }
    }
}
