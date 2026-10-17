package com.example.revlens.ui.screens.security

import com.example.revlens.domain.model.LockTimeout
import com.example.revlens.domain.model.UserSession

data class SecuritySettingsUiState(
    val isBiometricSupported: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val timeout: LockTimeout = LockTimeout.ONE_MINUTE,
    val isGuest: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    val sessions: List<UserSession> = listOf(
        UserSession(
            id = "sess_current",
            deviceName = "Current Device",
            location = "Local Session",
            lastActive = "Active now",
            isCurrent = true
        )
    ),
    val showPasswordSheet: Boolean = false,
    val showTwoFactorSheet: Boolean = false,
    val showUpgradePrompt: Boolean = false
)

sealed interface SecuritySettingsEvent {
    data class ToggleBiometric(val enabled: Boolean) : SecuritySettingsEvent
    data class ChangeTimeout(val timeout: LockTimeout) : SecuritySettingsEvent
    data object LockNow : SecuritySettingsEvent
    data object OpenPasswordSheet : SecuritySettingsEvent
    data object DismissPasswordSheet : SecuritySettingsEvent
    data object ToggleTwoFactor : SecuritySettingsEvent
    data object DismissTwoFactorSheet : SecuritySettingsEvent
    data class RevokeSession(val sessionId: String) : SecuritySettingsEvent
    data object RevokeAllOtherSessions : SecuritySettingsEvent
    data object ClickUpgradeCloudSecurity : SecuritySettingsEvent
    data object DismissUpgradePrompt : SecuritySettingsEvent
}

sealed interface SecuritySettingsEffect {
    data class ShowToast(val message: String) : SecuritySettingsEffect
}
