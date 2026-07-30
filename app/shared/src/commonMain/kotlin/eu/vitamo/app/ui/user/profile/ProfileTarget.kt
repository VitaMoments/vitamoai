package eu.vitamo.app.ui.user.profile

import kotlin.uuid.Uuid

sealed interface ProfileTarget {

    data object CurrentUser : ProfileTarget

    data class User(
        val userId: Uuid,
    ) : ProfileTarget
}