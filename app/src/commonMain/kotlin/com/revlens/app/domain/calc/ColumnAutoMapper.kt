package com.revlens.app.domain.calc

enum class CanonicalField(val displayName: String, val isRequired: Boolean) {
    // Subscription fields
    CUSTOMER_ID("Customer ID", true),
    PLAN("Plan Name", true),
    START_DATE("Start Date", true),
    END_DATE("End Date", false),
    MRR("MRR / Amount", true),
    CHANNEL("Acquisition Channel", false),
    COUNTRY("Country / Region", false),

    // Spend fields
    MONTH("Month", true),
    SPEND("Monthly Spend", true),
    NEW_CUSTOMERS("New Customers", false)
}

object ColumnAutoMapper {

    private val SYNONYMS = mapOf(
        CanonicalField.CUSTOMER_ID to listOf("cust_id", "customer_id", "customer id", "user_id", "userid", "id", "account_id"),
        CanonicalField.PLAN to listOf("plan", "plan_name", "tier", "subscription", "product"),
        CanonicalField.START_DATE to listOf("start_date", "started", "created_at", "signup_date", "date_joined", "start"),
        CanonicalField.END_DATE to listOf("end_date", "ended", "cancelled_at", "churn_date", "expiration", "end"),
        CanonicalField.MRR to listOf("mrr", "amount", "revenue", "price", "monthly_amount", "value"),
        CanonicalField.CHANNEL to listOf("channel", "source", "acquisition_channel", "utm_source", "medium"),
        CanonicalField.COUNTRY to listOf("country", "region", "geo", "location"),
        CanonicalField.MONTH to listOf("month", "date", "period", "spend_month"),
        CanonicalField.SPEND to listOf("spend", "cost", "budget", "marketing_spend", "ad_spend"),
        CanonicalField.NEW_CUSTOMERS to listOf("new_customers", "customers", "acquisitions", "signups", "conversions")
    )

    fun autoMap(headers: List<String>, targetFields: List<CanonicalField>): Map<CanonicalField, String?> {
        val result = mutableMapOf<CanonicalField, String?>()
        val normalizedHeaders = headers.map { it.trim().lowercase().replace("-", "_").replace(" ", "_") }

        for (field in targetFields) {
            val synonyms = SYNONYMS[field] ?: emptyList()
            var matchedHeader: String? = null

            for ((index, header) in normalizedHeaders.withIndex()) {
                if (synonyms.any { syn -> header == syn || header.contains(syn) }) {
                    matchedHeader = headers[index]
                    break
                }
            }
            result[field] = matchedHeader
        }

        return result
    }
}
