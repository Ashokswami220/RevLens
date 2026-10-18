package com.revlens.app.domain.calc

import com.revlens.app.domain.model.Dataset
import com.revlens.app.domain.model.SubscriptionRecord
import com.revlens.app.model.BusinessProfile
import com.revlens.app.ui.components.display.DataSourceMode
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

enum class CohortMetric(val label: String) {
    LOGO_RETENTION("Logo Retention"),
    REVENUE_RETENTION("Revenue Retention")
}

enum class CohortGranularity(val label: String) {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly")
}

@Serializable
data class CohortCell(
    val offsetIndex: Int, // 0 for M0, 1 for M1, etc.
    val retainedValue: Double, // customer count or MRR dollars
    val retentionPercent: Double, // 100.0, 91.2, etc.
    val isYounger: Boolean, // true if cohort hasn't reached this month yet
    val isSmallSample: Boolean // < 20 customers
)

@Serializable
data class CohortRow(
    val cohortLabel: String, // e.g. "Jan 26"
    val initialSize: Int,
    val initialMrrMinor: Long,
    val cells: List<CohortCell>
)

@Serializable
data class CohortMatrixResult(
    val sourceMode: DataSourceMode,
    val rows: List<CohortRow>,
    val averageCurve: List<Double>, // percentage at each offset M0, M1, ...
    val bestCohortInsight: String,
    val modeledRetentionCurve: List<Double>? = null
)

object CohortCalculator {

    fun build(
        profile: BusinessProfile?,
        dataset: Dataset?,
        metric: CohortMetric = CohortMetric.LOGO_RETENTION,
        granularity: CohortGranularity = CohortGranularity.MONTHLY,
        maxOffsets: Int = 12
    ): CohortMatrixResult {
        val hasActual = dataset != null && dataset.subscriptions.isNotEmpty()

        return if (hasActual) {
            buildActualMatrix(dataset!!, metric, maxOffsets)
        } else {
            buildModeledCurve(profile, maxOffsets)
        }
    }

    private fun buildActualMatrix(
        dataset: Dataset,
        metric: CohortMetric,
        maxOffsets: Int
    ): CohortMatrixResult {
        val subscriptions = dataset.subscriptions

        // Group by signup month YYYY-MM
        val cohortsByMonth: Map<String, List<SubscriptionRecord>> = subscriptions.groupBy {
            val date = it.startDate
            if (date.length >= 7) date.substring(0, 7) else "2026-01"
        }

        val cohortKeys: List<String> = cohortsByMonth.keys.sorted()
        val rows = ArrayList<CohortRow>(cohortKeys.size)

        for ((cohortIndex, cohortMonth) in cohortKeys.withIndex()) {
            val cohortSubs: List<SubscriptionRecord> = cohortsByMonth[cohortMonth] ?: emptyList()
            val initialSize = cohortSubs.size
            val initialMrr: Long = cohortSubs.sumOf { it.mrrMinor }
            val isSmall = initialSize < 20

            val cells = ArrayList<CohortCell>(maxOffsets)
            val availableMonths = cohortKeys.size - cohortIndex

            for (offset in 0 until maxOffsets) {
                if (offset >= availableMonths) {
                    // Younger cohort, not yet reached
                    cells.add(
                        CohortCell(
                            offsetIndex = offset,
                            retainedValue = 0.0,
                            retentionPercent = 0.0,
                            isYounger = true,
                            isSmallSample = isSmall
                        )
                    )
                } else if (offset == 0) {
                    val initialVal =
                        if (metric == CohortMetric.LOGO_RETENTION) initialSize.toDouble() else (initialMrr / 100.0)
                    cells.add(
                        CohortCell(
                            offsetIndex = 0,
                            retainedValue = initialVal,
                            retentionPercent = 100.0,
                            isYounger = false,
                            isSmallSample = isSmall
                        )
                    )
                } else {
                    // Subscriptions retained at this month offset
                    val retainedSubs: List<SubscriptionRecord> =
                        cohortSubs.filter { sub: SubscriptionRecord ->
                            if (sub.endDate == null) {
                                true
                            } else {
                                val endMonth = if (sub.endDate.length >= 7) sub.endDate.substring(
                                    0, 7
                                ) else "9999-12"
                                val diffMonths = monthsBetween(cohortMonth, endMonth)
                                diffMonths >= offset
                            }
                        }

                    val retainedCount = retainedSubs.size
                    val retainedMrr: Long = retainedSubs.sumOf { it.mrrMinor }

                    val percent = if (metric == CohortMetric.LOGO_RETENTION) {
                        if (initialSize > 0) (retainedCount.toDouble() / initialSize * 100.0) else 0.0
                    } else {
                        if (initialMrr > 0) (retainedMrr.toDouble() / initialMrr * 100.0) else 0.0
                    }

                    val value =
                        if (metric == CohortMetric.LOGO_RETENTION) retainedCount.toDouble() else (retainedMrr / 100.0)
                    cells.add(
                        CohortCell(
                            offsetIndex = offset,
                            retainedValue = (value * 10).roundToInt() / 10.0,
                            retentionPercent = (percent * 10).roundToInt() / 10.0,
                            isYounger = false,
                            isSmallSample = isSmall
                        )
                    )
                }
            }

            rows.add(
                CohortRow(
                    cohortLabel = formatMonthLabel(cohortMonth),
                    initialSize = initialSize,
                    initialMrrMinor = initialMrr,
                    cells = cells
                )
            )
        }

        // Calculate average retention per offset across all non-younger cohorts
        val averageCurve = ArrayList<Double>(maxOffsets)
        for (offset in 0 until maxOffsets) {
            val validCells: List<Double> = rows.mapNotNull { row: CohortRow ->
                val cell = row.cells.getOrNull(offset)
                if (cell != null && !cell.isYounger) cell.retentionPercent else null
            }
            val avg = if (validCells.isNotEmpty()) {
                (validCells.average() * 10).roundToInt() / 10.0
            } else 0.0
            averageCurve.add(avg)
        }

        // Find best cohort at Month 3 (or latest common offset)
        val m3Offset = 3.coerceAtMost(maxOffsets - 1)
        val bestCohort: CohortRow? = rows.filter { !it.cells[m3Offset].isYounger }
            .maxByOrNull { it.cells[m3Offset].retentionPercent }

        val insight = if (bestCohort != null) {
            "Best cohort: ${bestCohort.cohortLabel} (${bestCohort.cells[m3Offset].retentionPercent}% retained at M$m3Offset)"
        } else {
            "Average Month-3 retention is ${averageCurve.getOrElse(3) { 85.0 }}%"
        }

        return CohortMatrixResult(
            sourceMode = DataSourceMode.ACTUAL,
            rows = rows,
            averageCurve = averageCurve,
            bestCohortInsight = insight
        )
    }

