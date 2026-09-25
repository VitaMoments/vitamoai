package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.subscribtions.SubscriptionPlan
import eu.vitamo.app.api.contracts.subscribtions.SubscriptionStatus
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.user.entity.UserSubscriptionEntity
import eu.vitamo.app.features.user.model.UserSubscriptionRecord
import eu.vitamo.app.features.user.table.UserSubscriptionsTable
import eu.vitamo.app.features.user.table.UsersTable
import eu.vitamo.app.repository.RepositoryResult
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserSubscriptionRepositoryImpl :
    UserSubscriptionRepository {

    override suspend fun resolvePlan(
        userId: Uuid,
        now: Instant,
    ): RepositoryResult<SubscriptionPlan> =
        dbQuery {
            val userEntityId =
                EntityID(
                    id = userId,
                    table = UsersTable,
                )

            val subscriptions =
                UserSubscriptionEntity
                    .find {
                        UserSubscriptionsTable.userId eq
                            userEntityId
                    }
                    .orderBy(
                        UserSubscriptionsTable.createdAt to
                            SortOrder.DESC,
                    )
                    .map {
                        it.toRecord()
                    }

            val effectiveSubscription =
                subscriptions.firstOrNull { subscription ->
                    subscription.isEffective(
                        now = now,
                    )
                }

            RepositoryResult.Success(
                data =
                    effectiveSubscription
                        ?.plan
                        ?: SubscriptionPlan.BASIC,
            )
        }

    private fun UserSubscriptionRecord.isEffective(
        now: Instant,
    ): Boolean {
        if (plan == SubscriptionPlan.BASIC) {
            return false
        }

        return when (status) {
            SubscriptionStatus.ACTIVE -> {
                currentPeriodEnd == null ||
                    currentPeriodEnd > now
            }

            SubscriptionStatus.CANCELLED -> {
                currentPeriodEnd != null &&
                    currentPeriodEnd > now
            }

            SubscriptionStatus.EXPIRED -> {
                false
            }
        }
    }

    private fun UserSubscriptionEntity.toRecord() =
        UserSubscriptionRecord(
            id = id.value,
            userId = userId.value,
            plan = plan,
            status = status,
            currentPeriodEnd =
                currentPeriodEnd?.let(
                    Instant::fromEpochSeconds,
                ),
            createdAt =
                Instant.fromEpochSeconds(
                    createdAt,
                ),
            updatedAt =
                Instant.fromEpochSeconds(
                    updatedAt,
                ),
        )
}