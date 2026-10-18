package com.revlens.app.data.fake

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeTeamRepository : TeamRepository {

    private val membersByWs = mutableMapOf<String, MutableStateFlow<List<Member>>>()
    private val invitesByWs = mutableMapOf<String, MutableStateFlow<List<TeamInvite>>>()

    private fun getMembersFlow(workspaceId: String): MutableStateFlow<List<Member>> {
        return membersByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    Member(
                        id = "mem_1",
                        name = "Alex Vance",
                        email = "alex.vance@gmail.com",
                        role = Role.Owner,
                        joinedAt = "Workspace Founder"
                    ),
                    Member(
                        id = "mem_2",
                        name = "Sarah Connor",
                        email = "sarah@skynet.ai",
                        role = Role.Admin,
                        joinedAt = "Joined 3 weeks ago"
                    ),
                    Member(
                        id = "mem_3",
                        name = "Elena Rostova",
                        email = "elena@orbit.io",
                        role = Role.Editor,
                        joinedAt = "Joined 5 days ago"
                    )
                )
            )
        }
    }

    private fun getInvitesFlow(workspaceId: String): MutableStateFlow<List<TeamInvite>> {
        return invitesByWs.getOrPut(workspaceId) {
            MutableStateFlow(
                listOf(
                    TeamInvite(
                        id = "inv_1",
                        email = "dave@discovery.org",
                        role = Role.Viewer,
                        sentAt = "Sent 2 days ago",
                        invitedBy = "Alex Vance"
                    )
                )
            )
        }
    }

    override fun getMembers(workspaceId: String): Flow<List<Member>> = getMembersFlow(workspaceId).asStateFlow()

    override fun getInvites(workspaceId: String): Flow<List<TeamInvite>> = getInvitesFlow(workspaceId).asStateFlow()

    override suspend fun inviteMember(workspaceId: String, email: String, role: Role): Result<TeamInvite> {
        val normalized = email.trim().lowercase()
        val currentMembers = getMembersFlow(workspaceId).value
        if (currentMembers.any { it.email.lowercase() == normalized }) {
            return Result.failure(IllegalArgumentException("User is already a member of this workspace."))
        }

        val currentInvites = getInvitesFlow(workspaceId).value
        if (currentInvites.any { it.email.lowercase() == normalized }) {
            return Result.failure(IllegalArgumentException("An invitation is already pending for this email."))
        }

        val invite = TeamInvite(
            id = "inv_${(currentInvites.size + 1)}",
            email = normalized,
            role = role,
            sentAt = "Just now",
            invitedBy = "Alex Vance"
        )
        getInvitesFlow(workspaceId).update { it + invite }
        return Result.success(invite)
    }

    override suspend fun updateMemberRole(workspaceId: String, memberId: String, newRole: Role): Result<Unit> {
        val flow = getMembersFlow(workspaceId)
        val current = flow.value
        val member = current.find { it.id == memberId }
            ?: return Result.failure(NoSuchElementException("Member not found"))

        if (member.role == Role.Owner && newRole != Role.Owner) {
            val ownerCount = current.count { it.role == Role.Owner }
            if (ownerCount <= 1) {
                return Result.failure(IllegalStateException("Cannot demote the sole workspace Owner. Transfer ownership first."))
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
                return Result.failure(IllegalStateException("Cannot remove the sole workspace Owner."))
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
}
