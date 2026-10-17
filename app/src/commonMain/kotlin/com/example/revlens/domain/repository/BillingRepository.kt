package com.example.revlens.domain.repository

import com.example.revlens.domain.model.BillingPeriod
import com.example.revlens.domain.model.Invoice
import com.example.revlens.domain.model.PlanTier
import com.example.revlens.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun getSubscription(workspaceId: String): Flow<Subscription>
    fun getInvoices(workspaceId: String): Flow<List<Invoice>>
    suspend fun changePlan(workspaceId: String, newTier: PlanTier, period: BillingPeriod): Result<Subscription>
    suspend fun cancelSubscription(workspaceId: String, reason: String? = null): Result<Subscription>
    suspend fun reactivateSubscription(workspaceId: String): Result<Subscription>
}
