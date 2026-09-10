package tmenier.fr.auth.actions

import jakarta.ws.rs.core.Cookie
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.MultivaluedMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.auth.dtos.requests.LoginRequest
import tmenier.fr.auth.services.JwtService
import tmenier.fr.auth.services.SessionContextService
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import java.util.Date
import java.util.Locale
import java.util.UUID

class LoginActionTest {
    @Test
    fun `unknown email is rejected as invalid credentials, not as a missing user`() {
        val action = buildAction()

        val exception =
            assertThrows<InvalidCredentialsException> {
                action.execute(LoginRequest(email = "nobody@example.com", password = KNOWN_PASSWORD))
            }

        assertEquals("INVALID_CREDENTIALS", exception.errorCode)
        assertEquals(401, exception.httpStatus.statusCode)
    }

    @Test
    fun `an unknown email is never echoed back to the caller`() {
        val action = buildAction()

        val exception =
            assertThrows<InvalidCredentialsException> {
                action.execute(LoginRequest(email = "nobody@example.com", password = KNOWN_PASSWORD))
            }

        assertEquals("Invalid email or password", exception.message)
        assertTrue(
            exception.message?.contains("nobody@example.com") == false,
            "The error message must not disclose the submitted email",
        )
    }

    @Test
    fun `unknown email and wrong password are indistinguishable`() {
        val unknownEmail =
            assertThrows<InvalidCredentialsException> {
                buildAction().execute(LoginRequest(email = "nobody@example.com", password = KNOWN_PASSWORD))
            }
        val wrongPassword =
            assertThrows<InvalidCredentialsException> {
                buildAction().execute(LoginRequest(email = KNOWN_EMAIL, password = "wrong-password"))
            }

        assertEquals(unknownEmail.errorCode, wrongPassword.errorCode)
        assertEquals(unknownEmail.httpStatus, wrongPassword.httpStatus)
        assertEquals(unknownEmail.message, wrongPassword.message)
    }

    @Test
    fun `an unknown email still costs a real bcrypt verification`() {
        val bcrypt = RecordingBcryptService()
        val action = buildAction(bcrypt = bcrypt)

        assertThrows<InvalidCredentialsException> {
            action.execute(LoginRequest(email = "nobody@example.com", password = KNOWN_PASSWORD))
        }

        val verifiedHash = bcrypt.lastVerifiedHash
        assertNotNull(verifiedHash, "An unknown email must still go through a password verification")
        assertTrue(
            verifiedHash!!.startsWith("\$2a\$12\$"),
            "The decoy must be a real bcrypt hash at the production cost factor, not a placeholder string: $verifiedHash",
        )
        assertEquals(
            60,
            verifiedHash.length,
            "A bcrypt hash is 60 characters; a shorter value would be rejected on parsing and cost no CPU time",
        )
    }

    @Test
    fun `no session is opened when authentication fails`() {
        val refreshTokens = RecordingRefreshTokenRepository()
        val action = buildAction(refreshTokens = refreshTokens)

        assertThrows<InvalidCredentialsException> {
            action.execute(LoginRequest(email = "nobody@example.com", password = KNOWN_PASSWORD))
        }
        assertThrows<InvalidCredentialsException> {
            action.execute(LoginRequest(email = KNOWN_EMAIL, password = "wrong-password"))
        }

        assertEquals(0, refreshTokens.storedCount)
    }

    @Test
    fun `valid credentials open a session carrying the client fingerprint`() {
        val refreshTokens = RecordingRefreshTokenRepository()
        val action =
            buildAction(
                refreshTokens = refreshTokens,
                headers =
                    mapOf(
                        HttpHeaders.USER_AGENT to "Firefox/128.0",
                        "X-Forwarded-For" to "203.0.113.7, 10.0.0.1",
                    ),
            )

        val response = action.execute(LoginRequest(email = KNOWN_EMAIL, password = KNOWN_PASSWORD))

        assertEquals("jwt-for-$KNOWN_EMAIL", response.token)
        assertEquals(1, refreshTokens.storedCount)
        assertEquals(refreshTokens.lastRefreshToken.toString(), response.refreshToken)
        assertEquals(refreshTokens.lastSessionId.toString(), response.sessionId)
        assertEquals("Firefox/128.0", refreshTokens.lastUserAgent)
        assertEquals("203.0.113.7", refreshTokens.lastIpAddress)
    }

