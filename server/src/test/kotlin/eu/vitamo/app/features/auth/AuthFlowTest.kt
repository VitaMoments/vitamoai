package eu.vitamo.app.features.auth

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.features.auth.model.EmailVerificationChallenge
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.service.EmailVerificationCodeService
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.features.auth.usecase.LoginUseCase
import eu.vitamo.app.features.auth.usecase.VerifyEmailUseCase
import eu.vitamo.app.config.JWTConfig
import eu.vitamo.app.features.user.model.UserAccount
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.infrastructure.security.PasswordHashService
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AuthFlowTest {
    @Test
    fun emailVerificationCode_hasSixDigits() {
        val code = EmailVerificationCodeService(SecureRandomStub(123456)).generateCode()

        assertTrue(code.matches(Regex("\\d{6}")))
    }

    @Test
    fun verifyEmail_marksAccountVerifiedAndConsumesChallenge() = runTest {
        val now = Clock.System.now()
        val userId = Uuid.parse("123e4567-e89b-12d3-a456-426614174000")

        val userRepo = FakeUserRepository(
            user = userAccount(
                id = userId,
                emailVerifiedAt = null,
            ),
        )

        val challengeRepo = FakeChallengeRepository(
            challenge = emailChallenge(
                id = Uuid.parse("123e4567-e89b-12d3-a456-426614174111"),
                userId = userId,
                email = "ava@example.com",
                codeHash = "hash-123456",
                createdAt = now,
                expiresAt = now + kotlin.time.Duration.parse("15m"),
            ),
        )

        val useCase = VerifyEmailUseCase(
            userRepository = userRepo,
            challengeRepository = challengeRepo,
            tokenHashService = FixedTokenHashService(),
        )

        val response = useCase.verify(
            VerifyEmailRequest(
                email = " ava@example.com ",
                code = "123456",
            ),
        )

        assertTrue(response.verified)
        assertEquals(1, challengeRepo.consumedCount)
        assertTrue(userRepo.savedUser?.emailVerifiedAt != null)
    }

    @Test
    fun verifyEmail_incrementsAttemptsOnMismatch() = runTest {
        val now = Clock.System.now()
        val userId = Uuid.parse("123e4567-e89b-12d3-a456-426614174222")
        val userRepo = FakeUserRepository(
            user = userAccount(
                id = userId,
                emailVerifiedAt = null,
            ),
        )
        val challengeRepo = FakeChallengeRepository(
            challenge = emailChallenge(
                id = Uuid.parse("123e4567-e89b-12d3-a456-426614174333"),
                userId = userId,
                email = "ava@example.com",
                codeHash = "hash-123456",
                createdAt = now,
                expiresAt = now + kotlin.time.Duration.parse("15m"),
            ),
        )
        val useCase = VerifyEmailUseCase(userRepo, challengeRepo, FixedTokenHashService())

        val failure = assertFailsWith<eu.vitamo.app.features.auth.model.AuthException> {
            useCase.verify(VerifyEmailRequest(email = "ava@example.com", code = "000000"))
        }

        assertEquals("INVALID_VERIFICATION_CODE", failure.code)
        assertEquals(1, challengeRepo.attempts)
    }

    @Test
    fun login_rejectsUnverifiedUser() = runTest {
        val userRepo = FakeUserRepository(
            user = userAccount(
                emailVerifiedAt = null,
                hashedPassword = "hashed-password",
            ),
        )
        val useCase = LoginUseCase(
            userRepository = userRepo,
            passwordHashService = AcceptingPasswordHashService(),
            jwtService = JWTService(testJwtConfig()),
            refreshTokenService = NoopRefreshTokenService(),
        )

        val failure = assertFailsWith<eu.vitamo.app.features.auth.model.AuthException> {
            useCase.login(LoginRequest(email = "ava@example.com", password = "secret"))
        }

        assertEquals("EMAIL_NOT_VERIFIED", failure.code)
        assertEquals(HttpStatusCode.Forbidden, failure.status)
    }

    private fun userAccount(
        id: Uuid = Uuid.parse("123e4567-e89b-12d3-a456-426614174999"),
        emailVerifiedAt: Instant? = Clock.System.now(),
        hashedPassword: String = "hashed-password",
    ): UserAccount = UserAccount(
        id = id,
        email = "ava@example.com",
        displayName = "Ava",
        hashedPassword = hashedPassword,
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

    private fun emailChallenge(
        id: Uuid,
        userId: Uuid,
        email: String,
        codeHash: String,
        createdAt: Instant,
        expiresAt: Instant,
    ) = EmailVerificationChallenge(
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

    private class AcceptingPasswordHashService : PasswordHashService {
        override fun hashPassword(rawPassword: String): String = "hashed-$rawPassword"
        override fun verifyPassword(rawPassword: String, passwordHash: String): Boolean = passwordHash == "hashed-password"
    }

    private class NoopRefreshTokenService : RefreshTokenService(
        tokenHashService = FixedTokenHashService(),
    )

    private fun testJwtConfig(): JWTConfig {
        return JWTConfig(
            issuer = "test-issuer",
            audience = "test-audience",
            secret = "test-secret-test-secret-test-secret-32",
        )
    }

    private class FakeUserRepository(
        private val user: UserAccount? = null,
    ) : UserRepository {
        var savedUser: UserAccount? = user

        override suspend fun findByEmail(email: String): UserAccount? = savedUser?.takeIf { it.email == email }
        override suspend fun findByEmailAsEntity(email: String): UserEntity? {
            throw NotImplementedError("Not needed for this test")
        }

        override suspend fun findById(id: Uuid): UserAccount? = savedUser?.takeIf { it.id == id }
        override fun deleteById(id: Uuid) {
            if (savedUser?.id == id) {
                savedUser = null
            }
        }
        override suspend fun createUser(
            email: String,
            displayName: String,
            hashedPassword: String,
            firstName: String?,
            lastName: String?,
            alias: String?,
            birthDate: kotlinx.datetime.LocalDate?,
            now: Long,
        ): UserAccount {
            savedUser = UserAccount(
                id = Uuid.parse("123e4567-e89b-12d3-a456-426614174999"),
                email = email,
                displayName = displayName,
                hashedPassword = hashedPassword,
                firstName = firstName,
                lastName = lastName,
                alias = alias,
                bio = null,
                birthDate = birthDate,
                role = UserRole.USER,
                emailVerifiedAt = null,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )
            return savedUser!!
        }

        override fun markEmailVerified(id: Uuid, emailVerifiedAt: Instant, updatedAt: Long) {
            savedUser = savedUser?.copy(emailVerifiedAt = emailVerifiedAt, updatedAt = updatedAt)
        }

        override fun updatePassword(userid: Uuid, hashedPassword: String) {
            savedUser = savedUser?.takeIf { it.id == userid }?.copy(hashedPassword = hashedPassword)
        }
    }

    private class FakeChallengeRepository(
        private val challenge: EmailVerificationChallenge? = null,
    ) : EmailVerificationChallengeRepository {
        var attempts: Int = challenge?.attempts ?: 0
        var consumedCount: Int = 0

        override fun create(
            userId: Uuid,
            email: String,
            codeHash: String,
            purpose: EmailVerificationPurpose,
            createdAt: Instant,
            expiresAt: Instant,
        ): EmailVerificationChallenge = challenge!!

        override fun findLatestActive(
            email: String,
            purpose: EmailVerificationPurpose,
            now: Instant,
        ): EmailVerificationChallenge? = challenge?.copy(attempts = attempts)

        override fun findLatestByEmailAndPurpose(
            email: String,
            purpose: EmailVerificationPurpose,
        ): EmailVerificationChallenge? = challenge

        override fun deleteById(id: Uuid) {
            // no-op in the test fake
        }

        override fun markConsumed(id: Uuid, consumedAt: Instant) {
            consumedCount += 1
        }

        override fun consumeActiveForUserAndPurpose(
            userId: Uuid,
            purpose: EmailVerificationPurpose,
            consumedAt: Instant,
        ) {
            consumedCount += 1
        }

        override fun incrementAttempts(id: Uuid, attemptedAt: Instant) {
            attempts += 1
        }
    }
}

private class SecureRandomStub(
    private val value: Int,
) : java.security.SecureRandom() {
    override fun nextInt(bound: Int): Int = value % bound
}
