package com.revlens.app.core.util

import com.revlens.app.core.ui.UiText

object Validators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun email(value: String): UiText? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return UiText.DynamicString("Email cannot be empty")
        if (!EMAIL_REGEX.matches(trimmed)) return UiText.DynamicString("Please enter a valid email address")
        return null
    }

    fun password(value: String): UiText? {
        if (value.isEmpty()) return UiText.DynamicString("Password cannot be empty")
        if (value.length < 8) return UiText.DynamicString("Password must be at least 8 characters")
        return null
    }

    fun workspaceName(value: String): UiText? {
        val trimmed = value.trim()
        if (trimmed.length < 2) return UiText.DynamicString("Workspace name must be at least 2 characters")
        if (trimmed.length > 40) return UiText.DynamicString("Workspace name cannot exceed 40 characters")
        return null
    }

    fun positiveNumber(value: String, fieldName: String = "Value"): UiText? {
        val num = value.toDoubleOrNull()
        if (num == null || num <= 0.0) return UiText.DynamicString("$fieldName must be greater than zero")
        return null
    }

    fun nonNegativeNumber(value: String, fieldName: String = "Value"): UiText? {
        val num = value.toDoubleOrNull()
        if (num == null || num < 0.0) return UiText.DynamicString("$fieldName cannot be negative")
        return null
    }
}
