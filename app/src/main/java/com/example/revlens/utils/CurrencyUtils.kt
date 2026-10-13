package com.example.revlens.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class AppCurrency(
    val code: String,
    val name: String,
    val symbol: String,
    val locale: Locale
)

object CurrencyUtils {
    val supportedCurrencies = listOf(
        AppCurrency("USD", "US Dollar ($)", "$", Locale.US),
        AppCurrency("INR", "Indian Rupee (₹)", "₹", Locale.forLanguageTag("en-IN")),
        AppCurrency("EUR", "Euro (€)", "€", Locale.GERMANY),
        AppCurrency("GBP", "British Pound (£)", "£", Locale.UK),
        AppCurrency("JPY", "Japanese Yen (¥)", "¥", Locale.JAPAN),
        AppCurrency("CNY", "Chinese Yuan (¥)", "¥", Locale.CHINA),
        AppCurrency("CAD", "Canadian Dollar (CA$)", "CA$", Locale.CANADA),
        AppCurrency("AUD", "Australian Dollar (A$)", "A$", Locale.forLanguageTag("en-AU")),
        AppCurrency("CHF", "Swiss Franc (CHF)", "CHF", Locale.forLanguageTag("de-CH")),
        AppCurrency("AED", "UAE Dirham (AED)", "AED", Locale.forLanguageTag("ar-AE"))
    )

    fun getCurrency(code: String?): AppCurrency {
        return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: supportedCurrencies.first()
    }

    fun getFormatter(code: String?): NumberFormat {
        val curr = getCurrency(code)
        return NumberFormat.getCurrencyInstance(curr.locale).apply {
            currency = Currency.getInstance(curr.code)
            maximumFractionDigits = 0
        }
    }
}
