package com.revlens.app.core.ui

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object InvalidCredentials : AppError
    data object Forbidden : AppError
    data object NotFound : AppError
    data class Conflict(val message: String? = null) : AppError
    data class Validation(val fields: Map<String, String>) : AppError
    data class RateLimited(val retryAfterSeconds: Int? = null) : AppError
    data class PlanLimit(val feature: String) : AppError
    data object Server : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}

fun AppError.toUiText(): UiText = when (this) {
    AppError.Network -> UiText.DynamicString(
        "Network error. Please check your connection and retry."
    )

    AppError.Unauthorized -> UiText.DynamicString("Your session has expired. Please sign in again.")
    AppError.InvalidCredentials -> UiText.DynamicString("Invalid email or password.")
    AppError.Forbidden -> UiText.DynamicString("You do not have permission to perform this action.")
    AppError.NotFound -> UiText.DynamicString("Requested resource was not found.")
    is AppError.Conflict -> UiText.DynamicString(
        message ?: "A conflicting resource already exists."
    )

    is AppError.Validation -> UiText.DynamicString(
        fields.values.firstOrNull() ?: "Please check input fields."
    )

    is AppError.RateLimited -> UiText.DynamicString(
        if (retryAfterSeconds != null) "Too many attempts. Try again in $retryAfterSeconds seconds."
        else "Too many attempts. Please try again later."
    )

    is AppError.PlanLimit -> UiText.DynamicString("Upgrade your plan to unlock $feature.")
    AppError.Server -> UiText.DynamicString("Server error. Please try again shortly.")
    is AppError.Unknown -> UiText.DynamicString(cause?.message ?: "An unexpected error occurred.")
}

class AppException(val error: AppError) : Exception(
    error.toUiText()
        .asString()
)

fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> this.error
    else -> AppError.Unknown(this)
}

fun Throwable.toUiText(): UiText = toAppError().toUiText()

