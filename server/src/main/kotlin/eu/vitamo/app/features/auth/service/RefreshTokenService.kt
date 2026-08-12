package eu.vitamo.app.features.auth.service

import eu.vitamo.app.features.auth.model.AuthToken
import eu.vitamo.app.features.auth.persistence.entity.RefreshTokenEntity
import eu.vitamo.app.features.auth.persistence.entity.isValid
import eu.vitamo.app.features.auth.persistence.entity.markCreated
import eu.vitamo.app.features.auth.persistence.entity.markExpires
import eu.vitamo.app.features.auth.persistence.entity.revoke
import eu.vitamo.app.features.auth.persistence.entity.touch
import eu.vitamo.app.features.auth.persistence.table.RefreshTokensTable
import eu.vitamo.app.features.device.table.DevicesTable
import eu.vitamo.app.features.user.table.UsersTable
import kotlin.time.Clock
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

open class RefreshTokenService(
    private val tokenHashService: TokenHashService,
) {

    fun create(
        authToken: AuthToken,
        userId: Uuid,
        deviceId: Uuid,
    ): RefreshTokenEntity = transaction {
        val now = Clock.System.now().epochSeconds
        val expiresAt = authToken.expiresAt.epochSeconds

        RefreshTokenEntity.new {
            tokenHash = tokenHashService.hash(
                authToken.token,
            )

            this.userId = EntityID(
                userId,
                UsersTable,
            )

            this.deviceId = EntityID(
                deviceId,
                DevicesTable,
            )

            markExpires(
                expiresAt,
            )

            markCreated(
                now,
            )

            revokedAt = null
            lastUsedAt = null
            replacedBySessionId = null
            deletedAt = null
        }
    }

    fun findValid(
        refreshToken: String,
    ): RefreshTokenEntity? = transaction {
        val tokenHash =
            tokenHashService.hash(refreshToken)

        val now =
            Clock.System.now().epochSeconds

        RefreshTokenEntity
            .find {
                RefreshTokensTable.tokenHash eq tokenHash
            }
            .firstOrNull { entity ->
                entity.isValid(now)
            }
    }

    fun markUsed(
        tokenId: EntityID<Long>,
    ) {
        transaction {
            RefreshTokenEntity
                .findById(tokenId)
                ?.touch(
                    Clock.System.now().epochSeconds,
                )
        }
    }

    fun revoke(
        tokenId: EntityID<Long>,
    ) {
        transaction {
            RefreshTokenEntity
                .findById(tokenId)
                ?.revoke(
                    Clock.System.now().epochSeconds,
                )
        }
    }
}