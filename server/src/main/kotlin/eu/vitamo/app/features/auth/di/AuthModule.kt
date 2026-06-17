package eu.vitamo.app.features.auth.di

import eu.vitamo.app.config.JWTConfig
import eu.vitamo.app.config.JWTConfigLoader
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.auth.service.Sha256TokenHashService
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.infrastructure.security.BCryptPasswordHashService
import eu.vitamo.app.infrastructure.security.PasswordHashService
import org.koin.dsl.module

val authModule = module {
    single<JWTConfig> { JWTConfigLoader.loadOrThrow() }

    single<TokenHashService> { Sha256TokenHashService() }
    single<PasswordHashService> { BCryptPasswordHashService() }

    single { JWTService(get()) }
    single { RefreshTokenService(get()) }
}
