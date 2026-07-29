package eu.vitamo.app.features.user.context

data class UserContext(
    val accessLevel: UserAccessLevel,
) {
    val isSelf: Boolean
        get() = accessLevel == UserAccessLevel.SELF
}