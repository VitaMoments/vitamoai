package eu.vitamo.app.api.contracts.friendship

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SendFriendRequestRequest(
    @Contextual
    val targetUserId: Uuid,
)