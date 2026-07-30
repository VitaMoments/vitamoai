package eu.vitamo.app.features.media.api

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.api.contracts.media.MediaPurpose
import eu.vitamo.app.api.contracts.media.MediaVisibility
import eu.vitamo.app.api.result.ApiResult

interface MediaApi {

    suspend fun uploadImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        purpose: MediaPurpose,
        visibility: MediaVisibility,
    ): ApiResult<MediaAsset>
}