package eu.vitamo.app.features.media.policy

import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.features.media.model.MediaAssetRecord
import kotlin.uuid.Uuid

class MediaAccessPolicyImpl : MediaAccessPolicy {

    override suspend fun canRead(
        currentUserId: Uuid,
        asset: MediaAssetRecord,
    ): Boolean {
        if (currentUserId == asset.ownerId) {
            return true
        }

        return when (asset.visibility) {
            MediaVisibility.PUBLIC ->
                true

            MediaVisibility.AUTHENTICATED ->
                true

            MediaVisibility.FRIENDS ->
                false

            MediaVisibility.PRIVATE ->
                false
        }
    }
}