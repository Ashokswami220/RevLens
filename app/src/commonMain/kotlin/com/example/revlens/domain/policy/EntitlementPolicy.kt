package com.example.revlens.domain.policy

import com.example.revlens.domain.model.PlanTier

object EntitlementPolicy {
    fun canCreateScenario(tier: PlanTier, currentScenarioCount: Int): Boolean =
        currentScenarioCount < tier.maxScenarios

    fun canInviteMember(tier: PlanTier, currentMemberCount: Int): Boolean =
        currentMemberCount < tier.maxSeats

    fun canAccessApiWebhooks(tier: PlanTier): Boolean =
        tier == PlanTier.ENTERPRISE

    fun canExportPdfReports(tier: PlanTier): Boolean =
        tier == PlanTier.PRO || tier == PlanTier.ENTERPRISE

    fun canSyncStripe(tier: PlanTier): Boolean =
        tier == PlanTier.PRO || tier == PlanTier.ENTERPRISE

    fun scenarioLimitRemaining(tier: PlanTier, currentScenarioCount: Int): Int =
        (tier.maxScenarios - currentScenarioCount).coerceAtLeast(0)

    fun seatLimitRemaining(tier: PlanTier, currentMemberCount: Int): Int =
        (tier.maxSeats - currentMemberCount).coerceAtLeast(0)
}
