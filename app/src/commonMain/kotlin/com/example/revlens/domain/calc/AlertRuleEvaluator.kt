package com.example.revlens.domain.calc

import com.example.revlens.domain.model.AlertSeverity
import com.example.revlens.domain.model.NotificationPrefs
import com.example.revlens.domain.model.TriggeredAlert
import com.example.revlens.model.BusinessProfile

object AlertRuleEvaluator {

    fun evaluate(
        prefs: NotificationPrefs,
        profile: BusinessProfile?,
        currentTimeString: String = "12:00"
    ): List<TriggeredAlert> {
        if (profile == null) return emptyList()

        val alerts = mutableListOf<TriggeredAlert>()

        // 1. Churn Spike Alert
        if (prefs.churnSpikeEnabled) {
            val churn = profile.monthlyChurnRate
            if (churn > prefs.churnSpikeThresholdPercent) {
                alerts.add(
                    TriggeredAlert(
                        id = "alert_churn_spike",
                        title = "High Churn Spike Detected",
                        message = "Monthly customer churn is currently at $churn%, which exceeds your alert threshold of ${prefs.churnSpikeThresholdPercent}%.",
                        severity = AlertSeverity.WARNING
                    )
                )
            }
        }

        // 2. Off-track Goal Alert
        if (prefs.offTrackWarnings && profile.mrrGoal != null) {
            val currentMrr = profile.currentMrr.toDouble()
            val goalMrr = profile.mrrGoal.toDouble()
            if (goalMrr > 0 && currentMrr < goalMrr * 0.70) {
                val percentOfGoal = ((currentMrr / goalMrr) * 100).toInt()
                alerts.add(
                    TriggeredAlert(
                        id = "alert_off_track_goal",
                        title = "MRR Goal Off-Track",
                        message = "Current revenue ($${currentMrr.toInt()}) is at $percentOfGoal% of your target ($${goalMrr.toInt()}).",
                        severity = AlertSeverity.INFO
                    )
                )
            }
        }

        return alerts
    }

    fun isQuietHour(
        currentTime: String,
        startTime: String,
        endTime: String
    ): Boolean {
        val currentMin = parseTimeToMinutes(currentTime) ?: return false
        val startMin = parseTimeToMinutes(startTime) ?: return false
        val endMin = parseTimeToMinutes(endTime) ?: return false

        return if (startMin <= endMin) {
            currentMin in startMin..endMin
        } else {
            // Wraps past midnight (e.g., 22:00 to 07:00)
            currentMin >= startMin || currentMin < endMin
        }
    }

    private fun parseTimeToMinutes(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val hours = parts[0].trim().toIntOrNull() ?: return null
        val mins = parts[1].trim().toIntOrNull() ?: return null
        return (hours * 60) + mins
    }
}
