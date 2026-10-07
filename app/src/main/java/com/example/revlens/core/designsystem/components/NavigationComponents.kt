package com.example.revlens.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.revlens.core.designsystem.theme.RevLensTheme
import com.example.revlens.core.designsystem.theme.RevLensTypography

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
                        imageVector = Icons.Filled.ArrowBack,
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
    NavigationBar(
        modifier = modifier,
        containerColor = RevLensTheme.colors.surface,
        contentColor = RevLensTheme.colors.textSecondary,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = RevLensTypography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RevLensTheme.colors.textPrimary,
                    selectedTextColor = RevLensTheme.colors.textPrimary,
                    unselectedIconColor = RevLensTheme.colors.textTertiary,
                    unselectedTextColor = RevLensTheme.colors.textTertiary,
                    indicatorColor = Color.Transparent // Material 3 adds a pill indicator by default; this hides it as per design
                )
            )
        }
    }
}
