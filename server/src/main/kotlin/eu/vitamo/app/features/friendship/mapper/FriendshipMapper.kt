package eu.vitamo.app.features.friendship.mapper

import eu.vitamo.app.features.friendship.model.FriendshipRecord
import eu.vitamo.app.features.friendship.table.FriendshipsTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toFriendshipRecord():
        FriendshipRecord {
    return FriendshipRecord(
        id = this[FriendshipsTable.id],
        userLowId =
            this[FriendshipsTable.userLowId],
        userHighId =
            this[FriendshipsTable.userHighId],
        requestedById =
            this[FriendshipsTable.requestedById],
        status =
            this[FriendshipsTable.status],
        createdAt =
            this[FriendshipsTable.createdAt],
        updatedAt =
            this[FriendshipsTable.updatedAt],
        acceptedAt =
            this[FriendshipsTable.acceptedAt],
    )
}