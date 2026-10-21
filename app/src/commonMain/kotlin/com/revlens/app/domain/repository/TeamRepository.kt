package com.revlens.app.domain.repository

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import com.revlens.app.domain.model.Workspace
import com.revlens.app.model.BusinessProfile
import kotlinx.coroutines.flow.Flow

enum class MembershipCheckResult {
    ACTIVE,
    REVOKED,
    UNKNOWN
}

interface TeamRepository {
    fun getMembers(workspaceId: String): Flow<List<Member>>
    fun getInvites(workspaceId: String): Flow<List<TeamInvite>>
    suspend fun inviteMember(
        workspaceId: String,
        email: String,
        role: Role,
        inviterName: String = "Workspace Owner"
    ): Result<TeamInvite>

    suspend fun updateMemberRole(workspaceId: String, memberId: String, newRole: Role): Result<Unit>
    suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit>
    suspend fun revokeInvite(workspaceId: String, inviteId: String): Result<Unit>
    suspend fun resendInvite(workspaceId: String, inviteId: String): Result<Unit>

    suspend fun getPendingInvitesForEmail(email: String): List<TeamInviteDetail> = emptyList()
    suspend fun acceptInvite(inviteId: String, user: UserAccount): Result<String> =
        Result.failure(UnsupportedOperationException())

    suspend fun declineInvite(inviteId: String, email: String): Result<Unit> = Result.success(Unit)
    suspend fun isUserAssociatedWithAnotherBusiness(
        email: String,
        currentWorkspaceId: String? = null
    ): Boolean = false

    suspend fun getUserRoleInWorkspace(workspaceId: String, userEmail: String): Role? = null
    suspend fun leaveWorkspace(workspaceId: String, userEmail: String): Result<Unit> =
        Result.success(Unit)

    suspend fun getWorkspaceProfile(workspaceId: String): BusinessProfile? = null
    suspend fun saveWorkspaceProfile(workspaceId: String, profile: BusinessProfile) {}

    suspend fun getWorkspaceJoinCode(workspaceId: String): String {
        val suffix = workspaceId.removePrefix("ws_")
            .uppercase()
            .take(6)
            .padEnd(6, '0')
        return "RL-$suffix"
    }

    suspend fun joinWorkspaceByCode(code: String, user: UserAccount): Result<String> =
        Result.failure(IllegalArgumentException("Invalid team code"))

    suspend fun getRemoteWorkspace(workspaceId: String): Workspace? = null

    suspend fun checkMembershipStatus(
        workspaceId: String,
        userId: String,
        userEmail: String
    ): MembershipCheckResult = MembershipCheckResult.UNKNOWN

    suspend fun transferOwnership(
        workspaceId: String,
        newOwnerMemberId: String
    ): Result<Unit> = Result.success(Unit)
}
