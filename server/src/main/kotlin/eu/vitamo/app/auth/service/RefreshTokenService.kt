package eu.vitamo.app.auth.service

import eu.vitamo.app.auth.ClientContext
import eu.vitamo.app.auth.model.AuthToken
import eu.vitamo.app.auth.persistence.refresh.RefreshTokenEntity
import eu.vitamo.app.auth.persistence.refresh.RefreshTokensTable
import eu.vitamo.app.auth.persistence.refresh.applyClientContext
import eu.vitamo.app.auth.persistence.refresh.isValid
import eu.vitamo.app.auth.persistence.refresh.markCreated
import eu.vitamo.app.auth.persistence.refresh.markExpires
import eu.vitamo.app.auth.persistence.refresh.revoke
import eu.vitamo.app.auth.persistence.refresh.touch
import eu.vitamo.app.auth.persistence.users.UserEntity
import eu.vitamo.app.auth.persistence.users.UsersTable
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class RefreshTokenService(
    private val tokenHashService: TokenHashService,
) {
    fun create(
        authToken: AuthToken,
        userEntity: UserEntity,
        context: ClientContext?,
    ): RefreshTokenEntity = transaction {
        val now = Clock.System.now().epochSeconds
        val expiresAt = authToken.expiresAt.epochSeconds
        RefreshTokenEntity.new {
            tokenHash = tokenHashService.hash(authToken.token)
            userId = EntityID(userEntity.id.value, UsersTable)
            applyClientContext(context)
            markExpires(expiresAt)
            markCreated(now)
            revokedAt = null
            lastUsedAt = null
            replacedBySessionId = null
            deletedAt = null
        }
    }

    fun findValid(refreshToken: String): RefreshTokenEntity? = transaction {
        val tokenHash = tokenHashService.hash(refreshToken)
        val now = Clock.System.now().epochSeconds

        RefreshTokenEntity.find {
            RefreshTokensTable.tokenHash eq tokenHash
        }
            .firstOrNull { entity ->
                entity.tokenHash == tokenHash && entity.isValid(now)
            }
    }

    fun markUsed(tokenId: EntityID<Long>) {
        transaction {
            RefreshTokenEntity.findById(tokenId)?.let {
                it.touch(Clock.System.now().epochSeconds)
            }
        }
    }

    fun revoke(tokenId: EntityID<Long>) {
        transaction {
            RefreshTokenEntity.findById(tokenId)?.let {
                it.revoke(Clock.System.now().epochSeconds)
            }
        }
    }
}
