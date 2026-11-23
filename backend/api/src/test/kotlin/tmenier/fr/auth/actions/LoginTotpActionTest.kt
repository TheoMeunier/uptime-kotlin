package tmenier.fr.auth.actions

import jakarta.ws.rs.core.HttpHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.auth.dtos.requests.LoginTotpRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.JwtService
import tmenier.fr.auth.services.SessionContextService
import tmenier.fr.auth.services.SessionIssuer
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.exceptions.twofactor.InvalidTwoFactorCodeException
import tmenier.fr.common.exceptions.twofactor.MfaChallengeInvalidException
import tmenier.fr.common.exceptions.twofactor.TooManyTwoFactorAttemptsException
import tmenier.fr.common.security.RecoveryCodeService
import tmenier.fr.common.security.SecureRandomService
import tmenier.fr.common.totp.TotpService
import tmenier.fr.common.utils.Sha256
import tmenier.fr.databases.entities.MfaChallengeEntity
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.MfaChallengeRepository
import tmenier.fr.databases.repositories.RecoveryCodeRepository
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.services.MfaChallengeService
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.lang.reflect.Proxy
import java.time.Instant
import java.util.UUID

class LoginTotpActionTest {
    private val now = Instant.parse("2026-11-23T12:00:00Z")

    @Test
    fun `a valid code opens the session and spends the challenge`() {
        val challenges = FakeChallengeRepository().apply { add(challenge()) }
        val sessions = RecordingSessionIssuer()

        val response = action(challenges, sessions = sessions).execute(LoginTotpRequest(TOKEN, VALID_CODE), now)

        assertEquals("jwt-for-${user.email}", response.token)
        assertEquals(listOf(user.id), sessions.openedFor)
        assertEquals(now, challenges.only().consumedAt)
    }

    @Test
    fun `a spent challenge cannot open a second session`() {
        val challenges = FakeChallengeRepository().apply { add(challenge()) }
        val action = action(challenges)

        action.execute(LoginTotpRequest(TOKEN, VALID_CODE), now)

        assertThrows<MfaChallengeInvalidException> { action.execute(LoginTotpRequest(TOKEN, VALID_CODE), now) }
    }

    @Test
    fun `a wrong code is counted and opens nothing`() {
        val challenges = FakeChallengeRepository().apply { add(challenge()) }
        val sessions = RecordingSessionIssuer()

        val error =
            assertThrows<InvalidTwoFactorCodeException> {
                action(challenges, sessions = sessions).execute(LoginTotpRequest(TOKEN, "000000"), now)
            }

        assertEquals(401, error.httpStatus.statusCode)
        assertEquals(1, challenges.only().attempts)
        assertNull(challenges.only().consumedAt)
        assertEquals(emptyList<UUID>(), sessions.openedFor)
    }

    @Test
    fun `a challenge is dead after five wrong codes, even with the right one`() {
        val challenges = FakeChallengeRepository().apply { add(challenge()) }
        val action = action(challenges)

        repeat(MfaChallengeService.MAX_ATTEMPTS_PER_CHALLENGE) {
            assertThrows<InvalidTwoFactorCodeException> { action.execute(LoginTotpRequest(TOKEN, "000000"), now) }
        }

        assertThrows<MfaChallengeInvalidException> { action.execute(LoginTotpRequest(TOKEN, VALID_CODE), now) }
    }

    @Test
    fun `too many failures for the user block every challenge for a while`() {
        val challenges =
            FakeChallengeRepository(recentFailures = MfaChallengeService.MAX_ATTEMPTS_PER_USER.toLong()).apply { add(challenge()) }

        val error = assertThrows<TooManyTwoFactorAttemptsException> { action(challenges).execute(LoginTotpRequest(TOKEN, VALID_CODE), now) }

        assertEquals(429, error.httpStatus.statusCode)
    }

