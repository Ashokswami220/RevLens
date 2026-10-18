package com.revlens.app.domain.repository

import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.domain.model.TeamInvite
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getMembers(workspaceId: String): Flow<List<Member>>
    fun getInvites(workspaceId: String): Flow<List<TeamInvite>>
    suspend fun inviteMember(workspaceId: String, email: String, role: Role): Result<TeamInvite>
    suspend fun updateMemberRole(workspaceId: String, memberId: String, newRole: Role): Result<Unit>
    suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit>
    suspend fun revokeInvite(workspaceId: String, inviteId: String): Result<Unit>
    suspend fun resendInvite(workspaceId: String, inviteId: String): Result<Unit>
}
