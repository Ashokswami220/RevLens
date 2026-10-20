package com.revlens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val avatarUrl: String? = null,
    val joinedAt: String = "Joined recently"
)

@Serializable
data class TeamInvite(
    val id: String,
    val email: String,
    val role: Role,
    val sentAt: String = "Just now",
    val invitedBy: String = "Owner"
)

@Serializable
data class TeamInviteDetail(
    val inviteId: String,
    val workspaceId: String,
    val workspaceName: String,
    val invitedEmail: String,
    val role: Role,
    val invitedBy: String,
    val sentAt: String = "Recently"
)

