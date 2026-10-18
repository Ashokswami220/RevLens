package com.revlens.app.domain.calc

import com.revlens.app.domain.model.MarketingSpendRecord
import com.revlens.app.domain.model.SubscriptionRecord

data class ValidationReport(
    val validRecords: List<SubscriptionRecord> = emptyList(),
    val validSpendRecords: List<MarketingSpendRecord> = emptyList(),
    val totalRows: Int,
    val validCount: Int,
    val warningCount: Int,
    val errorCount: Int,
    val errorMessages: List<String>,
    val warningMessages: List<String>
)

object ImportValidator {

    fun validateSubscriptions(
        headers: List<String>,
        rows: List<List<String>>,
        mapping: Map<CanonicalField, String?>
    ): ValidationReport {
        val headerIndices = headers.mapIndexed { idx, h -> h to idx }.toMap()

        val custIdx = mapping[CanonicalField.CUSTOMER_ID]?.let { headerIndices[it] }
        val planIdx = mapping[CanonicalField.PLAN]?.let { headerIndices[it] }
        val startIdx = mapping[CanonicalField.START_DATE]?.let { headerIndices[it] }
        val endIdx = mapping[CanonicalField.END_DATE]?.let { headerIndices[it] }
        val mrrIdx = mapping[CanonicalField.MRR]?.let { headerIndices[it] }
        val channelIdx = mapping[CanonicalField.CHANNEL]?.let { headerIndices[it] }
        val countryIdx = mapping[CanonicalField.COUNTRY]?.let { headerIndices[it] }

        val validRecords = mutableListOf<SubscriptionRecord>()
        val errorMessages = mutableListOf<String>()
        val warningMessages = mutableListOf<String>()
        val seenKeys = mutableSetOf<String>()

        for ((rowIndex, row) in rows.withIndex()) {
            val displayRow = rowIndex + 2 // 1-indexed header + 1

            val custId = custIdx?.let { row.getOrNull(it)?.trim() } ?: ""
            val plan = planIdx?.let { row.getOrNull(it)?.trim() } ?: ""
            val startDate = startIdx?.let { row.getOrNull(it)?.trim() } ?: ""
            val endDate = endIdx?.let { row.getOrNull(it)?.trim().takeIf { d -> !d.isNullOrBlank() } }
            val mrrRaw = mrrIdx?.let { row.getOrNull(it)?.trim() } ?: ""
            val channel = channelIdx?.let { row.getOrNull(it)?.trim() }
            val country = countryIdx?.let { row.getOrNull(it)?.trim() }

            if (custId.isBlank()) {
                errorMessages.add("Row $displayRow: Missing Customer ID")
                continue
            }

            if (startDate.isBlank()) {
                errorMessages.add("Row $displayRow: Missing Start Date")
                continue
            }

            val parsedMrr = parseAmountToMinor(mrrRaw)
            if (parsedMrr == null || parsedMrr < 0) {
                errorMessages.add("Row $displayRow: Invalid MRR amount '$mrrRaw'")
                continue
            }

            // Check duplicate key
            val dedupeKey = "$custId|$startDate"
            if (seenKeys.contains(dedupeKey)) {
                warningMessages.add("Row $displayRow: Duplicate subscription record for customer '$custId'")
            } else {
                seenKeys.add(dedupeKey)
            }

            validRecords.add(
                SubscriptionRecord(
                    customerId = custId,
                    plan = if (plan.isNotBlank()) plan else "Standard",
                    startDate = normalizeDate(startDate),
                    endDate = endDate?.let { normalizeDate(it) },
                    mrrMinor = parsedMrr,
                    channel = channel,
                    country = country
                )
            )
        }

        return ValidationReport(
            validRecords = validRecords,
            validSpendRecords = emptyList(),
            totalRows = rows.size,
            validCount = validRecords.size,
            warningCount = warningMessages.size,
            errorCount = errorMessages.size,
            errorMessages = errorMessages,
            warningMessages = warningMessages
        )
    }

    fun validateMarketingSpend(
        headers: List<String>,
        rows: List<List<String>>,
        mapping: Map<CanonicalField, String?>
    ): ValidationReport {
        val headerIndices = headers.mapIndexed { idx, h -> h to idx }.toMap()

        val monthIdx = mapping[CanonicalField.MONTH]?.let { headerIndices[it] }
        val spendIdx = mapping[CanonicalField.SPEND]?.let { headerIndices[it] }
        val channelIdx = mapping[CanonicalField.CHANNEL]?.let { headerIndices[it] }
        val customersIdx = mapping[CanonicalField.NEW_CUSTOMERS]?.let { headerIndices[it] }

        val validSpend = mutableListOf<MarketingSpendRecord>()
        val errorMessages = mutableListOf<String>()
        val warningMessages = mutableListOf<String>()

        for ((rowIndex, row) in rows.withIndex()) {
            val displayRow = rowIndex + 2

            val month = monthIdx?.let { row.getOrNull(it)?.trim() } ?: ""
            val spendRaw = spendIdx?.let { row.getOrNull(it)?.trim() } ?: ""
            val channel = channelIdx?.let { row.getOrNull(it)?.trim() } ?: "All"
            val custRaw = customersIdx?.let { row.getOrNull(it)?.trim() }

            if (month.isBlank()) {
                errorMessages.add("Row $displayRow: Missing Month")
                continue
            }

            val parsedSpend = parseAmountToMinor(spendRaw)
            if (parsedSpend == null || parsedSpend < 0) {
                errorMessages.add("Row $displayRow: Invalid Spend amount '$spendRaw'")
                continue
            }

            val newCust = custRaw?.toIntOrNull()

            validSpend.add(
                MarketingSpendRecord(
                    month = normalizeDate(month),
                    channel = channel,
                    spendMinor = parsedSpend,
                    newCustomers = newCust
                )
            )
        }

        return ValidationReport(
            validRecords = emptyList(),
            validSpendRecords = validSpend,
            totalRows = rows.size,
            validCount = validSpend.size,
            warningCount = warningMessages.size,
            errorCount = errorMessages.size,
            errorMessages = errorMessages,
            warningMessages = warningMessages
        )
    }

    fun parseAmountToMinor(raw: String): Long? {
        val cleaned = raw.replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("₹", "")
            .replace(",", "")
            .trim()
        val num = cleaned.toDoubleOrNull() ?: return null
        return (num * 100).toLong()
    }

    private fun normalizeDate(date: String): String {
        return date.replace("/", "-").trim()
    }
}
