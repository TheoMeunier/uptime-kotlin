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
    ): String =
        Jwt
            .claims()
            .subject(userId.toString())
            .issuer(jwtIssuer)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(Duration.ofHours(10)))
            .claim("name", username)
            .claim("email", email)
            .sign(keyProvider.privateKey)

    fun generateRefreshToken(): UUID = UUID.randomUUID()
}
