package eu.vitamo.app.features.friendship.table

import eu.vitamo.app.features.friendship.model.FriendshipStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object FriendshipsTable : Table(
    name = "friendships",
) {
    val id = uuid("id")

    val userLowId =
        uuid("user_low_id")

    val userHighId =
        uuid("user_high_id")

    val requestedById =
        uuid("requested_by_id")

    val status =
        enumerationByName<FriendshipStatus>(
            name = "status",
            length = 20,
        )

    val createdAt =
        timestamp("created_at")

    val updatedAt =
        timestamp("updated_at")

    val acceptedAt =
        timestamp("accepted_at")
            .nullable()

    override val primaryKey =
        PrimaryKey(
            id,
            name = "pk_friendships",
        )
}