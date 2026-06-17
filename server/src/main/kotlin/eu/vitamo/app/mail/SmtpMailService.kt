package eu.vitamo.app.mail

import eu.vitamo.app.config.SmtpConfig
import eu.vitamo.app.mail.model.MailMessage
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.Properties

class SmtpMailService(
    private val config: SmtpConfig,
) : MailService {
    private val logger = LoggerFactory.getLogger(SmtpMailService::class.java)

    override suspend fun send(message: MailMessage) {
        withContext(Dispatchers.IO) {
            val session = if (config.authEnabled) {
                Session.getInstance(createProperties(), authenticator())
            } else {
                Session.getInstance(createProperties())
            }
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(config.fromAddress, config.fromName))
                setRecipient(Message.RecipientType.TO, InternetAddress(message.to))
                setSubject(message.subject, StandardCharsets.UTF_8.name())

                when {
                    message.textBody != null && message.htmlBody != null -> {
                        val multipart = MimeMultipart("alternative")

                        multipart.addBodyPart(MimeBodyPart().apply {
                            setText(message.textBody, StandardCharsets.UTF_8.name())
                        })
                        multipart.addBodyPart(MimeBodyPart().apply {
                            setContent(message.htmlBody, "text/html; charset=${StandardCharsets.UTF_8.name()}")
                        })

                        setContent(multipart)
                    }

                    message.htmlBody != null -> {
                        setContent(message.htmlBody, "text/html; charset=${StandardCharsets.UTF_8.name()}")
                    }

                    else -> {
                        setText(message.textBody.orEmpty(), StandardCharsets.UTF_8.name())
                    }
                }
            }

            Transport.send(mimeMessage)
            logger.info("Sent mail to {}", message.to)
        }
    }

    private fun createProperties(): Properties = Properties().apply {
        put("mail.smtp.host", config.host)
        put("mail.smtp.port", config.port.toString())
        put("mail.smtp.auth", config.authEnabled.toString())
        put("mail.smtp.ssl.enable", config.sslEnabled.toString())
        put("mail.smtp.starttls.enable", config.startTlsEnabled.toString())
        put("mail.smtp.connectiontimeout", DEFAULT_TIMEOUT_MS)
        put("mail.smtp.timeout", DEFAULT_TIMEOUT_MS)
        put("mail.smtp.writetimeout", DEFAULT_TIMEOUT_MS)
    }

    private fun authenticator(): Authenticator? {
        if (!config.authEnabled) {
            return null
        }

        return object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = "10000"
    }
}
