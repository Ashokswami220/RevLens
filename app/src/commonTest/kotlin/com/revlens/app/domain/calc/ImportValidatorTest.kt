package com.revlens.app.domain.calc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportValidatorTest {

    @Test
    fun testValidateSubscriptionsValidRows() {
        val headers = listOf("cust_id", "tier", "started", "price")
        val rows = listOf(
            listOf("CUST_001", "Pro", "2024-01-15", "$79.00"),
            listOf("CUST_002", "Starter", "2024-02-01", "29")
        )
        val mapping = mapOf(
            CanonicalField.CUSTOMER_ID to "cust_id",
            CanonicalField.PLAN to "tier",
            CanonicalField.START_DATE to "started",
            CanonicalField.MRR to "price"
        )

        val report = ImportValidator.validateSubscriptions(headers, rows, mapping)

        assertEquals(2, report.totalRows)
        assertEquals(2, report.validCount)
        assertEquals(0, report.errorCount)
        assertEquals(7900L, report.validRecords[0].mrrMinor)
        assertEquals(2900L, report.validRecords[1].mrrMinor)
    }

    @Test
    fun testValidateSubscriptionsWithMissingFields() {
        val headers = listOf("cust_id", "tier", "started", "price")
        val rows = listOf(
            listOf("", "Pro", "2024-01-15", "$79.00"), // missing cust_id
            listOf("CUST_002", "Starter", "", "29"), // missing start_date
            listOf("CUST_003", "Enterprise", "2024-03-01", "invalid_mrr") // bad price
        )
        val mapping = mapOf(
            CanonicalField.CUSTOMER_ID to "cust_id",
            CanonicalField.PLAN to "tier",
            CanonicalField.START_DATE to "started",
            CanonicalField.MRR to "price"
        )

        val report = ImportValidator.validateSubscriptions(headers, rows, mapping)

        assertEquals(3, report.totalRows)
        assertEquals(0, report.validCount)
        assertEquals(3, report.errorCount)
        assertTrue(report.errorMessages.any { it.contains("Missing Customer ID") })
        assertTrue(report.errorMessages.any { it.contains("Missing Start Date") })
        assertTrue(report.errorMessages.any { it.contains("Invalid MRR amount") })
    }

    @Test
    fun testValidateMarketingSpend() {
        val headers = listOf("month", "spend", "channel", "signups")
        val rows = listOf(
            listOf("2024-01-01", "1,500.00", "Google Ads", "25"),
            listOf("2024-02-01", "2200", "Meta Ads", "40")
        )
        val mapping = mapOf(
            CanonicalField.MONTH to "month",
            CanonicalField.SPEND to "spend",
            CanonicalField.CHANNEL to "channel",
            CanonicalField.NEW_CUSTOMERS to "signups"
        )

        val report = ImportValidator.validateMarketingSpend(headers, rows, mapping)

        assertEquals(2, report.totalRows)
        assertEquals(2, report.validCount)
        assertEquals(0, report.errorCount)
        assertEquals(150000L, report.validSpendRecords[0].spendMinor)
        assertEquals(25, report.validSpendRecords[0].newCustomers)
        assertEquals(220000L, report.validSpendRecords[1].spendMinor)
        assertEquals(40, report.validSpendRecords[1].newCustomers)
    }
}
