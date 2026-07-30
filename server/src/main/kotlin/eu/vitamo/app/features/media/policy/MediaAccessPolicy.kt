package eu.vitamo.app.features.media.policy

import eu.vitamo.app.features.media.model.MediaAssetRecord
import kotlin.uuid.Uuid

interface MediaAccessPolicy {

    suspend fun canRead(
        currentUserId: Uuid,
        asset: MediaAssetRecord,
    ): Boolean
}