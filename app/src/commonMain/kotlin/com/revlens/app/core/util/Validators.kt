package com.revlens.app.core.util

import com.revlens.app.core.ui.UiText

object Validators {
    fun workspaceName(value: String): UiText? {
        val trimmed = value.trim()
        if (trimmed.length < 2) return UiText.DynamicString(
            "Workspace name must be at least 2 characters"
        )
        if (trimmed.length > 40) return UiText.DynamicString(
            "Workspace name cannot exceed 40 characters"
        )
        return null
    }
}
