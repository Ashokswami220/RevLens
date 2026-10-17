package com.example.revlens.domain.calc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ColumnAutoMapperTest {

    @Test
    fun testAutoMapSubscriptionHeaders() {
        val headers = listOf("User_Id", "Plan_Name", "Signup_Date", "Cancelled_At", "Monthly_Amount", "UTM_Source", "Geo_Location")
        val targetFields = listOf(
            CanonicalField.CUSTOMER_ID,
            CanonicalField.PLAN,
            CanonicalField.START_DATE,
            CanonicalField.END_DATE,
            CanonicalField.MRR,
            CanonicalField.CHANNEL,
            CanonicalField.COUNTRY
        )

        val mapping = ColumnAutoMapper.autoMap(headers, targetFields)

        assertEquals("User_Id", mapping[CanonicalField.CUSTOMER_ID])
        assertEquals("Plan_Name", mapping[CanonicalField.PLAN])
        assertEquals("Signup_Date", mapping[CanonicalField.START_DATE])
        assertEquals("Cancelled_At", mapping[CanonicalField.END_DATE])
        assertEquals("Monthly_Amount", mapping[CanonicalField.MRR])
        assertEquals("UTM_Source", mapping[CanonicalField.CHANNEL])
        assertEquals("Geo_Location", mapping[CanonicalField.COUNTRY])
    }

    @Test
    fun testAutoMapSpendHeaders() {
        val headers = listOf("Month", "Marketing_Spend", "Channel", "Signups")
        val targetFields = listOf(
            CanonicalField.MONTH,
            CanonicalField.SPEND,
            CanonicalField.CHANNEL,
            CanonicalField.NEW_CUSTOMERS
        )

        val mapping = ColumnAutoMapper.autoMap(headers, targetFields)

        assertEquals("Month", mapping[CanonicalField.MONTH])
        assertEquals("Marketing_Spend", mapping[CanonicalField.SPEND])
        assertEquals("Channel", mapping[CanonicalField.CHANNEL])
        assertEquals("Signups", mapping[CanonicalField.NEW_CUSTOMERS])
    }

    @Test
    fun testUnmatchedHeaderIsNull() {
        val headers = listOf("random_1", "random_2")
        val mapping = ColumnAutoMapper.autoMap(headers, listOf(CanonicalField.CUSTOMER_ID))
        assertNull(mapping[CanonicalField.CUSTOMER_ID])
    }
}
