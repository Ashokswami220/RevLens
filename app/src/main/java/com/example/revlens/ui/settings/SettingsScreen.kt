package com.example.revlens.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.revlens.ui.components.RevLensTopAppBar
import com.example.revlens.ui.components.SectionCard
import com.example.revlens.ui.theme.RevLensTheme
import com.example.revlens.ui.theme.RevLensTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var darkModeEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RevLensTopAppBar(title = "Settings")
        },
        containerColor = RevLensTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fake Avatar
                Column(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RevLensTheme.colors.brandPrimary)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "FD", style = RevLensTypography.headlineMedium,
                        color = RevLensTheme.colors.background
                    )
                }

                Column {
                    Text(
                        "Founder", style = RevLensTypography.titleLarge,
                        fontWeight = FontWeight.Bold, color = RevLensTheme.colors.textPrimary
                    )
                    Text(
                        "founder@startup.com", style = RevLensTypography.bodyMedium,
                        color = RevLensTheme.colors.textSecondary
                    )
                }
            }

            // Preferences
            SectionCard(title = "Preferences") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingToggleRow(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Toggle dark/light app theme",
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )

                    SettingToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = "Push Notifications",
                        subtitle = "Alerts for MRR goals & activity",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )

                    SettingActionRow(
                        icon = Icons.Filled.Language,
                        title = "Currency",
                        subtitle = "USD ($)"
                    )
                }
            }

            // Security & Privacy
            SectionCard(title = "Security & Privacy") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingActionRow(
                        icon = Icons.Filled.Lock,
                        title = "Biometric Lock",
                        subtitle = "Require FaceID to open RevLens"
                    )
                }
            }

            // About
            SectionCard(title = "About RevLens") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingActionRow(
                        icon = Icons.Filled.Info,
                        title = "Version",
                        subtitle = "1.0.0 (MVP)"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, contentDescription = null,
                tint = RevLensTheme.colors.brandPrimary
            )
            Column {
                Text(
                    text = title, style = RevLensTypography.bodyLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = subtitle, style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RevLensTheme.colors.background,
                checkedTrackColor = RevLensTheme.colors.brandPrimary
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, contentDescription = null,
                tint = RevLensTheme.colors.brandPrimary
            )
            Column {
                Text(
                    text = title, style = RevLensTypography.bodyLarge,
                    color = RevLensTheme.colors.textPrimary
                )
                Text(
                    text = subtitle, style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight, contentDescription = null,
            tint = RevLensTheme.colors.textTertiary
        )
    }
}
