package com.example.revlens.domain.repository

import com.example.revlens.domain.model.AppLockPreferences
import com.example.revlens.domain.model.LockTimeout
import kotlinx.coroutines.flow.Flow

interface AppLockRepository {
    val preferencesFlow: Flow<AppLockPreferences>
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setTimeout(timeout: LockTimeout)
    suspend fun setLocked(locked: Boolean)
    suspend fun recordBackground(timestampMillis: Long)
    suspend fun checkAndApplyTimeout(currentTimestampMillis: Long): Boolean
}
