package eu.vitamo.app.features.auth.persistence.table

import eu.vitamo.app.features.device.table.DevicesTable
import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object RefreshTokensTable : LongIdTable("refresh_tokens") {
    val tokenHash = varchar(name = "token_hash", length = 255).uniqueIndex("refresh_tokens_token_hash_uidx")

    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index(
        "refresh_tokens_user_id_idx",
    )

    val deviceId = reference(
        name = "device_id",
        foreign = DevicesTable,
        onDelete = ReferenceOption.CASCADE,
    ).index(
        "refresh_tokens_device_id_idx",
    )

    val expiredAt = long(
        name = "expired_at_epoch_seconds",
    ).index(
        "refresh_tokens_expired_at_idx",
    )

    val lastUsedAt = long(
        name = "last_used_at_epoch_seconds",
    ).nullable()

    val revokedAt = long(
        name = "revoked_at_epoch_seconds",
    ).nullable().index(
        "refresh_tokens_revoked_at_idx",
    )

    val replacedBySessionId = uuid(
        name = "replaced_by_session_id",
    ).nullable()

    val createdAt = long(
        name = "created_at_epoch_seconds",
    )

    val updatedAt = long(
        name = "updated_at_epoch_seconds",
    )

    val deletedAt = long(
        name = "deleted_at_epoch_seconds",
    ).nullable()
}