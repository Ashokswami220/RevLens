package com.revlens.app.ui.screens.notifications

import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.domain.model.NotificationPrefs

class NotificationSettingsViewModel :
    MviViewModel<NotificationSettingsUiState, NotificationSettingsEvent, NotificationSettingsEffect>(
        NotificationSettingsUiState()
    ) {

    override fun onEvent(event: NotificationSettingsEvent) {
        val current = state.value.prefs
        val updated = when (event) {
            is NotificationSettingsEvent.TogglePush -> current.copy(pushEnabled = event.enabled)
            is NotificationSettingsEvent.ToggleEmail -> current.copy(emailEnabled = event.enabled)
            is NotificationSettingsEvent.ToggleGoalMilestones -> current.copy(
                goalMilestones = event.enabled
            )

            is NotificationSettingsEvent.ToggleOffTrackWarnings -> current.copy(
                offTrackWarnings = event.enabled
            )

            is NotificationSettingsEvent.ToggleChurnSpike -> current.copy(
                churnSpikeEnabled = event.enabled
            )

            is NotificationSettingsEvent.UpdateChurnThreshold -> current.copy(
                churnSpikeThresholdPercent = event.threshold
            )

            is NotificationSettingsEvent.ToggleMrrDrop -> current.copy(
                mrrDropEnabled = event.enabled
            )

            is NotificationSettingsEvent.UpdateMrrDropThreshold -> current.copy(
                mrrDropThresholdPercent = event.threshold
            )

            is NotificationSettingsEvent.ToggleExperimentFinished -> current.copy(
                experimentFinished = event.enabled
            )

            is NotificationSettingsEvent.ToggleWeeklySummary -> current.copy(
                weeklySummaryEnabled = event.enabled
            )

            is NotificationSettingsEvent.UpdateWeeklyDay -> current.copy(weeklyDay = event.day)
            is NotificationSettingsEvent.UpdateWeeklyTime -> current.copy(weeklyTime = event.time)
            is NotificationSettingsEvent.ToggleMonthlyReport -> current.copy(
                monthlyReportReady = event.enabled
            )

            is NotificationSettingsEvent.ToggleQuietHours -> current.copy(
                quietHoursEnabled = event.enabled
            )

            is NotificationSettingsEvent.UpdateQuietHoursTimes -> current.copy(
                quietHoursStart = event.start, quietHoursEnd = event.end
            )

            NotificationSettingsEvent.RetrySync -> current
        }

        update {
            copy(
                prefs = updated,
                syncStatus = NotificationSyncStatus.Synced,
                isPushRegistered = updated.pushEnabled
            )
        }
    }
}
