package com.revlens.app.domain.model

enum class LockTimeout(val displayName: String, val durationMillis: Long) {
    IMMEDIATE("Immediately", 0L),
    ONE_MINUTE("1 minute", 60_000L),
    FIVE_MINUTES("5 minutes", 300_000L),
    FIFTEEN_MINUTES("15 minutes", 900_000L)
}

data class AppLockPreferences(
    val isBiometricEnabled: Boolean = false,
    val timeout: LockTimeout = LockTimeout.ONE_MINUTE,
    val lastBackgroundTimestamp: Long = 0L,
    val isLocked: Boolean = false
)

data class UserSession(
    val id: String,
    val deviceName: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean
)
