package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.user.PublicUser
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface BaseFeedItem {
    @Contextual
    val uuid: Uuid
    val author: PublicUser
    val content: RichTextDocument
    val privacy: PrivacyStatus

    @Contextual
    val createdAt: Instant

    @Contextual
    val updatedAt: Instant

    @Contextual
    val deletedAt: Instant?
}
