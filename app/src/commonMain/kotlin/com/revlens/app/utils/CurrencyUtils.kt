package com.revlens.app.utils

import com.revlens.app.model.BigDecimal
import com.revlens.app.model.toBigDecimalOrNull
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/**
 * Platform-independent formatting interface for RevLens.
 * Decouples Compose UI, domain engines, and calculators from java.text.NumberFormat
 * to enable seamless Compose Multiplatform (Wasm / Web) execution.
 */
interface RevLensFormatter {
    fun format(amount: BigDecimal): String
    fun format(amount: Double): String
    fun format(amount: Long): String
    fun format(amount: Int): String
    fun format(amount: Number): String
    fun format(amount: Any?): String
}

data class AppCurrency(
    val code: String,
    val name: String,
    val symbol: String,
    val symbolOnLeft: Boolean = true
)

object CurrencyUtils {
    val supportedCurrencies = listOf(
        AppCurrency("USD", "US Dollar", "$"),
        AppCurrency("INR", "Indian Rupee", "₹"),
        AppCurrency("EUR", "Euro", "€", symbolOnLeft = false),
        AppCurrency("GBP", "British Pound", "£"),
        AppCurrency("JPY", "Japanese Yen", "¥"),
        AppCurrency("CNY", "Chinese Yuan", "¥"),
        AppCurrency("CAD", "Canadian Dollar", "CA$"),
        AppCurrency("AUD", "Australian Dollar", "A$"),
        AppCurrency("CHF", "Swiss Franc", "CHF"),
        AppCurrency("AED", "UAE Dirham", "AED")
    )

    fun getCurrency(code: String?): AppCurrency {
        return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: supportedCurrencies.first()
    }

    /**
     * Returns a multiplatform-ready currency formatter for the given currency code.
     */
    fun getFormatter(code: String?): RevLensFormatter {
        val curr = getCurrency(code)
        return MultiplatformCurrencyFormatter(curr)
    }

    /**
     * Returns a multiplatform-ready percentage formatter.
     * e.g., 0.125 -> "12.5%"
     */
    fun getPercentFormatter(maxFractionDigits: Int = 1): RevLensFormatter {
        return MultiplatformPercentFormatter(maxFractionDigits)
    }

    /**
     * Pure Kotlin decimal formatting without java.lang.String.format or java.util.Locale.
     */
    fun formatDecimal(value: Double, decimals: Int = 1): String {
        val factor = 10.0.pow(decimals.toDouble())
        val rounded = round(value * factor) / factor
        return if (decimals == 0) {
            rounded.toLong()
                .toString()
        } else {
            val s = rounded.toString()
            val parts = s.split(".")
            val intPart = parts[0]
            val decPart = if (parts.size > 1) parts[1].padEnd(decimals, '0')
                .take(decimals) else "0".repeat(decimals)
            "$intPart.$decPart"
        }
    }

    /**
     * Helper to format a percentage directly without creating a formatter instance.
     */
    fun formatPercent(
        value: Double, isFraction: Boolean = true, maxFractionDigits: Int = 1
    ): String {
        val pct = if (isFraction) value * 100.0 else value
        return "${formatDecimal(pct, maxFractionDigits)}%"
    }
}

private class MultiplatformCurrencyFormatter(private val appCurrency: AppCurrency) :
    RevLensFormatter {
    override fun format(amount: BigDecimal): String = formatValue(amount.toDouble())
    override fun format(amount: Double): String = formatValue(amount)
    override fun format(amount: Long): String = formatValue(amount.toDouble())
    override fun format(amount: Int): String = formatValue(amount.toDouble())
    override fun format(amount: Number): String = formatValue(amount.toDouble())

    override fun format(amount: Any?): String = when (amount) {
        is BigDecimal -> format(amount)
        is Double -> format(amount)
        is Long -> format(amount)
        is Int -> format(amount)
        is Number -> format(amount)
        is String -> amount.toBigDecimalOrNull()
            ?.let { format(it) } ?: amount

        null -> "-"
        else -> amount.toString()
    }

    private fun formatValue(value: Double): String {
        val rounded = round(value).toLong()
        val isNegative = rounded < 0
        val absVal = abs(rounded)
        val formattedNumber = absVal.toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        val prefix = if (isNegative) "-" else ""
        return if (appCurrency.symbolOnLeft) "$prefix${appCurrency.symbol}$formattedNumber" else "$prefix$formattedNumber ${appCurrency.symbol}"
    }
}

private class MultiplatformPercentFormatter(private val maxFractionDigits: Int = 1) :
    RevLensFormatter {
    override fun format(amount: BigDecimal): String = formatValue(amount.toDouble())
    override fun format(amount: Double): String = formatValue(amount)
    override fun format(amount: Long): String = formatValue(amount.toDouble())
    override fun format(amount: Int): String = formatValue(amount.toDouble())
    override fun format(amount: Number): String = formatValue(amount.toDouble())

    override fun format(amount: Any?): String = when (amount) {
        is BigDecimal -> format(amount)
        is Double -> format(amount)
        is Long -> format(amount)
        is Int -> format(amount)
        is Number -> format(amount)
        is String -> amount.toDoubleOrNull()
            ?.let { format(it) } ?: amount

        null -> "-"
        else -> amount.toString()
    }

    private fun formatValue(fraction: Double): String {
        val pct = fraction * 100.0
        val factor = 10.0.pow(maxFractionDigits.toDouble())
        val rounded = round(pct * factor) / factor
        return "$rounded%"
    }
}
