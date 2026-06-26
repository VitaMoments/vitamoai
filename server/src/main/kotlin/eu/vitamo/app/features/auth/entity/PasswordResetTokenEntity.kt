package eu.vitamo.app.features.auth.entity

import eu.vitamo.app.features.auth.table.PasswordResetTokensTable
import eu.vitamo.app.features.user.entity.UserEntity
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class PasswordResetTokenEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<PasswordResetTokenEntity>(PasswordResetTokensTable)

    var user by UserEntity referencedOn PasswordResetTokensTable.user
    var tokenHash by PasswordResetTokensTable.tokenHash
    var createdAt by PasswordResetTokensTable.createdAt
    var expiresAt by PasswordResetTokensTable.expiresAt
    var consumedAt by PasswordResetTokensTable.consumedAt
    var attempts by PasswordResetTokensTable.attempts
    var lastAttemptAt by PasswordResetTokensTable.lastAttemptAt

    val isConsumed: Boolean
        get() = consumedAt != null
}