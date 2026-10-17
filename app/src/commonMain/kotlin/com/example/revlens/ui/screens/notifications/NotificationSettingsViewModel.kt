package com.example.revlens.ui.screens.notifications

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.platform.LocalNotifier
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.domain.model.NotificationPrefs
import com.example.revlens.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    private val preferencesRepository: NotificationPreferencesRepository,
    private val localNotifier: LocalNotifier
) : MviViewModel<NotificationSettingsUiState, NotificationSettingsEvent, NotificationSettingsEffect>(
    NotificationSettingsUiState()
) {

    private var syncJob: Job? = null

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                update { copy(prefs = prefs, isPushRegistered = prefs.pushEnabled) }
            }
        }
    }

    override fun onEvent(event: NotificationSettingsEvent) {
        if (event is NotificationSettingsEvent.RetrySync) {
            triggerRemoteSync(state.value.prefs)
            return
        }

        val current = state.value.prefs
        val updated = when (event) {
            is NotificationSettingsEvent.TogglePush -> current.copy(pushEnabled = event.enabled)
            is NotificationSettingsEvent.ToggleEmail -> current.copy(emailEnabled = event.enabled)
            is NotificationSettingsEvent.ToggleGoalMilestones -> current.copy(goalMilestones = event.enabled)
            is NotificationSettingsEvent.ToggleOffTrackWarnings -> current.copy(offTrackWarnings = event.enabled)
            is NotificationSettingsEvent.ToggleChurnSpike -> current.copy(churnSpikeEnabled = event.enabled)
            is NotificationSettingsEvent.UpdateChurnThreshold -> current.copy(churnSpikeThresholdPercent = event.threshold)
            is NotificationSettingsEvent.ToggleMrrDrop -> current.copy(mrrDropEnabled = event.enabled)
            is NotificationSettingsEvent.UpdateMrrDropThreshold -> current.copy(mrrDropThresholdPercent = event.threshold)
            is NotificationSettingsEvent.ToggleExperimentFinished -> current.copy(experimentFinished = event.enabled)
            is NotificationSettingsEvent.ToggleWeeklySummary -> current.copy(weeklySummaryEnabled = event.enabled)
            is NotificationSettingsEvent.UpdateWeeklyDay -> current.copy(weeklyDay = event.day)
            is NotificationSettingsEvent.UpdateWeeklyTime -> current.copy(weeklyTime = event.time)
            is NotificationSettingsEvent.ToggleMonthlyReport -> current.copy(monthlyReportReady = event.enabled)
            is NotificationSettingsEvent.ToggleQuietHours -> current.copy(quietHoursEnabled = event.enabled)
            is NotificationSettingsEvent.UpdateQuietHoursTimes -> current.copy(quietHoursStart = event.start, quietHoursEnd = event.end)
            NotificationSettingsEvent.RetrySync -> current
        }

        update {
            copy(
                prefs = updated,
                syncStatus = NotificationSyncStatus.Syncing,
                isPushRegistered = updated.pushEnabled
            )
        }

        triggerRemoteSync(updated)
    }

    private fun triggerRemoteSync(prefs: NotificationPrefs) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            delay(500)
            try {
                preferencesRepository.updatePreferences(prefs)
                update { copy(syncStatus = NotificationSyncStatus.Synced) }
            } catch (_: Exception) {
                update { copy(syncStatus = NotificationSyncStatus.Error) }
                emit(NotificationSettingsEffect.ShowToast("Failed to sync notifications to cloud"))
            }
        }
    }
}
