package eu.vitamo.app.features.auth.service

import eu.vitamo.app.config.SmtpConfig
import eu.vitamo.app.mail.MailService
import eu.vitamo.app.mail.model.MailMessage
import eu.vitamo.app.mail.template.MailTemplateRenderer

class AuthMailSender(
    private val mailService: MailService,
    private val templateRenderer: MailTemplateRenderer,
    private val smtpConfig: SmtpConfig,
) {
    suspend fun sendVerificationEmail(
        email: String,
        displayName: String,
        code: String,
        expiresInMinutes: Int,
    ) {
        val variables = mapOf(
            "displayName" to displayName,
            "code" to code,
            "expiresInMinutes" to expiresInMinutes,
            "appName" to smtpConfig.fromName.orEmpty().ifBlank { "VitaMo" },
        )

        mailService.send(
            MailMessage(
                to = email,
                subject = "Verifieer je VitaMo e-mailadres",
                textBody = templateRenderer.renderText("email-verification", variables),
                htmlBody = templateRenderer.renderHtml("email-verification", variables),
            ),
        )
    }

    suspend fun sendPasswordResetMail(
        email: String,
        displayName: String,
        resetToken: String,
    ) {
        val resetLink = "${smtpConfig.resetPasswordLinkBaseUrl}?token=$resetToken"

        val variables = mapOf(
            "displayName" to displayName,
            "resetLink" to resetLink,
            "expiresInMinutes" to smtpConfig.resetPasswordExpirationMinutes,
            "appName" to "VitaMo",
        )

        val htmlBody = templateRenderer.renderHtml(
            templateName = "password-reset",
            variables = variables,
        )

        val textBody = templateRenderer.renderText(
            templateName = "password-reset",
            variables = variables,
        )

        mailService.send(
            MailMessage(
                to = email,
                subject = "Reset je VitaMo wachtwoord",
                textBody = textBody,
                htmlBody = htmlBody,
            ),
        )
    }
}
