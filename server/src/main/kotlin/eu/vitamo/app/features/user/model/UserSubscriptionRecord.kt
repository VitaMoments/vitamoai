package eu.vitamo.app.features.user.model

import eu.vitamo.app.api.contracts.subscribtions.SubscriptionPlan
import eu.vitamo.app.api.contracts.subscribtions.SubscriptionStatus
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserSubscriptionRecord(
    val id: Uuid,
    val userId: Uuid,
    val plan: SubscriptionPlan,
    val status: SubscriptionStatus,
    val currentPeriodEnd: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)