package tmenier.fr.auth.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class SessionResponse(
    val id: UUID,
    val createdAt: LocalDateTime,
    val lastUsedAt: LocalDateTime?,
    val expiredAt: LocalDateTime,
    val userAgent: String?,
    val ipAddress: String?,
    val current: Boolean,
)
