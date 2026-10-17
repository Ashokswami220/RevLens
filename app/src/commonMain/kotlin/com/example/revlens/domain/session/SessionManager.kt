package com.example.revlens.domain.session

import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SessionManager {
    val state: StateFlow<SessionState>
    suspend fun continueAsGuest()
    suspend fun onLoggedIn(user: UserAccount)
    suspend fun signOut(wipeLocalSyncedData: Boolean = false)
}

class DefaultSessionManager(initialState: SessionState = SessionState.Guest) : SessionManager {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    override suspend fun continueAsGuest() {
        _state.value = SessionState.Guest
    }

    override suspend fun onLoggedIn(user: UserAccount) {
        _state.value = SessionState.Authenticated(user)
    }

    override suspend fun signOut(wipeLocalSyncedData: Boolean) {
        _state.value = SessionState.SignedOut
    }
}
