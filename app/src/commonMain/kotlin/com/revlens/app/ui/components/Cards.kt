package com.revlens.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    trailingAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBorder = if (isDark) null else BorderStroke(1.dp, RevLensTheme.colors.borderDefault)
    val borderModifier =
        if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(16.dp)) else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RevLensTheme.colors.surface)
            .then(borderModifier)
            .padding(16.dp)
    ) {
        if (title != null || trailingAction != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = RevLensTypography.titleMedium,
                        color = RevLensTheme.colors.textPrimary
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (trailingAction != null) {
                    trailingAction()
                }
            }
        }
        content()
    }
}