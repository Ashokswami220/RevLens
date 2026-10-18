package com.revlens.app.domain.repository

import com.revlens.app.domain.model.AppLockPreferences
import com.revlens.app.domain.model.LockTimeout
import kotlinx.coroutines.flow.Flow

interface AppLockRepository {
    val preferencesFlow: Flow<AppLockPreferences>
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setTimeout(timeout: LockTimeout)
    suspend fun setLocked(locked: Boolean)
    suspend fun recordBackground(timestampMillis: Long)
    suspend fun checkAndApplyTimeout(currentTimestampMillis: Long): Boolean
}
