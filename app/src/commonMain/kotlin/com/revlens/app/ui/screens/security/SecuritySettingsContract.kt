package com.revlens.app.ui.screens.security

import com.revlens.app.domain.model.LockTimeout
import com.revlens.app.domain.model.UserSession

data class SecuritySettingsUiState(
    val isBiometricSupported: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val timeout: LockTimeout = LockTimeout.ONE_MINUTE,
    val isGuest: Boolean = true,
    val isGoogleConnected: Boolean = false,
    val googleAccountEmail: String? = null,
    val googleLinkedDate: String = "Active (Google OAuth 2.0)",
    val sessions: List<UserSession> = listOf(
        UserSession(
            id = "sess_current",
            deviceName = "Current Device",
            location = "Local Session",
            lastActive = "Active now",
            isCurrent = true
        )
    ),
    val showUpgradePrompt: Boolean = false
)

sealed interface SecuritySettingsEvent {
    data class ToggleBiometric(val enabled: Boolean) : SecuritySettingsEvent
    data class ChangeTimeout(val timeout: LockTimeout) : SecuritySettingsEvent
    data object LockNow : SecuritySettingsEvent
    data class RevokeSession(val sessionId: String) : SecuritySettingsEvent
    data object RevokeAllOtherSessions : SecuritySettingsEvent
    data object ClickUpgradeCloudSecurity : SecuritySettingsEvent
    data object DismissUpgradePrompt : SecuritySettingsEvent
}

sealed interface SecuritySettingsEffect {
    data class ShowToast(val message: String) : SecuritySettingsEffect
}
