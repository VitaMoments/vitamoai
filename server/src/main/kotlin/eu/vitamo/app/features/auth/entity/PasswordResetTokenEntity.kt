package eu.vitamo.app.features.auth.entity

import eu.vitamo.app.database.helpers.kotlinUuid
import eu.vitamo.app.features.auth.model.PasswordResetTokenRecord
import eu.vitamo.app.features.auth.table.PasswordResetTokensTable
import eu.vitamo.app.features.user.entity.UserEntity
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class PasswordResetTokenEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<PasswordResetTokenEntity>(PasswordResetTokensTable)

    var userRef by PasswordResetTokensTable.user
    var user by UserEntity referencedOn PasswordResetTokensTable.user

    var tokenHash by PasswordResetTokensTable.tokenHash
    var createdAt by PasswordResetTokensTable.createdAt
    var expiresAt by PasswordResetTokensTable.expiresAt
    var consumedAt by PasswordResetTokensTable.consumedAt
    var attempts by PasswordResetTokensTable.attempts
    var lastAttemptAt by PasswordResetTokensTable.lastAttemptAt

    val userId: Uuid
        get() = userRef.value

    val isConsumed: Boolean
        get() = consumedAt != null
}

fun PasswordResetTokenEntity.toRecord() : PasswordResetTokenRecord {
    return PasswordResetTokenRecord(
        id = this.kotlinUuid,
        userId = this.userId,
        tokenHash = this.tokenHash,
        createdAt = this.createdAt,
        expiresAt = this.expiresAt,
        consumedAt = this.consumedAt,
        attempts = this.attempts,
        lastAttemptAt = this.lastAttemptAt
    )
}