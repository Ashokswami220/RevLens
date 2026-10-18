package com.revlens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    Owner,
    Admin,
    Editor,
    Viewer
}

@Serializable
data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean = false,
    val avatarUrl: String? = null,
    val jobTitle: String? = null
)

sealed interface SessionState {
    data object Unknown : SessionState
    data object SignedOut : SessionState
    data object Guest : SessionState
    data class Authenticated(val user: UserAccount) : SessionState
}
