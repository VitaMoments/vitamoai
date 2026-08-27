package eu.vitamo.app.features.media.table

import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaStatus
import eu.vitamo.app.api.contracts.media.MediaType
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.features.feed.table.FeedItemTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object MediaAssetsTable : UuidTable(
    name = "media_assets",
) {
    val ownerId = uuid(name = "owner_id",).index()

    val feedItemId = reference(
        name = "feed_item_id",
        foreign = FeedItemTable,
        onDelete = ReferenceOption.SET_NULL,
    ).nullable()
        .index("media_assets_feed_item_id_idx")

    val position = integer(
        name = "position",
    ).nullable()

    val mediaType = enumerationByName(
        name = "media_type",
        length = 32,
        klass = MediaType::class,
    )

    val purpose = enumerationByName(
        name = "purpose",
        length = 64,
        klass = MediaPurpose::class,
    )

    val status = enumerationByName(
        name = "status",
        length = 32,
        klass = MediaStatus::class,
    )

    val visibility = enumerationByName(
        name = "visibility",
        length = 32,
        klass = MediaVisibility::class,
    )

    val storageKey = varchar(
        name = "storage_key",
        length = 512,
    ).uniqueIndex()

    val mimeType = varchar(
        name = "mime_type",
        length = 128,
    )

    val sizeBytes = long("size_bytes")

    val width = integer("width")
        .nullable()

    val height = integer("height")
        .nullable()

    val sha256 = varchar(
        name = "sha256",
        length = 64,
    )

    val createdAt = timestamp("created_at")

    val updatedAt = timestamp("updated_at")

    val deletedAt = timestamp("deleted_at")
        .nullable()

    init {
        uniqueIndex(
            "media_assets_feed_item_position_uidx",
            feedItemId, position
        )
    }
}