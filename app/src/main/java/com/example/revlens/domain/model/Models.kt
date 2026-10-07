package com.example.revlens.domain.model

import java.math.BigDecimal
import java.util.UUID

enum class BillingPeriod {
    MONTHLY,
    ANNUALLY
}

data class Plan(
    val id: String = UUID.randomUUID()
        .toString(),
    val name: String,
    val price: BigDecimal,
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY,
    val customerCount: Int
)

data class BusinessProfile(
    val monthlyChurnRate: Double, // e.g. 0.021 for 2.1%
    val monthlyGrowthRate: Double, // e.g. 0.06 for 6.0%
    val cac: BigDecimal, // Customer Acquisition Cost
    val fixedCosts: BigDecimal,
    val variableCostPerCustomer: BigDecimal,
    val plans: List<Plan>
) {
    val totalCustomers: Int
        get() = plans.sumOf { it.customerCount }

    val currentMrr: BigDecimal
        get() = plans.sumOf {
            val monthlyPrice = if (it.billingPeriod == BillingPeriod.ANNUALLY) {
                it.price.divide(BigDecimal(12), 2, java.math.RoundingMode.HALF_UP)
            } else {
                it.price
            }
            monthlyPrice.multiply(BigDecimal(it.customerCount))
        }

    val arpu: BigDecimal
        get() = if (totalCustomers > 0) {
            currentMrr.divide(BigDecimal(totalCustomers), 2, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
}

data class Assumptions(
    val priceChangePercent: Double = 0.0, // e.g. 0.10 for +10%
    val churnChangePercent: Double = 0.0, // e.g. 0.015 for +1.5% point
    val cacChange: BigDecimal = BigDecimal.ZERO, // absolute change in CAC
    val growthChangePercent: Double = 0.0, // e.g. 0.02 for +2% point
    val fixedCostChange: BigDecimal = BigDecimal.ZERO
)

data class Scenario(
    val id: String = UUID.randomUUID()
        .toString(),
    val name: String,
    val baselineProfile: BusinessProfile,
    val assumptions: Assumptions,
    val createdAt: Long = System.currentTimeMillis()
)

data class MetricResult(
    val label: String,
    val value: BigDecimal,
    val formattedValue: String,
    val deltaPercent: Double? = null,
    val isPositive: Boolean = true
)
