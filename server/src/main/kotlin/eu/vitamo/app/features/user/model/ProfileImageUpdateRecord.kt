package eu.vitamo.app.features.user.model

import kotlin.uuid.Uuid

data class ProfileImageUpdateRecord(
    val user: UserRecord,
    val previousProfileImageId: Uuid?,
)