    private fun buildModeledCurve(
        profile: BusinessProfile?,
        maxOffsets: Int
    ): CohortMatrixResult {
        val churnRate = (profile?.monthlyChurnRate ?: 3.0).coerceIn(0.1, 99.0) / 100.0
        val retentionBase = 1.0 - churnRate

        val curve = ArrayList<Double>(maxOffsets)
        for (m in 0 until maxOffsets) {
            val retention = retentionBase.pow(m.toDouble()) * 100.0
            curve.add((retention * 10).roundToInt() / 10.0)
        }

        val m3Retention = curve.getOrElse(3) { 91.0 }
        val insight =
            "Modeled Month-3 retention is $m3Retention% based on your ${((churnRate * 100 * 10).roundToInt() / 10.0)}% monthly churn rate."

        return CohortMatrixResult(
            sourceMode = DataSourceMode.MODELED,
            rows = emptyList(),
            averageCurve = curve,
            bestCohortInsight = insight,
            modeledRetentionCurve = curve
        )
    }

    private fun formatMonthLabel(yyyyMm: String): String {
        val parts = yyyyMm.split("-")
        if (parts.size != 2) return yyyyMm
        val year = parts[0].takeLast(2)
        val monthName = when (parts[1]) {
            "01" -> "Jan"
            "02" -> "Feb"
            "03" -> "Mar"
            "04" -> "Apr"
            "05" -> "May"
            "06" -> "Jun"
            "07" -> "Jul"
            "08" -> "Aug"
            "09" -> "Sep"
            "10" -> "Oct"
            "11" -> "Nov"
            "12" -> "Dec"
            else -> parts[1]
        }
        return "$monthName '$year"
    }

    private fun monthsBetween(start: String, end: String): Int {
        val p1 = start.split("-")
        val p2 = end.split("-")
        if (p1.size < 2 || p2.size < 2) return 0
        val y1 = p1[0].toIntOrNull() ?: 2026
        val m1 = p1[1].toIntOrNull() ?: 1
        val y2 = p2[0].toIntOrNull() ?: 2026
        val m2 = p2[1].toIntOrNull() ?: 1
        return (y2 - y1) * 12 + (m2 - m1)
    }
}
