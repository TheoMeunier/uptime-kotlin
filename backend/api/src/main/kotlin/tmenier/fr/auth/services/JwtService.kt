package tmenier.fr.auth.services

import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class JwtService {
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://uptime-kotlin.theomeunier.fr")
    private lateinit var jwtIssuer: String

    @Inject
    private lateinit var keyProvider: JwtKeyProvider

    fun generateJwt(
        userId: UUID,
        username: String,
        email: String,
    ): String {
        val now = Instant.now()

        return Jwt
            .claims()
            .subject(userId.toString())
            .issuer(jwtIssuer)
            .issuedAt(now)
            .expiresAt(now.plus(ACCESS_TOKEN_LIFETIME))
            .claim("name", username)
            .claim("email", email)
            .sign(keyProvider.privateKey)
    }

    fun generateRefreshToken(): UUID = UUID.randomUUID()

    companion object {
        val ACCESS_TOKEN_LIFETIME: Duration = Duration.ofMinutes(15)
    }
}
