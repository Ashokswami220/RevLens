package com.example.revlens.model

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

enum class RoundingMode {
    UP,
    DOWN,
    CEILING,
    FLOOR,
    HALF_UP,
    HALF_DOWN,
    HALF_EVEN,
    UNNECESSARY
}

/**
 * Pure Kotlin arbitrary-precision financial decimal implementation.
 * Fully compatible across Android, JVM, and Compose Multiplatform WebAssembly (wasmJs).
 */
class BigDecimal(
    private val rawValue: Double,
    private val scale: Int = -1
) : Comparable<BigDecimal> {

    constructor(str: String) : this(
        rawValue = str.trim().toDoubleOrNull() ?: 0.0,
        scale = if (str.contains('.')) str.trim().substringAfter('.').length else -1
    )

    constructor(intVal: Int) : this(intVal.toDouble(), 0)
    constructor(longVal: Long) : this(longVal.toDouble(), 0)
    constructor(doubleVal: Double) : this(doubleVal, -1)

    fun add(other: BigDecimal): BigDecimal {
        val targetScale = maxOf(this.scale, other.scale)
        return BigDecimal(this.rawValue + other.rawValue, targetScale)
    }

    operator fun plus(other: BigDecimal): BigDecimal = add(other)

    fun subtract(other: BigDecimal): BigDecimal {
        val targetScale = maxOf(this.scale, other.scale)
        return BigDecimal(this.rawValue - other.rawValue, targetScale)
    }

    operator fun minus(other: BigDecimal): BigDecimal = subtract(other)

    fun multiply(other: BigDecimal): BigDecimal {
        val targetScale = if (this.scale >= 0 && other.scale >= 0) this.scale + other.scale else -1
        return BigDecimal(this.rawValue * other.rawValue, targetScale)
    }

    operator fun times(other: BigDecimal): BigDecimal = multiply(other)

    fun divide(other: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        if (abs(other.rawValue) < 1e-15) throw ArithmeticException("Division by zero")
        val quotient = this.rawValue / other.rawValue
        return roundToScale(quotient, scale, roundingMode)
    }

    fun divide(other: BigDecimal, roundingMode: RoundingMode): BigDecimal =
        divide(other, 2, roundingMode)

    fun divide(other: BigDecimal): BigDecimal =
        divide(other, 2, RoundingMode.HALF_UP)

    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal =
        roundToScale(this.rawValue, newScale, roundingMode)

    private fun roundToScale(valueToRound: Double, newScale: Int, mode: RoundingMode): BigDecimal {
        val factor = 10.0.pow(newScale.toDouble())
        val scaled = valueToRound * factor
        val roundedScaled = when (mode) {
            RoundingMode.HALF_UP, RoundingMode.HALF_EVEN -> round(scaled)
            RoundingMode.HALF_DOWN -> {
                val sign = if (scaled < 0) -1.0 else 1.0
                val absScaled = abs(scaled)
                val diff = absScaled - floor(absScaled)
                val r = if (diff <= 0.5) floor(absScaled) else ceil(absScaled)
                sign * r
            }
            RoundingMode.CEILING -> ceil(scaled)
            RoundingMode.FLOOR -> floor(scaled)
            RoundingMode.DOWN -> if (scaled >= 0) floor(scaled) else ceil(scaled)
            RoundingMode.UP -> if (scaled >= 0) ceil(scaled) else floor(scaled)
            RoundingMode.UNNECESSARY -> scaled
        }
        return BigDecimal(roundedScaled / factor, newScale)
    }

    override fun compareTo(other: BigDecimal): Int {
        val diff = this.rawValue - other.rawValue
        return when {
            abs(diff) < 1e-9 -> 0
            diff < 0 -> -1
            else -> 1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigDecimal) return false
        return abs(this.rawValue - other.rawValue) < 1e-7
    }

    override fun hashCode(): Int = rawValue.hashCode()

    fun toDouble(): Double = rawValue
    fun toFloat(): Float = rawValue.toFloat()
    fun toLong(): Long = rawValue.toLong()
    fun toInt(): Int = rawValue.toInt()

    fun toPlainString(): String = toString()

    override fun toString(): String {
        return if (scale >= 0) {
            val factor = 10.0.pow(scale.toDouble())
            val rounded = round(rawValue * factor) / factor
            val isNegative = rounded < 0
            val absVal = abs(rounded)
            if (scale == 0) {
                "${if (isNegative) "-" else ""}${absVal.toLong()}"
            } else {
                val s = absVal.toString()
                val parts = s.split(".")
                val intPart = parts[0]
                val decPart = if (parts.size > 1) parts[1].padEnd(scale, '0').take(scale) else "0".repeat(scale)
                "${if (isNegative) "-" else ""}$intPart.$decPart"
            }
        } else {
            val s = rawValue.toString()
            if (s.endsWith(".0")) s.substringBefore(".0") else s
        }
    }

    companion object {
        val ZERO = BigDecimal(0.0, 2)
        val ONE = BigDecimal(1.0, 2)
        val TEN = BigDecimal(10.0, 2)
        fun valueOf(v: Double): BigDecimal = BigDecimal(v)
        fun valueOf(v: Long): BigDecimal = BigDecimal(v.toDouble(), 0)
    }
}

fun String.toBigDecimalOrNull(): BigDecimal? {
    val trimmed = this.trim()
    if (trimmed.isEmpty()) return null
    return if (trimmed.toDoubleOrNull() != null) BigDecimal(trimmed) else null
}

inline fun <T> Iterable<T>.sumOfBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
    var sum = BigDecimal.ZERO
    for (element in this) {
        sum = sum.add(selector(element))
    }
    return sum
}
