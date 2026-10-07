package com.example.revlens.domain.calc

import com.example.revlens.model.BusinessProfile
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

data class MonthlyForecast(
    val monthIndex: Int,
    val customers: Int,
    val mrr: BigDecimal,
    val cumulativeRevenue: BigDecimal,
    val cumulativeCosts: BigDecimal
)

object ForecastEngine {

    /**
     * Projects the business metrics over a specified number of months.
     */
    fun forecast(profile: BusinessProfile, months: Int = 12): List<MonthlyForecast> {
        val forecasts = mutableListOf<MonthlyForecast>()

        var currentCustomers = profile.totalCustomers.toDouble()
        val arpu = profile.arpu

        var cumulativeRevenue = BigDecimal.ZERO
        var cumulativeCosts = BigDecimal.ZERO

        for (month in 0..months) {
            val roundedCustomers = currentCustomers.roundToInt()
            val mrr = arpu.multiply(BigDecimal(roundedCustomers))
                .setScale(2, RoundingMode.HALF_UP)

            if (month > 0) {
                cumulativeRevenue = cumulativeRevenue.add(mrr)

                val newCustomers = currentCustomers * profile.monthlyGrowthRate
                val marketingSpend = profile.cac.multiply(BigDecimal(newCustomers.roundToInt()))

                val fixedCosts = profile.fixedCosts
                val variableCosts =
                    profile.variableCostPerCustomer.multiply(BigDecimal(roundedCustomers))

                val monthlyCosts = fixedCosts.add(variableCosts)
                    .add(marketingSpend)
                cumulativeCosts = cumulativeCosts.add(monthlyCosts)

                // Update customers for next iteration
                val churnedCustomers = currentCustomers * profile.monthlyChurnRate
                currentCustomers = currentCustomers + newCustomers - churnedCustomers
            }

            forecasts.add(
                MonthlyForecast(
                    monthIndex = month,
                    customers = roundedCustomers,
                    mrr = mrr,
                    cumulativeRevenue = cumulativeRevenue,
                    cumulativeCosts = cumulativeCosts
                )
            )
        }

        return forecasts
    }
}
