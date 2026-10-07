package com.example.revlens.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    valueString: String = value.toString(),
    onValueStringChange: (String) -> Unit = {},
    caption: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = RevLensTypography.bodyLarge,
                color = RevLensTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Simplified NumberField chip for MVP
            NumberField(
                value = valueString,
                onValueChange = onValueStringChange,
                label = "",
                modifier = Modifier.width(100.dp)
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = RevLensTheme.colors.primaryAction,
                activeTrackColor = RevLensTheme.colors.brandPrimary,
                inactiveTrackColor = RevLensTheme.colors.borderDefault
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (caption != null) {
            Text(
                text = caption,
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
