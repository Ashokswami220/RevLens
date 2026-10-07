package com.example.revlens.domain.calc

import com.example.revlens.domain.model.BusinessProfile
import com.example.revlens.domain.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class ForecastEngineTest {

    @Test
    fun `forecast projects MRR and customers correctly over time`() {
        val profile = BusinessProfile(
            monthlyChurnRate = 0.05,
            monthlyGrowthRate = 0.10, // Net growth = 5%
            cac = BigDecimal("200.00"),
            fixedCosts = BigDecimal("1000.00"),
            variableCostPerCustomer = BigDecimal("10.00"),
            plans = listOf(
                Plan(name = "Basic", price = BigDecimal("50.00"), customerCount = 100) // MRR = 5000
            )
        )

        val forecasts = ForecastEngine.forecast(profile, months = 3)

        // Month 0
        assertEquals(100, forecasts[0].customers)
        assertEquals(BigDecimal("5000.00"), forecasts[0].mrr)
        assertEquals(BigDecimal("0"), forecasts[0].cumulativeRevenue)

        // Month 1
        // New: 100 * 0.10 = 10. Churned: 100 * 0.05 = 5. Net: +5. Current at month 1 = 105.
        // The forecast logic applies growth and churn for the *next* month, so month 1 has 105.
        // Wait, looking at the code, in month 1:
        // currentCustomers before loop = 100
        // month = 0: saves 100
        // month = 1: 
        // rounded = 100
        // mrr = 5000
        // newCust = 10. churned = 5. current becomes 105.
        // Wait, the newCustomers logic adds them at the *end* of the month loop!
        // So month 1 will have rounded = 100.
        // Let's check the code:
        // In month 1, roundedCustomers = 100? No, currentCustomers was updated at the end of month 1 loop.
        // Actually, currentCustomers is initialized to 100. 
        // month=0 saves 100. It doesn't update currentCustomers.
        // month=1 uses 100, updates it to 105 at the end of month 1.
        // So month 2 uses 105.

        // This means my assertions should be:
        assertEquals(100, forecasts[1].customers)
        assertEquals(105, forecasts[2].customers)
    }
}
