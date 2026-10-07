package com.example.revlens.core.designsystem.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.revlens.core.designsystem.theme.RevLensTheme
import com.example.revlens.core.designsystem.theme.RevLensTypography

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    trailingAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RevLensTheme.colors.surfaceElevated)
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

@Composable
fun KpiCard(
    label: String,
    value: String,
    delta: String,
    deltaType: DeltaType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    sparkline: @Composable (() -> Unit)? = null
) {
    SectionCard(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column {
            Text(
                text = label,
                style = RevLensTypography.labelMedium,
                color = RevLensTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = RevLensTypography.headlineMedium,
                color = RevLensTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DeltaChip(delta = delta, type = deltaType)
                if (sparkline != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.height(32.dp)) {
                        sparkline()
                    }
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    delta: String,
    deltaType: DeltaType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    KpiCard(
        label = label,
        value = value,
        delta = delta,
        deltaType = deltaType,
        modifier = modifier.heightIn(min = 120.dp),
        onClick = onClick
    )
}

@Composable
fun ScenarioCard(
    title: String,
    subtitle: String,
    delta: String,
    deltaType: DeltaType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorDot: Color? = null
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(RevLensTheme.colors.surfaceElevated)
        .clickable(onClick = onClick)
        .then(
            if (selected) Modifier.border(
                2.dp, RevLensTheme.colors.primaryAction, RoundedCornerShape(16.dp)
            )
            else Modifier
        )
        .padding(16.dp)

    Row(
        modifier = cardModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = RevLensTheme.colors.primaryAction,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        } else if (colorDot != null) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorDot)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = RevLensTypography.titleMedium,
                color = RevLensTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        DeltaChip(delta = delta, type = deltaType)
    }
}

@Composable
fun PlanCard(
    name: String,
    priceBilling: String,
    customerCount: Int,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        trailingAction = {
            IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Plan",
                    tint = RevLensTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    style = RevLensTypography.titleMedium,
                    color = RevLensTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = priceBilling,
                    style = RevLensTypography.bodyMedium,
                    color = RevLensTheme.colors.textSecondary
                )
            }
            Text(
                text = "$customerCount cust.",
                style = RevLensTypography.bodyMedium,
                color = RevLensTheme.colors.textSecondary
            )
        }
    }
}
