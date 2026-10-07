package com.example.revlens.domain.calc

import java.math.BigDecimal
import java.math.RoundingMode

object MrrCalculator {
    /**
     * Calculates Monthly Recurring Revenue (MRR).
     * @param customers The total number of paying customers.
     * @param arpu Average Revenue Per User in exact precision.
     * @return MRR = customers * arpu, rounded to 2 decimal places.
     */
    fun calculateMrr(customers: Int, arpu: BigDecimal): BigDecimal {
        return arpu.multiply(BigDecimal(customers))
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Calculates Average Revenue Per User (ARPU) given MRR and customer count.
     */
    fun calculateArpu(mrr: BigDecimal, customers: Int): BigDecimal {
        if (customers == 0) return BigDecimal.ZERO
        return mrr.divide(BigDecimal(customers), 2, RoundingMode.HALF_UP)
    }
}
