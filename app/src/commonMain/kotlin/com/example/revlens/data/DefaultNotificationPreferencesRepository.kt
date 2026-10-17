package com.example.revlens.data

import com.example.revlens.domain.model.NotificationPrefs
import com.example.revlens.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultNotificationPreferencesRepository : NotificationPreferencesRepository {

    private val _preferencesFlow = MutableStateFlow(NotificationPrefs())
    override val preferencesFlow: Flow<NotificationPrefs> = _preferencesFlow.asStateFlow()

    private val _dismissedAlertIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    override val dismissedAlertIdsFlow: Flow<Set<String>> = _dismissedAlertIdsFlow.asStateFlow()

    override suspend fun updatePreferences(prefs: NotificationPrefs) {
        _preferencesFlow.value = prefs
    }

    override suspend fun dismissAlert(alertId: String) {
        _dismissedAlertIdsFlow.update { it + alertId }
    }

    override suspend fun resetDismissedAlerts() {
        _dismissedAlertIdsFlow.value = emptySet()
    }
}
