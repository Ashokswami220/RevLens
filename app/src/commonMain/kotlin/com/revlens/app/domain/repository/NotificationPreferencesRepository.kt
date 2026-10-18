package com.revlens.app.domain.repository

import com.revlens.app.domain.model.NotificationPrefs
import kotlinx.coroutines.flow.Flow

interface NotificationPreferencesRepository {
    val preferencesFlow: Flow<NotificationPrefs>
    val dismissedAlertIdsFlow: Flow<Set<String>>
    suspend fun updatePreferences(prefs: NotificationPrefs)
    suspend fun dismissAlert(alertId: String)
    suspend fun resetDismissedAlerts()
}
