package eu.vitamo.app.features.auth.di

import eu.vitamo.app.config.JWTConfig
import eu.vitamo.app.config.JWTConfigLoader
import eu.vitamo.app.config.SmtpConfig
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.repository.ExposedEmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.EmailVerificationCodeService
import eu.vitamo.app.features.auth.service.EmailVerificationMailSender
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.auth.service.Sha256TokenHashService
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.features.auth.usecase.LoginUseCase
import eu.vitamo.app.features.auth.usecase.RegisterUseCase
import eu.vitamo.app.features.auth.usecase.ResendEmailVerificationUseCase
import eu.vitamo.app.features.auth.usecase.VerifyEmailUseCase
import eu.vitamo.app.features.user.repository.ExposedUserRepository
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.mail.MailService
import eu.vitamo.app.mail.template.MailTemplateRenderer
import eu.vitamo.app.infrastructure.security.BCryptPasswordHashService
import eu.vitamo.app.infrastructure.security.PasswordHashService
import org.koin.dsl.module

val authModule = module {
    single<JWTConfig> { JWTConfigLoader.loadOrThrow() }

    single<TokenHashService> { Sha256TokenHashService() }
    single<PasswordHashService> { BCryptPasswordHashService() }

    single<UserRepository> { ExposedUserRepository() }
    single<EmailVerificationChallengeRepository> { ExposedEmailVerificationChallengeRepository() }

    single { EmailVerificationCodeService() }
    single { EmailVerificationMailSender(get<MailService>(), get<MailTemplateRenderer>(), get<SmtpConfig>()) }

    single { JWTService(get()) }
    single { RefreshTokenService(get()) }

    single { RegisterUseCase(get(), get(), get(), get(), get(), get()) }
    single { ResendEmailVerificationUseCase(get(), get(), get(), get(), get()) }
    single { VerifyEmailUseCase(get(), get(), get()) }
    single { LoginUseCase(get(), get(), get(), get()) }
}
