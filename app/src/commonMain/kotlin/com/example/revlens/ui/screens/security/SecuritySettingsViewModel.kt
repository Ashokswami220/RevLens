package com.example.revlens.ui.screens.security

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.platform.BiometricAuthenticator
import com.example.revlens.core.platform.BiometricStatus
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.domain.model.LockTimeout
import com.example.revlens.domain.repository.AppLockRepository
import com.example.revlens.domain.session.SessionManager
import kotlinx.coroutines.launch

class SecuritySettingsViewModel(
    private val appLockRepository: AppLockRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val sessionManager: SessionManager
) : MviViewModel<SecuritySettingsUiState, SecuritySettingsEvent, SecuritySettingsEffect>(
    SecuritySettingsUiState()
) {

    init {
        checkBiometricsAndSession()
        observePreferences()
    }

    private fun checkBiometricsAndSession() {
        val status = biometricAuthenticator.checkStatus()
        val isSupported = status != BiometricStatus.NotAvailable
        val isGuest = sessionManager.state.value is com.example.revlens.domain.model.SessionState.Guest
        update {
            copy(
                isBiometricSupported = isSupported,
                isGuest = isGuest
            )
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            appLockRepository.preferencesFlow.collect { prefs ->
                update {
                    copy(
                        isBiometricEnabled = prefs.isBiometricEnabled,
                        timeout = prefs.timeout
                    )
                }
            }
        }
    }

    override fun onEvent(event: SecuritySettingsEvent) {
        when (event) {
            is SecuritySettingsEvent.ToggleBiometric -> {
                viewModelScope.launch {
                    if (event.enabled) {
                        val auth = biometricAuthenticator.authenticate("Enable App Lock")
                        if (auth) {
                            appLockRepository.setBiometricEnabled(true)
                            emit(SecuritySettingsEffect.ShowToast("App Lock enabled"))
                        } else {
                            emit(SecuritySettingsEffect.ShowToast("Biometric verification cancelled"))
                        }
                    } else {
                        appLockRepository.setBiometricEnabled(false)
                        emit(SecuritySettingsEffect.ShowToast("App Lock disabled"))
                    }
                }
            }
            is SecuritySettingsEvent.ChangeTimeout -> {
                viewModelScope.launch {
                    appLockRepository.setTimeout(event.timeout)
                    emit(SecuritySettingsEffect.ShowToast("Lock timeout updated"))
                }
            }
            SecuritySettingsEvent.LockNow -> {
                viewModelScope.launch {
                    appLockRepository.setLocked(true)
                }
            }
            SecuritySettingsEvent.OpenPasswordSheet -> {
                if (state.value.isGuest) {
                    update { copy(showUpgradePrompt = true) }
                } else {
                    update { copy(showPasswordSheet = true) }
                }
            }
            SecuritySettingsEvent.DismissPasswordSheet -> update { copy(showPasswordSheet = false) }
            SecuritySettingsEvent.ToggleTwoFactor -> {
                if (state.value.isGuest) {
                    update { copy(showUpgradePrompt = true) }
                } else {
                    update { copy(twoFactorEnabled = !twoFactorEnabled) }
                }
            }
            SecuritySettingsEvent.DismissTwoFactorSheet -> update { copy(showTwoFactorSheet = false) }
            is SecuritySettingsEvent.RevokeSession -> {
                val updated = state.value.sessions.filterNot { it.id == event.sessionId }
                update { copy(sessions = updated) }
                emit(SecuritySettingsEffect.ShowToast("Session revoked"))
            }
            SecuritySettingsEvent.RevokeAllOtherSessions -> {
                val updated = state.value.sessions.filter { it.isCurrent }
                update { copy(sessions = updated) }
                emit(SecuritySettingsEffect.ShowToast("Signed out of all other devices"))
            }
            SecuritySettingsEvent.ClickUpgradeCloudSecurity -> update { copy(showUpgradePrompt = true) }
            SecuritySettingsEvent.DismissUpgradePrompt -> update { copy(showUpgradePrompt = false) }
        }
    }
}
