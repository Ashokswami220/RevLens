package com.example.revlens.domain.calc

import com.example.revlens.model.BusinessProfile
import java.math.BigDecimal
import java.math.RoundingMode

object BreakEvenCalculator {

    /**
     * Calculates the number of months required to recover the Customer Acquisition Cost (CAC)
     * for a single customer.
     * 
     * Formula: CAC / (ARPU - VariableCostPerCustomer)
     */
    fun calculateCacPaybackPeriod(profile: BusinessProfile): Double? {
        val arpu = profile.arpu
        val marginPerUser = arpu.subtract(profile.variableCostPerCustomer)

        if (marginPerUser <= BigDecimal.ZERO) return null // Never pays back

        return profile.cac.divide(marginPerUser, 2, RoundingMode.HALF_UP)
            .toDouble()
    }

    /**
     * Calculates the number of customers needed to reach profitability (Break-Even Point).
     * 
     * Formula: Fixed Costs / (ARPU - VariableCostPerCustomer)
     */
    fun calculateBreakEvenCustomers(profile: BusinessProfile): Int? {
        val arpu = profile.arpu
        val marginPerUser = arpu.subtract(profile.variableCostPerCustomer)

        if (marginPerUser <= BigDecimal.ZERO) return null

        val customersNeeded = profile.fixedCosts.divide(marginPerUser, 0, RoundingMode.CEILING)
        return customersNeeded.toInt()
    }
}
