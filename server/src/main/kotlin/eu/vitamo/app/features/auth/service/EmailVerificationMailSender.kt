package eu.vitamo.app.features.auth.service

import eu.vitamo.app.config.SmtpConfig
import eu.vitamo.app.mail.MailService
import eu.vitamo.app.mail.model.MailMessage
import eu.vitamo.app.mail.template.MailTemplateRenderer

class EmailVerificationMailSender(
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
}
