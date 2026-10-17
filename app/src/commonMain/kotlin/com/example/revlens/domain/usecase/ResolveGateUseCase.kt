package com.example.revlens.domain.usecase

import com.example.revlens.domain.model.SessionState
import com.example.revlens.domain.model.Workspace
import com.example.revlens.model.BusinessProfile
import com.example.revlens.ui.navigation.Gate
import com.example.revlens.ui.navigation.PublicStart

class ResolveGateUseCase {
    operator fun invoke(
        session: SessionState,
        workspace: Workspace?,
        baseline: BusinessProfile?,
        isWeb: Boolean = false,
        introSeen: Boolean = true
    ): Gate {
        // Priority 1: Signed out (not guest)
        if (session is SessionState.SignedOut || session is SessionState.Unknown) {
            val start = when {
                isWeb -> PublicStart.Landing
                !introSeen -> PublicStart.Intro
                else -> PublicStart.Login
            }
            return Gate.Public(start)
        }

        // Signed in or Guest:
        // Priority 2: No workspace created yet
        if (workspace == null) {
            return Gate.CreateWorkspace
        }

        // Priority 3: Workspace exists, but no baseline saved
        if (baseline == null) {
            return Gate.Onboarding
        }

        // Priority 4: Everything present -> Main app
        return Gate.Main
    }
}
