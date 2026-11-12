package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.users.UserRole
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class UserListItemDto(
    val id: UUID,
    val name: String,
    val email: String,
    val role: UserRole,
    val createdAt: Instant,
    val current: Boolean,
    val mustChangePassword: Boolean = false,
)
