package eu.vitamo.app.features.user.model

import kotlin.uuid.Uuid

data class UserSettingsRecord(
    val userId: Uuid,
    val commentsEnabled: Boolean,
)