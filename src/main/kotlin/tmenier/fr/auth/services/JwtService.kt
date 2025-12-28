package tmenier.fr.auth.services

import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class JwtService {
    fun generateJwt(userId: UUID): String =
        Jwt
            .claims()
            .subject(userId.toString())
            .issuer("https://uptime-kotlin.theomeunier.fr/")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(Duration.ofHours(10)))
            .sign()

    fun generateRefreshToken(): String = UUID.randomUUID().toString()
}
