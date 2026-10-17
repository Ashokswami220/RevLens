package com.example.revlens.domain.calc

import com.example.revlens.domain.model.Experiment
import com.example.revlens.domain.model.ExperimentMetric
import com.example.revlens.domain.model.ExperimentStatus
import com.example.revlens.domain.model.ExperimentType
import com.example.revlens.domain.model.Observation
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExperimentAnalyzerTest {

    @Test
    fun testPValueApproximationAccuracy() {
        // At z = 0, p-value should be 1.0
        val p0 = ExperimentAnalyzer.computeTwoTailedPValue(0.0)
        assertTrue(abs(p0 - 1.0) < 0.01)

        // At z = 1.96, p-value should be ~ 0.05
        val p196 = ExperimentAnalyzer.computeTwoTailedPValue(1.96)
        assertTrue(abs(p196 - 0.05) < 0.01)

        // At z = 2.576, p-value should be ~ 0.01
        val p258 = ExperimentAnalyzer.computeTwoTailedPValue(2.576)
        assertTrue(abs(p258 - 0.01) < 0.005)
    }

    @Test
    fun testSignificantPositiveExperiment() {
        val observations = listOf(
            Observation("o1", "2026-10-01", controlValue = 100.0, controlSampleSize = 100, treatmentValue = 115.0, treatmentSampleSize = 100),
            Observation("o2", "2026-10-02", controlValue = 102.0, controlSampleSize = 120, treatmentValue = 118.0, treatmentSampleSize = 120)
        )

        val experiment = Experiment(
            id = "exp_1",
            workspaceId = "ws_1",
            name = "Pro Price to $39",
            type = ExperimentType.PRICE_TEST,
            metric = ExperimentMetric.MRR,
            predictedLiftPercent = 14.0,
            successThresholdPercent = 5.0,
            startDate = "2026-10-01",
            plannedDurationDays = 30,
            observations = observations
        )

        val analysis = ExperimentAnalyzer.analyze(experiment, currentDayOffset = 15)

        assertTrue(analysis.actualLiftPercent > 10.0)
        assertTrue(analysis.isEnoughData)
        assertTrue(analysis.confidencePercent > 90.0)
        assertEquals(ExperimentStatus.WON, analysis.suggestedVerdict)
        assertEquals(0.5f, analysis.progressPercent)
    }

    @Test
    fun testInsufficientDataExperiment() {
        val observations = listOf(
            Observation("o1", "2026-10-01", controlValue = 50.0, controlSampleSize = 5, treatmentValue = 70.0, treatmentSampleSize = 5)
        )

        val experiment = Experiment(
            id = "exp_2",
            workspaceId = "ws_1",
            name = "Small Sample Test",
            type = ExperimentType.NEW_PLAN,
            metric = ExperimentMetric.CONVERSION,
            predictedLiftPercent = 10.0,
            successThresholdPercent = 5.0,
            startDate = "2026-10-01",
            observations = observations
        )

        val analysis = ExperimentAnalyzer.analyze(experiment)

        assertFalse(analysis.isEnoughData)
        assertFalse(analysis.isSignificant)
        assertEquals(ExperimentStatus.INCONCLUSIVE, analysis.suggestedVerdict)
    }
}
