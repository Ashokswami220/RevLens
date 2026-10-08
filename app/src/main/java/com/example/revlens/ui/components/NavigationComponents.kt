package com.example.revlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevLensTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = RevLensTypography.titleLarge,
                color = RevLensTheme.colors.textPrimary
            )
        },
        modifier = modifier.fillMaxWidth(),
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RevLensTheme.colors.textPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = RevLensTheme.colors.background,
            scrolledContainerColor = RevLensTheme.colors.background,
            navigationIconContentColor = RevLensTheme.colors.textPrimary,
            titleContentColor = RevLensTheme.colors.textPrimary,
            actionIconContentColor = RevLensTheme.colors.textPrimary
        )
    )
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun RevLensBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 12.dp)
            .fillMaxWidth()
            .background(RevLensTheme.colors.surface)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val color = if (isSelected) RevLensTheme.colors.brandPrimary
                else RevLensTheme.colors.textTertiary

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                onNavigate(item.route)
                            }
                        }
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Active indicator line sitting exactly on the top edge
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RevLensTheme.colors.brandPrimary)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.label,
                        style = RevLensTypography.labelMedium,
                        color = color
                    )
                }
            }
        }
    }
}
