package eu.vitamo.app.features.user.repository.helper

import eu.vitamo.app.api.contracts.media.MediaAsset
import kotlin.uuid.Uuid

interface UserMediaReader {
    suspend fun getProfileImage(
        userId: Uuid
    ): MediaAsset?

    suspend fun getCoverImage(
        userId: Uuid
    ): MediaAsset?
}