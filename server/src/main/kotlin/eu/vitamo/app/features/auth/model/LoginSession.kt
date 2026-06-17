package eu.vitamo.app.features.auth.model

import eu.vitamo.app.features.auth.model.AuthToken
import eu.vitamo.app.features.user.model.UserAccount

data class LoginSession(
    val user: UserAccount,
    val accessToken: AuthToken,
    val refreshToken: AuthToken,
)
