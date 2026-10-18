package com.revlens.app.domain.calc

import com.revlens.app.model.BigDecimal
import com.revlens.app.model.BillingPeriod
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Plan
import kotlin.test.Test
import kotlin.test.assertEquals

class MrrCalculatorTest {

    @Test
    fun `calculateMrr multiplies customers by ARPU correctly`() {
        val customers = 150
        val arpu = BigDecimal("65.67")
        // 150 * 65.67 = 9850.50
        val mrr = MrrCalculator.calculateMrr(customers, arpu)
        assertEquals(BigDecimal("9850.50"), mrr)
    }

    @Test
    fun `calculateArpu divides MRR by customers correctly`() {
        val mrr = BigDecimal("9850.00")
        val customers = 150
        // 9850.00 / 150 = 65.67
        val arpu = MrrCalculator.calculateArpu(mrr, customers)
        assertEquals(BigDecimal("65.67"), arpu)
    }

    @Test
    fun `calculateArpu returns zero if customer count is zero`() {
        val arpu = MrrCalculator.calculateArpu(BigDecimal("5000.00"), 0)
        assertEquals(BigDecimal.ZERO, arpu)
    }

    @Test
    fun `BusinessProfile calculates totalCustomers, currentMrr, and arpu across multi-tier plans`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.02,
            monthlyGrowthRate = 0.05,
            cac = BigDecimal("150.00"),
            fixedCosts = BigDecimal("5000.00"),
            variableCostPerCustomer = BigDecimal("15.00"),
            plans = listOf(
                Plan(name = "Starter", price = BigDecimal("49.00"), customerCount = 100),
                Plan(name = "Pro", price = BigDecimal("99.00"), customerCount = 50),
                // Annual plan: $1200/yr -> $100/mo * 10 customers = $1000/mo
                Plan(
                    name = "Enterprise Annual",
                    price = BigDecimal("1200.00"),
                    billingPeriod = BillingPeriod.ANNUALLY,
                    customerCount = 10
                )
            )
        )

        // Total customers: 100 + 50 + 10 = 160
        assertEquals(160, profile.totalCustomers)

        // Starter: 49 * 100 = 4900
        // Pro: 99 * 50 = 4950
        // Enterprise: (1200 / 12) * 10 = 1000
        // Total MRR: 4900 + 4950 + 1000 = 10850.00
        assertEquals(BigDecimal("10850.00"), profile.currentMrr)

        // ARPU: 10850 / 160 = 67.81
        assertEquals(BigDecimal("67.81"), profile.arpu)
    }
}
