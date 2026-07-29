package eu.vitamo.app.features.auth.model

import eu.vitamo.app.features.user.model.UserRecord

data class UserCredentialsRecord(
    val user: UserRecord,
    val passwordHash: String,
)