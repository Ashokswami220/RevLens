package com.example.revlens.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
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
        AppCurrency("USD", "US Dollar ($)", "$"),
        AppCurrency("INR", "Indian Rupee (₹)", "₹"),
        AppCurrency("EUR", "Euro (€)", "€", symbolOnLeft = false),
        AppCurrency("GBP", "British Pound (£)", "£"),
        AppCurrency("JPY", "Japanese Yen (¥)", "¥"),
        AppCurrency("CNY", "Chinese Yuan (¥)", "¥"),
        AppCurrency("CAD", "Canadian Dollar (CA$)", "CA$"),
        AppCurrency("AUD", "Australian Dollar (A$)", "A$"),
        AppCurrency("CHF", "Swiss Franc (CHF)", "CHF"),
        AppCurrency("AED", "UAE Dirham (AED)", "AED")
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
        return PlatformCurrencyFormatter(curr)
    }

    /**
     * Returns a multiplatform-ready percentage formatter.
     * e.g., 0.125 -> "12.5%"
     */
    fun getPercentFormatter(maxFractionDigits: Int = 1): RevLensFormatter {
        return PlatformPercentFormatter(maxFractionDigits)
    }

    /**
     * Pure Kotlin decimal formatting without java.lang.String.format or java.util.Locale.
     */
    fun formatDecimal(value: Double, decimals: Int = 1): String {
        val factor = 10.0.pow(decimals.toDouble())
        val rounded = round(value * factor) / factor
        return if (decimals == 0) {
            rounded.toLong().toString()
        } else {
            val s = rounded.toString()
            val parts = s.split(".")
            val intPart = parts[0]
            val decPart = if (parts.size > 1) parts[1].padEnd(decimals, '0').take(decimals) else "0".repeat(decimals)
            "$intPart.$decPart"
        }
    }

    /**
     * Helper to format a percentage directly without creating a formatter instance.
     */
    fun formatPercent(value: Double, isFraction: Boolean = true, maxFractionDigits: Int = 1): String {
        val pct = if (isFraction) value * 100.0 else value
        return "${formatDecimal(pct, maxFractionDigits)}%"
    }
}

private class PlatformCurrencyFormatter(private val appCurrency: AppCurrency) : RevLensFormatter {
    private val jvmFormatter: NumberFormat? by lazy {
        try {
            val loc = when (appCurrency.code) {
                "USD" -> Locale.US
                "INR" -> Locale.forLanguageTag("en-IN")
                "EUR" -> Locale.GERMANY
                "GBP" -> Locale.UK
                "JPY" -> Locale.JAPAN
                "CNY" -> Locale.CHINA
                "CAD" -> Locale.CANADA
                "AUD" -> Locale.forLanguageTag("en-AU")
                "CHF" -> Locale.forLanguageTag("de-CH")
                "AED" -> Locale.forLanguageTag("ar-AE")
                else -> Locale.US
            }
            NumberFormat.getCurrencyInstance(loc).apply {
                this.currency = Currency.getInstance(appCurrency.code)
                maximumFractionDigits = 0
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun format(amount: BigDecimal): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Double): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount)
    } catch (_: Exception) {
        formatFallback(amount)
    }

    override fun format(amount: Long): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Int): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Number): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Any?): String = when (amount) {
        is BigDecimal -> format(amount)
        is Double -> format(amount)
        is Long -> format(amount)
        is Int -> format(amount)
        is Number -> format(amount)
        is String -> amount.toBigDecimalOrNull()?.let { format(it) } ?: amount
        null -> "-"
        else -> amount.toString()
    }

    private fun formatFallback(value: Double): String {
        val rounded = round(value).toLong()
        val isNegative = rounded < 0
        val absVal = kotlin.math.abs(rounded)
        val formattedNumber = absVal.toString().reversed().chunked(3).joinToString(",").reversed()
        val prefix = if (isNegative) "-" else ""
        return if (appCurrency.symbolOnLeft) "$prefix${appCurrency.symbol}$formattedNumber" else "$prefix$formattedNumber ${appCurrency.symbol}"
    }
}

private class PlatformPercentFormatter(private val maxFractionDigits: Int = 1) : RevLensFormatter {
    private val jvmFormatter: NumberFormat? by lazy {
        try {
            NumberFormat.getPercentInstance(Locale.US).apply {
                minimumFractionDigits = maxFractionDigits
                maximumFractionDigits = maxFractionDigits
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun format(amount: BigDecimal): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Double): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount)
    } catch (_: Exception) {
        formatFallback(amount)
    }

    override fun format(amount: Long): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Int): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Number): String = try {
        jvmFormatter?.format(amount) ?: formatFallback(amount.toDouble())
    } catch (_: Exception) {
        formatFallback(amount.toDouble())
    }

    override fun format(amount: Any?): String = when (amount) {
        is BigDecimal -> format(amount)
        is Double -> format(amount)
        is Long -> format(amount)
        is Int -> format(amount)
        is Number -> format(amount)
        is String -> amount.toDoubleOrNull()?.let { format(it) } ?: amount
        null -> "-"
        else -> amount.toString()
    }

    private fun formatFallback(fraction: Double): String {
        val pct = fraction * 100.0
        val factor = 10.0.pow(maxFractionDigits.toDouble())
        val rounded = round(pct * factor) / factor
        return "$rounded%"
    }
}
