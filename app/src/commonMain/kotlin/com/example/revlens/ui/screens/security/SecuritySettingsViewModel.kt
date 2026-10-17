package com.example.revlens.ui.screens.security

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.platform.BiometricAuthenticator
import com.example.revlens.core.platform.BiometricStatus
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.domain.model.LockTimeout
import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.model.UserSession
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
        val session = sessionManager.state.value
        val isGuest = session !is SessionState.Authenticated

        val (googleEmail, isConnected) = when (session) {
            is SessionState.Authenticated -> session.user.email to true
            else -> null to false
        }

        val defaultSessions = if (isGuest) {
            listOf(
                UserSession(
                    id = "sess_current",
                    deviceName = "Current Device (Local)",
                    location = "Local Sandbox",
                    lastActive = "Active now",
                    isCurrent = true
                )
            )
        } else {
            listOf(
                UserSession(
                    id = "sess_current",
                    deviceName = "Current Device",
                    location = "San Francisco, CA",
                    lastActive = "Active now",
                    isCurrent = true
                ),
                UserSession(
                    id = "sess_web_mac",
                    deviceName = "Chrome on macOS (Web Wasm)",
                    location = "San Francisco, CA",
                    lastActive = "2 hours ago",
                    isCurrent = false
                ),
                UserSession(
                    id = "sess_mobile",
                    deviceName = "Pixel 8 Pro · Android App",
                    location = "San Jose, CA",
                    lastActive = "Yesterday",
                    isCurrent = false
                )
            )
        }

        update {
            copy(
                isBiometricSupported = isSupported,
                isGuest = isGuest,
                isGoogleConnected = isConnected,
                googleAccountEmail = googleEmail,
                sessions = defaultSessions
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
            is SecuritySettingsEvent.RevokeSession -> {
                val updated = state.value.sessions.filterNot { it.id == event.sessionId }
                update { copy(sessions = updated) }
                emit(SecuritySettingsEffect.ShowToast("Session revoked"))
            }
            SecuritySettingsEvent.RevokeAllOtherSessions -> {
                if (state.value.isGuest) {
                    update { copy(showUpgradePrompt = true) }
                } else {
                    val currentOnly = state.value.sessions.filter { it.isCurrent }
                    update { copy(sessions = currentOnly) }
                    emit(SecuritySettingsEffect.ShowToast("Signed out of all other sessions"))
                }
            }
            SecuritySettingsEvent.ClickUpgradeCloudSecurity -> {
                update { copy(showUpgradePrompt = true) }
            }
            SecuritySettingsEvent.DismissUpgradePrompt -> {
                update { copy(showUpgradePrompt = false) }
            }
        }
    }
}
