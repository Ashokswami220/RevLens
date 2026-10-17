package com.example.revlens.data

import com.example.revlens.domain.model.AppLockPreferences
import com.example.revlens.domain.model.LockTimeout
import com.example.revlens.domain.repository.AppLockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultAppLockRepository : AppLockRepository {

    private val _preferencesFlow = MutableStateFlow(AppLockPreferences())
    override val preferencesFlow: Flow<AppLockPreferences> = _preferencesFlow.asStateFlow()

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        _preferencesFlow.update { it.copy(isBiometricEnabled = enabled, isLocked = false) }
    }

    override suspend fun setTimeout(timeout: LockTimeout) {
        _preferencesFlow.update { it.copy(timeout = timeout) }
    }

    override suspend fun setLocked(locked: Boolean) {
        _preferencesFlow.update { it.copy(isLocked = locked) }
    }

    override suspend fun recordBackground(timestampMillis: Long) {
        _preferencesFlow.update { it.copy(lastBackgroundTimestamp = timestampMillis) }
    }

    override suspend fun checkAndApplyTimeout(currentTimestampMillis: Long): Boolean {
        val current = _preferencesFlow.value
        if (!current.isBiometricEnabled) return false

        val elapsed = currentTimestampMillis - current.lastBackgroundTimestamp
        val shouldLock = current.lastBackgroundTimestamp > 0 && elapsed >= current.timeout.durationMillis
        if (shouldLock) {
            _preferencesFlow.update { it.copy(isLocked = true) }
        }
        return shouldLock
    }
}
