package com.revlens.app.domain.calc

import com.revlens.app.domain.model.Experiment
import com.revlens.app.domain.model.ExperimentStatus
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Serializable
data class ExperimentAnalysis(
    val controlTotal: Double,
    val controlSampleSize: Int,
    val treatmentTotal: Double,
    val treatmentSampleSize: Int,
    val actualLiftPercent: Double,
    val predictedLiftPercent: Double,
    val zScore: Double,
    val pValue: Double,
    val confidencePercent: Double,
    val isSignificant: Boolean,
    val isEnoughData: Boolean,
    val suggestedVerdict: ExperimentStatus,
    val daysElapsed: Int,
    val progressPercent: Float
)

object ExperimentAnalyzer {

    fun analyze(experiment: Experiment, currentDayOffset: Int = 12): ExperimentAnalysis {
        val obs = experiment.observations
        val plannedDays = experiment.plannedDurationDays.coerceAtLeast(1)
        val daysElapsed = currentDayOffset.coerceIn(0, plannedDays)
        val progress = (daysElapsed.toFloat() / plannedDays.toFloat()).coerceIn(0f, 1f)

        if (obs.isEmpty()) {
            return ExperimentAnalysis(
                controlTotal = 0.0,
                controlSampleSize = 0,
                treatmentTotal = 0.0,
                treatmentSampleSize = 0,
                actualLiftPercent = 0.0,
                predictedLiftPercent = experiment.predictedLiftPercent,
                zScore = 0.0,
                pValue = 1.0,
                confidencePercent = 0.0,
                isSignificant = false,
                isEnoughData = false,
                suggestedVerdict = ExperimentStatus.INCONCLUSIVE,
                daysElapsed = daysElapsed,
                progressPercent = progress
            )
        }

        val totalControlSample = obs.sumOf { it.controlSampleSize }
        val totalTreatmentSample = obs.sumOf { it.treatmentSampleSize }

        val avgControlValue = if (obs.isNotEmpty()) obs.map { it.controlValue }.average() else 0.0
        val avgTreatmentValue = if (obs.isNotEmpty()) obs.map { it.treatmentValue }.average() else 0.0

        val liftPercent = if (avgControlValue > 0.0) {
            ((avgTreatmentValue - avgControlValue) / avgControlValue * 100.0)
        } else {
            0.0
        }

        // Two-sample z-score calculation
        val nC = totalControlSample.toDouble().coerceAtLeast(1.0)
        val nT = totalTreatmentSample.toDouble().coerceAtLeast(1.0)

        // Estimated standard error
        val pPooled = (avgControlValue * nC + avgTreatmentValue * nT) / (nC + nT)
        val se = sqrt(abs(pPooled * (1.0 - (pPooled / 1000.0).coerceIn(0.01, 0.99))) * (1.0 / nC + 1.0 / nT)).coerceAtLeast(0.001)

        val zScore = (avgTreatmentValue - avgControlValue) / se
        val pValue = computeTwoTailedPValue(zScore)
        val confidence = ((1.0 - pValue) * 100.0).coerceIn(0.0, 99.9)

        val isEnoughData = totalControlSample >= 30 && totalTreatmentSample >= 30 && obs.size >= 2
        val isSignificant = isEnoughData && pValue <= 0.05

        val verdict = when {
            !isEnoughData -> ExperimentStatus.INCONCLUSIVE
            isSignificant && liftPercent >= experiment.successThresholdPercent -> ExperimentStatus.WON
            isSignificant && liftPercent < 0.0 -> ExperimentStatus.LOST
            else -> ExperimentStatus.INCONCLUSIVE
        }

        return ExperimentAnalysis(
            controlTotal = (avgControlValue * 10).roundToInt() / 10.0,
            controlSampleSize = totalControlSample,
            treatmentTotal = (avgTreatmentValue * 10).roundToInt() / 10.0,
            treatmentSampleSize = totalTreatmentSample,
            actualLiftPercent = (liftPercent * 10).roundToInt() / 10.0,
            predictedLiftPercent = experiment.predictedLiftPercent,
            zScore = (zScore * 100).roundToInt() / 100.0,
            pValue = (pValue * 1000).roundToInt() / 1000.0,
            confidencePercent = (confidence * 10).roundToInt() / 10.0,
            isSignificant = isSignificant,
            isEnoughData = isEnoughData,
            suggestedVerdict = verdict,
            daysElapsed = daysElapsed,
            progressPercent = progress
        )
    }

    /**
     * Approximates standard normal two-tailed p-value using Abramowitz & Stegun polynomial.
     */
    fun computeTwoTailedPValue(z: Double): Double {
        val absZ = abs(z)
        val t = 1.0 / (1.0 + 0.2316419 * absZ)
        val d = 0.3989422804014327 * exp(-absZ * absZ / 2.0)
        val prob = d * t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + 1.330274429 * t))))
        val twoTailed = 2.0 * prob
        return twoTailed.coerceIn(0.0001, 1.0)
    }
}
