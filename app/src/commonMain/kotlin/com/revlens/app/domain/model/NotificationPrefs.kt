package com.revlens.app.domain.model

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class TriggeredAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long = 1760000000L
)

data class NotificationPrefs(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val goalMilestones: Boolean = true,
    val offTrackWarnings: Boolean = true,
    val churnSpikeEnabled: Boolean = true,
    val churnSpikeThresholdPercent: Double = 3.0,
    val mrrDropEnabled: Boolean = true,
    val mrrDropThresholdPercent: Double = -5.0,
    val experimentFinished: Boolean = true,
    val weeklySummaryEnabled: Boolean = true,
    val weeklyDay: String = "Mon",
    val weeklyTime: String = "09:00",
    val monthlyReportReady: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00"
)
