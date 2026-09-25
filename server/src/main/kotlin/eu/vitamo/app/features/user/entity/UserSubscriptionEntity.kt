package eu.vitamo.app.features.user.entity

import eu.vitamo.app.features.user.table.UserSubscriptionsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class UserSubscriptionEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object :
        UuidEntityClass<UserSubscriptionEntity>(
            UserSubscriptionsTable,
        )

    var userId by UserSubscriptionsTable.userId

    var plan by UserSubscriptionsTable.plan
    var status by UserSubscriptionsTable.status

    var currentPeriodEnd by
        UserSubscriptionsTable.currentPeriodEnd

    var createdAt by UserSubscriptionsTable.createdAt
    var updatedAt by UserSubscriptionsTable.updatedAt
}