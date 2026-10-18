package com.revlens.app.domain.calc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CsvParserTest {

    @Test
    fun testStandardCommaCsv() {
        val csv = """
            cust_id,plan,start_date,mrr
            c_001,Starter,2026-01-15,$29.00
            c_002,Pro,2026-02-01,79.00
        """.trimIndent()

        val parsed = CsvParser.parse(csv)
        assertEquals(',', parsed.delimiter)
        assertEquals(listOf("cust_id", "plan", "start_date", "mrr"), parsed.headers)
        assertEquals(2, parsed.rows.size)
        assertEquals("c_001", parsed.rows[0][0])
        assertEquals("79.00", parsed.rows[1][3])
    }

    @Test
    fun testSemicolonAndQuotedFields() {
        val csv = """
            "customer id";"plan name";"amount"
            "c_101";"Enterprise, Custom";"1,200.00"
            "c_102";"Starter";"29.00"
        """.trimIndent()

        val parsed = CsvParser.parse(csv)
        assertEquals(';', parsed.delimiter)
        assertEquals(3, parsed.headers.size)
        assertEquals("Enterprise, Custom", parsed.rows[0][1])
    }

    @Test
    fun testColumnAutoMapper() {
        val headers = listOf("User ID", "Subscription Tier", "Signup Date", "Monthly Revenue", "UTM Source")
        val targetFields = listOf(
            CanonicalField.CUSTOMER_ID,
            CanonicalField.PLAN,
            CanonicalField.START_DATE,
            CanonicalField.MRR,
            CanonicalField.CHANNEL
        )

        val mapping = ColumnAutoMapper.autoMap(headers, targetFields)
        assertEquals("User ID", mapping[CanonicalField.CUSTOMER_ID])
        assertEquals("Subscription Tier", mapping[CanonicalField.PLAN])
        assertEquals("Signup Date", mapping[CanonicalField.START_DATE])
        assertEquals("Monthly Revenue", mapping[CanonicalField.MRR])
        assertEquals("UTM Source", mapping[CanonicalField.CHANNEL])
    }

    @Test
    fun testImportValidator() {
        val headers = listOf("cust_id", "plan", "start_date", "mrr")
        val rows = listOf(
            listOf("c_1", "Starter", "2026-01-01", "$49.00"),
            listOf("c_2", "Pro", "2026-01-05", "99.00"),
            listOf("", "Pro", "2026-01-10", "99.00") // Error: missing customer id
        )
        val mapping = mapOf(
            CanonicalField.CUSTOMER_ID to "cust_id",
            CanonicalField.PLAN to "plan",
            CanonicalField.START_DATE to "start_date",
            CanonicalField.MRR to "mrr"
        )

        val report = ImportValidator.validateSubscriptions(headers, rows, mapping)
        assertEquals(2, report.validCount)
        assertEquals(1, report.errorCount)
        assertEquals(4900L, report.validRecords[0].mrrMinor)
        assertEquals(9900L, report.validRecords[1].mrrMinor)
    }
}
