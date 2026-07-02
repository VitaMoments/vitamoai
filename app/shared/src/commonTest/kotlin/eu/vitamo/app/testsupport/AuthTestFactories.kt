package eu.vitamo.app.testsupport

import eu.vitamo.app.api.contracts.auth.SessionResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.UserRole
import kotlin.uuid.Uuid

fun sessionResponse(email: String): SessionResponse {
    return SessionResponse(
        user = fakeUser(email),
    )
}

fun fakeUser(email: String): AuthenticatedUser {
    return AuthenticatedUser(
        id = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
        displayName = "Test User",
        bio = null,
        role = UserRole.USER,
        firstName = "Test",
        lastName = "User",
        alias = null,
        birthDate = null,
        email = email,
    )
}
