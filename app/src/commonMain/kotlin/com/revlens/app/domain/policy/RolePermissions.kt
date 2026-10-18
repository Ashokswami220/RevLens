package com.revlens.app.domain.policy

import com.revlens.app.domain.model.Role

data class PermissionFeature(
    val name: String,
    val description: String,
    val owner: Boolean,
    val admin: Boolean,
    val editor: Boolean,
    val viewer: Boolean
)

object RolePermissions {
    val matrix = listOf(
        PermissionFeature("Manage Billing & Subscriptions", "Upgrade plans, view invoices, change payment methods", owner = true, admin = false, editor = false, viewer = false),
        PermissionFeature("Transfer Workspace Ownership", "Pass Owner role to another team member", owner = true, admin = false, editor = false, viewer = false),
        PermissionFeature("Delete Workspace", "Permanently delete workspace and all revenue models", owner = true, admin = false, editor = false, viewer = false),
        PermissionFeature("Manage Team & Invites", "Invite members, change member roles, revoke invitations", owner = true, admin = true, editor = false, viewer = false),
        PermissionFeature("Manage API & Integrations", "Create API keys, configure webhooks, connect Stripe", owner = true, admin = true, editor = false, viewer = false),
        PermissionFeature("Edit Baselines & Scenarios", "Create scenarios, update price points, run simulations", owner = true, admin = true, editor = true, viewer = false),
        PermissionFeature("Export Reports & CSVs", "Generate investor PDF reports and export clean datasets", owner = true, admin = true, editor = true, viewer = true),
        PermissionFeature("View Revenue Dashboards", "Inspect MRR, ARR, CAC, LTV and Cohort charts", owner = true, admin = true, editor = true, viewer = true)
    )

    fun canManageBilling(role: Role): Boolean = role == Role.Owner
    fun canManageTeam(role: Role): Boolean = role == Role.Owner || role == Role.Admin
    fun canEditModels(role: Role): Boolean = role == Role.Owner || role == Role.Admin || role == Role.Editor
}
