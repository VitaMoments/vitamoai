package eu.vitamo.app.features.auth.entity

import eu.vitamo.app.features.auth.model.EmailVerificationChallenge
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.table.EmailVerificationChallengesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.time.Instant
import kotlin.uuid.Uuid

class EmailVerificationChallengeEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<EmailVerificationChallengeEntity>(EmailVerificationChallengesTable)

    var userId by EmailVerificationChallengesTable.userId
    var email by EmailVerificationChallengesTable.email
    var codeHash by EmailVerificationChallengesTable.codeHash
    var purpose by EmailVerificationChallengesTable.purpose
    var createdAt by EmailVerificationChallengesTable.createdAt
    var expiresAt by EmailVerificationChallengesTable.expiresAt
    var consumedAt by EmailVerificationChallengesTable.consumedAt
    var attempts by EmailVerificationChallengesTable.attempts
    var lastAttemptAt by EmailVerificationChallengesTable.lastAttemptAt
}

fun EmailVerificationChallengeEntity.toModel(): EmailVerificationChallenge = EmailVerificationChallenge(
    id = id.value,
    userId = userId.value,
    email = email,
    codeHash = codeHash,
    purpose = EmailVerificationPurpose.valueOf(purpose),
    createdAt = createdAt,
    expiresAt = expiresAt,
    consumedAt = consumedAt,
    attempts = attempts,
    lastAttemptAt = lastAttemptAt,
)
