package eu.vitamo.app.features.auth.model

import eu.vitamo.app.api.contracts.user.AuthenticatedUser

data class LoginSession(
    val user: AuthenticatedUser,
    val accessToken: AuthToken,
    val refreshToken: AuthToken,
)
