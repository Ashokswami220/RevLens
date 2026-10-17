package com.example.revlens.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlanTier(
    val id: String,
    val title: String,
    val monthlyPriceUsd: Int,
    val annualPriceUsd: Int,
    val maxSeats: Int,
    val maxScenarios: Int,
    val features: List<String>
) {
    FREE(
        id = "free",
        title = "Starter",
        monthlyPriceUsd = 0,
        annualPriceUsd = 0,
        maxSeats = 1,
        maxScenarios = 3,
        features = listOf(
            "1 Team Seat",
            "Up to 3 Scenarios",
            "Basic MRR & ARR Simulation",
            "CSV Data Import (up to 1,000 rows)",
            "Community Support"
        )
    ),
    PRO(
        id = "pro",
        title = "Growth Pro",
        monthlyPriceUsd = 49,
        annualPriceUsd = 39,
        maxSeats = 5,
        maxScenarios = 20,
        features = listOf(
            "Up to 5 Team Seats",
            "Up to 20 Simulation Scenarios",
            "Advanced Cohort & CAC Analytics",
            "Unlimited CSV & Stripe Sync",
            "Investor PDF Report Exports",
            "Priority Support"
        )
    ),
    ENTERPRISE(
        id = "team",
        title = "Scale & Team",
        monthlyPriceUsd = 149,
        annualPriceUsd = 119,
        maxSeats = 25,
        maxScenarios = 100,
        features = listOf(
            "Up to 25 Team Seats",
            "100 Simulation Scenarios",
            "Developer API & Webhooks",
            "Custom Role Permissions",
            "Audit Logs & SSO Integration",
            "Dedicated Account Manager"
        )
    )
}

@Serializable
enum class BillingPeriod(val label: String, val discountPercent: Int) {
    MONTHLY("Monthly", 0),
    ANNUAL("Annual (Save 20%)", 20)
}

@Serializable
enum class SubscriptionStatus(val label: String) {
    Active("Active"),
    Trialing("Trialing"),
    PastDue("Past Due"),
    Cancelled("Cancelled")
}

@Serializable
data class Subscription(
    val planTier: PlanTier = PlanTier.PRO,
    val status: SubscriptionStatus = SubscriptionStatus.Active,
    val period: BillingPeriod = BillingPeriod.ANNUAL,
    val renewsAt: String = "November 15, 2026",
    val paymentMethod: String = "Visa ending in 4242",
    val cancelAtPeriodEnd: Boolean = false
)

@Serializable
data class Invoice(
    val id: String,
    val date: String,
    val amountFormatted: String,
    val status: String,
    val pdfUrl: String = "https://revlens.app/invoices/mock-pdf"
)
