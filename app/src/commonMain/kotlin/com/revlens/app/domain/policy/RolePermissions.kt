package com.revlens.app.domain.policy

import com.revlens.app.domain.model.Role

data class PermissionCapability(
    val name: String,
    val description: String,
    val owner: Boolean,
    val admin: Boolean,
    val editor: Boolean,
    val viewer: Boolean
)

object RolePermissions {

    val matrix: List<PermissionCapability> = listOf(
        PermissionCapability(
            name = "Manage Billing & Subscriptions",
            description = "Upgrade plans, view invoices, change payment methods",
            owner = true, admin = false, editor = false, viewer = false
        ),
        PermissionCapability(
            name = "Transfer Workspace Ownership",
            description = "Pass Owner role to another team member",
            owner = true, admin = false, editor = false, viewer = false
        ),
        PermissionCapability(
            name = "Delete Workspace",
            description = "Permanently delete workspace and all revenue models",
            owner = true, admin = false, editor = false, viewer = false
        ),
        PermissionCapability(
            name = "Manage Team & Invites",
            description = "Invite members, change member roles, revoke invitations",
            owner = true, admin = true, editor = false, viewer = false
        ),
        PermissionCapability(
            name = "Manage API & Integrations",
            description = "Create API keys, configure webhooks, connect Stripe",
            owner = true, admin = true, editor = false, viewer = false
        ),
        PermissionCapability(
            name = "Edit Baselines & Scenarios",
            description = "Create scenarios, update price points, run simulations",
            owner = true, admin = true, editor = true, viewer = false
        ),
        PermissionCapability(
            name = "Export Reports & CSVs",
            description = "Generate investor PDF reports and export clean datasets",
            owner = true, admin = true, editor = true, viewer = true
        ),
        PermissionCapability(
            name = "View Revenue Dashboards",
            description = "Inspect MRR, ARR, CAC, LTV and Cohort charts",
            owner = true, admin = true, editor = true, viewer = true
        )
    )

    fun canManageBilling(role: Role): Boolean = role == Role.Owner

    fun canTransferOwnership(role: Role): Boolean = role == Role.Owner

    fun canDeleteWorkspace(role: Role): Boolean = role == Role.Owner

    fun canManageTeamAndInvites(role: Role): Boolean =
        role == Role.Owner || role == Role.Admin

    fun canManageApiAndIntegrations(role: Role): Boolean =
        role == Role.Owner || role == Role.Admin

    fun canEditBaselinesAndScenarios(role: Role): Boolean =
        role == Role.Owner || role == Role.Admin || role == Role.Editor

    /**
     * Only the Workspace Owner can remove members.
     * No user (including Owner) can remove themselves via the team list, and Owner cannot be removed.
     */
    fun canRemoveMember(
        actorRole: Role,
        targetRole: Role,
        isTargetSelf: Boolean
    ): Boolean {
        if (isTargetSelf) return false
        if (targetRole == Role.Owner) return false
        return actorRole == Role.Owner
    }

    /**
     * Owner can change any other member's role (except Owner).
     * Admin can change Editor/Viewer roles, but cannot change Owner, other Admins, or themselves.
     */
    fun canChangeMemberRole(
        actorRole: Role,
        targetRole: Role,
        isTargetSelf: Boolean
    ): Boolean {
        if (isTargetSelf) return false
        if (targetRole == Role.Owner) return false
        return when (actorRole) {
            Role.Owner -> true
            Role.Admin -> targetRole == Role.Editor || targetRole == Role.Viewer
            Role.Editor, Role.Viewer -> false
        }
    }

    fun roleSummary(role: Role): List<String> = when (role) {
        Role.Owner -> listOf(
            "Full control over billing, subscriptions, and workspace deletion",
            "Invite & remove team members and transfer workspace ownership",
            "Configure API keys, webhooks, and Stripe/Paddle integrations",
            "Edit baseline metrics, pricing plans, and simulation scenarios"
        )

        Role.Admin -> listOf(
            "Invite team members and manage pending invitations",
            "Configure API keys, webhooks, and third-party integrations",
            "Create & edit baselines, pricing simulations, and scenarios",
            "Export PDF investor reports and view all revenue dashboards"
        )

        Role.Editor -> listOf(
            "Edit business baselines, pricing plans, and growth assumptions",
            "Create, compare, and delete simulation scenarios & experiments",
            "Export investor PDF reports and CSV datasets",
            "Cannot manage team members, integrations, API keys, or billing"
        )

        Role.Viewer -> listOf(
            "Read-only access to all revenue dashboards, cohorts, and forecasts",
            "Export investor PDF reports and CSV datasets",
            "Cannot edit baselines, create/delete scenarios, or import CSV data",
            "Cannot manage team members, integrations, or billing"
        )
    }
}
