package com.example.revlens.ui.screens.profile

import com.example.revlens.domain.model.Role

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val jobTitle: String = "",
    val role: Role = Role.Owner,
    val timezone: String = "UTC",
    val currency: String = "USD",
    val isGuest: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val hasChanges: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteError: String? = null
)

sealed interface ProfileEvent {
    data class NameChanged(val name: String) : ProfileEvent
    data class JobTitleChanged(val jobTitle: String) : ProfileEvent
    data class TimezoneChanged(val timezone: String) : ProfileEvent
    data class CurrencyChanged(val currency: String) : ProfileEvent
    data object SaveChanges : ProfileEvent
    data object OpenDeleteDialog : ProfileEvent
    data object DismissDeleteDialog : ProfileEvent
    data class ConfirmDeleteAccount(val confirmationWord: String) : ProfileEvent
}

sealed interface ProfileEffect {
    data class ShowToast(val message: String) : ProfileEffect
    data object NavigateBack : ProfileEffect
    data object AccountDeleted : ProfileEffect
}
