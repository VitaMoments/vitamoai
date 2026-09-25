package eu.vitamo.app.features.user.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object UserSettingsTable : UuidTable(
    name = "user_settings",
) {
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).uniqueIndex(
        "user_settings_user_id_uidx",
    )

    val commentsEnabled = bool(
        name = "comments_enabled",
    ).default(true)

    val createdAt = long(
        name = "created_at_epoch_seconds",
    )

    val updatedAt = long(
        name = "updated_at_epoch_seconds",
    )
}