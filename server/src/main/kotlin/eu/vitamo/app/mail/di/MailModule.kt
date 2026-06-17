package eu.vitamo.app.mail.di

import eu.vitamo.app.config.SmtpConfig
import eu.vitamo.app.config.SmtpConfigLoader
import eu.vitamo.app.mail.MailService
import eu.vitamo.app.mail.SmtpMailService
import eu.vitamo.app.mail.template.MailTemplateRenderer
import eu.vitamo.app.mail.template.ThymeleafMailTemplateRenderer
import org.koin.dsl.module

val mailModule = module {
    single<SmtpConfig> { SmtpConfigLoader.loadOrThrow() }
    single<MailTemplateRenderer> { ThymeleafMailTemplateRenderer() }
    single<MailService> { SmtpMailService(get()) }
}
