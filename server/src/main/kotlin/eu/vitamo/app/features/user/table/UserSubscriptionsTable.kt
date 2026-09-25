package eu.vitamo.app.features.user.table

import eu.vitamo.app.api.contracts.subscribtions.SubscriptionPlan
import eu.vitamo.app.api.contracts.subscribtions.SubscriptionStatus
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object UserSubscriptionsTable : UuidTable(
    name = "user_subscriptions",
) {
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index(
        "user_subscriptions_user_id_idx",
    )

    val plan = enumerationByName<SubscriptionPlan>(
        name = "plan",
        length = 32,
    )

    val status = enumerationByName<SubscriptionStatus>(
        name = "status",
        length = 32,
    )

    val currentPeriodEnd = long(
        name = "current_period_end_epoch_seconds",
    ).nullable()

    val createdAt = long(
        name = "created_at_epoch_seconds",
    )

    val updatedAt = long(
        name = "updated_at_epoch_seconds",
    )
}