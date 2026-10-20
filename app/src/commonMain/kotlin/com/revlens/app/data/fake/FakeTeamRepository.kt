package com.revlens.app.data.fake

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeTeamRepository(
    private val authRepository: com.revlens.app.domain.repository.AuthRepository? = null,
    private val sessionManager: com.revlens.app.domain.session.SessionManager? = null,
    initialMembers: Map<String, List<Member>>? = null,
    initialInvites: Map<String, List<TeamInvite>>? = null
) : TeamRepository {

    private val membersByWs = mutableMapOf<String, MutableStateFlow<List<Member>>>()
    private val invitesByWs = mutableMapOf<String, MutableStateFlow<List<TeamInvite>>>()
    private val workspaceNames = mutableMapOf<String, String>(
        "ws_default" to "My Workspace",
        "ws_1" to "Acme Corp"
    )

    init {
        initialMembers?.forEach { (wsId, members) ->
            membersByWs[wsId] = MutableStateFlow(members)
        }
        initialInvites?.forEach { (wsId, invites) ->
            invitesByWs[wsId] = MutableStateFlow(invites)
        }
    }

    private fun getMembersFlow(workspaceId: String): MutableStateFlow<List<Member>> {
        return membersByWs.getOrPut(workspaceId) {
            val sessionUser = (sessionManager?.state?.value as? com.revlens.app.domain.model.SessionState.Authenticated)?.user
            val ownerName = sessionUser?.name?.takeIf { it.isNotBlank() }
                ?: (if (sessionUser != null && sessionUser.email.isNotBlank()) sessionUser.email.substringBefore("@") else "Workspace Owner")
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
        return invitesByWs.getOrPut(workspaceId) {
            MutableStateFlow(emptyList())
        }
    }

    override fun getMembers(workspaceId: String): Flow<List<Member>> {
        val flow = getMembersFlow(workspaceId)
        val sessionUser = (sessionManager?.state?.value as? com.revlens.app.domain.model.SessionState.Authenticated)?.user
        if (sessionUser != null && sessionUser.email.isNotBlank()) {
            val current = flow.value
            if (current.any { it.email == "owner@revlens.io" }) {
                flow.update { list ->
                    list.map { m ->
                        if (m.email == "owner@revlens.io") {
                            m.copy(
                                id = sessionUser.id.ifBlank { m.id },
                                name = sessionUser.name.ifBlank { sessionUser.email.substringBefore("@") },
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
        val normalized = email.trim().lowercase()
        val currentMembers = getMembersFlow(workspaceId).value
        if (currentMembers.any { it.email.lowercase() == normalized }) {
            return Result.failure(
                IllegalArgumentException("User is already a member of this workspace.")
            )
        }

        val currentInvites = getInvitesFlow(workspaceId).value
        if (currentInvites.any { it.email.lowercase() == normalized }) {
            return Result.failure(
                IllegalArgumentException("An invitation is already pending for this email.")
            )
        }

        // Strict Single-Tenancy: check if user is already associated with any other business
        if (isUserAssociatedWithAnotherBusiness(normalized, currentWorkspaceId = workspaceId)) {
            return Result.failure(
                IllegalArgumentException("Cannot invite user: This account is already associated with another business. A user can only belong to one business at a time.")
            )
        }

        val invite = TeamInvite(
            id = "inv_${(currentInvites.size + 1)}",
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
        val normalized = email.trim().lowercase()
        val results = mutableListOf<TeamInviteDetail>()
        invitesByWs.forEach { (wsId, flow) ->
            val wsName = workspaceNames[wsId] ?: "Acme Corp"
            flow.value.filter { it.email.lowercase() == normalized }.forEach { inv ->
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

        invitesByWs.forEach { (wsId, flow) ->
            val match = flow.value.find { it.id == inviteId }
            if (match != null) {
                targetWsId = wsId
                foundInvite = match
            }
        }

        if (targetWsId == null || foundInvite == null) {
            return Result.failure(NoSuchElementException("Invitation not found or has expired."))
        }

        val wsId = targetWsId!!
        val invite = foundInvite!!

        // Remove from invites
        invitesByWs[wsId]?.update { list -> list.filterNot { it.id == inviteId } }

        // Clear any other pending invites for this user (single-tenant exclusivity)
        val normalizedUserEmail = user.email.trim().lowercase()
        invitesByWs.forEach { (_, flow) ->
            flow.update { list -> list.filterNot { it.email.lowercase() == normalizedUserEmail } }
        }

        // Add as member in the workspace
        val newMember = Member(
            id = user.id.ifBlank { "mem_${user.email.hashCode()}" },
            name = user.name.ifBlank { user.email.substringBefore("@") },
            email = user.email,
            role = invite.role,
            avatarUrl = user.avatarUrl,
            joinedAt = "Joined just now"
        )
        getMembersFlow(wsId).update { list -> list + newMember }

        return Result.success(wsId)
    }

    override suspend fun declineInvite(inviteId: String, email: String): Result<Unit> {
        invitesByWs.forEach { (_, flow) ->
            flow.update { list -> list.filterNot { it.id == inviteId } }
        }
        return Result.success(Unit)
    }

    override suspend fun isUserAssociatedWithAnotherBusiness(
        email: String, currentWorkspaceId: String?
    ): Boolean {
        val normalized = email.trim().lowercase()
        return membersByWs.any { (wsId, flow) ->
            (currentWorkspaceId == null || wsId != currentWorkspaceId) &&
                flow.value.any { it.email.lowercase() == normalized }
        }
    }

    override suspend fun getUserRoleInWorkspace(workspaceId: String, userEmail: String): Role? {
        val normalized = userEmail.trim().lowercase()
        return getMembersFlow(workspaceId).value.find { it.email.lowercase() == normalized }?.role
    }

    override suspend fun leaveWorkspace(workspaceId: String, userEmail: String): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        val normalized = userEmail.trim().lowercase()
        val member = flow.value.find { it.email.lowercase() == normalized }
            ?: return Result.failure(NoSuchElementException("You are not a member of this workspace."))

        if (member.role == Role.Owner) {
            val ownerCount = flow.value.count { it.role == Role.Owner }
            if (ownerCount <= 1) {
                return Result.failure(
                    IllegalStateException("Sole workspace owner cannot leave the business. Transfer ownership or delete the workspace instead.")
                )
            }
        }

        flow.update { list -> list.filterNot { it.id == member.id } }
        return Result.success(Unit)
    }
}
