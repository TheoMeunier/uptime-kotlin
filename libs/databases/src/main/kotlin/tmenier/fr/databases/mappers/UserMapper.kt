package tmenier.fr.databases.mappers

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.dtos.UserListItemDto
import tmenier.fr.databases.entities.UserEntity
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class UserDto(
    val id: UUID,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: Instant? = null,
    val role: UserRole = UserRole.ADMIN,
    val mustChangePassword: Boolean = false,
)

object UserMapper {
    fun fromEntity(userEntity: UserEntity): UserDto =
        UserDto(
            id = userEntity.id,
            name = userEntity.name,
            email = userEntity.email,
            password = userEntity.password,
            createdAt = userEntity.createdAt,
            role = userEntity.role,
            mustChangePassword = userEntity.mustChangePassword,
        )

    fun toListItemDto(
        userEntity: UserEntity,
        currentUserId: UUID,
    ): UserListItemDto =
        UserListItemDto(
            id = userEntity.id,
            name = userEntity.name,
            email = userEntity.email,
            role = userEntity.role,
            createdAt = userEntity.createdAt,
            current = userEntity.id == currentUserId,
            mustChangePassword = userEntity.mustChangePassword,
        )

    fun toEntity(userDto: UserDto): UserEntity =
        UserEntity().apply {
            id = userDto.id
            name = userDto.name
            email = userDto.email
            password = userDto.password
            createdAt = userDto.createdAt ?: Instant.now()
            role = userDto.role
        }
}
