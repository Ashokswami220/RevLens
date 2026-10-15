package com.example.revlens.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Multiplatform date and calendar formatting helper.
 * Centralizes date operations so Compose UI screens and domain engines
 * do not depend directly on platform-specific java.util.Calendar or SimpleDateFormat.
 */
object DateUtils {

    /**
     * Generates a list of forward calendar month names.
     * e.g., ["Nov (Now)", "Dec", "Jan", "Feb", "Mar", "Apr"]
     */
    fun getForwardMonthNames(count: Int = 6): List<String> {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("MMM", Locale.US)
        return (0 until count).map { idx ->
            val name = fmt.format(cal.time)
            cal.add(Calendar.MONTH, 1)
            if (idx == 0) "$name (Now)" else name
        }
    }

    /**
     * Generates full and short month labels for forecast projection and table breakdown.
     * e.g., Pair("Nov (Now)", "Now"), Pair("Dec 2026", "Dec"), ...
     */
    fun getForecastMonthLabels(count: Int): List<Pair<String, String>> {
        val cal = Calendar.getInstance()
        val fmtFull = SimpleDateFormat("MMM yyyy", Locale.US)
        val fmtShort = SimpleDateFormat("MMM", Locale.US)
        return (0..count).map { index ->
            val full = if (index == 0) "${fmtShort.format(cal.time)} (Now)" else fmtFull.format(cal.time)
            val short = if (index == 0) "Now" else fmtShort.format(cal.time)
            cal.add(Calendar.MONTH, 1)
            Pair(full, short)
        }
    }
}
