package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.subscribtions.SubscriptionPlan
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface UserSubscriptionRepository {

    suspend fun resolvePlan(
        userId: Uuid,
        now: Instant,
    ): RepositoryResult<SubscriptionPlan>
}