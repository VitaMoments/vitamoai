package eu.vitamo.app.media

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.api.contracts.media.MediaAssetContext
import eu.vitamo.app.api.contracts.media.MediaReferenceType
import kotlin.uuid.Uuid

class MediaAccessService {

    suspend fun canView(
        media: MediaAssetContext,
        requesterUserId: Uuid?
    ): Boolean {
        return when (media.privacy) {
            PrivacyStatus.PUBLIC -> true

            PrivacyStatus.PRIVATE -> {
                requesterUserId != null && requesterUserId == media.createdBy
            }

            PrivacyStatus.FRIENDS_ONLY -> {
                requesterUserId != null && when (media.referenceType) {
                    MediaReferenceType.USER -> requesterUserId == media.referenceId || requesterUserId == media.createdBy
                    MediaReferenceType.FEED_ITEM -> true
                    else -> requesterUserId == media.createdBy
                }
            }
        }
    }

    suspend fun canDelete(
        media: MediaAssetContext,
        requesterUserId: Uuid?
    ): Boolean {
        return requesterUserId != null && requesterUserId == media.createdBy
    }

    suspend fun canUpload(
        referenceId: Uuid,
        referenceType: MediaReferenceType,
        requesterUserId: Uuid?
    ): Boolean {
        if (requesterUserId == null) return false

        return when (referenceType) {
            MediaReferenceType.USER -> requesterUserId == referenceId
            else -> true
        }
    }
}