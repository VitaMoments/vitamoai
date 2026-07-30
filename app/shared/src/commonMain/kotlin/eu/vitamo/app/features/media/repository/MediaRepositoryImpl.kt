package eu.vitamo.app.features.media.repository

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.features.media.api.MediaApi
import eu.vitamo.app.mapper.toRepositoryResult
import eu.vitamo.app.repository.RepositoryResult

class MediaRepositoryImpl(
    private val mediaApi: MediaApi,
) : MediaRepository {

    override suspend fun uploadImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        purpose: MediaPurpose,
        visibility: MediaVisibility,
    ): RepositoryResult<MediaAsset> {
        return mediaApi.uploadImage(
            fileName = fileName,
            mimeType = mimeType,
            bytes = bytes,
            purpose = purpose,
            visibility = visibility,
        ).toRepositoryResult { mediaAsset ->
            mediaAsset
        }
    }
}