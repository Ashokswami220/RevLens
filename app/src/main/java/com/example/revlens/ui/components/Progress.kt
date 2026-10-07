package com.example.revlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
        color = RevLensTheme.colors.brandPrimary,
        trackColor = RevLensTheme.colors.surfaceElevated,
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun StepProgressBar(
    steps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until steps) {
            val color =
                if (i < currentStep) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.surfaceElevated
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(color)
            )
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(160.dp)) {
        androidx.compose.material3.CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = RevLensTheme.colors.brandPrimary,
            trackColor = RevLensTheme.colors.surfaceElevated,
            strokeWidth = 14.dp,
            strokeCap = StrokeCap.Round
        )
        androidx.compose.material3.Text(
            text = "${(progress * 100).toInt()}%",
            style = com.example.revlens.ui.theme.RevLensTypography.displayLarge,
            color = RevLensTheme.colors.textPrimary
        )
    }
}
