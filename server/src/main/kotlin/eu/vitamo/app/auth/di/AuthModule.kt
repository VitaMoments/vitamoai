package eu.vitamo.app.auth.di

import eu.vitamo.app.auth.config.JWTConfig
import eu.vitamo.app.auth.config.JWTConfigLoader
import eu.vitamo.app.auth.service.JWTService
import eu.vitamo.app.auth.service.RefreshTokenService
import eu.vitamo.app.auth.service.Sha256TokenHashService
import eu.vitamo.app.auth.service.TokenHashService
import org.koin.dsl.module

val authModule = module {
    single<JWTConfig> { JWTConfigLoader.loadOrThrow() }

    single<TokenHashService> { Sha256TokenHashService() }

    single { JWTService(get()) }
    single { RefreshTokenService(get()) }
}
