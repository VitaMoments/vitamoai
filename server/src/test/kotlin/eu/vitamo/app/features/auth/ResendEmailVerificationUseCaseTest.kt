package eu.vitamo.app.features.auth

import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.config.SmtpConfig
import eu.vitamo.app.features.auth.model.EmailVerificationChallenge
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.EmailVerificationCodeService
import eu.vitamo.app.features.auth.service.AuthMailSender
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.features.auth.usecase.ResendEmailVerificationUseCase
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.model.UserAccount
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.mail.MailService
import eu.vitamo.app.mail.model.MailMessage
import eu.vitamo.app.mail.template.MailTemplateRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.runBlocking

class ResendEmailVerificationUseCaseTest {
    @Test
    fun resend_existingUnverifiedUser_consumesOldChallengeCreatesNewChallengeAndSendsMail() = runBlocking {
        val now = Clock.System.now()
        val userId = Uuid.parse("123e4567-e89b-12d3-a456-426614174555")
        val userRepo = FakeUserRepository(
            user = userAccount(id = userId, emailVerifiedAt = null),
        )
        val challengeRepo = FakeChallengeRepository(
            challenges = mutableListOf(
                challenge(
                    id = Uuid.parse("123e4567-e89b-12d3-a456-426614174556"),
                    userId = userId,
                    email = "ava@example.com",
                    codeHash = "hash-old",
                    createdAt = now - 2.minutes,
                    expiresAt = now + 13.minutes,
                ),
            ),
        )
        val mailService = RecordingMailService()
        val useCase = buildUseCase(userRepo, challengeRepo, mailService)

        val response = useCase.resend(ResendEmailVerificationRequest(email = " ava@example.com "))

        assertEquals(GENERIC_MESSAGE, response.message)
        assertEquals(1, mailService.sentCount)
        assertEquals(2, challengeRepo.challenges.size)
        assertTrue(challengeRepo.challenges.first().consumedAt != null)
        assertTrue(challengeRepo.challenges.last().consumedAt == null)
    }

    @Test
    fun resend_unknownEmail_returnsGenericResponseWithoutMail() = runBlocking {
        val useCase = buildUseCase(
            userRepo = FakeUserRepository(),
            challengeRepo = FakeChallengeRepository(),
            mailService = RecordingMailService(),
        )
        val response = useCase.resend(ResendEmailVerificationRequest(email = "missing@example.com"))

        assertEquals(GENERIC_MESSAGE, response.message)
        assertEquals(0, useCase.mailService.sentCount)
        assertEquals(0, useCase.challengeRepo.challenges.size)
    }

    @Test
    fun resend_verifiedUser_returnsGenericResponseWithoutMail() = runBlocking {
        val useCase = buildUseCase(
            userRepo = FakeUserRepository(
                user = userAccount(emailVerifiedAt = Clock.System.now()),
            ),
            challengeRepo = FakeChallengeRepository(),
            mailService = RecordingMailService(),
        )
        val response = useCase.resend(ResendEmailVerificationRequest(email = "ava@example.com"))

        assertEquals(GENERIC_MESSAGE, response.message)
        assertEquals(0, useCase.mailService.sentCount)
        assertEquals(0, useCase.challengeRepo.challenges.size)
    }

    @Test
    fun resend_withCooldown_returnsGenericResponseWithoutMail() = runBlocking {
        val now = Clock.System.now()
        val userId = Uuid.parse("123e4567-e89b-12d3-a456-426614174557")
        val challengeRepo = FakeChallengeRepository(
            challenges = mutableListOf(
                challenge(
                    id = Uuid.parse("123e4567-e89b-12d3-a456-426614174558"),
                    userId = userId,
                    email = "ava@example.com",
                    codeHash = "hash-old",
                    createdAt = now - 30.seconds,
                    expiresAt = now + 14.minutes,
                ),
            ),
        )
        val useCase = buildUseCase(
            userRepo = FakeUserRepository(userAccount(id = userId, emailVerifiedAt = null)),
            challengeRepo = challengeRepo,
            mailService = RecordingMailService(),
        )

        val response = useCase.resend(ResendEmailVerificationRequest(email = "ava@example.com"))

        assertEquals(GENERIC_MESSAGE, response.message)
        assertEquals(0, useCase.mailService.sentCount)
        assertEquals(1, challengeRepo.challenges.size)
    }

    private fun buildUseCase(
        userRepo: FakeUserRepository,
        challengeRepo: FakeChallengeRepository,
        mailService: RecordingMailService,
    ): BuiltUseCase {
        val sender = AuthMailSender(
            mailService = mailService,
            templateRenderer = StubTemplateRenderer(),
            smtpConfig = SmtpConfig(
                host = "smtp.example.com",
                port = 465,
                username = "mailer@example.com",
                password = "secret",
                fromAddress = "mailer@example.com",
                fromName = "VitaMo",
                sslEnabled = true,
                startTlsEnabled = false,
                authEnabled = true,
                resetPasswordLinkBaseUrl = "https://example.com/reset-password",
                resetPasswordExpirationMinutes = 15
            ),
        )
        val useCase = ResendEmailVerificationUseCase(
            userRepository = userRepo,
            challengeRepository = challengeRepo,
            codeService = EmailVerificationCodeService(FixedSecureRandom()),
            mailSender = sender,
            tokenHashService = FixedTokenHashService(),
        )
        return BuiltUseCase(useCase, userRepo, challengeRepo, mailService)
    }

    private data class BuiltUseCase(
        val useCase: ResendEmailVerificationUseCase,
        val userRepo: FakeUserRepository,
        val challengeRepo: FakeChallengeRepository,
        val mailService: RecordingMailService,
    ) {
        suspend fun resend(request: ResendEmailVerificationRequest) = useCase.resend(request)
    }

