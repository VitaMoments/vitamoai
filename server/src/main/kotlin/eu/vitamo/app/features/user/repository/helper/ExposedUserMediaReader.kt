package eu.vitamo.app.features.user.repository.helper

import eu.vitamo.app.api.contracts.media.MediaAsset
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.user.entity.UserEntity
import kotlin.uuid.Uuid

class ExposedUserMediaReader : UserMediaReader {

    override suspend fun getProfileImage(
        userId: Uuid
    ): MediaAsset? = dbQuery {
        val user = UserEntity.findById(userId) ?: return@dbQuery null


        null
//        val profileImageMediaId = user.profileImageMediaId ?: return@dbQuery null
//
//        MediaAssetEntity.findById(profileImageMediaId.toJavaUuid())
//            ?.takeIf { it.deletedAt == null }
//            ?.toMediaAsset()
    }

    override suspend fun getCoverImage(
        userId: Uuid
    ): MediaAsset? = dbQuery {
        null
    }
}