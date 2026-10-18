package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun CsvPreviewTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RevLensTheme.colors.surface)
            .padding(12.dp)
            .horizontalScroll(scrollState)
    ) {
        Column {
            // Header Row
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        style = RevLensTypography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RevLensTheme.colors.textPrimary,
                        modifier = Modifier
                            .width(130.dp)
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = RevLensTheme.colors.divider)

            // Rows (preview up to 5)
            rows.take(5)
                .forEach { row ->
                    Row(modifier = Modifier.padding(vertical = 6.dp)) {
                        headers.indices.forEach { colIndex ->
                            val cellValue = row.getOrNull(colIndex) ?: ""
                            Text(
                                text = cellValue,
                                style = RevLensTypography.bodySmall,
                                color = RevLensTheme.colors.textSecondary,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(130.dp)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = RevLensTheme.colors.divider.copy(alpha = 0.5f))
                }
        }
    }
}
