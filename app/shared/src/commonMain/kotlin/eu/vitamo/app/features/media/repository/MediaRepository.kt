package eu.vitamo.app.features.media.repository

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.repository.RepositoryResult

interface MediaRepository {

    suspend fun uploadImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        purpose: MediaPurpose,
        visibility: MediaVisibility,
    ): RepositoryResult<MediaAsset>
}