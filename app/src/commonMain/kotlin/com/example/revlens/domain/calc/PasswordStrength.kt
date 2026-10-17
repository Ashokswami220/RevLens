package com.example.revlens.domain.calc

data class PasswordEvaluation(
    val score: Int, // 0 to 4
    val label: String,
    val feedback: String? = null
)

object PasswordStrength {
    private val COMMON_PASSWORDS = setOf(
        "password", "12345678", "123456789", "qwerty", "admin123",
        "welcome", "password123", "letmein", "testing123"
    )

    fun evaluate(password: String): PasswordEvaluation {
        if (password.isEmpty()) {
            return PasswordEvaluation(0, "Empty")
        }

        if (password.lowercase() in COMMON_PASSWORDS) {
            return PasswordEvaluation(1, "Very Weak", "This password is too common")
        }

        var points = 0
        if (password.length >= 8) points++
        if (password.length >= 12) points++

        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        if (hasLower && hasUpper) points++

        val hasDigit = password.any { it.isDigit() }
        if (hasDigit) points++

        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (hasSpecial) points++

        // Scale to 0..4
        val score = when {
            password.length < 8 -> 1
            points <= 1 -> 1
            points == 2 -> 2
            points in 3..4 -> 3
            else -> 4
        }

        val label = when (score) {
            0, 1 -> "Weak"
            2 -> "Fair"
            3 -> "Good"
            else -> "Strong"
        }

        val feedback = when (score) {
            0, 1 -> "Use at least 8 characters with numbers and symbols"
            2 -> "Add numbers and uppercase letters to strengthen"
            3 -> "Add special symbols to make it very strong"
            else -> null
        }

        return PasswordEvaluation(score = score, label = label, feedback = feedback)
    }
}
