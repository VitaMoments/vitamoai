package eu.vitamo.app.features.media

import eu.vitamo.app.features.media.image.ImageCompressor
import eu.vitamo.app.features.media.api.MediaApi
import eu.vitamo.app.features.media.api.MediaApiImpl
import eu.vitamo.app.features.media.repository.MediaRepository
import eu.vitamo.app.features.media.repository.MediaRepositoryImpl
import org.koin.dsl.module

val mediaModule = module {
    single { ImageCompressor() }

    single<MediaApi> {
        MediaApiImpl(
            get(),
            get(),
        )
    }

    single<MediaRepository> {
        MediaRepositoryImpl(
            get(),
        )
    }
}