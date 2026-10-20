package com.revlens.app.domain.repository

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import com.revlens.app.domain.model.TeamInviteDetail
import com.revlens.app.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getMembers(workspaceId: String): Flow<List<Member>>
    fun getInvites(workspaceId: String): Flow<List<TeamInvite>>
    suspend fun inviteMember(workspaceId: String, email: String, role: Role, inviterName: String = "Admin"): Result<TeamInvite>
    suspend fun updateMemberRole(workspaceId: String, memberId: String, newRole: Role): Result<Unit>
    suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit>
    suspend fun revokeInvite(workspaceId: String, inviteId: String): Result<Unit>
    suspend fun resendInvite(workspaceId: String, inviteId: String): Result<Unit>

    suspend fun getPendingInvitesForEmail(email: String): List<TeamInviteDetail>
    suspend fun acceptInvite(inviteId: String, user: UserAccount): Result<String>
    suspend fun declineInvite(inviteId: String, email: String): Result<Unit>
    suspend fun isUserAssociatedWithAnotherBusiness(email: String, currentWorkspaceId: String? = null): Boolean
    suspend fun getUserRoleInWorkspace(workspaceId: String, userEmail: String): Role?
    suspend fun leaveWorkspace(workspaceId: String, userEmail: String): Result<Unit>
}
