package eu.vitamo.app.media.table

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.api.contracts.media.MediaPurposeType
import eu.vitamo.app.api.contracts.media.MediaReferenceType
import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object MediaAssetsTable : UuidTable(name = "media_assets") {
    val referenceId = uuid(name = "reference_id")
    val referenceType = enumerationByName<MediaReferenceType>(name = "reference_type", length = 30)
    val purpose = enumerationByName<MediaPurposeType>(name = "purpose", length = 15)
    val privacy = enumerationByName<PrivacyStatus>(name = "privacy", length = 15)
    val originalFileName = varchar(name = "original_file_name", length = 255).nullable()
    val storedFileName = varchar(name = "stored_file_name", length = 255)
    val objectKey = varchar(name = "object_key", length =1000).uniqueIndex()
    val contentType = varchar(name = "content_type", length =100)
    val sizeBytes = long(name = "size_bytes")

    val width = integer(name = "width").nullable()
    val height = integer(name = "height").nullable()

    val createdAt = long(name = "created_at")
    val updatedAt = long(name = "updated_at")
    val deletedAt = long(name = "deleted_at").nullable()

    val createdBy = reference(
        name = "created_by",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE)

    init {
        index(false, referenceId, referenceType)
        index(false, referenceType, purpose)
        index(false, privacy)
    }
}