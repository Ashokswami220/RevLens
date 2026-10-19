package com.revlens.app.model

import com.revlens.app.domain.model.GrowthAssumptions
import com.revlens.app.utils.getCurrentTimeMillis
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.random.Random

private fun randomUuid(): String {
    val hex = "0123456789abcdef"
    val sb = StringBuilder(36)
    for (i in 0 until 36) {
        when (i) {
            8, 13, 18, 23 -> sb.append('-')
            14 -> sb.append('4')
            19 -> sb.append(hex[(Random.nextInt(16) and 0x3) or 0x8])
            else -> sb.append(hex[Random.nextInt(16)])
        }
    }
    return sb.toString()
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) =
        encoder.encodeString(value.toPlainString())

    override fun deserialize(decoder: Decoder): BigDecimal = BigDecimal(decoder.decodeString())
}

@Serializable
enum class BillingPeriod {
    MONTHLY,
    ANNUALLY
}

@Serializable
data class Plan(
    val id: String = randomUuid(),
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
    val plans: List<Plan>,
    @Serializable(with = BigDecimalSerializer::class)
    val mrrGoal: BigDecimal? = null,
    val currencyCode: String = "USD"
) {
    val totalCustomers: Int
        get() = plans.sumOf { it.customerCount }

    val currentMrr: BigDecimal
        get() = plans.sumOfBigDecimal {
            val monthlyPrice = if (it.billingPeriod == BillingPeriod.ANNUALLY) {
                it.price.divide(BigDecimal(12), 2, RoundingMode.HALF_UP)
            } else {
                it.price
            }
            monthlyPrice.multiply(BigDecimal(it.customerCount))
        }

    val arpu: BigDecimal
        get() = if (totalCustomers > 0) {
            currentMrr.divide(BigDecimal(totalCustomers), 2, RoundingMode.HALF_UP)
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
    val fixedCostChange: BigDecimal = BigDecimal.ZERO,
    val targetPlanId: String? = null,
    val growth: GrowthAssumptions? = null
)

@Serializable
data class Scenario(
    val id: String = randomUuid(),
    val name: String,
    val baselineProfile: BusinessProfile,
    val assumptions: Assumptions,
    val createdAt: Long = getCurrentTimeMillis()
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

fun createDefaultGuestProfile(): BusinessProfile = BusinessProfile(
    monthlyChurnRate = 0.045,
    monthlyGrowthRate = 0.085,
    cac = BigDecimal("175.00"),
    fixedCosts = BigDecimal("4500.00"),
    variableCostPerCustomer = BigDecimal("12.00"),
    plans = listOf(
        Plan(id = "plan_starter", name = "Starter", price = BigDecimal("29.00"), customerCount = 120),
        Plan(id = "plan_pro", name = "Pro", price = BigDecimal("79.00"), customerCount = 65)
    ),
    mrrGoal = BigDecimal("15000.00"),
    currencyCode = "USD"
)
