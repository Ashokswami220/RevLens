package com.revlens.app.ui.navigation

enum class PublicStart {
    Landing,
    Intro,
    Login
}

sealed interface Gate {
    data class Public(val start: PublicStart) : Gate
    data object CreateWorkspace : Gate
    data object Onboarding : Gate
    data object Main : Gate
}
