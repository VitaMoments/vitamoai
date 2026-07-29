package eu.vitamo.app.features.user.model

data class UserCredentialsRecord(
    val user: UserRecord,
    val passwordHash: String,
)