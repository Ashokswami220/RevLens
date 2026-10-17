package com.example.revlens.domain.calc

import com.example.revlens.domain.model.Dataset
import com.example.revlens.domain.model.SubscriptionRecord
import com.example.revlens.model.BusinessProfile
import com.example.revlens.ui.components.display.DataSourceMode
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.roundToInt

enum class CustomerSegmentBy {
    PLAN,
    CHANNEL,
    COUNTRY
}

enum class AnalyticsPeriod(val label: String, val months: Int) {
    THREE_MONTHS("3M", 3),
    SIX_MONTHS("6M", 6),
    TWELVE_MONTHS("12M", 12)
}

enum class ConcentrationHealth {
    HEALTHY,    // Top 10% <= 30% of MRR
    MODERATE,   // Top 10% 31% - 50% of MRR
    HIGH_RISK   // Top 10% > 50% of MRR
}

@Serializable
data class CustomerGrowthPoint(
    val monthLabel: String,
    val activeCustomers: Int,
    val newCustomers: Int,
    val churnedCustomers: Int
)

@Serializable
data class CustomerSegmentShare(
    val name: String,
    val customerCount: Int,
    val percentShare: Double, // 0.0 to 100.0
    val totalMrrMinor: Long,
    val churnRatePercent: Double
)

@Serializable
data class CustomerRecordItem(
    val id: String,
    val displayName: String,
    val planName: String,
    val mrrMinor: Long,
    val isActive: Boolean,
    val channel: String?,
    val country: String?,
    val startDate: String,
    val endDate: String? = null
)

@Serializable
data class CustomerAnalyticsResult(
    val sourceMode: DataSourceMode,
    val activeCustomers: Int,
    val newCustomers: Int,
    val churnedCustomers: Int,
    val netCustomers: Int,
    val growthTimeline: List<CustomerGrowthPoint>,
    val segments: List<CustomerSegmentShare>,
    val concentrationTop10PercentMrrShare: Double,
    val concentrationBenchmark: ConcentrationHealth,
    val topCustomers: List<CustomerRecordItem>,
    val recentlyDowngraded: List<CustomerRecordItem>,
    val isChannelOrCountryAvailable: Boolean
)

object CustomerSegmentationCalculator {

    fun calculate(
        profile: BusinessProfile?,
        dataset: Dataset?,
        period: AnalyticsPeriod = AnalyticsPeriod.THREE_MONTHS,
        segmentBy: CustomerSegmentBy = CustomerSegmentBy.PLAN
    ): CustomerAnalyticsResult {
        val hasActual = dataset != null && dataset.subscriptions.isNotEmpty()

        return if (hasActual) {
            calculateActual(dataset!!, period, segmentBy)
        } else {
            calculateModeled(profile, period, segmentBy)
        }
    }

    private fun calculateActual(
        dataset: Dataset,
        period: AnalyticsPeriod,
        segmentBy: CustomerSegmentBy
    ): CustomerAnalyticsResult {
        val allSubs = dataset.subscriptions
        val activeSubs = allSubs.filter { it.endDate == null }
        val churnedSubs = allSubs.filter { it.endDate != null }

        val activeCount = activeSubs.size
        val churnedCount = churnedSubs.size
        val newCount = max(activeCount / period.months, 1)
        val netCount = newCount - (churnedCount / period.months)

        // Grouping for segments
        val segments = when (segmentBy) {
            CustomerSegmentBy.PLAN -> {
                activeSubs.groupBy { it.plan.ifBlank { "Standard" } }
            }
            CustomerSegmentBy.CHANNEL -> {
                activeSubs.groupBy { it.channel?.ifBlank { "Direct" } ?: "Direct / Organic" }
            }
            CustomerSegmentBy.COUNTRY -> {
                activeSubs.groupBy { it.country?.ifBlank { "Other" } ?: "Other" }
            }
        }.map { (name, group) ->
            val count = group.size
            val share = if (activeCount > 0) (count.toDouble() / activeCount * 100.0) else 0.0
            val mrr = group.sumOf { it.mrrMinor }
            val groupChurned = churnedSubs.count {
                when (segmentBy) {
                    CustomerSegmentBy.PLAN -> it.plan.equals(name, ignoreCase = true)
                    CustomerSegmentBy.CHANNEL -> it.channel.equals(name, ignoreCase = true)
                    CustomerSegmentBy.COUNTRY -> it.country.equals(name, ignoreCase = true)
                }
            }
            val churnRate = if (count > 0) (groupChurned.toDouble() / count * 100.0).coerceAtMost(100.0) else 0.0
            CustomerSegmentShare(
                name = name,
                customerCount = count,
                percentShare = (share * 10.0).roundToInt() / 10.0,
                totalMrrMinor = mrr,
                churnRatePercent = (churnRate * 10.0).roundToInt() / 10.0
            )
        }.sortedByDescending { it.customerCount }

        // Concentration: top 10% of customers
        val sortedByMrr = activeSubs.sortedByDescending { it.mrrMinor }
        val totalMrr = activeSubs.sumOf { it.mrrMinor }
        val top10Count = max(1, (activeCount * 0.10).roundToInt())
        val top10Mrr = sortedByMrr.take(top10Count).sumOf { it.mrrMinor }
        val concentrationShare = if (totalMrr > 0) {
            (top10Mrr.toDouble() / totalMrr * 100.0)
        } else 0.0

        val health = when {
            concentrationShare <= 30.0 -> ConcentrationHealth.HEALTHY
            concentrationShare <= 50.0 -> ConcentrationHealth.MODERATE
            else -> ConcentrationHealth.HIGH_RISK
        }

        // Top 5 customers (masked)
        val topCustomers = sortedByMrr.take(5).map { record ->
            CustomerRecordItem(
                id = record.customerId,
                displayName = maskCustomerId(record.customerId),
                planName = record.plan.ifBlank { "Standard" },
                mrrMinor = record.mrrMinor,
                isActive = true,
                channel = record.channel,
                country = record.country,
                startDate = record.startDate,
                endDate = record.endDate
            )
        }

        // Recently churned / downgraded
        val downgraded = churnedSubs.take(5).map { record ->
            CustomerRecordItem(
                id = record.customerId,
                displayName = maskCustomerId(record.customerId),
                planName = record.plan.ifBlank { "Standard" },
                mrrMinor = record.mrrMinor,
                isActive = false,
                channel = record.channel,
                country = record.country,
                startDate = record.startDate,
                endDate = record.endDate
            )
        }

        // Growth timeline points
        val growthTimeline = generateTimeline(activeCount, newCount, churnedCount, period.months)

        return CustomerAnalyticsResult(
            sourceMode = DataSourceMode.ACTUAL,
            activeCustomers = activeCount,
            newCustomers = newCount * period.months,
            churnedCustomers = churnedCount,
            netCustomers = netCount * period.months,
            growthTimeline = growthTimeline,
            segments = segments,
            concentrationTop10PercentMrrShare = (concentrationShare * 10.0).roundToInt() / 10.0,
            concentrationBenchmark = health,
            topCustomers = topCustomers,
            recentlyDowngraded = downgraded,
            isChannelOrCountryAvailable = true
        )
    }

