package tmenier.fr.auth.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class SessionResponse(
    val id: UUID,
    val createdAt: Instant,
    val lastUsedAt: Instant?,
    val expiredAt: Instant,
    val userAgent: String?,
    val ipAddress: String?,
    val current: Boolean,
)
