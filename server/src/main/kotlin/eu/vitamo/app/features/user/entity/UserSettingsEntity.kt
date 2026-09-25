package eu.vitamo.app.features.user.entity

import eu.vitamo.app.features.user.table.UserSettingsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class UserSettingsEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object :
        UuidEntityClass<UserSettingsEntity>(
            UserSettingsTable,
        )

    var userId by UserSettingsTable.userId
    var commentsEnabled by UserSettingsTable.commentsEnabled

    var createdAt by UserSettingsTable.createdAt
    var updatedAt by UserSettingsTable.updatedAt
}