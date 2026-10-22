package com.revlens.app.data

import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.Invoice
import com.revlens.app.domain.model.PlanTier
import com.revlens.app.domain.model.Subscription
import com.revlens.app.domain.model.SubscriptionStatus
import com.revlens.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultBillingRepository : BillingRepository {

    private val subByWs = mutableMapOf<String, MutableStateFlow<Subscription>>()
    private val invoicesByWs = mutableMapOf<String, MutableStateFlow<List<Invoice>>>()

    private fun getSubFlow(workspaceId: String): MutableStateFlow<Subscription> {
        return subByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                Subscription(
                    planTier = PlanTier.PRO,
                    status = SubscriptionStatus.Active,
                    period = BillingPeriod.ANNUAL,
                    renewsAt = "November 15, 2026",
                    paymentMethod = "Visa ending in 4242",
                    cancelAtPeriodEnd = false
                )
            )
        }
    }

    private fun getInvoicesFlow(workspaceId: String): MutableStateFlow<List<Invoice>> {
        return invoicesByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    Invoice(
                        id = "INV-2026-003",
                        date = "Oct 15, 2026",
                        amountFormatted = "$39.00",
                        status = "Paid"
                    ),
                    Invoice(
                        id = "INV-2026-002",
                        date = "Sep 15, 2026",
                        amountFormatted = "$39.00",
                        status = "Paid"
                    ),
                    Invoice(
                        id = "INV-2026-001",
                        date = "Aug 15, 2026",
                        amountFormatted = "$39.00",
                        status = "Paid"
                    )
                )
            )
        }
    }

    override fun getSubscription(workspaceId: String): Flow<Subscription> =
        getSubFlow(workspaceId).asStateFlow()

    override fun getInvoices(workspaceId: String): Flow<List<Invoice>> =
        getInvoicesFlow(workspaceId).asStateFlow()

    override suspend fun changePlan(
        workspaceId: String,
        newTier: PlanTier,
        period: BillingPeriod
    ): Result<Subscription> {
        val flow = getSubFlow(workspaceId)
        val updated = flow.value.copy(
            planTier = newTier,
            period = period,
            status = SubscriptionStatus.Active,
            cancelAtPeriodEnd = false
        )
        flow.value = updated

        // If tier changed to a paid tier, pretend an invoice was generated
        if (newTier != PlanTier.FREE) {
            val price =
                if (period == BillingPeriod.ANNUAL) newTier.annualPriceUsd else newTier.monthlyPriceUsd
            val newInvoice = Invoice(
                id = "INV-2026-${
                    (getInvoicesFlow(workspaceId).value.size + 1).toString()
                        .padStart(3, '0')
                }",
                date = "Today",
                amountFormatted = "$$price.00",
                status = "Paid"
            )
            getInvoicesFlow(workspaceId).update { listOf(newInvoice) + it }
        }

        return Result.success(updated)
    }

    override suspend fun cancelSubscription(
        workspaceId: String,
        reason: String?
    ): Result<Subscription> {
        val flow = getSubFlow(workspaceId)
        val updated = flow.value.copy(
            cancelAtPeriodEnd = true
        )
        flow.value = updated
        return Result.success(updated)
    }

    override suspend fun reactivateSubscription(workspaceId: String): Result<Subscription> {
        val flow = getSubFlow(workspaceId)
        val updated = flow.value.copy(
            cancelAtPeriodEnd = false,
            status = SubscriptionStatus.Active
        )
        flow.value = updated
        return Result.success(updated)
    }
}
