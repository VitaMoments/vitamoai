package eu.vitamo.app.features.media.di

import eu.vitamo.app.features.media.policy.MediaAccessPolicy
import eu.vitamo.app.features.media.policy.MediaAccessPolicyImpl
import eu.vitamo.app.features.media.processor.ImageProcessor
import eu.vitamo.app.features.media.repository.MediaAssetRepository
import eu.vitamo.app.features.media.repository.MediaAssetRepositoryImpl
import eu.vitamo.app.features.media.storage.LocalMediaStorage
import eu.vitamo.app.features.media.storage.MediaStorage
import eu.vitamo.app.features.media.usecase.GetMediaContentUseCase
import eu.vitamo.app.features.media.usecase.UploadImageUseCase
import eu.vitamo.app.infrastructure.media.MediaStorageConfig
import eu.vitamo.app.infrastructure.media.MediaStorageConfigLoader
import org.koin.dsl.module

val mediaModule = module {
    single {
        MediaStorageConfigLoader.loadOrThrow()
    }

    single<MediaStorage> {
        LocalMediaStorage(
            rootDirectory = get<MediaStorageConfig>()
                .rootDirectory,
        )
    }
    single {  }

    single {
        ImageProcessor()
    }

    single<MediaAssetRepository> {
        MediaAssetRepositoryImpl()
    }


    single<MediaAccessPolicy> {
        MediaAccessPolicyImpl()
    }

    single {
        UploadImageUseCase(
            mediaRepository = get(),
            imageProcessor = get(),
            mediaStorage = get(),
        )
    }

    single {
        GetMediaContentUseCase(
            mediaRepository = get(),
            mediaAccessPolicy = get(),
            mediaStorage = get(),
        )
    }
}