    @Test
    fun `an expired or unknown challenge is refused`() {
        val expired = FakeChallengeRepository().apply { add(challenge(expiresAt = now)) }

        assertThrows<MfaChallengeInvalidException> { action(expired).execute(LoginTotpRequest(TOKEN, VALID_CODE), now) }
        assertThrows<MfaChallengeInvalidException> { action(FakeChallengeRepository()).execute(LoginTotpRequest("unknown", VALID_CODE), now) }
    }

    @Test
    fun `a challenge is refused once two-factor authentication was turned off`() {
        val challenges = FakeChallengeRepository().apply { add(challenge()) }
        val disabled = twoFactorUser().apply { totpEnabledAt = null }

        assertThrows<MfaChallengeInvalidException> {
            action(challenges, users = FakeTotpUserRepository(disabled)).execute(LoginTotpRequest(TOKEN, VALID_CODE), now)
        }
    }

    private fun action(
        challenges: FakeChallengeRepository,
        users: FakeTotpUserRepository = FakeTotpUserRepository(user),
        sessions: RecordingSessionIssuer = RecordingSessionIssuer(),
    ) = LoginTotpAction(MfaChallengeService(challenges, SecureRandomService()), users, FakeVerifier(), sessions)

    private fun challenge(expiresAt: Instant = now.plusSeconds(300)) =
        MfaChallengeEntity().apply {
            id = UUID.randomUUID()
            userId = user.id
            tokenHash = Sha256.hex(TOKEN)
            this.expiresAt = expiresAt
            createdAt = now.minusSeconds(10)
        }

    private companion object {
        const val TOKEN = "challenge-token"
        const val VALID_CODE = "123456"

        val user = twoFactorUser()

        fun twoFactorUser() =
            UserEntity().apply {
                id = UUID.fromString("33333333-3333-3333-3333-333333333333")
                name = "Test user"
                email = "user@example.test"
                password = "unused"
                createdAt = Instant.parse("2026-01-01T00:00:00Z")
                totpSecret = "enc:unused"
                totpEnabledAt = Instant.parse("2026-06-01T00:00:00Z")
            }
    }
}

private class FakeChallengeRepository(
    private val recentFailures: Long = 0,
) : MfaChallengeRepository() {
    private val challenges = mutableListOf<MfaChallengeEntity>()

    fun add(challenge: MfaChallengeEntity) {
        challenges += challenge
    }

    fun only(): MfaChallengeEntity = challenges.single()

    override fun findByTokenHashForUpdate(tokenHash: String): MfaChallengeEntity? = challenges.find { it.tokenHash == tokenHash }

    override fun failedAttemptsSince(
        userId: UUID,
        since: Instant,
    ): Long = recentFailures

    override fun recordFailedAttempt(challenge: MfaChallengeEntity) {
        challenge.attempts += 1
    }

    override fun markConsumed(
        challenge: MfaChallengeEntity,
        at: Instant,
    ) {
        challenge.consumedAt = at
    }
}

private class FakeTotpUserRepository(
    private val user: UserEntity,
) : UserRepository() {
    override fun findByIdOrNull(id: UUID): UserEntity? = user.takeIf { it.id == id }
}

private class FakeVerifier :
    TwoFactorVerifier(
        EncryptionService("0123456789abcdef0123456789abcdef"),
        TotpService(SecureRandomService()),
        RecoveryCodeService(SecureRandomService()),
        UserRepository(),
        RecoveryCodeRepository(),
    ) {
    override fun accept(
        user: UserEntity,
        code: String,
        allowRecoveryCode: Boolean,
        now: Instant,
    ): Boolean = code == "123456"
}

private class RecordingSessionIssuer :
    SessionIssuer(RefreshTokenRepository(), JwtService(), SessionContextService(noHeaders())) {
    val openedFor = mutableListOf<UUID>()

    override fun open(user: UserDto): LoginResponse {
        openedFor += user.id
        return LoginResponse(token = "jwt-for-${user.email}", refreshToken = "refresh", sessionId = "session")
    }
}

private fun noHeaders(): HttpHeaders =
    Proxy.newProxyInstance(HttpHeaders::class.java.classLoader, arrayOf(HttpHeaders::class.java)) { _, _, _ -> null } as HttpHeaders
