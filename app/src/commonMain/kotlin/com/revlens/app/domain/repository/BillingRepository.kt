package com.revlens.app.domain.repository

import com.revlens.app.domain.model.BillingPeriod
import com.revlens.app.domain.model.Invoice
import com.revlens.app.domain.model.PlanTier
import com.revlens.app.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun getSubscription(workspaceId: String): Flow<Subscription>
    fun getInvoices(workspaceId: String): Flow<List<Invoice>>
    suspend fun changePlan(
        workspaceId: String, newTier: PlanTier, period: BillingPeriod
    ): Result<Subscription>

    suspend fun cancelSubscription(
        workspaceId: String, reason: String? = null
    ): Result<Subscription>

    suspend fun reactivateSubscription(workspaceId: String): Result<Subscription>
}
