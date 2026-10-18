package com.revlens.app.domain.calc

import com.revlens.app.domain.model.AlertSeverity
import com.revlens.app.domain.model.NotificationPrefs
import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlertRuleEvaluatorTest {

    @Test
    fun testChurnSpikeTriggered() {
        val prefs = NotificationPrefs(
            churnSpikeEnabled = true,
            churnSpikeThresholdPercent = 3.0
        )
        val profile = BusinessProfile(
            monthlyChurnRate = 4.5,
            monthlyGrowthRate = 8.0,
            cac = BigDecimal("120.00"),
            fixedCosts = BigDecimal("4000.00"),
            variableCostPerCustomer = BigDecimal("8.00"),
            plans = listOf(Plan("1", "Pro", BigDecimal("49.00"), customerCount = 100))
        )

        val alerts = AlertRuleEvaluator.evaluate(prefs, profile)
        assertEquals(1, alerts.size)
        assertEquals(AlertSeverity.WARNING, alerts[0].severity)
        assertTrue(alerts[0].title.contains("Churn Spike"))
    }

    @Test
    fun testChurnSpikeNotTriggeredWhenUnderThreshold() {
        val prefs = NotificationPrefs(
            churnSpikeEnabled = true,
            churnSpikeThresholdPercent = 5.0
        )
        val profile = BusinessProfile(
            monthlyChurnRate = 3.2,
            monthlyGrowthRate = 8.0,
            cac = BigDecimal("120.00"),
            fixedCosts = BigDecimal("4000.00"),
            variableCostPerCustomer = BigDecimal("8.00"),
            plans = listOf(Plan("1", "Pro", BigDecimal("49.00"), customerCount = 100))
        )

        val alerts = AlertRuleEvaluator.evaluate(prefs, profile)
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun testQuietHoursCalculation() {
        val start = "22:00"
        val end = "07:00"

        // Overnight cases
        assertTrue(AlertRuleEvaluator.isQuietHour("22:30", start, end))
        assertTrue(AlertRuleEvaluator.isQuietHour("23:59", start, end))
        assertTrue(AlertRuleEvaluator.isQuietHour("04:15", start, end))
        assertTrue(AlertRuleEvaluator.isQuietHour("06:59", start, end))

        // Daytime non-quiet cases
        assertFalse(AlertRuleEvaluator.isQuietHour("07:01", start, end))
        assertFalse(AlertRuleEvaluator.isQuietHour("12:00", start, end))
        assertFalse(AlertRuleEvaluator.isQuietHour("21:45", start, end))

        // Same-day quiet hours (e.g., 13:00 to 15:00)
        assertTrue(AlertRuleEvaluator.isQuietHour("14:00", "13:00", "15:00"))
        assertFalse(AlertRuleEvaluator.isQuietHour("16:00", "13:00", "15:00"))
    }
}
