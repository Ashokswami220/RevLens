package com.example.revlens.ui.screens.notifications

import com.example.revlens.domain.model.NotificationPrefs

enum class NotificationSyncStatus {
    Synced,
    Syncing,
    Error
}

data class NotificationSettingsUiState(
    val prefs: NotificationPrefs = NotificationPrefs(),
    val isSaving: Boolean = false,
    val syncStatus: NotificationSyncStatus = NotificationSyncStatus.Synced,
    val isPushRegistered: Boolean = false
)

sealed interface NotificationSettingsEvent {
    data class TogglePush(val enabled: Boolean) : NotificationSettingsEvent
    data class ToggleEmail(val enabled: Boolean) : NotificationSettingsEvent
    data class ToggleGoalMilestones(val enabled: Boolean) : NotificationSettingsEvent
    data class ToggleOffTrackWarnings(val enabled: Boolean) : NotificationSettingsEvent
    data class ToggleChurnSpike(val enabled: Boolean) : NotificationSettingsEvent
    data class UpdateChurnThreshold(val threshold: Double) : NotificationSettingsEvent
    data class ToggleMrrDrop(val enabled: Boolean) : NotificationSettingsEvent
    data class UpdateMrrDropThreshold(val threshold: Double) : NotificationSettingsEvent
    data class ToggleExperimentFinished(val enabled: Boolean) : NotificationSettingsEvent
    data class ToggleWeeklySummary(val enabled: Boolean) : NotificationSettingsEvent
    data class UpdateWeeklyDay(val day: String) : NotificationSettingsEvent
    data class UpdateWeeklyTime(val time: String) : NotificationSettingsEvent
    data class ToggleMonthlyReport(val enabled: Boolean) : NotificationSettingsEvent
    data class ToggleQuietHours(val enabled: Boolean) : NotificationSettingsEvent
    data class UpdateQuietHoursTimes(val start: String, val end: String) : NotificationSettingsEvent
    data object RetrySync : NotificationSettingsEvent
}

sealed interface NotificationSettingsEffect {
    data class ShowToast(val message: String) : NotificationSettingsEffect
}
