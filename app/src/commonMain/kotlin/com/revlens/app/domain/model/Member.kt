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
    val sentAt: String = "2 days ago",
    val invitedBy: String = "Alex Vance"
)