    private fun calculateModeled(
        profile: BusinessProfile?,
        period: AnalyticsPeriod,
        segmentBy: CustomerSegmentBy
    ): CustomerAnalyticsResult {
        val safeCustomers = profile?.totalCustomers?.coerceAtLeast(1) ?: 100
        val growthRate = profile?.monthlyGrowthRate?.coerceAtLeast(0.0) ?: 5.0
        val churnRate = profile?.monthlyChurnRate?.coerceAtLeast(0.0) ?: 3.0

        val newPerMonth = max(1, (safeCustomers * (growthRate / 100.0)).roundToInt())
        val churnPerMonth = (safeCustomers * (churnRate / 100.0)).roundToInt()
        val totalNew = newPerMonth * period.months
        val totalChurned = churnPerMonth * period.months
        val netGain = totalNew - totalChurned

        val isChannelOrCountryAvailable = segmentBy == CustomerSegmentBy.PLAN

        val segments = if (isChannelOrCountryAvailable && profile != null && profile.plans.isNotEmpty()) {
            profile.plans.map { plan ->
                val count = plan.customerCount
                val share = if (safeCustomers > 0) (count.toDouble() / safeCustomers * 100.0) else 0.0
                val priceCents = (plan.price.toDouble() * 100).toLong()
                val totalMrr = priceCents * count
                CustomerSegmentShare(
                    name = plan.name,
                    customerCount = count,
                    percentShare = (share * 10.0).roundToInt() / 10.0,
                    totalMrrMinor = totalMrr,
                    churnRatePercent = churnRate
                )
            }.sortedByDescending { it.customerCount }
        } else {
            emptyList()
        }

        val timeline = generateTimeline(safeCustomers, newPerMonth, churnPerMonth, period.months)

        return CustomerAnalyticsResult(
            sourceMode = DataSourceMode.MODELED,
            activeCustomers = safeCustomers,
            newCustomers = totalNew,
            churnedCustomers = totalChurned,
            netCustomers = netGain,
            growthTimeline = timeline,
            segments = segments,
            concentrationTop10PercentMrrShare = 24.5,
            concentrationBenchmark = ConcentrationHealth.HEALTHY,
            topCustomers = emptyList(),
            recentlyDowngraded = emptyList(),
            isChannelOrCountryAvailable = isChannelOrCountryAvailable
        )
    }

    private fun generateTimeline(
        currentActive: Int,
        newPerMonth: Int,
        churnPerMonth: Int,
        months: Int
    ): List<CustomerGrowthPoint> {
        val list = ArrayList<CustomerGrowthPoint>(months)
        var running = max(1, currentActive - (newPerMonth - churnPerMonth) * months)
        for (m in 1..months) {
            val net = newPerMonth - churnPerMonth
            running = max(1, running + net)
            list.add(
                CustomerGrowthPoint(
                    monthLabel = "M$m",
                    activeCustomers = running,
                    newCustomers = newPerMonth,
                    churnedCustomers = churnPerMonth
                )
            )
        }
        return list
    }

    private fun maskCustomerId(id: String): String {
        return if (id.length <= 4) {
            "Customer ***$id"
        } else {
            "Customer ***${id.takeLast(4)}"
        }
    }
}
