package eu.vitamo.app.media.di

import eu.vitamo.app.media.LocalMediaStorage
import eu.vitamo.app.media.MediaAccessService
import eu.vitamo.app.media.MediaService
import eu.vitamo.app.media.MediaStorage
import eu.vitamo.app.media.MediaValidationService
import org.koin.dsl.module

val mediaModule = module {
    single<MediaStorage> {
        LocalMediaStorage(
            baseDir = "./media"
        )
    }
    single<MediaValidationService> { MediaValidationService() }
    single<MediaAccessService> { MediaAccessService() }
    single<MediaService> { MediaService(get(), get(), get(), get()) }
}