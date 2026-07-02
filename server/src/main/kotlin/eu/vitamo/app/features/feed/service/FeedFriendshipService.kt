package eu.vitamo.app.features.feed.service

import kotlin.uuid.Uuid

interface FeedFriendshipService {
    suspend fun areFriends(userA: Uuid, userB: Uuid): Boolean
}

class NoopFeedFriendshipService : FeedFriendshipService {
    override suspend fun areFriends(userA: Uuid, userB: Uuid): Boolean = false
}