    @Test
    fun `a session with no proxy header records no ip address`() {
        val refreshTokens = RecordingRefreshTokenRepository()
        val action = buildAction(refreshTokens = refreshTokens, headers = emptyMap())

        action.execute(LoginRequest(email = KNOWN_EMAIL, password = KNOWN_PASSWORD))

        assertNull(refreshTokens.lastIpAddress)
        assertNull(refreshTokens.lastUserAgent)
    }

    private fun buildAction(
        bcrypt: BcryptService = BcryptService(),
        refreshTokens: RecordingRefreshTokenRepository = RecordingRefreshTokenRepository(),
        headers: Map<String, String> = emptyMap(),
    ) = LoginAction(
        userRepository = FakeUserRepository(mapOf(KNOWN_EMAIL to knownUser)),
        refreshTokenRepository = refreshTokens,
        passwordService = bcrypt,
        jwtService = FakeJwtService(),
        sessionContextService = SessionContextService(FakeHttpHeaders(headers)),
    )

    private companion object {
        const val KNOWN_EMAIL = "someone@example.com"
        const val KNOWN_PASSWORD = "correct-horse-battery"

        /** Hashed once: bcrypt at cost 12 is deliberately slow. */
        val knownUser: UserDto =
            UserDto(
                id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                name = "Someone",
                email = KNOWN_EMAIL,
                password = BcryptService().hashPassword(KNOWN_PASSWORD),
            )
    }
}

private class FakeUserRepository(
    private val users: Map<String, UserDto>,
) : UserRepository() {
    override fun findByEmailOrNull(email: String): UserDto? = users[email]
}

private class RecordingRefreshTokenRepository : RefreshTokenRepository() {
    final var storedCount = 0
        private set
    final var lastRefreshToken: UUID? = null
        private set
    final var lastSessionId: UUID? = null
        private set
    final var lastUserAgent: String? = null
        private set
    final var lastIpAddress: String? = null
        private set

    override fun storeRefreshToken(
        refreshToken: UUID,
        userDto: UserDto,
        userAgent: String?,
        ipAddress: String?,
    ): UUID {
        storedCount++
        lastRefreshToken = refreshToken
        lastUserAgent = userAgent
        lastIpAddress = ipAddress
        lastSessionId = UUID.randomUUID()

        return lastSessionId!!
    }
}

private class RecordingBcryptService : BcryptService() {
    final var lastVerifiedHash: String? = null
        private set

    override fun verifyPassword(
        password: String,
        hash: String?,
    ): Boolean {
        lastVerifiedHash = hash
        return super.verifyPassword(password, hash)
    }
}

private class FakeJwtService : JwtService() {
    override fun generateJwt(
        userId: UUID,
        username: String,
        email: String,
    ): String = "jwt-for-$email"

    override fun generateRefreshToken(): UUID = UUID.randomUUID()
}

private class FakeHttpHeaders(
    private val headers: Map<String, String>,
) : HttpHeaders {
    override fun getRequestHeader(name: String): List<String> = headers[name]?.let { listOf(it) } ?: emptyList()

    override fun getHeaderString(name: String): String? = headers[name]

    override fun getRequestHeaders(): MultivaluedMap<String, String> =
        MultivaluedHashMap<String, String>().also { map -> headers.forEach { (key, value) -> map.add(key, value) } }

    override fun getAcceptableMediaTypes(): List<MediaType> = emptyList()

    override fun getAcceptableLanguages(): List<Locale> = emptyList()

    override fun getMediaType(): MediaType? = null

    override fun getLanguage(): Locale? = null

    override fun getCookies(): Map<String, Cookie> = emptyMap()

    override fun getDate(): Date? = null

    override fun getLength(): Int = -1
}
