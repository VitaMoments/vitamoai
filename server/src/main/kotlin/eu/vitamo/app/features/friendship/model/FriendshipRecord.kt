package eu.vitamo.app.features.friendship.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class FriendshipRecord(
    val id: Uuid,
    val userLowId: Uuid,
    val userHighId: Uuid,
    val requestedById: Uuid,
    val status: FriendshipStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val acceptedAt: Instant?,
) {
    fun containsUser(
        userId: Uuid,
    ): Boolean {
        return userLowId == userId ||
                userHighId == userId
    }

    fun otherUserId(
        currentUserId: Uuid,
    ): Uuid {
        require(containsUser(currentUserId)) {
            "User is not part of this friendship."
        }

        return if (userLowId == currentUserId) {
            userHighId
        } else {
            userLowId
        }
    }

    fun isRequestedBy(
        userId: Uuid,
    ): Boolean {
        return requestedById == userId
    }
}