    private fun userAccount(
        id: Uuid = Uuid.parse("123e4567-e89b-12d3-a456-426614174559"),
        emailVerifiedAt: Instant? = null,
    ): UserAccount = UserAccount(
        id = id,
        email = "ava@example.com",
        displayName = "Ava",
        hashedPassword = "hashed-password",
        firstName = null,
        lastName = null,
        alias = null,
        bio = null,
        birthDate = null,
        role = UserRole.USER,
        emailVerifiedAt = emailVerifiedAt,
        createdAt = 1,
        updatedAt = 1,
        deletedAt = null,
    )

    private fun challenge(
        id: Uuid,
        userId: Uuid,
        email: String,
        codeHash: String,
        createdAt: Instant,
        expiresAt: Instant,
    ): EmailVerificationChallenge = EmailVerificationChallenge(
        id = id,
        userId = userId,
        email = email,
        codeHash = codeHash,
        purpose = EmailVerificationPurpose.REGISTER_EMAIL_VERIFY,
        createdAt = createdAt,
        expiresAt = expiresAt,
        consumedAt = null,
        attempts = 0,
        lastAttemptAt = null,
    )

    private class FixedTokenHashService : TokenHashService {
        override fun hash(rawToken: String): String = "hash-$rawToken"
        override fun matches(rawToken: String, hashedToken: String): Boolean = hash(rawToken) == hashedToken
    }

    private class FixedSecureRandom : java.security.SecureRandom() {
        private var next = 123456
        override fun nextInt(bound: Int): Int = next % bound
    }

    private class RecordingMailService : MailService {
        var sentCount: Int = 0
        var lastMessage: MailMessage? = null

        override suspend fun send(message: MailMessage) {
            sentCount += 1
            lastMessage = message
        }
    }

    private class StubTemplateRenderer : MailTemplateRenderer {
        override fun renderHtml(templateName: String, variables: Map<String, Any?>): String {
            return "html:${variables["code"]}"
        }

        override fun renderText(templateName: String, variables: Map<String, Any?>): String {
            return "text:${variables["code"]}"
        }
    }

    private class FakeUserRepository(
        private val user: UserAccount? = null,
    ) : UserRepository {
        override suspend fun findByEmail(email: String): UserAccount? = user?.takeIf { it.email == email.trim().lowercase() }
        override suspend fun findByEmailAsEntity(email: String): UserEntity? {
            throw NotImplementedError("Not used in this test")
        }

        override suspend fun findById(id: Uuid): UserAccount? = user?.takeIf { it.id == id }
        override fun deleteById(id: Uuid) = Unit
        override suspend fun createUser(
            email: String,
            displayName: String,
            hashedPassword: String,
            firstName: String?,
            lastName: String?,
            alias: String?,
            birthDate: kotlinx.datetime.LocalDate?,
            now: Long,
        ): UserAccount = error("not used")
        override fun markEmailVerified(id: Uuid, emailVerifiedAt: Instant, updatedAt: Long) = Unit
        override fun updatePassword(userid: Uuid, hashedPassword: String) {
            throw NotImplementedError("Not used in this test")
        }
    }

    private class FakeChallengeRepository(
        val challenges: MutableList<EmailVerificationChallenge> = mutableListOf(),
    ) : EmailVerificationChallengeRepository {
        override fun create(
            userId: Uuid,
            email: String,
            codeHash: String,
            purpose: EmailVerificationPurpose,
            createdAt: Instant,
            expiresAt: Instant,
        ): EmailVerificationChallenge {
            val challenge = EmailVerificationChallenge(
                id = Uuid.parse("123e4567-e89b-12d3-a456-426614174560"),
                userId = userId,
                email = email,
                codeHash = codeHash,
                purpose = purpose,
                createdAt = createdAt,
                expiresAt = expiresAt,
                consumedAt = null,
                attempts = 0,
                lastAttemptAt = null,
            )
            challenges += challenge
            return challenge
        }

        override fun findLatestActive(
            email: String,
            purpose: EmailVerificationPurpose,
            now: Instant,
        ): EmailVerificationChallenge? = challenges
            .filter { it.email == email && it.purpose == purpose && it.consumedAt == null && it.expiresAt > now }
            .maxByOrNull { it.createdAt }

        override fun findLatestByEmailAndPurpose(
            email: String,
            purpose: EmailVerificationPurpose,
        ): EmailVerificationChallenge? = challenges
            .filter { it.email == email && it.purpose == purpose }
            .maxByOrNull { it.createdAt }

        override fun deleteById(id: Uuid) {
            challenges.removeAll { it.id == id }
        }

        override fun markConsumed(id: Uuid, consumedAt: Instant) {
            challenges.replaceAll {
                if (it.id == id) it.copy(consumedAt = consumedAt, lastAttemptAt = consumedAt) else it
            }
        }

        override fun consumeActiveForUserAndPurpose(
            userId: Uuid,
            purpose: EmailVerificationPurpose,
            consumedAt: Instant,
        ) {
            challenges.replaceAll {
                if (it.userId == userId && it.purpose == purpose && it.consumedAt == null && it.expiresAt > consumedAt) {
                    it.copy(consumedAt = consumedAt, lastAttemptAt = consumedAt)
                } else {
                    it
                }
            }
        }

        override fun incrementAttempts(id: Uuid, attemptedAt: Instant) {
            challenges.replaceAll {
                if (it.id == id) it.copy(attempts = it.attempts + 1, lastAttemptAt = attemptedAt) else it
            }
        }
    }

    private companion object {
        const val GENERIC_MESSAGE =
            "If an account exists and requires verification, a new verification email has been sent."
    }
}
