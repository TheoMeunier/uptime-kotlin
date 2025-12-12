package tmenier.fr.services

import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.util.UUID
import java.time.Instant

@ApplicationScoped
class JwtService
{
    fun generateJwt(userId: UUID): String {
        return Jwt.claims()
            .subject(userId.toString())
            .issuer("https://uptime-kotlin.theomeunier.fr/")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(Duration.ofHours(10)))
            .sign()
    }

    fun generateRefreshToken(): String {
        return UUID.randomUUID().toString()
    }
}