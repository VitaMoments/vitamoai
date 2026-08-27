package eu.vitamo.app.features.feed.entity

import eu.vitamo.app.features.feed.table.PostTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class PostEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {

    companion object : UuidEntityClass<PostEntity>(PostTable)

    var feedItemId by PostTable.feedItemId
    var title by PostTable.title
    var messageJson by PostTable.messageJson
}