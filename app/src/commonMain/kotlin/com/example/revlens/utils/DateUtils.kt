package com.example.revlens.utils

/**
 * Multiplatform date and calendar formatting helper.
 * Completely pure Kotlin, zero JVM or platform dependencies.
 */
object DateUtils {
    private val MONTH_NAMES =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val DAYS_IN_MONTHS = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun getCurrentYearAndMonth(): Pair<Int, Int> {
        val days = (getCurrentTimeMillis() / (1000L * 60 * 60 * 24)).toInt()
        var d = days
        var year = 1970
        while (true) {
            val daysInYear = if (isLeapYear(year)) 366 else 365
            if (d < daysInYear) break
            d -= daysInYear
            year++
        }
        var month = 0
        while (month < 12) {
            val dim = if (month == 1 && isLeapYear(year)) 29 else DAYS_IN_MONTHS[month]
            if (d < dim) break
            d -= dim
            month++
        }
        return Pair(year, month)
    }

    /**
     * Generates a list of forward calendar month names.
     * e.g., ["Nov (Now)", "Dec", "Jan", "Feb", "Mar", "Apr"]
     */
    fun getForwardMonthNames(count: Int = 6): List<String> {
        val (_, currentMonth) = getCurrentYearAndMonth()
        return (0 until count).map { idx ->
            val m = (currentMonth + idx) % 12
            val name = MONTH_NAMES[m]
            if (idx == 0) "$name (Now)" else name
        }
    }

    /**
     * Generates full and short month labels for forecast projection and table breakdown.
     * e.g., Pair("Nov (Now)", "Now"), Pair("Dec 2026", "Dec"), ...
     */
    fun getForecastMonthLabels(count: Int): List<Pair<String, String>> {
        val (currentYear, currentMonth) = getCurrentYearAndMonth()
        return (0..count).map { index ->
            val totalMonths = currentMonth + index
            val m = totalMonths % 12
            val y = currentYear + (totalMonths / 12)
            val name = MONTH_NAMES[m]
            val full = if (index == 0) "$name (Now)" else "$name $y"
            val short = if (index == 0) "Now" else name
            Pair(full, short)
        }
    }
}
