package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

enum class DataSourceMode {
    MODELED,
    ACTUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSourceBadge(
    mode: DataSourceMode,
    modifier: Modifier = Modifier,
    actualDateLabel: String? = null
) {
    var showSheet by remember { mutableStateOf(false) }

    val isActual = mode == DataSourceMode.ACTUAL
    val bgColor = if (isActual) {
        RevLensTheme.colors.brandTertiary.copy(alpha = 0.15f)
    } else {
        RevLensTheme.colors.surfaceElevated
    }
    val contentColor = if (isActual) {
        RevLensTheme.colors.brandTertiaryText
    } else {
        RevLensTheme.colors.textSecondary
    }
    val label = if (isActual) {
        if (!actualDateLabel.isNullOrBlank()) "Your data · $actualDateLabel" else "Your data"
    } else {
        "Modeled"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(24.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { showSheet = true }
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = if (isActual) Icons.Filled.Check else Icons.Filled.Functions,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = RevLensTypography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = RevLensTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isActual) "Actual Data Active" else "Modeled Data Active",
                    style = RevLensTypography.titleLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isActual) {
                        "This report is computed directly from your uploaded subscription records and actual transaction history."
                    } else {
                        "This report is projected based on your baseline business parameters (churn rate, growth rate, pricing plans, and CAC assumptions). Import CSV data or connect an integration to view actual numbers."
                    },
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
