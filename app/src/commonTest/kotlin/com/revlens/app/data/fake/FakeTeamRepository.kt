package com.revlens.app.data.fake

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.MembershipCheckResult
import com.revlens.app.domain.repository.TeamRepository
import com.revlens.app.model.BusinessProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeTeamRepository : TeamRepository {

    private val membersByWs = mutableMapOf<String, MutableStateFlow<List<Member>>>()
    private val invitesByWs = mutableMapOf<String, MutableStateFlow<List<TeamInvite>>>()
    private val workspaceProfiles = mutableMapOf<String, BusinessProfile>()
    private val workspaceNames = mutableMapOf<String, String>()
    private val joinCodeMap = mutableMapOf<String, String>()
    private val declinedInviteIds = mutableSetOf<String>()

    private fun getMembersFlow(workspaceId: String): MutableStateFlow<List<Member>> {
        val key = workspaceId.ifBlank { "ws_default" }
        return membersByWs.getOrPut(key) {
            MutableStateFlow(emptyList())
        }
    }

    private fun getInvitesFlow(workspaceId: String): MutableStateFlow<List<TeamInvite>> {
        val key = workspaceId.ifBlank { "ws_default" }
        return invitesByWs.getOrPut(key) {
            MutableStateFlow(emptyList())
        }
    }

    override fun getMembers(workspaceId: String): Flow<List<Member>> {
        return getMembersFlow(workspaceId).asStateFlow()
    }

    override fun getInvites(workspaceId: String): Flow<List<TeamInvite>> {
        return getInvitesFlow(workspaceId).asStateFlow()
    }

    override suspend fun inviteMember(
        workspaceId: String,
        email: String,
        role: Role,
        inviterName: String
    ): Result<TeamInvite> {
        val cleanWs = workspaceId.ifBlank { "ws_default" }
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }

        val membersFlow = getMembersFlow(cleanWs)
        if (membersFlow.value.any { it.email.equals(normalized, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("$normalized is already a member of this team"))
        }

        val invitesFlow = getInvitesFlow(cleanWs)
        val existing = invitesFlow.value.firstOrNull {
            it.email.equals(normalized, ignoreCase = true)
        }
        if (existing != null) {
            val updated = existing.copy(role = role, sentAt = "Just now", invitedBy = inviterName)
            invitesFlow.update { list ->
                list.map { if (it.id == existing.id) updated else it }
            }
            return Result.success(updated)
        }

        val invite = TeamInvite(
            id = "inv_${cleanWs}_${normalized.hashCode()}_${invitesFlow.value.size + 1}",
            email = normalized,
            role = role,
            sentAt = "Just now",
            invitedBy = inviterName
        )
        invitesFlow.update { it + invite }
        return Result.success(invite)
    }

    override suspend fun updateMemberRole(
        workspaceId: String,
        memberId: String,
        newRole: Role
    ): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        flow.update { list ->
            list.map { if (it.id == memberId) it.copy(role = newRole) else it }
        }
        return Result.success(Unit)
    }

    override suspend fun removeMember(
        workspaceId: String,
        memberId: String
    ): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        flow.update { list ->
            list.filterNot { it.id == memberId || it.email.equals(memberId, ignoreCase = true) }
        }
        return Result.success(Unit)
    }

    override suspend fun revokeInvite(
        workspaceId: String,
        inviteId: String
    ): Result<Unit> {
        val flow = getInvitesFlow(workspaceId)
        flow.update { list -> list.filterNot { it.id == inviteId } }
        return Result.success(Unit)
    }

    override suspend fun resendInvite(
        workspaceId: String,
        inviteId: String
    ): Result<Unit> {
        val flow = getInvitesFlow(workspaceId)
        flow.update { list ->
            list.map { if (it.id == inviteId) it.copy(sentAt = "Resent just now") else it }
        }
        return Result.success(Unit)
    }

    override suspend fun getPendingInvitesForEmail(email: String): List<TeamInviteDetail> {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank()) return emptyList()
        val result = mutableListOf<TeamInviteDetail>()
        invitesByWs.forEach { (wsId, flow) ->
            flow.value
                .filter {
                    it.email.equals(normalized, ignoreCase = true) &&
                        !declinedInviteIds.contains(it.id)
                }
                .forEach { inv ->
                    result.add(
                        TeamInviteDetail(
                            inviteId = inv.id,
                            workspaceId = wsId,
                            workspaceName = workspaceNames[wsId] ?: "RevLens Workspace",
                            invitedEmail = inv.email,
                            role = inv.role,
                            invitedBy = inv.invitedBy,
                            sentAt = inv.sentAt
                        )
                    )
                }
        }
        return result
    }

    override suspend fun acceptInvite(
        inviteId: String,
        user: UserAccount
    ): Result<String> {
        var matchedWsId: String? = null
        var matchedInvite: TeamInvite? = null

        invitesByWs.forEach { (wsId, flow) ->
            val found = flow.value.firstOrNull { it.id == inviteId }
            if (found != null) {
                matchedWsId = wsId
                matchedInvite = found
            }
        }

        val targetWs = matchedWsId
            ?: return Result.failure(NoSuchElementException("Invite not found"))
        val invite = matchedInvite!!

        getInvitesFlow(targetWs).update { list -> list.filterNot { it.id == inviteId } }

        val newMember = Member(
            id = user.id.ifBlank { "mem_${user.email.hashCode()}" },
            name = user.name.ifBlank { user.email.substringBefore("@") },
            email = user.email.trim().lowercase(),
            role = invite.role,
            avatarUrl = user.avatarUrl,
            joinedAt = "Joined just now"
        )
        getMembersFlow(targetWs).update { list ->
            list.filterNot {
                it.email.equals(user.email, ignoreCase = true) || it.id == newMember.id
            } + newMember
        }
        return Result.success(targetWs)
    }

    override suspend fun declineInvite(
        inviteId: String,
        email: String
    ): Result<Unit> {
        declinedInviteIds.add(inviteId)
        invitesByWs.values.forEach { flow ->
            flow.update { list -> list.filterNot { it.id == inviteId } }
        }
        return Result.success(Unit)
    }

    override suspend fun isUserAssociatedWithAnotherBusiness(
        email: String,
        currentWorkspaceId: String?
    ): Boolean {
        return false
    }

    override suspend fun getUserRoleInWorkspace(
        workspaceId: String,
        userEmail: String
    ): Role? {
        val normalized = userEmail.trim().lowercase()
        return getMembersFlow(workspaceId).value
            .firstOrNull { it.email.equals(normalized, ignoreCase = true) }
            ?.role
    }

    override suspend fun leaveWorkspace(
        workspaceId: String,
        userEmail: String
    ): Result<Unit> {
        val normalized = userEmail.trim().lowercase()
        getMembersFlow(workspaceId).update { list ->
            list.filterNot { it.email.equals(normalized, ignoreCase = true) }
        }
        return Result.success(Unit)
    }

    override suspend fun getWorkspaceProfile(workspaceId: String): BusinessProfile? {
        return workspaceProfiles[workspaceId.ifBlank { "ws_default" }]
    }

    override suspend fun saveWorkspaceProfile(
        workspaceId: String,
        profile: BusinessProfile
    ) {
        workspaceProfiles[workspaceId.ifBlank { "ws_default" }] = profile
    }

    override suspend fun getWorkspaceJoinCode(workspaceId: String): String {
        val clean = workspaceId.ifBlank { "ws_default" }
        val code = "RL-" + clean.removePrefix("ws_").uppercase().take(6).padEnd(6, '0')
        joinCodeMap[code] = clean
        return code
    }

    override suspend fun joinWorkspaceByCode(
        code: String,
        user: UserAccount
    ): Result<String> {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a valid team code."))
        }

        val pending = getPendingInvitesForEmail(user.email)
        val matchedInvite = pending.firstOrNull { it.inviteId.equals(trimmed, ignoreCase = true) }
        if (matchedInvite != null) {
            return acceptInvite(matchedInvite.inviteId, user)
        }

        val defaultCode = getWorkspaceJoinCode("ws_default")
        val targetWsId = when {
            joinCodeMap.containsKey(trimmed.uppercase()) -> joinCodeMap.getValue(trimmed.uppercase())
            trimmed.startsWith("ws_", ignoreCase = true) -> trimmed
            trimmed.equals(defaultCode, ignoreCase = true) -> "ws_default"
            else -> return Result.failure(IllegalArgumentException("No workspace found for code $trimmed"))
        }

        val existingInvite = pending.firstOrNull { it.workspaceId == targetWsId }
        val assignedRole = existingInvite?.role ?: Role.Viewer
        if (existingInvite != null) {
            revokeInvite(targetWsId, existingInvite.inviteId)
        }

        val flow = getMembersFlow(targetWsId)
        val newMember = Member(
            id = user.id.ifBlank { "mem_${user.email.hashCode()}" },
            name = user.name.ifBlank { user.email.substringBefore("@") },
            email = user.email.trim().lowercase(),
            role = assignedRole,
            avatarUrl = user.avatarUrl,
            joinedAt = "Joined via code"
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
        if (list.isEmpty()) {
            return MembershipCheckResult.UNKNOWN
        }
        val present = list.any { member ->
            (userId.isNotBlank() && member.id == userId) ||
                (userEmail.isNotBlank() && member.email.equals(userEmail, ignoreCase = true))
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
