package com.example.revlens.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.util.UUID

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BigDecimal) = encoder.encodeString(value.toPlainString())
    override fun deserialize(decoder: Decoder): BigDecimal = BigDecimal(decoder.decodeString())
}

@Serializable
enum class BillingPeriod {
    MONTHLY,
    ANNUALLY
}

@Serializable
data class Plan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY,
    val customerCount: Int
)

@Serializable
data class BusinessProfile(
    val monthlyChurnRate: Double,
    val monthlyGrowthRate: Double,
    @Serializable(with = BigDecimalSerializer::class)
    val cac: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val fixedCosts: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
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

@Serializable
data class Assumptions(
    val priceChangePercent: Double = 0.0,
    val churnChangePercent: Double = 0.0,
    @Serializable(with = BigDecimalSerializer::class)
    val cacChange: BigDecimal = BigDecimal.ZERO,
    val growthChangePercent: Double = 0.0,
    @Serializable(with = BigDecimalSerializer::class)
    val fixedCostChange: BigDecimal = BigDecimal.ZERO
)

@Serializable
data class Scenario(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baselineProfile: BusinessProfile,
    val assumptions: Assumptions,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class MetricResult(
    val label: String,
    @Serializable(with = BigDecimalSerializer::class)
    val value: BigDecimal,
    val formattedValue: String,
    val deltaPercent: Double? = null,
    val isPositive: Boolean = true
)
