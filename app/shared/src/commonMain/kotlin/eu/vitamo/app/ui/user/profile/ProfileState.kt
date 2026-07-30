package eu.vitamo.app.ui.user.profile

import eu.vitamo.app.api.contracts.user.UserWithContext

data class ProfileState(
    val profile: UserWithContext? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isProfileImageUploading: Boolean = false,
    val errorMessage: String? = null,
)