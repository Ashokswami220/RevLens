package com.revlens.app.data.fake

import com.revlens.app.core.util.IdGenerator
import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.SessionState
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.MembershipCheckResult
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.domain.session.SessionManager
import com.revlens.app.model.BusinessProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeTeamRepository(
    private val sessionManager: SessionManager? = null,
    initialMembers: Map<String, List<Member>>? = null,
    initialInvites: Map<String, List<TeamInvite>>? = null
) : TeamRepository {

    companion object {
        val persistentMembers = mutableMapOf<String, MutableStateFlow<List<Member>>>()
        val persistentInvites = mutableMapOf<String, MutableStateFlow<List<TeamInvite>>>()
        val workspaceNames = mutableMapOf(
            "ws_default" to "My Workspace",
            "ws_1" to "Acme Corp"
        )
        val workspaceProfiles = mutableMapOf<String, BusinessProfile>()

        fun reset() {
            persistentMembers.clear()
            persistentInvites.clear()
            workspaceNames.clear()
            workspaceNames.putAll(
                mapOf(
                    "ws_default" to "My Workspace",
                    "ws_1" to "Acme Corp"
                )
            )
            workspaceProfiles.clear()
        }
    }

    init {
        initialMembers?.forEach { (wsId, members) ->
            persistentMembers[wsId] = MutableStateFlow(members)
        }
        initialInvites?.forEach { (wsId, invites) ->
            persistentInvites[wsId] = MutableStateFlow(invites)
        }
    }

    private fun getMembersFlow(workspaceId: String): MutableStateFlow<List<Member>> {
        return persistentMembers.getOrPut(workspaceId) {
            val sessionUser =
                (sessionManager?.state?.value as? SessionState.Authenticated)?.user
            val ownerName = sessionUser?.name?.takeIf { it.isNotBlank() }
                ?: (if (sessionUser != null && sessionUser.email.isNotBlank()) sessionUser.email.substringBefore(
                    "@"
                ) else "Workspace Owner")
            val ownerEmail = sessionUser?.email?.takeIf { it.isNotBlank() } ?: "owner@revlens.io"
            val ownerId = sessionUser?.id?.takeIf { it.isNotBlank() } ?: "mem_owner"

            MutableStateFlow(
                listOf(
                    Member(
                        id = ownerId,
                        name = ownerName,
                        email = ownerEmail,
                        role = Role.Owner,
                        avatarUrl = sessionUser?.avatarUrl,
                        joinedAt = "Workspace Founder"
                    )
                )
            )
        }
    }

    private fun getInvitesFlow(workspaceId: String): MutableStateFlow<List<TeamInvite>> {
        return persistentInvites.getOrPut(workspaceId) {
            MutableStateFlow(emptyList())
        }
    }

    override fun getMembers(workspaceId: String): Flow<List<Member>> {
        val flow = getMembersFlow(workspaceId)
        val sessionUser =
            (sessionManager?.state?.value as? SessionState.Authenticated)?.user
        if (sessionUser != null && sessionUser.email.isNotBlank()) {
            val current = flow.value
            // Update the Founder record if it has the placeholder email or if matching session
            if (current.any { it.role == Role.Owner && (it.email == "owner@revlens.io" || it.email == sessionUser.email) }) {
                flow.update { list ->
                    list.map { m ->
                        if (m.role == Role.Owner && (m.email == "owner@revlens.io" || m.email == sessionUser.email)) {
                            m.copy(
                                id = sessionUser.id.ifBlank { m.id },
                                name = sessionUser.name.ifBlank {
                                    sessionUser.email.substringBefore(
                                        "@"
                                    )
                                },
                                email = sessionUser.email,
                                avatarUrl = sessionUser.avatarUrl
                            )
                        } else m
                    }
                }
            }
        }
        return flow.asStateFlow()
    }

    override fun getInvites(workspaceId: String): Flow<List<TeamInvite>> =
        getInvitesFlow(workspaceId).asStateFlow()

    override suspend fun inviteMember(
        workspaceId: String, email: String, role: Role, inviterName: String
    ): Result<TeamInvite> {
        val normalized = email.trim()
            .lowercase()
        val currentMembers = getMembersFlow(workspaceId).value
        val sessionUser =
            (sessionManager?.state?.value as? SessionState.Authenticated)?.user
        val currentOwner = currentMembers.firstOrNull { it.role == Role.Owner }
        val ownerEmail = currentOwner?.email?.trim()
            ?.lowercase() ?: sessionUser?.email?.trim()
            ?.lowercase()

        // 1. Prevent inviting oneself
        if (ownerEmail != null && ownerEmail == normalized) {
            return Result.failure(
                IllegalArgumentException("You cannot invite yourself to your own workspace.")
            )
        }

        // 2. Prevent inviting someone who is already an accepted non-owner member
        if (currentMembers.any {
                it.role != Role.Owner && it.email.trim()
                    .lowercase() == normalized
            }) {
            return Result.failure(
                IllegalArgumentException("User is already a member of this workspace.")
            )
        }

        // 3. If an invitation is already pending, refresh it instead of throwing an error
        val currentInvites = getInvitesFlow(workspaceId).value
        val existingInvite = currentInvites.find {
            it.email.trim()
                .lowercase() == normalized
        }
        if (existingInvite != null) {
            val updated = existingInvite.copy(
                role = role,
                sentAt = "Resent just now",
                invitedBy = inviterName
            )
            getInvitesFlow(workspaceId).update { list ->
                list.map { if (it.id == existingInvite.id) updated else it }
            }
            return Result.success(updated)
        }

        // 4. Strict Single-Tenancy: check if user is already an accepted member of another business
        if (isUserAssociatedWithAnotherBusiness(normalized, currentWorkspaceId = workspaceId)) {
            return Result.failure(
                IllegalArgumentException(
                    "Cannot invite user: This account is already associated with another business. A user can only belong to one business at a time."
                )
            )
        }

        val invite = TeamInvite(
            id = IdGenerator.newId("inv"),
            email = normalized,
            role = role,
            sentAt = "Just now",
            invitedBy = inviterName
        )
        getInvitesFlow(workspaceId).update { it + invite }
        return Result.success(invite)
    }

    override suspend fun updateMemberRole(
        workspaceId: String, memberId: String, newRole: Role
    ): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        val current = flow.value
        val member = current.find { it.id == memberId }
            ?: return Result.failure(NoSuchElementException("Member not found"))

        if (member.role == Role.Owner && newRole != Role.Owner) {
            val ownerCount = current.count { it.role == Role.Owner }
            if (ownerCount <= 1) {
                return Result.failure(
                    IllegalStateException(
                        "Cannot demote the sole workspace Owner. Transfer ownership first."
                    )
                )
            }
        }

        flow.update { list ->
            list.map { if (it.id == memberId) it.copy(role = newRole) else it }
        }
        return Result.success(Unit)
    }

    override suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        val current = flow.value
        val member = current.find { it.id == memberId }
            ?: return Result.failure(NoSuchElementException("Member not found"))

        if (member.role == Role.Owner) {
            val ownerCount = current.count { it.role == Role.Owner }
            if (ownerCount <= 1) {
                return Result.failure(
                    IllegalStateException("Cannot remove the sole workspace Owner.")
                )
            }
        }

        flow.update { list -> list.filterNot { it.id == memberId } }
        return Result.success(Unit)
    }

    override suspend fun revokeInvite(workspaceId: String, inviteId: String): Result<Unit> {
        getInvitesFlow(workspaceId).update { list -> list.filterNot { it.id == inviteId } }
        return Result.success(Unit)
    }

    override suspend fun resendInvite(workspaceId: String, inviteId: String): Result<Unit> {
        getInvitesFlow(workspaceId).update { list ->
            list.map { if (it.id == inviteId) it.copy(sentAt = "Resent just now") else it }
        }
        return Result.success(Unit)
    }

    override suspend fun getPendingInvitesForEmail(email: String): List<TeamInviteDetail> {
        val normalized = email.trim()
            .lowercase()
        val results = mutableListOf<TeamInviteDetail>()
        persistentInvites.forEach { (wsId, flow) ->
            val wsName = workspaceNames[wsId] ?: "My Workspace"
            flow.value.filter {
                it.email.trim()
                    .lowercase() == normalized
            }
                .forEach { inv ->
                    results.add(
                        TeamInviteDetail(
                            inviteId = inv.id,
                            workspaceId = wsId,
                            workspaceName = wsName,
                            invitedEmail = inv.email,
                            role = inv.role,
                            invitedBy = inv.invitedBy,
                            sentAt = inv.sentAt
                        )
                    )
                }
        }
        return results
    }

    override suspend fun acceptInvite(inviteId: String, user: UserAccount): Result<String> {
        var targetWsId: String? = null
        var foundInvite: TeamInvite? = null

        persistentInvites.forEach { (wsId, flow) ->
            val match = flow.value.find { it.id == inviteId }
            if (match != null) {
                targetWsId = wsId
                foundInvite = match
            }
        }

        if (targetWsId == null || foundInvite == null) {
            return Result.failure(NoSuchElementException("Invitation not found or has expired."))
        }

        val wsId = targetWsId
        val invite = foundInvite

        // Remove from invites
        persistentInvites[wsId]?.update { list -> list.filterNot { it.id == inviteId } }

        // Clear any other pending invites for this user (single-tenant exclusivity)
        val normalizedUserEmail = user.email.trim()
            .lowercase()
        persistentInvites.forEach { (_, flow) ->
            flow.update { list ->
                list.filterNot {
                    it.email.trim()
                        .lowercase() == normalizedUserEmail
                }
            }
        }

        // Add as member in the workspace
        val newMember = Member(
            id = user.id.ifBlank { "mem_${user.email.hashCode()}" },
            name = user.name.ifBlank { user.email.substringBefore("@") },
            email = user.email.trim()
                .lowercase(),
            role = invite.role,
            avatarUrl = user.avatarUrl,
            joinedAt = "Joined just now"
        )
        val targetFlow = getMembersFlow(wsId)
        targetFlow.update { list ->
            val hasOwner = list.any { it.role == Role.Owner }
            val baseList = if (!hasOwner) {
                val inviter = invite.invitedBy.ifBlank { "Workspace Owner" }
                listOf(
                    Member(
                        id = "mem_owner",
                        name = inviter,
                        email = "owner@revlens.io",
                        role = Role.Owner,
                        joinedAt = "Workspace Founder"
                    )
                ) + list
            } else list

            if (baseList.any {
                    it.email.trim()
                        .lowercase() == normalizedUserEmail
                }) {
                baseList.map {
                    if (it.email.trim()
                            .lowercase() == normalizedUserEmail
                    ) newMember else it
                }
            } else {
                baseList + newMember
            }
        }

        // Mirror to ws_default if wsId != ws_default and ws_default exists
        if (wsId != "ws_default" && persistentMembers.containsKey("ws_default")) {
            persistentMembers["ws_default"]?.update { list ->
                if (list.none {
                        it.email.trim()
                            .lowercase() == normalizedUserEmail
                    }) {
                    list + newMember
                } else {
                    list.map {
                        if (it.email.trim()
                                .lowercase() == normalizedUserEmail
                        ) newMember else it
                    }
                }
            }
        }
        // Mirror to other workspaces if wsId == ws_default
        if (wsId == "ws_default") {
            persistentMembers.filterKeys { it != "ws_default" }.values.forEach { flow ->
                flow.update { list ->
                    if (list.none {
                            it.email.trim()
                                .lowercase() == normalizedUserEmail
                        }) {
                        list + newMember
                    } else {
                        list.map {
                            if (it.email.trim()
                                    .lowercase() == normalizedUserEmail
                            ) newMember else it
                        }
                    }
                }
            }
        }

        return Result.success(wsId)
    }

    override suspend fun declineInvite(inviteId: String, email: String): Result<Unit> {
        persistentInvites.forEach { (_, flow) ->
            flow.update { list -> list.filterNot { it.id == inviteId } }
        }
        return Result.success(Unit)
    }

    override suspend fun isUserAssociatedWithAnotherBusiness(
        email: String, currentWorkspaceId: String?
    ): Boolean {
        val normalized = email.trim()
            .lowercase()
        return persistentMembers.any { (wsId, flow) ->
            (currentWorkspaceId == null || wsId != currentWorkspaceId) &&
                    flow.value.any {
                        it.email.trim()
                            .lowercase() == normalized && it.role != Role.Owner
                    }
        }
    }

    override suspend fun getUserRoleInWorkspace(workspaceId: String, userEmail: String): Role? {
        val normalized = userEmail.trim()
            .lowercase()
        return getMembersFlow(workspaceId).value.find {
            it.email.trim()
                .lowercase() == normalized
        }?.role
    }

    override suspend fun leaveWorkspace(workspaceId: String, userEmail: String): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        val normalized = userEmail.trim()
            .lowercase()
        val member = flow.value.find {
            it.email.trim()
                .lowercase() == normalized
        }
            ?: return Result.failure(
                NoSuchElementException("You are not a member of this workspace.")
            )

        if (member.role == Role.Owner) {
            val ownerCount = flow.value.count { it.role == Role.Owner }
            if (ownerCount <= 1) {
                return Result.failure(
                    IllegalStateException(
                        "Sole workspace owner cannot leave the business. Transfer ownership or delete the workspace instead."
                    )
                )
            }
        }

        flow.update { list -> list.filterNot { it.id == member.id } }
        return Result.success(Unit)
    }

    override suspend fun getWorkspaceProfile(
        workspaceId: String
    ): BusinessProfile? {
        return workspaceProfiles[workspaceId] ?: workspaceProfiles["ws_default"]
        ?: workspaceProfiles.values.firstOrNull()
    }

    override suspend fun saveWorkspaceProfile(
        workspaceId: String, profile: BusinessProfile
    ) {
        workspaceProfiles[workspaceId] = profile
        if (workspaceId != "ws_default") {
            workspaceProfiles["ws_default"] = profile
        }
    }

    private val joinCodeMap = mutableMapOf<String, String>()
    private val revokedEmailsByWs = mutableMapOf<String, MutableSet<String>>()

    override suspend fun getWorkspaceJoinCode(workspaceId: String): String {
        val clean = workspaceId.ifBlank { "ws_default" }
        val code = "RL-" + clean.removePrefix("ws_")
            .uppercase()
            .take(6)
            .padEnd(6, '0')
        joinCodeMap[code] = clean
        return code
    }

    override suspend fun joinWorkspaceByCode(code: String, user: UserAccount): Result<String> {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a valid team code."))
        }
        // Check if code matches any invite ID first
        val pending = getPendingInvitesForEmail(user.email)
        val matchedInvite = pending.firstOrNull { it.inviteId.equals(trimmed, ignoreCase = true) }
        if (matchedInvite != null) {
            return acceptInvite(matchedInvite.inviteId, user)
        }

        val targetWsId = joinCodeMap[trimmed.uppercase()]
            ?: if (trimmed.startsWith("ws_", ignoreCase = true)) trimmed
            else if (trimmed.equals(
                    getWorkspaceJoinCode("ws_default"), ignoreCase = true
                )
            ) "ws_default"
            else return Result.failure(
                IllegalArgumentException("No workspace found for code $trimmed")
            )

        val existingInvite = pending.firstOrNull { it.workspaceId == targetWsId }
        val assignedRole = existingInvite?.role ?: Role.Viewer
        if (existingInvite != null) {
            revokeInvite(targetWsId, existingInvite.inviteId)
        }

        val flow = getMembersFlow(targetWsId)
        val newMember = Member(
            id = user.id.ifBlank { "mem_${user.email.hashCode()}" },
            name = user.name.ifBlank { user.email.substringBefore("@") },
            email = user.email.trim()
                .lowercase(),
            role = assignedRole,
            avatarUrl = user.avatarUrl,
            joinedAt = "Joined via code"
        )
        revokedEmailsByWs[targetWsId]?.remove(
            user.email.trim()
                .lowercase()
        )
        flow.value = flow.value.filterNot {
            it.email.equals(user.email, ignoreCase = true) || it.id == user.id
        } + newMember

        return Result.success(targetWsId)
    }

    override suspend fun checkMembershipStatus(
        workspaceId: String,
        userId: String,
        userEmail: String
    ): MembershipCheckResult {
        val cleanWs = workspaceId.ifBlank { "ws_default" }
        val list = getMembersFlow(cleanWs).value
        if (list.isEmpty()) return MembershipCheckResult.UNKNOWN
        val present = list.any {
            (userId.isNotBlank() && it.id == userId) ||
                    (userEmail.isNotBlank() && it.email.equals(userEmail, ignoreCase = true))
        }
        return if (present) {
            MembershipCheckResult.ACTIVE
        } else {
            MembershipCheckResult.REVOKED
        }
    }

    override suspend fun transferOwnership(
        workspaceId: String,
        newOwnerMemberId: String
    ): Result<Unit> {
        val cleanWs = workspaceId.ifBlank { "ws_default" }
        val flow = getMembersFlow(cleanWs)
        val target = flow.value.firstOrNull { it.id == newOwnerMemberId }
            ?: return Result.failure(NoSuchElementException("Member not found"))
        flow.value = flow.value.map { member ->
            when {
                member.id == target.id -> member.copy(role = Role.Owner)
                member.role == Role.Owner -> member.copy(role = Role.Admin)
                else -> member
            }
        }
        return Result.success(Unit)
    }
}
